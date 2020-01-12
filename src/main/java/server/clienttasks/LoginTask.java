package server.clienttasks;

import org.mindrot.jbcrypt.BCrypt;
import server.*;
import server.iotasks.ReadFriendlistTask;
import server.iotasks.ReadUserInfoTask;
import server.iotasks.SendSimpleResponse;

import java.io.*;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class LoginTask implements Runnable{
    private final String nickname;
    private final String password;
    private final SelectionKey key;
    private final UsersGraph usersGraph;
    private final ExecutorService diskOperator;
    private final ExecutorService answerersOperator;

    public LoginTask(String nickname, String password, UsersGraph usersGraph, ExecutorService diskOperator,
                     ExecutorService answerersOperator, SelectionKey key) {
        this.nickname = nickname;
        this.password = password;
        this.key = key;
        this.usersGraph = usersGraph;
        this.diskOperator = diskOperator;
        this.answerersOperator = answerersOperator;
    }

    @Override
    public void run() {
        Clique userClique = usersGraph.getClique(nickname);

        if (userClique == null) {
            answerersOperator.execute(new SendSimpleResponse(ResponseMessages.USER_NOT_EXISTS.toString(), key));
        }

        if (!BCrypt.checkpw(password, userClique.getHash())) {
            answerersOperator.execute(new SendSimpleResponse(ResponseMessages.WRONG_PASSWORD.toString(), key));
        }
        else {
            if (userClique.getPlayerInfo().setOnline(true)) {
                userClique.setClientKey(key);

                answerersOperator.execute(new SendSimpleResponse(ResponseMessages.USER_LOGGED.toString(), key));
            } else {
                answerersOperator.execute(new SendSimpleResponse(ResponseMessages.USER_ALREADY_LOGGED.toString(), key));
            }
        }
    }
}
