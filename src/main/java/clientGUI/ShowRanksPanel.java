package clientGUI;

import javax.swing.*;
import java.awt.*;

public class ShowRanksPanel extends JPanel {
    JPanel cardPanel;

    public ShowRanksPanel(JPanel cardPanel) {
        this.cardPanel = cardPanel;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JButton backButton = WQGUIUtilities.createButton("Indietro");
        backButton.addActionListener(actionEvent -> {
            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, MainFrame.MAINMENU_PANEL);
        });

        JPanel backButtonPanel = WQGUIUtilities.createSouthPanel();
        backButtonPanel.add(backButton);

        add(backButtonPanel, BorderLayout.SOUTH);
    }

    public void notifyRanks(String[][] rankings) {
        String[] titles = {"Nicknames", "Punteggio", "Vittorie", "Sconfitte", "Rateo"};

        BorderLayout layout = (BorderLayout) getLayout();
        Component component = layout.getLayoutComponent(BorderLayout.CENTER);

        if (component != null) {
            remove(layout.getLayoutComponent(BorderLayout.CENTER));
        }

        add(WQGUIUtilities.createTable(titles, rankings), BorderLayout.CENTER);

        revalidate();

        CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
        cardLayout.show(cardPanel, MainFrame.SHOWRANKS_PANEL);
    }
}
