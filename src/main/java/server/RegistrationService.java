package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.rmi.server.RemoteServer;
import java.util.TreeMap;

public class RegistrationService extends RemoteServer implements IRegistrationService {
    private final UsersGraph usersGraph;
    private final Gson gson;

    public RegistrationService(UsersGraph usersGraph) {
        this.usersGraph = usersGraph;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.setExclusionStrategies(new UserExclusionStrategy());

        this.gson = gsonBuilder.create();
    }

    public String registra_utente(String nickname, String password) {
        String clientPath = usersGraph.MAIN_PATH + "/client_" + nickname;
        File userDirectory = new File(clientPath);

        if (!userDirectory.mkdir()) {
            return ResponseMessages.USER_ALREADY_REGISTERED.toString();
        }

        User user = new User(nickname, password, clientPath);
        Clique clique = new Clique(user, new TreeMap<String, User>());

        try (FileOutputStream fileOutput = new FileOutputStream(user.getUserInfoPath());
             BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutput);
             OutputStreamWriter streamWriter = new OutputStreamWriter(bufferedOutput)
            ) {
            gson.toJson(user, streamWriter);

            File friendlistFile = new File(user.getUserFriendlistPath());
            friendlistFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseMessages.REGISTRATION_ERROR.toString();
        }

        usersGraph.insertClique(nickname, clique);

        return ResponseMessages.USER_REGISTERED.toString();
    }
}
