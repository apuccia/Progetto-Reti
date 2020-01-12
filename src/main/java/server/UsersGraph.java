package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.ConcurrentHashMap;

public class UsersGraph {
    private final ConcurrentHashMap<String, Clique> users; // struttura dati che contiene tutti gli utenti di Word Quizzle.

    // imposto le informazioni per la creazione dell'istanza di Gson.
    public static final Gson GSON = new GsonBuilder()
                                        .setPrettyPrinting()
                                        .addSerializationExclusionStrategy(new UserExclusionStrategy())
                                        .create();
    public static final String MAIN_PATH = "Progetto-Reti/clients"; // path della cartella contenente le informazioni dei client.

    /**
     * Costruisce un nuovo oggetto UsersGraph che andrà a contenere tutte le informazioni degli utenti di Word Quizzle.
     */
    public UsersGraph() {
        users = new ConcurrentHashMap<String, Clique>();
    }

    /**
     * Metodo che inserisce l'utente e la sua cerchia di amicizie nella struttura dati.
     * @param nickname nickname dell'utente
     * @param clique struttura dati contenente informazioni dell'utente e le sue amicizie.
     */
    public Clique insertClique(String nickname, Clique clique) {
        assert nickname != null : "nickname nullo";
        assert clique != null : "clique nulla";

        return users.putIfAbsent(nickname, clique);
    }

    /**
     * Metodo che restituisce le informazioni dell'utente e le sue amicizie.
     * @param nickname nickname dell'utente.
     * @return oggetto istanza della classe Clique contenente le informazioni dell'utente e delle sue amicizie.
     */
    public Clique getClique(String nickname) {
        assert nickname != null : "nickname nullo";

        return users.get(nickname);
    }
}
