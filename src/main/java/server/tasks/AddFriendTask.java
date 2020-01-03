package server.tasks;

import java.nio.channels.SelectionKey;

public class AddFriendTask implements Runnable {
    private final String friendNickname;
    private final SelectionKey clientKey;

    public AddFriendTask(String friendNickname, SelectionKey clientKey) {
        this.friendNickname = friendNickname;
        this.clientKey = clientKey;
    }

    @Override
    public void run() {

    }
}
