package clientGUI;

import server.RequestMessages;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ArrayBlockingQueue;

public class MainMenuPanel extends JPanel {
    public static String nickname = null;

    private final JPanel cardPanel;

    private final JButton addFriendButton;
    private final JButton showUserscoreButton;
    private final JButton showRanksButton;
    private final JButton showFriendlistButton;
    private final JButton challengeFriendButton;
    private final JButton showChallengersButton;
    private final JButton logoutButton;
    private final JLabel loadingLabel;

    public MainMenuPanel(JPanel cardPanel, ArrayBlockingQueue<String> requests) {
        this.cardPanel = cardPanel;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        GridBagConstraints gbcMainMenu = new GridBagConstraints();

        ImageIcon loading = new ImageIcon(MainFrame.LOADINGICO_PATH);
        loadingLabel = new JLabel(" ", JLabel.CENTER);

        gbcMainMenu.insets = new Insets(0, 0, 10, 0);

        addFriendButton = WQGUIUtilities.createButton("Aggiungi amico");
        addFriendButton.addActionListener(actionEvent -> {
            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, MainFrame.ADDFRIEND_PANEL);
        });

        showUserscoreButton = WQGUIUtilities.createButton("Punteggio utente");
        showUserscoreButton.addActionListener(actionEvent -> {
            setAllEnabled(false);
            loadingLabel.setIcon(loading);
            revalidate();
            requests.offer(RequestMessages.SHOW_USERSCORE.toString() + " " + nickname);
            GUIClientMainClass.clientSelector.wakeup();
        });

        showRanksButton = WQGUIUtilities.createButton("Classifica");
        showRanksButton.addActionListener(actionEvent -> {
            setAllEnabled(false);
            loadingLabel.setIcon(loading);
            revalidate();
            requests.offer(RequestMessages.SHOW_RANKS.toString() + " " + nickname);
            GUIClientMainClass.clientSelector.wakeup();
        });

        showFriendlistButton = WQGUIUtilities.createButton("Lista amici");
        showFriendlistButton.addActionListener(actionEvent -> {
            setAllEnabled(false);
            loadingLabel.setIcon(loading);
            revalidate();
            requests.offer(RequestMessages.SHOW_FRIENDS.toString() + " " + nickname);
            GUIClientMainClass.clientSelector.wakeup();
        });

        challengeFriendButton = WQGUIUtilities.createButton("Sfida amico");
        challengeFriendButton.addActionListener(actionEvent -> {
            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, MainFrame.CHALLENGEFRIEND_PANEL);
        });

        showChallengersButton = WQGUIUtilities.createButton("Visualizza sfide");
        showChallengersButton.setEnabled(false);
        showChallengersButton.addActionListener(actionEvent -> {
            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, MainFrame.SHOWCHALLENGES_PANEL);
        });

        logoutButton = WQGUIUtilities.createButton("Logout");
        logoutButton.addActionListener(actionEvent -> {
            setAllEnabled(false);
            loadingLabel.setIcon(loading);
            revalidate();
            requests.offer(RequestMessages.LOGOUT.toString() + " " + nickname);
            GUIClientMainClass.clientSelector.wakeup();
        });

        JPanel mainMenuPanel = new JPanel(new GridBagLayout());
        mainMenuPanel.setBackground(Color.WHITE);

        gbcMainMenu.fill = GridBagConstraints.HORIZONTAL;
        mainMenuPanel.add(addFriendButton, gbcMainMenu);

        gbcMainMenu.gridy = 1;
        mainMenuPanel.add(showUserscoreButton, gbcMainMenu);

        gbcMainMenu.gridy = 2;
        mainMenuPanel.add(showRanksButton, gbcMainMenu);

        gbcMainMenu.gridy = 3;
        mainMenuPanel.add(showFriendlistButton, gbcMainMenu);

        gbcMainMenu.gridy = 4;
        mainMenuPanel.add(challengeFriendButton, gbcMainMenu);

        gbcMainMenu.gridy = 5;
        mainMenuPanel.add(showChallengersButton, gbcMainMenu);

        gbcMainMenu.gridy = 6;
        mainMenuPanel.add(logoutButton, gbcMainMenu);

        gbcMainMenu.gridy = 7;
        mainMenuPanel.add(loadingLabel, gbcMainMenu);

        add(mainMenuPanel, BorderLayout.CENTER);
        add(WQGUIUtilities.createSouthPanel(), BorderLayout.SOUTH);
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void clearChallengeFriendButton() {
        challengeFriendButton.setEnabled(true);
        loadingLabel.setIcon(null);

        revalidate();
    }

    public void clearShowChallengesButton() {
        showChallengersButton.setEnabled(true);
        loadingLabel.setIcon(null);

        revalidate();
    }

    public void notifyLogoutResponse(String message) {
        setAllEnabled(true);
        loadingLabel.setIcon(null);

        CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
        cardLayout.show(cardPanel, MainFrame.LOGIN_PANEL);

        JOptionPane.showMessageDialog(this, message);

        revalidate();
    }

    public void notifyCrash() {
        setAllEnabled(true);
        loadingLabel.setIcon(null);

        revalidate();
    }

    public void setAllEnabled(boolean mode) {
        showFriendlistButton.setEnabled(mode);
        showUserscoreButton.setEnabled(mode);
        showRanksButton.setEnabled(mode);
        showFriendlistButton.setEnabled(mode);
        challengeFriendButton.setEnabled(mode);
        logoutButton.setEnabled(mode);
    }

    public void unsetLoadingIcon() {
        loadingLabel.setIcon(null);
    }

    public void notifyNewChallenge() {
        showChallengersButton.setEnabled(true);
    }

    public void notifyNoChallenge() {
        showChallengersButton.setEnabled(false);
    }
}
