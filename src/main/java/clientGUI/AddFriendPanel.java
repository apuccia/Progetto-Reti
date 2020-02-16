package clientGUI;

import server.RequestMessages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.ArrayBlockingQueue;

public class AddFriendPanel extends JPanel {
    private final JPanel cardPanel;
    private final JTextField nicknameField;
    private final JButton addFriendButton;
    private final JLabel loadingLabel;
    private final JButton backButton;

    public AddFriendPanel(JPanel cardPanel, ArrayBlockingQueue<String> request) {
        this.cardPanel = cardPanel;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        GridBagConstraints gbcAddFriendLabel = new GridBagConstraints();
        GridBagConstraints gbcAddFriendText = new GridBagConstraints();
        GridBagConstraints gbcAddFriendButton = new GridBagConstraints();

        backButton = WQGUIUtilities.createButton("Indietro");

        JPanel backButtonPanel = WQGUIUtilities.createSouthPanel();
        backButtonPanel.add(backButton);

        add(backButtonPanel, BorderLayout.SOUTH);

        JPanel addFriendPanel = new JPanel(new GridBagLayout());
        addFriendPanel.setBackground(Color.WHITE);

        JLabel addFriendLabel = WQGUIUtilities.createStandardLabel("Nickname utente:");

        nicknameField = WQGUIUtilities.createTextField();

        backButton.addActionListener(actionEvent -> {
            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, MainFrame.MAINMENU_PANEL);

            nicknameField.setText(" ");
        });

        ImageIcon loading = new ImageIcon("ajax-loader.gif");
        loadingLabel = new JLabel(" ");

        addFriendButton = WQGUIUtilities.createButton("Aggiungi");
        addFriendButton.addActionListener(actionEvent -> {
            String friendNickname = nicknameField.getText();

            if (friendNickname.isEmpty()) {
                nicknameField.setBorder(BorderFactory.createLineBorder(Color.RED));
            }
            else if (friendNickname.equals(MainMenuPanel.nickname)) {
                JOptionPane.showMessageDialog(this, "Valore non valido");
            }
            else if (friendNickname.contains(" ")) {
                JOptionPane.showMessageDialog(this, "Non sono ammessi spazi");
            }
            else {
                nicknameField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                addFriendButton.setEnabled(false);
                backButton.setEnabled(false);

                loadingLabel.setIcon(loading);
                revalidate();

                request.offer(RequestMessages.ADD_FRIEND.toString() + " " + MainMenuPanel.nickname + " " +
                        friendNickname);
                GUIClientMainClass.clientSelector.wakeup();
            }
        });

        // in modo da "ascoltare" il pressing del tasto invio.
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "myAction");
        getActionMap().put("myAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                addFriendButton.doClick();
            }
        });

        gbcAddFriendText.insets = new Insets(0, 5, 10, 0);
        gbcAddFriendLabel.insets = new Insets(0, 0, 10, 5);
        gbcAddFriendButton.insets = new Insets(10, 0, 0, 0);

        addFriendPanel.add(addFriendLabel, gbcAddFriendLabel);

        gbcAddFriendText.gridx = 1;
        addFriendPanel.add(nicknameField, gbcAddFriendText);

        gbcAddFriendButton.gridx = 0;
        gbcAddFriendButton.gridy = 1;
        gbcAddFriendButton.gridwidth = 2;
        addFriendPanel.add(addFriendButton, gbcAddFriendButton);

        gbcAddFriendButton.gridy = 2;
        addFriendPanel.add(loadingLabel, gbcAddFriendButton);

        add(addFriendPanel, BorderLayout.CENTER);
    }

    public void addFriendResponse(boolean ok, String message) {
        if (ok) {
            nicknameField.setText("");
        }

        addFriendButton.setEnabled(true);
        backButton.setEnabled(true);
        loadingLabel.setIcon(null);

        JOptionPane.showMessageDialog(this, message);

        revalidate();
    }

    public void notifyCrash() {
        nicknameField.setText("");
        nicknameField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        addFriendButton.setEnabled(true);
        backButton.setEnabled(true);
        loadingLabel.setIcon(null);

        revalidate();
    }
}
