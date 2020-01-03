package server.tasks;

import java.nio.channels.SelectionKey;

public class LogoutTask implements Runnable {
    private final String nickname;
    private final SelectionKey clientKey;

    public LogoutTask(String nickname, SelectionKey clientKey) {
        this.nickname = nickname;
        this.clientKey = clientKey;
    }

    @Override
    public void run() {

    }
}
