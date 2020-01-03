package server.tasks;

import java.nio.channels.SelectionKey;

public class LoginTask implements Runnable{
    private final String nickname;
    private final String password;
    private final SelectionKey key;

    public LoginTask(String nickname, String password, SelectionKey key) {
        this.nickname = nickname;
        this.password = password;
        this.key = key;
    }

    @Override
    public void run() {

    }
}
