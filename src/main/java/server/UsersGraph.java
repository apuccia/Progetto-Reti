package server;

import java.util.concurrent.ConcurrentHashMap;

public class UsersGraph {
    private final ConcurrentHashMap<String, Clique> users; // struttura dati che contiene tutti gli utenti di Word Quizzle.
    public final static String MAIN_PATH = "Progetto-Reti/clients" ;

    public UsersGraph() {
        users = new ConcurrentHashMap<String, Clique>();
    }

    public boolean existsUser(String nickname) {
        return users.contains(nickname);
    }

    public void insertClique(String nickname, Clique clique) {
        users.put(nickname, clique);
    }

    public Clique getClique(String nickname) {
        return users.get(nickname);
    }
}
