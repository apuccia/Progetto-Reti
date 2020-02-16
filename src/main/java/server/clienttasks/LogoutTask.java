package server.clienttasks;

import server.Clique;
import server.ResponseMessages;
import server.UsersGraph;
import server.WorkersThreadpool;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LogoutTask implements Runnable {
    private final String nickname;                        // nickname dell'utente.
    private final UsersGraph usersGraph;                  // struttura dati degli utenti.
    private final WorkersThreadpool workersThreadpool;    // threadpool che si occupa di inviare le risposte.
    private final ConcurrentHashMap<String, InetSocketAddress> onlineUsers;
    private final SelectionKey clientKey;

    /**
     * Costruisce un nuovo oggetto LogoutTask che si occupa di svolgere le operazioni relative al logout di un utente.
     *
     * @param nickname nickname dell'utente che vuole effettuare il logout.
     * @param usersGraph struttura dati contenente gli utenti di Word Quizzle.
     * @param workersThreadpool threadpool che si occupa di inviare le risposte ai client.
     */
    public LogoutTask(String nickname, UsersGraph usersGraph, WorkersThreadpool workersThreadpool,
                      ConcurrentHashMap<String, InetSocketAddress> onlineUsers, SelectionKey clientKey) {
        this.nickname = nickname;
        this.usersGraph = usersGraph;
        this.onlineUsers = onlineUsers;
        this.workersThreadpool = workersThreadpool;
        this.clientKey = clientKey;
    }

    @Override
    public void run() {
        onlineUsers.remove(nickname);

        // task per spedire risposta di logout con successo.
        workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.USER_LOGOUTED.toString(), clientKey);
    }
}
