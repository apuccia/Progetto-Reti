package server.clienttasks;

import org.mindrot.jbcrypt.BCrypt;
import server.*;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class LoginTask implements Callable {
    private final String nickname;                      // nickname dell'utente.
    private final String password;                      // password in chiaro dell'utente.
    private final SelectionKey key;                     // selection key associata al client.
    private final UsersGraph usersGraph;                // struttura dati degli utenti.

    private final WorkersThreadpool workersThreadpool;
    private final ConcurrentHashMap<String, InetSocketAddress> onlineUsers;

    /**
     * Costruisce un nuovo oggetto LoginTask che si occupa di effettuare tutte le operazioni relative alla fase di login
     * di un utente.
     *
     * @param nickname nickname dell'utente.
     * @param password password in chiaro dell'utente.
     * @param usersGraph riferimento alla struttura dati contenente le informazioni degli utenti.
     * @param workersThreadpool riferimento all'oggetto che incapsula i principali worker di Word Quizzle.
     * @param key SelectionKey che identifica il client.
     */
    public LoginTask(String nickname, String password, UsersGraph usersGraph, WorkersThreadpool workersThreadpool,
                     ConcurrentHashMap<String, InetSocketAddress> onlineUsers, SelectionKey key) {
        this.nickname = nickname;
        this.password = password;
        this.key = key;
        this.usersGraph = usersGraph;
        this.workersThreadpool = workersThreadpool;
        this.onlineUsers = onlineUsers;
    }

    @Override
    public String call() {
        Clique userClique = usersGraph.getClique(nickname);

        // verifico che l'utente sia registrato.
        if (userClique == null) {
            // nuovo task per spedire la risposta negativa.
            workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.USER_NOT_EXISTS.toString(), key);
            return null;
        }

        assert userClique != null : "Utente nullo";

        // controllo della password.
        if (!BCrypt.checkpw(password, userClique.getHash())) {
            // password sbagliata.

            // nuovo task per spedire la risposta negativa.
            workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.WRONG_PASSWORD.toString(), key);
        }
        else {
            // password corretta.

            SocketChannel clientChannel = (SocketChannel) key.channel();
            InetSocketAddress clientAddress = (InetSocketAddress) clientChannel.socket().getRemoteSocketAddress();

            System.out.println("--- " + clientAddress.toString());
            if (onlineUsers.putIfAbsent(nickname, clientAddress) == null) {
                // l'utente è online, salvo il suo indirizzo.

                // nuovo task per spedire la risposta di avvenuto login.
                workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.USER_LOGGED.toString(), key);
            } else {
                // un altro utente ha già effettuato il login.

                // nuovo task per spedire la risposta negativa.
                workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.USER_ALREADY_LOGGED.toString(), key);

                return null;
            }
        }

        return nickname;
    }
}
