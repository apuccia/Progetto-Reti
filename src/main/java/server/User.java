package server;

import org.mindrot.jbcrypt.BCrypt;

public class User {
    private String userInfoPath; // path del file contenente le informazioni dell'utente.
    private String userFriendlistPath; // path del file contenente le amicizie dell'utente.

    private final String nickname; // nickname dell'utente.
    private final String hash; // risultato dell'hashing applicato alla password dell'utente.
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

        // salvo i path dei file con le informazioni da persistere.
        userInfoPath = clientDirPath + "/UserInfo.json";
        userFriendlistPath = clientDirPath + "/UserFriendlist.json";

        this.nickname = nickname;

        // genero e mantengo un hash a partire dalla password dell'utente.
        hash = BCrypt.hashpw(password, BCrypt.gensalt());

        userScore = 0;
        wins = 0;
        losses = 0;
        rateo = 0;
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
        return userScore;
    }

    /**
     * Metodo che aggiunge il punteggio di una partita al punteggio utente.
     * @param matchScore punteggio della partita.
     */
    private void addMatchScore(long matchScore) {
        assert matchScore > 0;

        userScore += matchScore;
    }

    /**
     * Metodo che restituisce le vittorie totali di un utente.
     * @return un valore long che rappresenta le vittorie dell'utente.
     */
    public long getWins() {
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
        assert this.userInfoPath != null;

        this.userInfoPath = userInfoPath;
    }

    /**
     * Metodo che restituisce il path del file contenente le informazioni dell'utente.
     * @return un oggetto String che rappresenta il path del file contenente le informazioni dell'utente.
     */
    public String getUserInfoPath() {
        return userInfoPath;
    }

    /**
     *  Metodo che permette di inizializzare il path del file contenente le amicizie dell'utente.
     * @param userFriendlistPath path del file contenente le amicizie dell'utente.
     */
    public void setUserFriendlistPath(String userFriendlistPath) {
        assert this.userFriendlistPath != null;

        this.userFriendlistPath = userFriendlistPath;
    }

    /**
     * Metodo che restituisce il path del file contenente le amicizie dell'utente.
     * @return un oggetto String che rappresenta il path del file contenente le amicizie dell'utente.
     */
    public String getUserFriendlistPath() {
        return userFriendlistPath;
    }

    /**
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        User user = (User) o;

        return nickname.equals(user.nickname);
    }

    /**
     * @return
     */
    @Override
    public int hashCode() {
        return nickname.hashCode();
    }
}
