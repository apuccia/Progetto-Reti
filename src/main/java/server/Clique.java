package server;

import java.util.TreeMap;

public class Clique {
    private final User user; // oggetto che rappresenta l'utente.
    private TreeMap<String, User> friends; // struttura dati che andrà a contenere le amicizie dell'utente.

    /**
     * Costruisce un nuovo oggetto Clique che rappresenta l'utente e le sue relazioni di amicizia.
     * @param user oggetto che rappresenta l'utente.
     * @param friends struttura dati che contiene le amicizie dell'utente.
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
        assert friend != null;

        friends.put(friend.getNickname(), friend);
    }

    /**
     * Metodo che restituisce un oggetto istanza della classe User contenente le informazioni dell'amico.
     * @param nickname stringa contenente il nome dell'amico.
     * @return un oggetto istanza della classe User che rappresenta l'utente amico.
     */
    public User getFriend(String nickname) {
        assert nickname != null;

        return friends.get(nickname);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Clique clique = (Clique) o;
        return user.equals(clique.user);
    }

    @Override
    public int hashCode() {
        return user.hashCode();
    }
}
