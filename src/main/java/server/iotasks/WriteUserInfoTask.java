package server.iotasks;

import server.Clique;
import server.UsersGraph;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class WriteUserInfoTask implements Runnable {
    private final String userInfoPath;  // path del file contenente le informazioni dell'utente.
    private final Clique user;          // oggetto da serializzare nel file.

    /**
     * Costruisce un nuovo oggetto WriteUserInfoTask che andrà ad effettuare la scrittura delle informazioni di un utente
     * su un file json.
     *
     * @param user oggetto da serializzare in json.
     * @param userInfoPath path del file contenente le informazioni dell'utente.
     */
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
