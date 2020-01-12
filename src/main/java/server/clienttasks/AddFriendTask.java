package server.clienttasks;

import server.*;
import server.iotasks.SendSimpleResponse;
import server.iotasks.WriteFriendlistTask;

import java.util.concurrent.ExecutorService;

public class AddFriendTask implements Runnable {
    private final String userNickname;
    private final String friendNickname;
    private final UsersGraph usersGraph;
    private final ExecutorService diskOperator;
    private final ExecutorService answerersOperator;

    public AddFriendTask(String userNickname, String friendNickname, UsersGraph usersGraph, ExecutorService diskOperator,
                         ExecutorService answerersOperator) {
        this.userNickname = userNickname;
        this.friendNickname = friendNickname;
        this.usersGraph = usersGraph;
        this.diskOperator = diskOperator;
        this.answerersOperator = answerersOperator;
    }

    @Override
    public void run() {
        Clique userClique = usersGraph.getClique(userNickname);
        Clique friendClique = usersGraph.getClique(friendNickname);

        if (friendClique == null) {
            answerersOperator.execute(new SendSimpleResponse(ResponseMessages.USER_NOT_EXISTS.toString(),
                    userClique.getClientKey()));
        }

        if (friendClique.insertFriend(userClique.getPlayerInfo()) != null) {
            answerersOperator.execute(new SendSimpleResponse(ResponseMessages.FRIEND_ALREADY_ADDED.toString(), userClique.getClientKey()));
        }
        userClique.insertFriend(friendClique.getPlayerInfo());

        diskOperator.submit(new WriteFriendlistTask(userNickname, friendNickname, userClique.getUserFriendlistPath()));
        diskOperator.submit(new WriteFriendlistTask(friendNickname, userNickname, friendClique.getUserFriendlistPath()));
    }
}
