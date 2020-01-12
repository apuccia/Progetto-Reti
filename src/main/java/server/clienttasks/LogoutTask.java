package server.clienttasks;

import server.Clique;
import server.ResponseMessages;
import server.UsersGraph;
import server.iotasks.SendSimpleResponse;

import java.util.concurrent.ExecutorService;

public class LogoutTask implements Runnable {
    private final String nickname;
    private final UsersGraph usersGraph;
    private final ExecutorService answerersOperator;

    public LogoutTask(String nickname, UsersGraph usersGraph, ExecutorService answerersOperator) {
        this.nickname = nickname;
        this.usersGraph = usersGraph;
        this.answerersOperator = answerersOperator;
    }

    @Override
    public void run() {
        Clique user = usersGraph.getClique(nickname);

        answerersOperator.execute(new SendSimpleResponse(ResponseMessages.USER_LOGOUTED.toString(), user.getClientKey()));

        user.getPlayerInfo().setOnline(false);
        user.setClientKey(null);
    }
}
