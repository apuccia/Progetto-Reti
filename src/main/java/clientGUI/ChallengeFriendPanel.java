package clientGUI;

import server.RequestMessages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.ArrayBlockingQueue;

public class ChallengeFriendPanel extends JPanel {
    private final JPanel cardPanel;

    private final JTextField nicknameField;
    private final JLabel loadingLabel;
    private final JButton challengeFriendButton;

    private final JButton backButton;

    public ChallengeFriendPanel(JPanel cardPanel, ArrayBlockingQueue<String> request) {
        this.cardPanel = cardPanel;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        GridBagConstraints gbcChallengeFriendLabel = new GridBagConstraints();
        GridBagConstraints gbcChallengeFriendText = new GridBagConstraints();
        GridBagConstraints gbcChallengeFriendButton = new GridBagConstraints();

        JPanel challengeFriendPanel = new JPanel(new GridBagLayout());
        challengeFriendPanel.setBackground(Color.WHITE);

        JLabel addFriendLabel = WQGUIUtilities.createStandardLabel("Nickname utente:");

        nicknameField = WQGUIUtilities.createTextField();

        ImageIcon loading = new ImageIcon("ajax-loader.gif");
        loadingLabel = new JLabel(" ", JLabel.CENTER);

        backButton = WQGUIUtilities.createButton("Indietro");

        JPanel backButtonPanel = WQGUIUtilities.createSouthPanel();
        backButtonPanel.add(backButton);

        add(backButtonPanel, BorderLayout.SOUTH);

        challengeFriendButton = WQGUIUtilities.createButton("Sfida");
        challengeFriendButton.addActionListener(actionEvent -> {
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
                challengeFriendButton.setEnabled(false);
                backButton.setEnabled(false);
                loadingLabel.setIcon(loading);
                revalidate();
                request.offer(RequestMessages.CHALLENGE_FROM.toString() + " " + MainMenuPanel.nickname + " "
                        + friendNickname);
                GUIClientMainClass.clientSelector.wakeup();
            }
        });

        backButton.addActionListener(actionEvent -> {
            nicknameField.setText("");

            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, MainFrame.MAINMENU_PANEL);
        });

        // in modo da "ascoltare" il pressing del tasto invio.
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "myAction");
        getActionMap().put("myAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                challengeFriendButton.doClick();
            }
        });

        gbcChallengeFriendText.insets = new Insets(0, 5, 10, 0);
        gbcChallengeFriendLabel.insets = new Insets(0, 0, 10, 5);
        gbcChallengeFriendButton.insets = new Insets(10, 0, 0, 0);

        challengeFriendPanel.add(addFriendLabel, gbcChallengeFriendLabel);

        gbcChallengeFriendText.gridx = 1;
        challengeFriendPanel.add(nicknameField, gbcChallengeFriendText);

        gbcChallengeFriendButton.gridx = 0;
        gbcChallengeFriendButton.gridy = 1;
        gbcChallengeFriendButton.gridwidth = 2;
        challengeFriendPanel.add(challengeFriendButton, gbcChallengeFriendButton);

        gbcChallengeFriendButton.gridy = 2;
        challengeFriendPanel.add(loadingLabel, gbcChallengeFriendButton);

        add(challengeFriendPanel, BorderLayout.CENTER);
    }

    public void challengeFriendResponse(boolean ok, String message) {
        if (ok) {
            nicknameField.setText("");

            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, MainFrame.CHALLENGE_PANEL);
        }
        else {
            JOptionPane.showMessageDialog(this, message);
        }

        challengeFriendButton.setEnabled(true);
        backButton.setEnabled(true);
        loadingLabel.setIcon(null);

        revalidate();
    }

    public void notifyCrash() {
        nicknameField.setText("");
        challengeFriendButton.setEnabled(true);
        backButton.setEnabled(true);
        loadingLabel.setIcon(null);

        revalidate();
    }
}
