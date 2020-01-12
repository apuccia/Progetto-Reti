package server.iotasks;

import server.UsersGraph;

public class SendFriendlistTask implements Runnable {
    private final String[] friends;

    public SendFriendlistTask(String[] friends) {
        this.friends = friends;
    }

    @Override
    public void run() {
        UsersGraph.GSON.toJson(friends);
    }
}
