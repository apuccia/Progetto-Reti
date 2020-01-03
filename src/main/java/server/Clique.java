package server;

import java.util.Set;
import java.util.TreeMap;

public class Clique {
    private final User user; // oggetto che rappresenta l'utente.
    private TreeMap<String, User> friends; // struttura dati che andrà a contenere le amicizie dell'utente.

    /**
     * Costruisce un nuovo oggetto Clique che rappresenta l'utente e le sue relazioni di amicizia.
     * @param user oggetto che rappresenta l'utente.
     */
    public Clique(User user) {
        this.user = user;
        this.friends = null;
    }

    /**
     * Costruisce un nuovo oggetto Clique che rappresenta l'utente e le sue relazioni di amicizia.
     * @param user oggetto che rappresenta l'utente.
     * @param friends oggetto che contiene le amicizie dell'utente.
     */
    public Clique(User user, TreeMap<String, User> friends) {
        this.user = user;
        this.friends = friends;
    }

    /**
     * Metodo che restituisce l'oggetto che rappresenta l'utente.
     * @return un oggetto istanza della classe User.
     */
    public User getMainUser() {
        return user;
    }

    /**
     * Metodo che aggiunge un utente nella cerchia di amicizie.
     * @param friend oggetto istanza della classe User che rappresenta l'utente da aggiungere nelle amicizie.
     */
    public void insertFriend(User friend) {
        assert friend != null : "istanza nulla";
        assert friend.getNickname() != null : "nickname dell'amico nullo";

        if (friends == null) {
            friends = new TreeMap<String, User>();
        }

        friends.put(friend.getNickname(), friend);
    }

    /**
     * Metodo che restituisce un oggetto istanza della classe User contenente le informazioni dell'amico.
     * @param friendNickname stringa contenente il nome dell'amico.
     * @return un oggetto istanza della classe User che rappresenta l'utente amico.
     */
    public User getFriend(String friendNickname) {
        assert friendNickname != null : "nickname dell'amico nullo";

        return friends.get(friendNickname);
    }

    /**
     * Metodo che restituisce gli amici dell'utente.
     * @return TreeMap degli utenti amici.
     */
    public TreeMap<String, User> getFriends() {
        return friends;
    }
}
