package server.tasks;

import server.RequestMessages;

import java.nio.channels.SelectionKey;

// factory di task.
public class TaskCreator {
    public TaskCreator() {

    }

    public Runnable createTask(String request, SelectionKey clientKey) {
        String[] tokens = RequestMessages.parseRequestMessage(request);
        RequestMessages type = RequestMessages.valueOf(tokens[0]);
        Runnable task;

        switch (type) {
            case LOGIN:
                task = new LoginTask(tokens[1], tokens[2], clientKey);
                break;
            case ADD_FRIEND:
                task = new AddFriendTask(tokens[1], clientKey);
                break;
            case SHOW_FRIENDS:
                ;
            case SHOW_RANKS:
                ;
            case CHALLENGE:
                ;
            default:
                task = null;
        }

        return task;
    }
}
