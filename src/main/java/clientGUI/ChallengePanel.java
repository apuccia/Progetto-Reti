package clientGUI;

import server.RequestMessages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.ArrayBlockingQueue;

public class ChallengePanel extends JPanel {
    private final JPanel cardPanel;

    private JPanel screen;
    private final JPanel southPanel;
    private final JLabel loadingLabel;
    private final JTextField inputField;
    private final JButton enterButton;
    private String currentWord;
    private JScrollPane scrollPanel;
    private final JButton backButton;

    public ChallengePanel(JPanel cardPanel, ArrayBlockingQueue<String> request) {
        this.cardPanel = cardPanel;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        southPanel = WQGUIUtilities.createSouthPanel();

        backButton = WQGUIUtilities.createButton("Menu");
        backButton.setEnabled(false);
        backButton.setVisible(false);

        southPanel.add(backButton);

        add(southPanel, BorderLayout.SOUTH);

        JPanel challengePanel = new JPanel(new GridBagLayout());
        challengePanel.setBackground(Color.WHITE);

        GridBagConstraints gbcChallenge = new GridBagConstraints();
        gbcChallenge.gridwidth = 2;
        gbcChallenge.fill = GridBagConstraints.BOTH;
        gbcChallenge.insets = new Insets(0, 0, 10, 0);

        screen = new JPanel();
        screen.setLayout(new BoxLayout(screen, BoxLayout.Y_AXIS));
        screen.setBackground(Color.WHITE);
        screen.setVisible(true);
        screen.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        scrollPanel = new JScrollPane(screen);

        JPanel panel = new JPanel(new BorderLayout());

        gbcChallenge.gridwidth = 1;
        gbcChallenge.gridy = 1;
        inputField = WQGUIUtilities.createTextField();

        challengePanel.add(inputField, gbcChallenge);

        backButton.addActionListener(actionEvent -> {
            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, MainFrame.MAINMENU_PANEL);

            inputField.setText("");
            screen.removeAll();
            revalidate();
        });

        ImageIcon loading = new ImageIcon("ajax-loader.gif");
        loadingLabel = new JLabel(" ", JLabel.CENTER);

        gbcChallenge.gridx = 1;
        enterButton = WQGUIUtilities.createButton("Invia");
        enterButton.addActionListener(actionEvent -> {
            String translation = inputField.getText();

            if (translation.contains(" ")) {
                JOptionPane.showMessageDialog(this, "Non sono ammessi spazi");
            }
            else if (!translation.isEmpty()) {
                inputField.setText("");
                loadingLabel.setIcon(loading);
                enterButton.setEnabled(false);
                request.offer(RequestMessages.TRANSLATION + " " + MainMenuPanel.nickname + " " + currentWord + " " +
                        translation);

                GUIClientMainClass.clientSelector.wakeup();

                JLabel answerLabel = WQGUIUtilities.createStandardLabel(translation);
                answerLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                screen.add(answerLabel);

                revalidate();
            }
        });

        // in modo da "ascoltare" il pressing del tasto invio.
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "enterAction");
        getActionMap().put("enterAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                enterButton.doClick();
            }
        });

        challengePanel.add(enterButton, gbcChallenge);

        gbcChallenge.gridy = 2;
        gbcChallenge.gridwidth = 2;
        challengePanel.add(loadingLabel, gbcChallenge);

        JPanel fakePanel = new JPanel();
        fakePanel.setBackground(Color.WHITE);
        fakePanel.setBorder(BorderFactory.createEmptyBorder(0, 100, 0, 100));
        JPanel fakePanel1 = new JPanel();
        fakePanel1.setBackground(Color.WHITE);
        fakePanel1.setBorder(BorderFactory.createEmptyBorder(0, 100, 0, 100));

        panel.add(scrollPanel, BorderLayout.CENTER);
        panel.add(challengePanel, BorderLayout.SOUTH);
        panel.add(fakePanel, BorderLayout.EAST);
        panel.add(fakePanel1, BorderLayout.WEST);

        add(panel, BorderLayout.CENTER);
    }

    public void notifyNewWord(String word) {
        currentWord = word;

        JLabel label = WQGUIUtilities.createStandardLabel("> " + word);

        screen.add(label);

        JScrollBar vertical = scrollPanel.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());

        loadingLabel.setIcon(null);
        enterButton.setEnabled(true);

        revalidate();
    }

    public void notifyEndChallenge(String message) {
        loadingLabel.setIcon(null);
        backButton.setEnabled(true);
        backButton.setVisible(true);
        enterButton.setEnabled(false);

        JOptionPane.showMessageDialog(this, message);

        revalidate();
    }

    public void notifyCrash() {
        screen.removeAll();
        inputField.setText("");
        loadingLabel.setIcon(null);
        enterButton.setEnabled(true);

        revalidate();
    }
}
