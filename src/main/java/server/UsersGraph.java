package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.ConcurrentHashMap;

public class UsersGraph {
    private final ConcurrentHashMap<String, Clique> users; // struttura dati che contiene tutti gli utenti di Word Quizzle.
    private final Gson gson; // istanza della classe Gson che sarà utilizzata per serializzare/deserializzare le informazioni degli utenti.
    public final static String MAIN_PATH = "Progetto-Reti/clients"; // path della cartella contenente le informazioni dei client.

    /**
     * Costruisce un nuovo oggetto UsersGraph che andrà a contenere tutte le informazioni degli utenti di Word Quizzle.
     */
    public UsersGraph() {
        users = new ConcurrentHashMap<String, Clique>();

        // imposto le informazioni per la creazione dell'istanza di Gson.
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.addSerializationExclusionStrategy(new UserExclusionStrategy());

        // creo l'istanza Gson.
        gson = gsonBuilder.create();
    }

    /**
     * Metodo che inserisce l'utente e la sua cerchia di amicizie nella struttura dati.
     * @param nickname nickname dell'utente
     * @param clique struttura dati contenente informazioni dell'utente e le sue amicizie.
     */
    public void insertClique(String nickname, Clique clique) {
        assert nickname != null : "nickname nullo";
        assert clique != null : "clique nulla";
        assert clique.getMainUser() != null : "utente principale nullo";

        users.put(nickname, clique);
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

    /**
     * Metodo che restituisce l'istanza della classe Gson per permettere la serializzazione/deserializzazione.
     * @return l'istanza della classe Gson.
     */
    public Gson getGson() {
        return gson;
    }
}
