package server.iotasks;

import server.UsersGraph;

import java.io.*;

public class WriteFriendlistTask implements Runnable {
    private final String userNickname;
    private final String friendNickname;
    private final String userFriendlistPath;

    public WriteFriendlistTask(String userNickname, String friendNickname, String userFriendlistPath) {
        this.userNickname = userNickname;
        this.friendNickname = friendNickname;
        this.userFriendlistPath = userFriendlistPath;
    }


    @Override
    public void run() {
        File userFile = new File(userFriendlistPath);

        if (!userFile.exists()) {
            try {
                userFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try (FileOutputStream fileOutput = new FileOutputStream(userFile);
                 BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutput);
                 OutputStreamWriter streamWriter = new OutputStreamWriter(bufferedOutput)
            ) {
                String[] friends = new String[1];
                friends[0] = friendNickname;
                UsersGraph.GSON.toJson(friends, streamWriter);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try (RandomAccessFile userFriendlistFile = new RandomAccessFile(userFile, "rw")) {

                userFriendlistFile.seek(userFriendlistFile.length() - 2);

                userFriendlistFile.writeBytes(",\n\t\"" + friendNickname + "\"\n]");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
