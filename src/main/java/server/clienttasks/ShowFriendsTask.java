package server.clienttasks;

import server.Clique;
import server.Player;
import server.UsersGraph;
import server.WorkersThreadpool;
import server.iotasks.SendFriendlistTask;

import java.nio.channels.SelectionKey;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class ShowFriendsTask implements Runnable {
    private final String nickname;
    private final UsersGraph usersGraph;
    private final WorkersThreadpool workersThreadpool;
    private final SelectionKey clientKey;

    /**
     * Costruisce un nuovo oggetto ShowFriendsTask che si occuperà di "clonare" la lista amici di un utente.
     *
     * @param nickname nickname dell'utente.
     * @param usersGraph struttura dati che contiene gli utenti di Word Quizzle.
     * @param workersThreadpool oggetto che incapsula i principali worker di Word Quizzle.
     * @param clientKey SelectionKey del client a cui inviare la risposta.
     */
    public ShowFriendsTask(String nickname, UsersGraph usersGraph, WorkersThreadpool workersThreadpool,
                           SelectionKey clientKey) {
        this.nickname = nickname;
        this.usersGraph = usersGraph;
        this.workersThreadpool = workersThreadpool;
        this.clientKey = clientKey;
    }

    @Override
    public void run() {
        ConcurrentHashMap<String, Player> friends = usersGraph.getClique(nickname).getFriends();

        String[] friendsNicknames = friends.keySet().toArray(new String[friends.size()]);
        workersThreadpool.executeSendFriendlistTask(friendsNicknames, clientKey);
    }
}
