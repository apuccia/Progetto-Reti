package server.clienttasks;

import server.Clique;
import server.Player;
import server.UsersGraph;
import server.iotasks.SendRanksTask;

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;

public class ShowRanksTask implements Runnable {
    private final UsersGraph usersGraph;
    private final String nickname;
    private final ExecutorService answerersOperator;

    public ShowRanksTask(String nickname, UsersGraph usersGraph, ExecutorService answerersOperator) {
        this.usersGraph = usersGraph;
        this.nickname = nickname;
        this.answerersOperator = answerersOperator;
    }


    @Override
    public void run() {
        Vector<Player> players = new Vector<>();

        Iterator friendsIterator = usersGraph.getClique(nickname).getFriends().values().iterator();

        while (friendsIterator.hasNext()) {
            Player player = (Player) friendsIterator.next();

            player.readLockUser();
                players.add(new Player(player.getNickname(), player.getUserScore(), player.getWins(), player.getLosses(),
                        player.getRateo()));
            player.readUnlockUser();
        }

        Clique userClique = usersGraph.getClique(nickname);
        Player userPlayer = userClique.getPlayerInfo();
        players.add(new Player(userPlayer.getNickname(), userPlayer.getUserScore(), userPlayer.getWins(),
                userPlayer.getLosses(), userPlayer.getRateo()));

        answerersOperator.execute(new SendRanksTask((Player[]) players.toArray(), userClique.getClientKey()));
    }
}
