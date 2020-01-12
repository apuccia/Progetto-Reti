package server.iotasks;

import server.UsersGraph;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

public class ReadUserInfoTask implements Callable<User> {
    private final String userInfoPath;

    public ReadUserInfoTask(String userInfoPath) {
        this.userInfoPath = userInfoPath;
    }

    @Override
    public User call() throws Exception {
        User user = null;

        try (FileInputStream fileInput = new FileInputStream(userInfoPath);
             BufferedInputStream bufferedInput = new BufferedInputStream(fileInput);
             InputStreamReader streamReader = new InputStreamReader(bufferedInput)) {

            user = (User) UsersGraph.GSON.fromJson(streamReader, User.class);
        }
        catch (IOException e) {
            e.printStackTrace();

            return null;
        }

        return user;
    }
}
