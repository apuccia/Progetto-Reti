package server.clienttasks;

import server.*;
import java.nio.channels.SelectionKey;

public class AddFriendTask implements Runnable {
    private final String userNickname;                  // nickname dell'utente.
    private final String friendNickname;                // nickname dell'utente da aggiungere.
    private final UsersGraph usersGraph;                // struttura dati degli utenti.
    private final WorkersThreadpool workersThreadpool;
    private final SelectionKey clientKey;

    /**
     * Costruisce un nuovo oggetto AddFriendTask che si occupa di svolgere la richiesta di aggiunta di un amico.
     * @param userNickname nickname dell'utente che ha inviato la richiesta.
     * @param friendNickname nickname dell'utente da aggiungere come amico.
     * @param usersGraph struttura dati contenente gli utenti di Word Quizzle.
     * @param workersThreadpool threadpool che si occupa di scrivere le modifiche sui file json.
     */
    public AddFriendTask(String userNickname, String friendNickname, UsersGraph usersGraph,
                         WorkersThreadpool workersThreadpool, SelectionKey clientKey) {
        this.userNickname = userNickname;
        this.friendNickname = friendNickname;
        this.usersGraph = usersGraph;
        this.workersThreadpool = workersThreadpool;
        this.clientKey = clientKey;
    }

    @Override
    public void run() {
        Clique userClique = usersGraph.getClique(userNickname);
        Clique friendClique = usersGraph.getClique(friendNickname);

        assert userClique != null : "Utente di partenza non esiste";

        // controllo che l'utente da aggiungere sia registrato a Word Quizzle.
        if (friendClique == null) {
            // spedisco messaggio di risposta negativa.
            workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.USER_NOT_EXISTS_ADD.toString(),
                    clientKey);
            return;
        }

        // inserisco l'amico.
        if (friendClique.insertFriend(userClique.getPlayerInfo()) != null) {
            // l'utente era già stato inserito tra gli amici.

            // spedisco messaggio di risposta negativa.
            workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.FRIEND_ALREADY_ADDED.toString(),
                    clientKey);
            return;
        }
        userClique.insertFriend(friendClique.getPlayerInfo());

        // task per l'aggiornamento della lista amici dell'utente e dell'amico appena aggiunto.
        workersThreadpool.executeWriteUserFriendlistTask(userNickname,
                userClique.getFriends().keySet().toArray(new String[userClique.getFriends().size()]),
                userClique.getUserFriendlistPath());
        workersThreadpool.executeWriteUserFriendlistTask(friendNickname,
                friendClique.getFriends().keySet().toArray(new String[friendClique.getFriends().size()]),
                friendClique.getUserFriendlistPath());

        workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.FRIEND_ADDED.toString(), clientKey);
    }
}
