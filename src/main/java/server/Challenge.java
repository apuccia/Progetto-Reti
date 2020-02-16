package server;

import java.nio.channels.SelectionKey;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Challenge {
    private final PlayerScore playerAScore, playerBScore;
    private String[] words;
    private AtomicInteger isEnded;
    private AtomicInteger isAccepted;

    public class PlayerScore {
        // nickname del giocatore.
        private String playerNickname;
        // statistiche della partita del giocatore.
        private int wordCounter = 1, correctWords = 0, wrongWords = 0, currentScore = 0;
        // SelectionKey del giocatore;
        private SelectionKey playerKey;
        // lock per aggiornare le statistiche della partita.
        private ReentrantLock playerScoreLock;

        /**
         * Costruisce un nuovo oggetto PlayerScore che rappresenterà lo stato di un giocatore durante una partita.
         *
         * @param playerNickname nickname del giocatore.
         */
        private PlayerScore(String playerNickname) {
            this.playerNickname = playerNickname;
            playerScoreLock = new ReentrantLock(true);
        }

        /**
         * Metodo che restituisce il nickname del giocatore.
         *
         * @return nickname del giocatore.
         */
        private String getPlayerNickname() {
            return playerNickname;
        }

        /**
         * Metodo che aggiorna le statistiche della partita del giocatore.
         *
         * @param correct booleano che indica se la traduzione proposta dal giocatore sia corretta o meno.
         */
        private void updateScore(boolean correct) {
            if (correct) {
                currentScore += 5;
                correctWords++;
            }
            else {
                wrongWords++;
            }
        }

        /**
         * Metodo che restituisce il punteggio corrente del giocatore.
         *
         * @return intero rappresentante il punteggio.
         */
        public int getCurrentScore() {
            return currentScore;
        }

        /**
         * Metodo che restituisce l'indice della prossima parola da inviare al giocatore.
         *
         * @return intero rappresentante l'indice della prossima parola.
         */
        private int nextWord() {
            return wordCounter++;
        }

        /**
         * Metodo che restituisce le parole corrette del giocatore.
         *
         * @return intero rappresentante il numero di parole corrette del giocatore.
         */
        public int getCorrectWords() {
            return correctWords;
        }

        /**
         * Metodo che restituisce le parole sbagliate dal giocatore.
         *
         * @return intero rappresentante il numero di parole sbagliate dal giocatore.
         */
        public int getWrongWords() {
            return wrongWords;
        }

        /**
         * Metodo che inizializza la SelectionKey del giocatore.
         *
         * @param playerKey SelectionKey da associare al giocatore.
         */
        private void setPlayerKey(SelectionKey playerKey) {
            this.playerKey = playerKey;
        }

        /**
         * Metodo che restituisce la SelectionKey del giocatore.
         *
         * @return SelectionKey del giocatore.
         */
        public SelectionKey getPlayerKey() {
            return playerKey;
        }

        /**
         * Metodo per acquisire la lock sulle informazioni del giocatore.
         */
        public void lockPlayerScore() {
            playerScoreLock.lock();
        }

        /**
         * Metodo per rilasciare la lock sulle informazioni dell'utente.
         */
        public void unlockPlayerScore() {
            playerScoreLock.unlock();
        }
    }

    /**
     * Metoodo che costruisce un nuovo oggetto Challenge che rappresenterà lo stato di una partita per entrambi i giocatori.
     *
     * @param playerANickname nickname del giocatore A.
     * @param playerBNickname nickname del giocatore B.
     */
    public Challenge(String playerANickname, String playerBNickname) {
        playerAScore = new PlayerScore(playerANickname);
        playerBScore = new PlayerScore(playerBNickname);
        words = null;
        isEnded = new AtomicInteger();
        isAccepted = new AtomicInteger();
    }

    /**
     * Metodo che aggiunge 1 nel caso in cui un giocatore abbia finito le parole a disposizione.
     *
     * @return intero che rappresenta il numero di giocatori che hanno finito le parole a disposizione.
     */
    public int playerEnded() {
        return isEnded.addAndGet(1);
    }

    /**
     * Metodo che permette di capire se una partita sia terminata a seguito della terminazione delle parole da parte di
     * entrambi i giocatori oppure a seguito del timeout T2.
     *
     * @return true se la partita è terminata, false altrimenti.
     */
    public boolean isEnded() {
        return isEnded.get() >= 2;
    }

    /**
     * Metodo che permette di capire se una partita possa essere terminata a seguito del timeout.
     *
     * @return intero che permetterà al chiamante di capire se creare o meno il task di terminazione della partita.
     */
    public int timeoutEnd() {
        return isEnded.getAndSet(3);
    }

    /**
     * Metodo che permette al giocatore sfidato di accettare la sfida.
     *
     * @return true se la sfida era accettabile, false altrimenti.
     */
    public boolean acceptChallenge() {
        return isAccepted.compareAndSet(0, 1);
    }

    /**
     * Metodo che permette al giocatore sfidato di declinare la sfida (di sua volontà oppure a seguito del timeout T1).
     *
     * @return true se la partita è stata declinata, false altrimenti.
     */
    public boolean declineChallenge() {
        return isAccepted.compareAndSet(0, 2);
    }

    /**
     * Metodo che permette di inizializzare l'array contenente le parole da inviare ad entrambi i giocatori.
     *
     * @param words Array di parole italiane da utilizzare durante la partita.
     */
    public void setWords(String[] words) {
        this.words = words;
    }

    /**
     * Metodo che restituisce il numero di parole totali.
     *
     * @return intero rappresentante il numero di parole totali.
     */
    public int getNumberWords() {
        return words.length;
    }

    /**
     * Metodo che restituisce il nickname del giocatore A.
     *
     * @return nickname del giocatore A.
     */
    public String getPlayerANickname() {
        return playerAScore.playerNickname;
    }

    /**
     * Metodo che restituisce il nickname del giocatore B.
     *
     * @return nickname del giocatore B.
     */
    public String getPlayerBNickname() {
        return playerBScore.playerNickname;
    }

    /**
     * Metodo che inizializza la SelectionKey del giocatore specificato.
     *
     * @param nickname nickname del giocatore a cui associare la SelectionKey.
     * @param playerKey SelectionKey del giocatore.
     */
    public void setPlayerKey(String nickname, SelectionKey playerKey) {
        if (playerAScore.getPlayerNickname().equals(nickname)) {
            playerAScore.setPlayerKey(playerKey);
        }
        else {
            playerBScore.setPlayerKey(playerKey);
        }
    }

    /**
     * Metodo che restituisce la SelectionKey del giocatore specificato.
     *
     * @param nickname nickname del giocatore.
     * @return SelectionKey del giocatore.
     */
    public SelectionKey getPlayerKey(String nickname) {
        if (playerAScore.getPlayerNickname().equals(nickname)) {
            return playerAScore.getPlayerKey();
        }
        else {
            return playerBScore.getPlayerKey();
        }
    }

    /**
     * Metodo che aggiorna il punteggio del giocatore specificato e restituisce la prossima parola.
     *
     * @param nickname nickname del giocatore.
     * @param correct booleano indicante la risposta corretta o meno.
     * @return restituisce la prossima parola da inviare al giocatore se presente, null altrimenti.
     */
    public String updateScore(String nickname, boolean correct) {
        int nextWord;

        if (playerAScore.getPlayerNickname().equals(nickname)) {
            if (correct) {
                playerAScore.updateScore(true);
            }
            else {
                playerAScore.updateScore(false);
            }
            nextWord = playerAScore.nextWord();
        }
        else {
            if (correct) {
                playerBScore.updateScore(true);
            }
            else {
                playerBScore.updateScore(false);
            }
            nextWord = playerBScore.nextWord();
        }

        if (nextWord == words.length) {
            return null;
        }

        return words[nextWord];
    }

    /**
     * Restituisce l'oggetto contenente le statistiche della partita del giocatore specificato.
     *
     * @param nickname nickname del giocatore.
     * @return oggetto PlayerScore che incapsula le statistiche del giocatore.
     */
    public PlayerScore getPlayerScore(String nickname) {
        if (nickname.equals(playerAScore.playerNickname)) {
            return playerAScore;
        }
        else {
            return playerBScore;
        }
    }

    /**
     * Metodo per acquisire la lock sulle informazioni del giocatore.
     *
     * @param nickname nickname del giocatore di cui acquisire la lock.
     */
    public void lockPlayerScore(String nickname) {
        if (playerAScore.getPlayerNickname().equals(nickname)) {
            playerAScore.lockPlayerScore();
        }
        else {
            playerBScore.lockPlayerScore();
        }
    }

    /**
     * Metodo per rilasciare la lock sulle informazioni del giocatore.
     *
     * @param nickname nickname del giocatore di cui rilasciare la lock.
     */
    public void unlockPlayerScore(String nickname) {
        if (playerAScore.getPlayerNickname().equals(nickname)) {
            playerAScore.unlockPlayerScore();
        }
        else {
            playerBScore.unlockPlayerScore();
        }
    }
}
