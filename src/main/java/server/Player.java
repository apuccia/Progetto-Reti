package server;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Player {
    private final String nickname;                  // nickname dell'utente.
    private long userscore;                         // punteggio utente.
    private long wins;                              // vittorie totali dell'utente.
    private long losses;                            // sconfitte totali dell'utente.
    private float rateo;                            // rapporto vittorie - sconfitte dell'utente.

    transient private boolean gaming;         // per verificare se un utente sta giocando.

    transient private ReentrantReadWriteLock userLock; // lock per la sincronizzazione delle informazioni dell'utente.

    /**
     * Costruisce un nuovo oggetto Player che rappresenta le informazioni dell'utente.
     *
     * @param nickname nickname dell'utente.
     */
    public Player(String nickname) {
        this.nickname = nickname;
        userscore = 0;
        wins = 0;
        losses = 0;
        rateo = 0;

        gaming = false;
        userLock = new ReentrantReadWriteLock(true);  // lock per la sincronizzazione delle informazioni dell'utente.
    }

    /**
     * Costruisce un nuovo oggetto Player che rappresenta le informazioni dell'utente a partire da informazioni già
     * esistenti.
     *
     * @param nickname nickname dell'utente.
     * @param userScore punteggio utente.
     * @param wins vittorie dell'utente.
     * @param losses sconfitte dell'utente.
     * @param rateo rapporto vittorie/sconfitte dell'utente.
     */
    public Player(String nickname, long userScore, long wins, long losses, float rateo) {
        this.nickname = nickname;
        this.userscore = userScore;
        this.wins = wins;
        this.losses = losses;
        this.rateo = rateo;

        gaming = false;
        userLock = null;
    }

    /**
     * Metodo che restituisce il nickname dell'utente.
     *
     * @return il nickname dell'utente.
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Metodo che restituisce il punteggio utente.
     *
     * @return il punteggio utente.
     */
    public long getUserScore() {
        long userScore = this.userscore;

        return userScore;
    }

    /**
     * Metodo che aggiunge il punteggio di una partita al punteggio utente.
     *
     * @param matchScore punteggio della partita.
     */
    private void addMatchScore(long matchScore) {
        assert matchScore > 0 : "matchscore non valido";

        userscore += matchScore;
    }

    /**
     * Metodo che restituisce le vittorie totali di un utente.
     *
     * @return un valore long che rappresenta le vittorie dell'utente.
     */
    public long getWins() {
        long wins = this.wins;

        return wins;
    }

    /**
     * Metodo che aggiunge una vittoria al totale vittorie dell'utente, aggiornando il punteggio utente e il rateo.
     *
     * @param matchScore punteggio della partita.
     */
    public void addWin(long matchScore) {
        addMatchScore(matchScore);
        wins++;
        setRateo();
    }

    /**
     * Metodo che restituisce le sconfitte totali di un utente.
     *
     * @return un valore long che rappresenta le sconfitte dell'utente.
     */
    public long getLosses() {
        long losses = this.losses;

        return losses;
    }

    /**
     * Metodo che aggiunge una sconfitta al totale sconfitte dell'utente, aggiornando il punteggio utente e il rateo.
     *
     * @param matchScore punteggio della partita.
     */
    public void addLoss(long matchScore) {
        addMatchScore(matchScore);
        losses++;
        setRateo();
    }

    /**
     * Metodo che aggiorna il punteggio utente senza modificare le vittorie e sconfitte a seguito di un pareggio tra 2
     * giocatori.
     *
     * @param matchScore punteggio della partita.
     */
    public void tie(long matchScore) {
        addMatchScore(matchScore);
    }

    /**
     * Metodo che restituisce il rateo (rapporto vittorie sconfitte) dell'utente.
     *
     * @return un valore float che rappresenta il rateo dell'utente.
     */
    public float getRateo() {
        float rateo = this.rateo;

        return rateo;
    }

    /**
     * Metodo che aggiorna il rateo di un utente.
     */
    private void setRateo() {
        if (losses == 0 ) {
            rateo = wins;
        }
        else {
            rateo = ((float) wins) / ((float) losses);
        }
    }

    /**
     * Metodo che permette di aggiornare se un utente è in gioco o viceversa.
     *
     * @param mode indica se l'utente è passato a giocare o viceversa.
     */
    public void setGaming(boolean mode) {
        gaming = mode;
    }

    /**
     * Metodo che permette di capire se un giocatore è in partita o meno.
     *
     * @return true se l'utente è in gioco, false altrimenti.
     */
    public boolean isGaming() {
        return gaming;
    }

    /**
     * Metodo per acquisire la lock in lettura sulle informazioni dell'utente.
     */
    public void readLockUser() {
        if (userLock == null) {
            userLock = new ReentrantReadWriteLock(true);
        }
        userLock.readLock().lock();
    }

    /**
     * Metodo per rilasciare la lock in scrittura sulle informazioni del'utente.
     */
    public void readUnlockUser() {
        userLock.readLock().unlock();
    }

    /**
     * Metodo per acquisire la lock in scrittura sulle informazioni dell'utente.
     */
    public void writeLockUser() {
        if (userLock == null) {
            userLock = new ReentrantReadWriteLock(true);
        }
        userLock.writeLock().lock();
    }

    /**
     * Metodo per rilasciare la lock in scrittura sulle informazioni dell'utente.
     */
    public void writeUnlockUser() {
        userLock.writeLock().unlock();
    }
}
