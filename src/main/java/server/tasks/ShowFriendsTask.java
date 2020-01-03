package server.tasks;

import java.nio.channels.SelectionKey;

public class ShowFriendsTask implements Runnable {
    private SelectionKey clientKey;

    public ShowFriendsTask(SelectionKey clientKey) {
        this.clientKey = clientKey;
    }

    @Override
    public void run() {

    }
}
