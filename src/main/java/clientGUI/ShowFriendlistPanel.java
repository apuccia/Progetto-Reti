package clientGUI;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ArrayBlockingQueue;

public class ShowFriendlistPanel extends JPanel {
    JPanel cardPanel;

    public ShowFriendlistPanel(JPanel cardPanel) {
        this.cardPanel = cardPanel;

        setLayout(new BorderLayout());

        JButton backButton = WQGUIUtilities.createButton("Indietro");
        backButton.addActionListener(actionEvent -> {
            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, MainFrame.MAINMENU_PANEL);
        });

        JPanel backButtonPanel = WQGUIUtilities.createSouthPanel();
        backButtonPanel.add(backButton);

        add(backButtonPanel, BorderLayout.SOUTH);
    }

    public void notifyFriendlist(String[][] friendlist) {
        String[] titles = {"Nicknames"};

        BorderLayout layout = (BorderLayout) getLayout();
        Component component = layout.getLayoutComponent(BorderLayout.CENTER);

        if (component != null) {
            remove(layout.getLayoutComponent(BorderLayout.CENTER));
        }

        add(WQGUIUtilities.createTable(titles, friendlist), BorderLayout.CENTER);

        CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
        cardLayout.show(cardPanel, MainFrame.SHOWFRIENDLIST_PANEL);
    }
}
