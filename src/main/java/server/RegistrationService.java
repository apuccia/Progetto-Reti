package server;

import java.io.*;
import java.rmi.server.RemoteServer;
import java.util.TreeMap;

public class RegistrationService extends RemoteServer implements IRegistrationService {
    private final UsersGraph usersGraph; // struttura dati contenente le informazioni degli utenti di Word Quizzle + informazioni di supporto.

    /**
     * Costruisce un nuovo oggetto RegistrationService per fornire via RMI il servizio di registrazione a Word Quizzle.
     * @param usersGraph Struttura dati di Word Quizzle.
     */
    public RegistrationService(UsersGraph usersGraph) {
        this.usersGraph = usersGraph;
    }

    /**
     * Metodo che permette ad un client di registrarsi a Word Quizzle.
     * @param nickname nickname dell'utente che si vuole registrare a Word Quizzle.
     * @param password password dell'utente che si vuole registrare a Word Quizzle.
     * @return se non ci sono stati errori un messaggio di avvenuta registrazione, altrimenti un messaggio di errore.
     */
    public String registra_utente(String nickname, String password) {
        assert nickname != null : "nickname nullo";
        assert password != null : "password nulla";

        String clientPath = usersGraph.MAIN_PATH + "/client_" + nickname;
        File userDirectory = new File(clientPath);

        // verifico che l'utente non si sia già registrato precedentemente a Word Quizzle.
        if (!userDirectory.mkdir()) {
            // restituisco messaggio di errore specifico.
            return ResponseMessages.USER_ALREADY_REGISTERED.toString();
        }

        // creo l'utente.
        User user = new User(nickname, password, clientPath);
        // creo la cerchia di amici dell'utente (al momento della registrazione 0 amici).
        Clique clique = new Clique(user);

        // serializzo le informazioni dell'utente su un suo file dedicato.
        try (FileOutputStream fileOutput = new FileOutputStream(user.getUserInfoPath());
             BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutput);
             OutputStreamWriter streamWriter = new OutputStreamWriter(bufferedOutput)
            ) {
            usersGraph.getGson().toJson(user, streamWriter);

            // creo il file che andrà a contenere le informazioni delle amicizie dell'utente (inizialmente vuoto).
            File friendlistFile = new File(user.getUserFriendlistPath());
            friendlistFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();

            // restituisco messaggio di errore generale per l'operazione di registrazione.
            return ResponseMessages.REGISTRATION_ERROR.toString();
        }

        // inserisco la cerchia di amicizie.
        usersGraph.insertClique(nickname, clique);

        assert usersGraph.getClique(nickname) != null;

        // restituisco messaggio positivo di avvenuta registrazione.
        return ResponseMessages.USER_REGISTERED.toString();
    }
}
