package clientGUI;

import server.RequestMessages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.ArrayBlockingQueue;

public class RegistrationPanel extends JPanel {
    private final JPanel cardPanel;
    private final JTextField nicknameField;
    private final JPasswordField passwordField;
    private final JButton registrationButton;
    private final JLabel loadingLabel;
    private final JButton backButton;

    public RegistrationPanel(JPanel cardPanel, ArrayBlockingQueue<String> registrationRequest) {
        this.cardPanel = cardPanel;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel registrationPanel = new JPanel(new GridBagLayout());
        registrationPanel.setBackground(Color.WHITE);

        GridBagConstraints labelGbc = new GridBagConstraints();
        GridBagConstraints fieldGbc = new GridBagConstraints();
        GridBagConstraints registrationButtonGbc = new GridBagConstraints();

        fieldGbc.insets = new Insets(0, 5, 20, 0);
        labelGbc.insets = new Insets(0, 0, 20, 5);
        registrationButtonGbc.insets = new Insets(0, 0, 20, 0);
        labelGbc.gridx = 0;
        labelGbc.anchor = GridBagConstraints.LINE_START;
        fieldGbc.gridx = 1;

        registrationButtonGbc.gridx = 0;
        registrationButtonGbc.gridy = 3;
        registrationButtonGbc.gridwidth = 2;

        JLabel nicknameLabel = WQGUIUtilities.createStandardLabel("Nickname:");
        registrationPanel.add(nicknameLabel, labelGbc);

        nicknameField = WQGUIUtilities.createTextField();
        registrationPanel.add(nicknameField, fieldGbc);

        JLabel passwordLabel = WQGUIUtilities.createStandardLabel("Password:");
        labelGbc.gridy = 1;
        registrationPanel.add(passwordLabel, labelGbc);

        passwordField = WQGUIUtilities.createPasswordField();
        fieldGbc.gridy = 1;
        registrationPanel.add(passwordField, fieldGbc);

        ImageIcon loading = new ImageIcon(MainFrame.LOADINGICO_PATH);
        loadingLabel = new JLabel(" ");
        registrationButtonGbc.gridy = 4;
        registrationPanel.add(loadingLabel, registrationButtonGbc);

        backButton = WQGUIUtilities.createButton("Indietro");
        backButton.addActionListener(actionEvent -> {
            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, MainFrame.LOGIN_PANEL);

            nicknameField.setText(" ");
            passwordField.setText(" ");
        });

        registrationButton = WQGUIUtilities.createButton("Registrati");
        registrationButton.addActionListener(actionEvent -> {
            String nickname = nicknameField.getText();
            String password = new String(passwordField.getPassword());

            if (nickname.isEmpty()) {
                nicknameField.setBorder(BorderFactory.createLineBorder(Color.RED));
            }
            else if (password.isEmpty()) {
                passwordField.setBorder(BorderFactory.createLineBorder(Color.RED));
            }
            else if (nickname.contains(" ") || password.contains(" ")) {
                JOptionPane.showMessageDialog(this, "Non sono ammessi spazi");
            }
            else {
                nicknameField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                passwordField.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                registrationButton.setEnabled(false);
                backButton.setEnabled(false);
                loadingLabel.setIcon(loading);
                revalidate();
                registrationRequest.offer(RequestMessages.REGISTER.toString() + " " + nickname + " " + password);
                GUIClientMainClass.clientSelector.wakeup();
            }
        });

        // in modo da "ascoltare" il pressing del tasto invio.
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "enterAction");
        getActionMap().put("enterAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                registrationButton.doClick();
            }
        });

        registrationButtonGbc.gridy = 3;
        registrationPanel.add(registrationButton, registrationButtonGbc);

        add(registrationPanel, BorderLayout.CENTER);

        JPanel backButtonPanel = WQGUIUtilities.createSouthPanel();
        backButtonPanel.add(backButton);

        add(backButtonPanel, BorderLayout.SOUTH);
    }

    public void registrationResponse(boolean ok, String message) {
        if (ok) {
            JOptionPane.showMessageDialog(this, message);

            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, MainFrame.LOGIN_PANEL);

            nicknameField.setText("");
            passwordField.setText("");
            registrationButton.setEnabled(true);
        }
        else {
            JOptionPane.showMessageDialog(this, message);
        }

        registrationButton.setEnabled(true);
        backButton.setEnabled(true);
        loadingLabel.setIcon(null);
        revalidate();
    }

    public void notifyCrash() {
        nicknameField.setText("");
        passwordField.setText("");
        registrationButton.setEnabled(true);
        backButton.setEnabled(true);
        nicknameField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        passwordField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        loadingLabel.setIcon(null);

        revalidate();
    }
}
