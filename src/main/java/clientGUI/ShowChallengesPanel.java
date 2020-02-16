package clientGUI;

import server.RequestMessages;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ArrayBlockingQueue;

public class ShowChallengesPanel extends JPanel {
    private final JPanel cardPanel;
    private final MainMenuPanel mainMenuPanel;

    private JComboBox<String> challengesComboBox;
    private final JLabel loadingLabel;
    private final ImageIcon loading;
    private JButton acceptButton;
    private JButton refuseButton;
    private JButton backButton;
    private String choose;

    public ShowChallengesPanel(JPanel cardPanel, MainMenuPanel mainMenuPanel, ArrayBlockingQueue<String> request,
                               ArrayBlockingQueue<String> challengeUpdateRequest) {
        this.cardPanel = cardPanel;
        this.mainMenuPanel = mainMenuPanel;

        setLayout(new BorderLayout());

        loading = new ImageIcon(MainFrame.LOADINGICO_PATH);
        loadingLabel = new JLabel(" ", JLabel.CENTER);

        backButton = WQGUIUtilities.createButton("Indietro");
        backButton.addActionListener(actionEvent -> {
            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, MainFrame.MAINMENU_PANEL);
        });

        JPanel backButtonPanel = WQGUIUtilities.createSouthPanel();
        backButtonPanel.add(backButton);

        JPanel challengersPanel = new JPanel(new GridBagLayout());
        challengersPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 10, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipadx = 100;

        challengesComboBox = new JComboBox<>();
        challengesComboBox.setBackground(Color.WHITE);
        challengesComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        challengesComboBox.setForeground(Color.BLACK);
        challengesComboBox.setEnabled(false);

        acceptButton = WQGUIUtilities.createButton("Accetta");
        acceptButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        acceptButton.setEnabled(false);

        acceptButton.addActionListener(actionEvent -> {
            choose = (String) challengesComboBox.getSelectedItem();
            challengesComboBox.removeItem(choose);

            if (challengesComboBox.getItemCount() == 0) {
                acceptButton.setEnabled(false);
                refuseButton.setEnabled(false);
                challengesComboBox.setEnabled(false);
                mainMenuPanel.notifyNoChallenge();
            }

            backButton.setEnabled(false);
            request.offer(RequestMessages.CHALLENGE_ACCEPTED.toString() + " " + choose + " " + MainMenuPanel.nickname);
            challengeUpdateRequest.offer(choose);

            loadingLabel.setIcon(loading);
            loadingLabel.setText("In attesa di risposta...");

            revalidate();
            GUIClientMainClass.clientSelector.wakeup();
        });

        refuseButton = WQGUIUtilities.createButton("Rifiuta");
        refuseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        refuseButton.setEnabled(false);

        refuseButton.addActionListener(actionEvent -> {
            choose = (String) challengesComboBox.getSelectedItem();

            challengesComboBox.removeItem(choose);
            if (challengesComboBox.getItemCount() == 0) {
                acceptButton.setEnabled(false);
                refuseButton.setEnabled(false);
                challengesComboBox.setEnabled(false);
                mainMenuPanel.notifyNoChallenge();
            }

            request.offer(RequestMessages.CHALLENGE_REFUSED.toString() + " " + choose);
            challengeUpdateRequest.offer(choose);

            GUIClientMainClass.clientSelector.wakeup();

            revalidate();
        });

        challengersPanel.add(challengesComboBox, gbc);

        gbc.gridy = 1;
        challengersPanel.add(acceptButton, gbc);

        gbc.gridy = 2;
        challengersPanel.add(refuseButton, gbc);

        gbc.gridy = 3;
        challengersPanel.add(loadingLabel, gbc);

        add(challengersPanel, BorderLayout.CENTER);
        add(backButtonPanel, BorderLayout.SOUTH);
    }

    public void notifyChallenges(String[] challengersNicknames) {
        if (challengersNicknames.length == 0) {
            acceptButton.setEnabled(false);
            refuseButton.setEnabled(false);
            challengesComboBox.setEnabled(false);
        }
        else {
            acceptButton.setEnabled(true);
            refuseButton.setEnabled(true);
            challengesComboBox.setEnabled(true);
        }

        challengesComboBox.removeAllItems();
        for (String challenger : challengersNicknames) {
            challengesComboBox.addItem(challenger);
        }

        revalidate();
    }

    public void notifyAccept(boolean ok, String message) {
        if (ok) {
            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, MainFrame.CHALLENGE_PANEL);
        }
        else {
            JOptionPane.showMessageDialog(this, message);
            challengesComboBox.removeItem(choose);
        }

        backButton.setEnabled(true);
        loadingLabel.setIcon(null);
        loadingLabel.setText("");

        revalidate();
    }

    public void notifyCrash() {
        if (acceptButton != null) {
            acceptButton.setEnabled(true);
        }

        if (refuseButton != null) {
            refuseButton.setEnabled(true);
        }

        if (loadingLabel != null) {
            loadingLabel.setIcon(null);
            loadingLabel.setText("");
        }

        revalidate();
    }
}
