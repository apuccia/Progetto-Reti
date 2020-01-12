package server;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Player {
    private final String nickname; // nickname dell'utente.
    private long userScore; // punteggio utente.
    private long wins; // vittorie totali dell'utente.
    private long losses; // sconfitte totali dell'utente.
    private float rateo; // rapporto vittorie - sconfitte dell'utente.
    private AtomicBoolean online;
    private AtomicBoolean gaming;

    private final ReentrantReadWriteLock userLock; // lock per la sincronizzazione delle informazioni dell'utente.

    public Player(String nickname) {
        this.nickname = nickname;
        userScore = 0;
        wins = 0;
        losses = 0;
        rateo = 0;

        online = new AtomicBoolean();
        gaming = new AtomicBoolean();

        userLock = new ReentrantReadWriteLock(true);
    }

    public Player(String nickname, long userScore, long wins, long losses, float rateo) {
        this.nickname = nickname;
        this.userScore = userScore;
        this.wins = wins;
        this.losses = losses;
        this.rateo = rateo;

        online = null;
        gaming = null;

        userLock = null;
    }

    /**
     * Metodo che restituisce il nickname dell'utente.
     * @return il nickname dell'utente.
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Metodo che restituisce il punteggio utente.
     * @return il punteggio utente.
     */
    public long getUserScore() {
        long userScore = this.userScore;

        return userScore;
    }

    /**
     * Metodo che aggiunge il punteggio di una partita al punteggio utente.
     * @param matchScore punteggio della partita.
     */
    private void addMatchScore(long matchScore) {
        assert matchScore > 0 : "matchscore non valido";

        userScore += matchScore;
    }

    /**
     * Metodo che restituisce le vittorie totali di un utente.
     * @return un valore long che rappresenta le vittorie dell'utente.
     */
    public long getWins() {
        long wins = this.wins;

        return wins;
    }

    /**
     * Metodo che aggiunge una vittoria al totale vittorie dell'utente, aggiornando il punteggio utente e il rateo.
     * @param matchScore punteggio della partita.
     */
    public void addWin(long matchScore) {
        addMatchScore(matchScore);
        wins++;
        setRateo();
    }

    /**
     * Metodo che restituisce le sconfitte totali di un utente.
     * @return un valore long che rappresenta le sconfitte dell'utente.
     */
    public long getLosses() {
        long losses = this.losses;

        return losses;
    }

    /**
     * Metodo che aggiunge una sconfitta al totale sconfitte dell'utente, aggiornando il punteggio utente e il rateo.
     * @param matchScore punteggio della partita.
     */
    public void addLoss(long matchScore) {
        addMatchScore(matchScore);
        losses++;
        setRateo();
    }

    /**
     * Metodo che restituisce il rateo (rapporto vittorie sconfitte) dell'utente.
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
        rateo = wins / losses;
    }

    /**
     * Metodo che permette di aggiornare la presenza di un utente.
     * @param mode indica se l'utente è passato online o viceversa.
     */
    public boolean setOnline(boolean mode) {
        return online.compareAndSet(!mode, mode);
    }

    /**
     * Metodo che verifica se l'utente è online.
     * @return true se l'utente è online, false altrimenti
     */
    public boolean isOnline() {
        return online.get();
    }

    public boolean setGaming(boolean mode) {
        return online.compareAndSet(!mode, mode);
    }

    public boolean isGaming() {
        return gaming.get();
    }

    public void readLockUser() {
        userLock.readLock().lock();
    }

    public void readUnlockUser() {
        userLock.readLock().unlock();
    }

    public void writeLockUser() {
        userLock.writeLock().lock();
    }

    public void writeUnlockUser() {
        userLock.writeLock().unlock();
    }
}
