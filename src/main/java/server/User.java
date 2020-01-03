package server;

import org.mindrot.jbcrypt.BCrypt;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class User {
    private String userInfoPath; // path del file contenente le informazioni dell'utente.
    private String userFriendlistPath; // path del file contenente le amicizie dell'utente.

    private ReentrantReadWriteLock userInfoLock; // lock per la sincronizzazione delle informazioni dell'utente.
    private ReentrantLock loginLock; // lock per garantire il login ad un solo client alla volta per quel nickname.

    private final String nickname; // nickname dell'utente.
    private final String hash; // risultato dell'hashing applicato alla password dell'utente.
    private boolean online;
    private long userScore; // punteggio utente.
    private long wins; // vittorie totali dell'utente.
    private long losses; // sconfitte totali dell'utente.
    private float rateo; // rapporto vittorie - sconfitte dell'utente.

    /**
     * Costruisce un nuovo oggetto User che andrà a contenere tutte le sue informazioni.
     * @param nickname nickname dell'utente.
     * @param password password dell'utente.
     * @param clientDirPath path della directory personale dell'utente.
     */
    public User(String nickname, String password, String clientDirPath) {
        assert nickname != null : "nickname nullo";
        assert password != null : "password nulla";
        assert clientDirPath != null : "path del client nullo";

        // salvo i path dei file con le informazioni da persistere.
        userInfoPath = clientDirPath + "/UserInfo.json";
        userFriendlistPath = clientDirPath + "/UserFriendlist.json";

        // inizializzo la lock per sincronizzare le informazioni dell'utente.
        userInfoLock = new ReentrantReadWriteLock(true);
        loginLock = new ReentrantLock();

        this.nickname = nickname;
        // genero e mantengo un hash a partire dalla password dell'utente.
        hash = BCrypt.hashpw(password, BCrypt.gensalt());
        online = false;
        userScore = 0;
        wins = 0;
        losses = 0;
        rateo = 0;
    }

    public void setOnline(boolean mode) {
        userInfoLock.writeLock().lock();
            online = mode;
        userInfoLock.writeLock().unlock();
    }

    public boolean isOnline() {
        userInfoLock.readLock().lock();
            boolean online = this.online;
        userInfoLock.readLock().unlock();

        return online;
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
        userInfoLock.readLock().lock();
            long userScore = this.userScore;
        userInfoLock.readLock().unlock();

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
        userInfoLock.readLock().lock();
            long wins = this.wins;
        userInfoLock.readLock().unlock();

        return wins;
    }

    /**
     * Metodo che aggiunge una vittoria al totale vittorie dell'utente, aggiornando il punteggio utente e il rateo.
     * @param matchScore punteggio della partita.
     */
    public void addWin(long matchScore) {
        userInfoLock.writeLock().lock();
            addMatchScore(matchScore);
            wins++;
            setRateo();
        userInfoLock.writeLock().unlock();
    }

    /**
     * Metodo che restituisce le sconfitte totali di un utente.
     * @return un valore long che rappresenta le sconfitte dell'utente.
     */
    public long getLosses() {
        userInfoLock.readLock().lock();
            long losses = this.losses;
        userInfoLock.readLock().unlock();

        return losses;
    }

    /**
     * Metodo che aggiunge una sconfitta al totale sconfitte dell'utente, aggiornando il punteggio utente e il rateo.
     * @param matchScore punteggio della partita.
     */
    public void addLoss(long matchScore) {
        userInfoLock.writeLock().lock();
            addMatchScore(matchScore);
            losses++;
            setRateo();
        userInfoLock.writeLock().unlock();
    }

    /**
     * Metodo che restituisce il rateo (rapporto vittorie sconfitte) dell'utente.
     * @return un valore float che rappresenta il rateo dell'utente.
     */
    public float getRateo() {
        userInfoLock.readLock().lock();
            float rateo = this.rateo;
        userInfoLock.readLock().unlock();

        return rateo;
    }

    /**
     * Metodo che aggiorna il rateo di un utente.
     */
    private void setRateo() {
        rateo = wins / losses;
    }

    /**
     * Metodo che permette di inizializzare il path del file contenente le informazioni dell'utente.
     * @param userInfoPath path del file contenente le informazioni dell'utente.
     */
    public void setUserInfoPath(String userInfoPath) {
        assert this.userInfoPath == null : "path del file contenente le info utente già inizializzato";

        this.userInfoPath = userInfoPath;
    }

    /**
     * Metodo che restituisce il path del file contenente le informazioni dell'utente.
     * @return un oggetto String che rappresenta il path del file contenente le informazioni dell'utente.
     */
    public String getUserInfoPath() {
        assert userInfoPath != null : "userInfoPath nullo";

        return userInfoPath;
    }

    /**
     *  Metodo che permette di inizializzare il path del file contenente le amicizie dell'utente.
     * @param userFriendlistPath path del file contenente le amicizie dell'utente.
     */
    public void setUserFriendlistPath(String userFriendlistPath) {
        assert this.userFriendlistPath == null : "path del file contenente le amicizie utente già inizializzato";

        this.userFriendlistPath = userFriendlistPath;
    }

    /**
     * Metodo che restituisce il path del file contenente le amicizie dell'utente.
     * @return un oggetto String che rappresenta il path del file contenente le amicizie dell'utente.
     */
    public String getUserFriendlistPath() {
        return userFriendlistPath;
    }

    public void lockLogin() {
        loginLock.lock();
    }

    public void unlockLogin() {
        loginLock.unlock();
    }
}
