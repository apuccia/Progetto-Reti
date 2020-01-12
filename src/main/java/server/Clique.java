package server;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.channels.SelectionKey;
import java.util.concurrent.ConcurrentHashMap;

public class Clique {
    transient private String userInfoPath; // path del file contenente le informazioni dell'utente.
    transient private String userFriendlistPath; // path del file contenente le amicizie dell'utente.
    transient private SelectionKey clientKey;

    private final String hash; // risultato dell'hashing applicato alla password dell'utente.

    private final Player playerInfo; // contiene le statistiche relative alle partite.
    transient private ConcurrentHashMap<String, Player> friends; // struttura dati che andrà a contenere le amicizie dell'utente.

    /**
     * Costruisce un nuovo oggetto Clique che rappresenta l'utente e le sue relazioni di amicizia.
     * @param nickname oggetto che rappresenta l'utente.
     */
    public Clique(String nickname, String password, String clientDirPath) {
        assert nickname != null : "nickname nullo";
        assert password != null : "password nulla";
        assert clientDirPath != null : "path del client nullo";

        // salvo i path dei file con le informazioni da persistere.
        userInfoPath = clientDirPath + "/UserInfo.json";
        userFriendlistPath = clientDirPath + "/UserFriendlist.json";

        // genero e mantengo un hash a partire dalla password dell'utente.
        hash = BCrypt.hashpw(password, BCrypt.gensalt());

        friends = new ConcurrentHashMap<>();
        playerInfo = new Player(nickname);
    }

    /**
     * Costruisce un nuovo oggetto Clique che rappresenta l'utente e le sue relazioni di amicizia.
     * @param nickname oggetto che rappresenta l'utente.
     */
    public Clique(String nickname, String hash, long userScore, long wins, long losses, float rateo) {
        assert nickname != null : "nickname nullo";

        this.hash = hash;
        playerInfo = new Player(nickname, userScore, wins, losses, rateo);

        friends = null;
    }

    /**
     * Metodo che restituisce l'oggetto che rappresenta l'utente.
     * @return un oggetto istanza della classe Player.
     */
    public Player getPlayerInfo() {
        return playerInfo;
    }

    public String getHash() {
        return hash;
    }

    /**
     * Metodo che aggiunge un utente nella cerchia di amicizie.
     * @param friend oggetto istanza della classe User che rappresenta l'utente da aggiungere nelle amicizie.
     */
    public Player insertFriend(Player friend) {
        assert friend != null : "istanza nulla";
        assert friend.getNickname() != null : "nickname dell'amico nullo";

        if (friends == null) {
            friends = new ConcurrentHashMap<>();
        }

        return friends.putIfAbsent(friend.getNickname(), friend);
    }

    /**
     * Metodo che restituisce un oggetto istanza della classe User contenente le informazioni dell'amico.
     * @param friendNickname stringa contenente il nome dell'amico.
     * @return un oggetto istanza della classe User che rappresenta l'utente amico.
     */
    public Player getFriend(String friendNickname) {
        assert friendNickname != null : "nickname dell'amico nullo";

        return friends.get(friendNickname);
    }

    /**
     * Metodo che restituisce gli amici dell'utente.
     * @return TreeMap degli utenti amici.
     */
    public ConcurrentHashMap<String, Player> getFriends() {
        return friends;
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
        assert userFriendlistPath != null : "path del file contenente le amicizie dell'utente nullo";

        return userFriendlistPath;
    }

    /**
     * Metodo che permette di associare la SelectionKey ad un utente.
     * @param clientKey la SelectionKey da associare all'utente.
     */
    public void setClientKey(SelectionKey clientKey) {
        this.clientKey = clientKey;
    }

    /**
     * Metodo che ritorna la SelectionKey associata all'utente.
     * @return la SelectionKey associata all'utente.
     */
    public SelectionKey getClientKey() {
        return clientKey;
    }
}
