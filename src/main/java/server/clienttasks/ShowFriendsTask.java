package server.clienttasks;

import server.UsersGraph;
import server.iotasks.SendFriendlistTask;

import java.util.Vector;
import java.util.concurrent.ExecutorService;

public class ShowFriendsTask implements Runnable {
    private final String nickname;
    private final UsersGraph usersGraph;
    private final ExecutorService answerersOperator;

    public ShowFriendsTask(String nickname, UsersGraph usersGraph, ExecutorService answerersOperator) {
        this.nickname = nickname;
        this.usersGraph = usersGraph;
        this.answerersOperator = answerersOperator;
    }

    @Override
    public void run() {
        String[] friends = (String[]) usersGraph.getClique(nickname).getFriends().keySet().toArray();

        answerersOperator.execute(new SendFriendlistTask(friends));
    }
}
