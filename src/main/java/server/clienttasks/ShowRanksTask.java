package server.clienttasks;

import server.Clique;
import server.Player;
import server.UsersGraph;
import server.WorkersThreadpool;

import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Iterator;

public class ShowRanksTask implements Runnable {
    private final UsersGraph usersGraph;
    private final String nickname;
    private final WorkersThreadpool workersThreadpool;
    private final SelectionKey clientKey;

    /**
     * Costruisce un nuovo oggetto ShowRanksTask che si occupa di "clonare" la classifica dell'utente e dei suoi amici.
     *
     * @param nickname nickname dell'utente.
     * @param usersGraph struttura dati che contiene gli utenti di Word Quizzle.
     * @param workersThreadpool oggetto che incapsula i principali worker di Word Quizzle.
     * @param clientKey SelectionKey del client a cui inviare la risposta.
     */
    public ShowRanksTask(String nickname, UsersGraph usersGraph, WorkersThreadpool workersThreadpool,
                         SelectionKey clientKey) {
        this.usersGraph = usersGraph;
        this.nickname = nickname;
        this.workersThreadpool = workersThreadpool;
        this.clientKey = clientKey;
    }

    @Override
    public void run() {
        ArrayList<Player> players = new ArrayList<>();
        Clique userClique = usersGraph.getClique(nickname);

        Iterator friendsIterator = userClique.getFriends().values().iterator();

        while (friendsIterator.hasNext()) {
            Player player = (Player) friendsIterator.next();

            player.readLockUser();
                players.add(new Player(player.getNickname(), player.getUserScore(), player.getWins(), player.getLosses(),
                        player.getRateo()));
            player.readUnlockUser();
        }

        Player userPlayer = userClique.getPlayerInfo();
        players.add(new Player(userPlayer.getNickname(), userPlayer.getUserScore(), userPlayer.getWins(),
                userPlayer.getLosses(), userPlayer.getRateo()));

        workersThreadpool.executeSendRanksTask(players.toArray(new Player[players.size()]), clientKey);
    }
}
