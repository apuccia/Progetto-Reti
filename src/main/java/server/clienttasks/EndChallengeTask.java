package server.clienttasks;

import server.*;

import java.nio.channels.SelectionKey;

public class EndChallengeTask implements Runnable {
    private final Challenge challenge;
    private final UsersGraph usersGraph;
    private final WorkersThreadpool workersThreadpool;

    /**
     * Costruisco un nuovo oggetto EndChallengeTask che si occuperà della terminazione della sfida.
     *
     * @param challenge oggetto che incapsula lo stato della sfida.
     * @param usersGraph riferimento alla struttura dati contenente gli utenti di Word Quizzle.
     * @param workersThreadpool oggetto che incapsula i principali worker di Word Quizzle.
     */
    public EndChallengeTask(Challenge challenge, UsersGraph usersGraph,
                            WorkersThreadpool workersThreadpool) {
        this.challenge = challenge;
        this.usersGraph = usersGraph;
        this.workersThreadpool = workersThreadpool;
    }

    @Override
    public void run() {
        String playerANickname = challenge.getPlayerANickname();
        String playerBNickname = challenge.getPlayerBNickname();
        Clique cliqueA = usersGraph.getClique(playerANickname);
        Clique cliqueB = usersGraph.getClique(playerBNickname);
        Player playerA = cliqueA.getPlayerInfo();
        Player playerB = cliqueB.getPlayerInfo();
        Challenge.PlayerScore playerScoreA = challenge.getPlayerScore(playerANickname);
        Challenge.PlayerScore playerScoreB = challenge.getPlayerScore(playerBNickname);

        // lock su entrambe le statistiche dei giocatori per ottenere le loro statistiche correnti.
        playerScoreA.lockPlayerScore();
        playerScoreB.lockPlayerScore();
            int scoreA = playerScoreA.getCurrentScore();
            int scoreB = playerScoreB.getCurrentScore();
            int correctWordsA = playerScoreA.getCorrectWords();
            int correctWordsB = playerScoreB.getCorrectWords();
            int wrongWordsA = playerScoreA.getWrongWords();
            int wrongWordsB = playerScoreB.getWrongWords();
        playerScoreA.unlockPlayerScore();
        playerScoreB.unlockPlayerScore();

        SelectionKey playerAKey = playerScoreA.getPlayerKey();
        SelectionKey playerBKey = playerScoreB.getPlayerKey();

        if (scoreA > scoreB) {
            playerA.writeLockUser();
                playerA.addWin(scoreA + 3);
            playerA.writeUnlockUser();

            playerB.writeLockUser();
                playerB.addLoss(scoreB);
            playerB.writeUnlockUser();

            workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.RESULT.toString() + " WIN "
                    + scoreA + " " + correctWordsA + " " + wrongWordsA + " " + (challenge.getNumberWords() - (correctWordsA +
                    wrongWordsA)) + " " + scoreB, playerAKey);

            workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.RESULT.toString() + " LOSS "
                    + scoreB + " " + correctWordsB + " " + wrongWordsB + " " + (challenge.getNumberWords() - (correctWordsB +
                    wrongWordsB)) + " " + scoreA, playerBKey);
        }
        else if (scoreA < scoreB) {
            playerB.writeLockUser();
                playerB.addWin(scoreB + 3);
            playerB.writeUnlockUser();

            playerA.writeLockUser();
                playerA.addLoss(scoreA);
            playerA.writeUnlockUser();

            workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.RESULT.toString() + " LOSS "
                    + scoreA + " " + correctWordsA + " " + wrongWordsA + " " + (challenge.getNumberWords() - (correctWordsA +
                    wrongWordsA)) + " " + scoreB, playerAKey);

            workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.RESULT.toString() + " WIN "
                    + scoreB + " " + correctWordsB + " " + wrongWordsB + " " + (challenge.getNumberWords() - (correctWordsB +
                    wrongWordsB)) + " " + scoreA, playerBKey);
        }
        else {
            // pareggio.
            playerA.writeLockUser();
                playerA.tie(scoreA);
            playerA.writeUnlockUser();

            playerB.writeLockUser();
                playerB.tie(scoreB);
            playerB.writeUnlockUser();

            workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.RESULT.toString() + " TIE "
                    + scoreA + " " + correctWordsA + " " + wrongWordsA + " " + (challenge.getNumberWords() - (correctWordsA +
                    wrongWordsA)) + " " + scoreB, playerAKey);

            workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.RESULT.toString() + " TIE "
                    + scoreB + " " + correctWordsB + " " + wrongWordsB + " " + (challenge.getNumberWords() - (correctWordsB +
                    wrongWordsB)) + " " + scoreA, playerBKey);
        }

        // serializzo le nuove informazioni sui file json dei giocatori.
        workersThreadpool.executeWriteUserInfoTask(cliqueA, cliqueA.getUserInfoPath());
        workersThreadpool.executeWriteUserInfoTask(cliqueB, cliqueB.getUserInfoPath());

        // entrambi i giocatori ora non sono più in gioco.
        playerA.setGaming(false);
        playerB.setGaming(false);
    }
}
