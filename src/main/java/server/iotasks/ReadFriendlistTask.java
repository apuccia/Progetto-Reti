package server.iotasks;

import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.Callable;

public class ReadFriendlistTask implements Callable<Vector<String>> {
    private final String userFriendlistPath;
    private final Vector<String> friends;

    public ReadFriendlistTask(String userFriendlistPath) {
        this.userFriendlistPath = userFriendlistPath;
        friends = new Vector<String>();
    }


    @Override
    public Vector<String> call() throws Exception {
        // carico gli amici dell'utente
        try (FileReader fileReader = new FileReader(userFriendlistPath);
             JsonReader jsonReader = new JsonReader(fileReader)) {

            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                jsonReader.beginObject();
                String friendName = jsonReader.nextString();
                jsonReader.endObject();

                friends.add(friendName);
            }

            return friends;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
