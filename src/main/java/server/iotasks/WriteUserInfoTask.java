package server.iotasks;

import server.Clique;
import server.UsersGraph;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class WriteUserInfoTask implements Runnable {
    private final String userInfoPath;
    private final Clique user;

    public WriteUserInfoTask(Clique user, String userInfoPath) {
        this.userInfoPath = userInfoPath;
        this.user = user;
    }

    @Override
    public void run() {
        // serializzo le informazioni dell'utente su un suo file dedicato.
        try (FileOutputStream fileOutput = new FileOutputStream(userInfoPath);
             BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutput);
             OutputStreamWriter streamWriter = new OutputStreamWriter(bufferedOutput)
        ) {
            UsersGraph.GSON.toJson(user, streamWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
