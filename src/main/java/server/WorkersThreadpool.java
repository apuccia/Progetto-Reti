package server;

import server.clienttasks.*;

import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class WorkersThreadpool {
    private final ExecutorService diskOperator;
    private final ExecutorService workersOperator;
    private final ExecutorService answerersOperator;
    private final UsersGraph usersGraph;
    private final ArrayList<String> italianWords;
    private final ConcurrentHashMap<String, String> translatedWords;

    public WorkersThreadpool(UsersGraph usersGraph, ExecutorService workersOperator, ExecutorService diskOperator,
                             ExecutorService answerersOperator, ArrayList<String> italianWords) {
        // si occupa di interagire con il disco per la scrittura e lettura dei file json
        this.diskOperator = diskOperator;

        // threadpool che si occuperà di svolgere le richieste.
        this.workersOperator = workersOperator;

        // threadpool che si occuperà di rispondere ai client.
        this.answerersOperator = answerersOperator;

        this.usersGraph = usersGraph;

        this.italianWords = italianWords;

        translatedWords = new ConcurrentHashMap<>();
    }

    public void executeLoginTask(String nickname, String password, SelectionKey clientKey) {
        workersOperator.execute(new LoginTask(nickname, password, usersGraph, diskOperator, answerersOperator, clientKey));
    }

    public void executeLogoutTask(String nickname) {
        workersOperator.execute(new LogoutTask(nickname, usersGraph, answerersOperator));
    }

    public void executeAddFriendTask(String nickname, String friendNickname) {
        workersOperator.execute(new AddFriendTask(nickname, friendNickname, usersGraph, diskOperator, answerersOperator));
    }

    public void executeShowFriendsTask(String nickname) {
        workersOperator.execute(new ShowFriendsTask(nickname, usersGraph, answerersOperator));
    }

    public void executeShowRanksTask(String nickname){
        workersOperator.execute(new ShowRanksTask(nickname, usersGraph, answerersOperator));
    }

    public void executeChallenge(String userNickname, String friendNickname) {
        Thread thread = new Thread(new ChallengeTask(userNickname, friendNickname, usersGraph, diskOperator, answerersOperator,
                italianWords, translatedWords));
        thread.start();
    }
}
