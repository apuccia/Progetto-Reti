package clientGUI;

import server.RequestMessages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.ArrayBlockingQueue;

public class LoginPanel extends JPanel {
    private final JPanel cardPanel;
    private final JTextField nicknameField;
    private final JPasswordField passwordField;
    private final JLabel notRegisteredLabel;
    private final JLabel loadingLabel;
    private final JButton loginButton;

    public LoginPanel(JPanel cardPanel, ArrayBlockingQueue<String> request) {
        this.cardPanel = cardPanel;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // label nickname.
        JLabel nicknameLabel = WQGUIUtilities.createStandardLabel("Nickname:");

        // label password.
        JLabel passwordLabel = WQGUIUtilities.createStandardLabel("Password:");

        // field nickname.
        nicknameField = WQGUIUtilities.createTextField();

        // field password.
        passwordField = WQGUIUtilities.createPasswordField();

        // immagine caricamento.
        ImageIcon loading = new ImageIcon(MainFrame.LOADINGICO_PATH);
        loadingLabel = new JLabel(" ");

        // label che porta al panel di registrazione se cliccata.
        notRegisteredLabel = new JLabel("Non sei ancora registrato?", JLabel.CENTER);
        notRegisteredLabel.setFont(new Font("Arial", Font.BOLD, 14));
        notRegisteredLabel.setForeground(Color.BLACK);
        notRegisteredLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
                cardLayout.show(cardPanel, MainFrame.REGISTRATION_PANEL);

                nicknameField.setText("");
                passwordField.setText("");
            }

            @Override
            public void mouseEntered(MouseEvent event) {
                notRegisteredLabel.setForeground(Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                notRegisteredLabel.setForeground(Color.BLACK);
            }
        });

        // bottone login.
        loginButton = WQGUIUtilities.createButton("Login");
        loginButton.addActionListener(actionEvent -> {
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
                loginButton.setEnabled(false);
                notRegisteredLabel.setEnabled(false);
                loadingLabel.setIcon(loading);
                revalidate();

                request.offer(RequestMessages.LOGIN.toString() + " " + nickname + " " + password);
                GUIClientMainClass.clientSelector.wakeup();
            }
        });

        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(Color.WHITE);

        // in modo da "ascoltare" il pressing del tasto invio.
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "enterAction");
        getActionMap().put("enterAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                loginButton.doClick();
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 20, 10);

        loginPanel.add(nicknameLabel, gbc);
        gbc.gridy = 1;
        loginPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        loginPanel.add(nicknameField, gbc);
        gbc.gridy = 1;
        loginPanel.add(passwordField, gbc);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 2;

        loginPanel.add(loginButton, gbc);
        gbc.ipadx = 0;
        gbc.gridy = 3;
        loginPanel.add(notRegisteredLabel, gbc);

        gbc.gridy = 4;

        loginPanel.add(loadingLabel, gbc);

        add(loginPanel, BorderLayout.CENTER);
        add(WQGUIUtilities.createSouthPanel(), BorderLayout.SOUTH);
    }

    public void loginResponse(boolean ok, String message) {
        if (ok) {
            MainMenuPanel.nickname = nicknameField.getText();

            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, MainFrame.MAINMENU_PANEL);

            nicknameField.setText("");
            passwordField.setText("");
        }

        loginButton.setEnabled(true);
        notRegisteredLabel.setEnabled(true);
        nicknameField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        passwordField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        loadingLabel.setIcon(null);

        JOptionPane.showMessageDialog(this, message);

        revalidate();
    }

    public void notifyCrash() {
        nicknameField.setText("");
        passwordField.setText("");
        loginButton.setEnabled(true);
        notRegisteredLabel.setEnabled(true);
        nicknameField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        passwordField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        loadingLabel.setIcon(null);

        revalidate();
    }
}
