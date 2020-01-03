package server.tasks;

import java.nio.channels.SelectionKey;

public class ShowRanksTask implements Runnable {
    private SelectionKey clientKey;

    public ShowRanksTask(SelectionKey clientKey) {
        this.clientKey = clientKey;
    }


    @Override
    public void run() {

    }
}
