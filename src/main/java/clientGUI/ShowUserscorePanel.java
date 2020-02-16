package clientGUI;

import javax.swing.*;
import java.awt.*;

public class ShowUserscorePanel extends JPanel {
    private final GridBagConstraints gbcResults;
    private final JPanel showUserscorePanel;
    private final JLabel[] resultsLabels;
    private final JPanel mainMenu;

    public ShowUserscorePanel(JPanel mainMenu) {
        this.mainMenu = mainMenu;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel[] titlesLabels = new JLabel[4];
        resultsLabels = new JLabel[4];

        for (int i = 0; i < 4; i++) {
            titlesLabels[i] = new JLabel();
            titlesLabels[i].setFont(new Font("Arial", Font.BOLD, 16));
            titlesLabels[i].setOpaque(true);
            titlesLabels[i].setForeground(Color.BLACK);
            titlesLabels[i].setBackground(Color.LIGHT_GRAY);
            titlesLabels[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            titlesLabels[i].setHorizontalAlignment(JLabel.CENTER);
        }

        for (int i = 0; i < 4; i++) {
            resultsLabels[i] = new JLabel();
            resultsLabels[i].setFont(new Font("Arial", Font.BOLD, 16));
            resultsLabels[i].setForeground(Color.BLACK);
            resultsLabels[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            resultsLabels[i].setHorizontalAlignment(JLabel.CENTER);
        }

        JButton backButton = WQGUIUtilities.createButton("Indietro");
        backButton.addActionListener(actionEvent -> {
            CardLayout cardLayout = (CardLayout) mainMenu.getLayout();
            cardLayout.show(mainMenu, MainFrame.MAINMENU_PANEL);
        });

        JPanel backButtonPanel = WQGUIUtilities.createSouthPanel();
        backButtonPanel.add(backButton);

        add(backButtonPanel, BorderLayout.SOUTH);

        showUserscorePanel = new JPanel(new GridBagLayout());
        showUserscorePanel.setBackground(Color.WHITE);

        GridBagConstraints gbcTitles = new GridBagConstraints();
        gbcResults = new GridBagConstraints();

        gbcTitles.gridx = 0;
        gbcResults.gridx = 1;
        gbcTitles.fill = GridBagConstraints.HORIZONTAL;
        gbcResults.fill = GridBagConstraints.BOTH;
        gbcTitles.ipadx = 50;
        gbcTitles.ipady = 25;
        gbcResults.ipadx = 50;
        gbcResults.ipady = 25;

        titlesLabels[0].setText("Punteggio Utente");
        showUserscorePanel.add(titlesLabels[0], gbcTitles);

        gbcTitles.gridy = 1;
        titlesLabels[1].setText("Vittorie");
        showUserscorePanel.add(titlesLabels[1], gbcTitles);

        gbcTitles.gridy = 2;
        titlesLabels[2].setText("Sconfitte");
        showUserscorePanel.add(titlesLabels[2], gbcTitles);

        gbcTitles.gridy = 3;
        titlesLabels[3].setText("Rateo");
        showUserscorePanel.add(titlesLabels[3], gbcTitles);

        add(showUserscorePanel, BorderLayout.CENTER);
    }

    public void showUserscoreResponse(long userscore, long wins, long losses, float rateo) {
        gbcResults.gridy = 0;
        resultsLabels[0].setText(Long.toString(userscore));
        showUserscorePanel.add(resultsLabels[0], gbcResults);

        gbcResults.gridy = 1;
        resultsLabels[1].setText(Long.toString(wins));
        showUserscorePanel.add(resultsLabels[1], gbcResults);

        gbcResults.gridy = 2;
        resultsLabels[2].setText(Long.toString(losses));
        showUserscorePanel.add(resultsLabels[2], gbcResults);

        gbcResults.gridy = 3;
        resultsLabels[3].setText(Float.toString(rateo));
        showUserscorePanel.add(resultsLabels[3], gbcResults);

        revalidate();

        CardLayout cardLayout = (CardLayout) mainMenu.getLayout();
        cardLayout.show(mainMenu, MainFrame.SHOWUSERSCORE_PANEL);
    }
}
