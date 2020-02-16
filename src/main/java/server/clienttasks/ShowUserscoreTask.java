package server.clienttasks;

import server.Player;
import server.UsersGraph;
import server.WorkersThreadpool;

import java.nio.channels.SelectionKey;

public class ShowUserscoreTask implements Runnable {
    private final String nickname;
    private final UsersGraph usersGraph;
    private final WorkersThreadpool workersThreadpool;
    private final SelectionKey clientKey;


    /**
     * Costruisce un nuovo oggetto ShowUserScoreTask che si occupa di "clonare" le informazioni dell'utente.
     *
     * @param nickname nickname dell'utente da cui prendere le statistiche.
     * @param usersGraph struttura dati che contiene gli utenti di Word Quizzle.
     * @param workersThreadpool oggetto che incapsula i principali worker di Word Quizzle.
     * @param clientKey SelectionKey del client a cui inviare la risposta.
     */
    public ShowUserscoreTask(String nickname, UsersGraph usersGraph, WorkersThreadpool workersThreadpool,
                             SelectionKey clientKey) {
        this.nickname = nickname;
        this.usersGraph = usersGraph;
        this.workersThreadpool = workersThreadpool;
        this.clientKey = clientKey;
    }

    @Override
    public void run() {
        Player player = usersGraph.getClique(nickname).getPlayerInfo();

        player.readLockUser();
            workersThreadpool.executeSendUserscoreTask(new Player(player.getNickname(), player.getUserScore(),
                player.getWins(), player.getLosses(), player.getRateo()), clientKey);
        player.readUnlockUser();
    }
}
