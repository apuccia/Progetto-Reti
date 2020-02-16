package clientGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

public class MainFrame extends JFrame {
    private final LoginPanel loginPanel;
    private final RegistrationPanel registrationPanel;
    private final MainMenuPanel mainMenuPanel;
    private final AddFriendPanel addFriendPanel;
    private final ShowUserscorePanel showUserscorePanel;
    private final ShowFriendlistPanel showFriendlistPanel;
    private final ShowRanksPanel showRanksPanel;
    private final ShowChallengesPanel showChallengesPanel;
    private final ChallengeFriendPanel challengeFriendPanel;
    private final ChallengePanel challengePanel;

    private final JPanel cardPanel;

    public static final String LOGIN_PANEL = "LOGIN_PANEL";
    public static final String REGISTRATION_PANEL = "REGISTRATION_PANEL";
    public static final String MAINMENU_PANEL = "MAINMENU_PANEL";
    public static final String ADDFRIEND_PANEL = "ADDFRIEND_PANEL";
    public static final String SHOWUSERSCORE_PANEL = "SHOWUSERSCORE_PANEL";
    public static final String SHOWFRIENDLIST_PANEL = "SHOWFRIENDLIST_PANEL";
    public static final String SHOWRANKS_PANEL = "SHOWRANKS_PANEL";
    public static final String SHOWCHALLENGES_PANEL = "SHOWCHALLENGES_PANEL";
    public static final String CHALLENGEFRIEND_PANEL = "CHALLENGEFRIEND_PANEL";
    public static final String CHALLENGE_PANEL = "CHALLENGE_PANEL";

    private static final String WQLOGO_PATH = "wqlogo.png";
    public static final String LOADINGICO_PATH = "ajax-loader.gif";

    public MainFrame(String windowTitle, ArrayBlockingQueue<String> registrationRequest,
                     ArrayBlockingQueue<String> request, ArrayBlockingQueue<String> challengeUpdateRequest) {
        super(windowTitle);

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(true);

        // pannello principale.
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel);

        // pannello immagine wordquizzle
        BufferedImage wqPicture = null;
        try {
            wqPicture = ImageIO.read(new File(WQLOGO_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JLabel picLabel = new JLabel(new ImageIcon(wqPicture));
        mainPanel.add(picLabel, BorderLayout.NORTH);

        // card layout per switchare tra i diversi panel.
        cardPanel = new JPanel(new CardLayout());

        cardPanel.add(loginPanel = new LoginPanel(cardPanel, request), LOGIN_PANEL);
        cardPanel.add(registrationPanel = new RegistrationPanel(cardPanel, registrationRequest), REGISTRATION_PANEL);
        cardPanel.add(mainMenuPanel = new MainMenuPanel(cardPanel, request), MAINMENU_PANEL);
        cardPanel.add(addFriendPanel = new AddFriendPanel(cardPanel, request), ADDFRIEND_PANEL);
        cardPanel.add(showUserscorePanel = new ShowUserscorePanel(cardPanel), SHOWUSERSCORE_PANEL);
        cardPanel.add(showRanksPanel = new ShowRanksPanel(cardPanel), SHOWRANKS_PANEL);
        cardPanel.add(showFriendlistPanel = new ShowFriendlistPanel(cardPanel), SHOWFRIENDLIST_PANEL);
        cardPanel.add(showChallengesPanel = new ShowChallengesPanel(cardPanel, mainMenuPanel, request, challengeUpdateRequest),
                SHOWCHALLENGES_PANEL);
        cardPanel.add(challengeFriendPanel = new ChallengeFriendPanel(cardPanel, request), CHALLENGEFRIEND_PANEL);
        cardPanel.add(challengePanel = new ChallengePanel(cardPanel, request), CHALLENGE_PANEL);

        mainPanel.add(cardPanel, BorderLayout.CENTER);
    }

    public void notifyLoginResponse(boolean ok, String message) {
        loginPanel.loginResponse(ok, message);
    }

    public void notifyRegistrationResponse(boolean ok, String message) {
        registrationPanel.registrationResponse(ok, message);
    }

    public void notifyAddFriendResponse(boolean ok, String message) {
        addFriendPanel.addFriendResponse(ok, message);
    }

    public void notifyUserscoreResponse(long userscore, long wins, long losses, float rateo) {
        showUserscorePanel.showUserscoreResponse(userscore, wins, losses, rateo);
        mainMenuPanel.setAllEnabled(true);
        mainMenuPanel.unsetLoadingIcon();
    }

    public void notifyRanksResponse(String[][] ranks) {
        showRanksPanel.notifyRanks(ranks);
        mainMenuPanel.setAllEnabled(true);
        mainMenuPanel.unsetLoadingIcon();
    }

    public void notifyFriendlistResponse(String[][] friendlist) {
        showFriendlistPanel.notifyFriendlist(friendlist);
        mainMenuPanel.setAllEnabled(true);
        mainMenuPanel.unsetLoadingIcon();
    }

    public void notifyLogoutResponse(String message) {
        mainMenuPanel.notifyLogoutResponse(message);
    }

    public void notifyChallenges(String[] challengers) {
        showChallengesPanel.notifyChallenges(challengers);
    }

    public void notifyChallengeRequest(boolean ok, String message) {
        challengeFriendPanel.challengeFriendResponse(ok, message);
    }

    public void notifyAccept(boolean ok, String message) {
        showChallengesPanel.notifyAccept(ok, message);
    }

    public void notifyWord(String word) {
        challengePanel.notifyNewWord(word);
    }

    public void notifyNewChallenge() {
        mainMenuPanel.notifyNewChallenge();
    }

    public void notifyEndChallenge(String message) {
        challengePanel.notifyEndChallenge(message);
    }

    public void notifyNoChallenge() {
        mainMenuPanel.notifyNoChallenge();
    }

    public void notifyCrash() {
        loginPanel.notifyCrash();
        registrationPanel.notifyCrash();
        mainMenuPanel.notifyCrash();
        addFriendPanel.notifyCrash();
        showChallengesPanel.notifyCrash();
        challengeFriendPanel.notifyCrash();
        challengePanel.notifyCrash();

        JOptionPane.showMessageDialog(this, "Il server è offline");

        CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
        cardLayout.show(cardPanel, LOGIN_PANEL);
    }
}
