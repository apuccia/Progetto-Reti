package server.iotasks;

import server.UsersGraph;

import java.io.*;

public class WriteFriendlistTask implements Runnable {
    private final String userNickname;          // nickname dell'utente.
    private final String[] friends;        // nickname dell'amico da aggiungere.
    private final String userFriendlistPath;    // path del file contenente la lista amici dell'utente.

    /**
     * Costruisce un nuovo oggetto WriteFriendlistTask che si occupa di creare (se l'utente non aveva ancora amici) o
     * modificare la lista degli amici.
     *
     * @param userNickname nickname dell'utente di cui creare/modificare la lista amici.
     * @param friends nickname dell'amico da aggiungere alla lista amici.
     * @param userFriendlistPath path del file contenente la lista amici dell'utente.
     */
    public WriteFriendlistTask(String userNickname, String[] friends, String userFriendlistPath) {
        this.userNickname = userNickname;
        this.friends = friends;
        this.userFriendlistPath = userFriendlistPath;
    }

    @Override
    public void run() {
        File userFile = new File(userFriendlistPath);

        try {
            // creo la lista amici se non esiste (utente registrato ma ancora non ha aggiunto nessuno).
            userFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileOutputStream fileOutput = new FileOutputStream(userFile);
             BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutput);
                 OutputStreamWriter streamWriter = new OutputStreamWriter(bufferedOutput)
        ) {
            UsersGraph.GSON.toJson(friends, streamWriter);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
