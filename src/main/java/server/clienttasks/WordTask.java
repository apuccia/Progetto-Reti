package server.clienttasks;

import server.*;

import java.util.concurrent.ConcurrentHashMap;

public class WordTask implements Runnable {
    private final Challenge challenge;
    private final String player;
    private final String word;
    private final String translation;
    private final ConcurrentHashMap<String, String> translatedWords;
    private final WorkersThreadpool workersThreadpool;
    private final UsersGraph usersGraph;

    /**
     * Costruisce un nuovo oggetto WordTask che si occupa del controllo di una traduzione e dell'invio della prossima
     * parola se necessario.
     *
     * @param challenge riferimento all'oggetto che incapsula lo stato della partita.
     * @param player nickname dell'utente che ha inviato la traduzione.
     * @param word parola italiana.
     * @param translation traduzione che ha inviato l'utente.
     * @param translatedWords struttura dati che contiene le traduzioni.
     * @param usersGraph struttura dati che contiene gli utenti di Word Quizzle.
     * @param workersThreadpool oggetto che incapsula i principali worker di Word Quizzle.
     */
    public WordTask(Challenge challenge, String player, String word, String translation,
                    ConcurrentHashMap<String, String> translatedWords, UsersGraph usersGraph,
                    WorkersThreadpool workersThreadpool) {
        this.challenge = challenge;
        this.player = player;
        this.word = word.toLowerCase();
        this.translation = translation;
        this.translatedWords = translatedWords;
        this.workersThreadpool = workersThreadpool;
        this.usersGraph = usersGraph;
    }

    @Override
    public void run() {
        if (!challenge.isEnded()) {
            String nextWord;

            System.out.println("---" + player + " " + word + " " + translation);

            challenge.lockPlayerScore(player);
                nextWord = challenge.updateScore(player, translatedWords.get(word).equals(translation));
            challenge.unlockPlayerScore(player);

            if (nextWord == null) {
                // il giocatore ha risposto a tutte le parole.
                if (challenge.playerEnded() == 2) {
                    // entrambi i giocatori hanno terminato.
                    workersThreadpool.executeEndChallengeTask(challenge.getPlayerANickname(), challenge.getPlayerBNickname());
                }
            }
            else {
                workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.WORD + " " + nextWord,
                        challenge.getPlayerKey(player));
            }
        }
    }
}
