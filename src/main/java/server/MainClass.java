package server;

import com.google.gson.stream.JsonReader;
import server.clienttasks.TaskAcceptor;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainClass {
    private final static int PORT = 8888;

    public static void main(String[] args) {
        ServerSocketChannel serverChannel = null;
        Selector serverSelector = null;
        UsersGraph usersGraph = new UsersGraph();

        ArrayList<String> italianWords = new ArrayList<>();
        ConcurrentHashMap<String, String> translatedWords = new ConcurrentHashMap<>();

        File file = new File(UsersGraph.MAIN_PATH);
        String clients[] = file.list();


        for (String client : clients) {
            String userInfoPath = UsersGraph.MAIN_PATH + "/" + client + "UserInfo.json";
            String userFriendlistPath = UsersGraph.MAIN_PATH + "/" + client + "UserFriendlist.json";

            try (FileInputStream fileInput = new FileInputStream(userInfoPath);
                 BufferedInputStream bufferedInput = new BufferedInputStream(fileInput);
                 InputStreamReader streamReader = new InputStreamReader(bufferedInput)) {

                Clique userClique = (Clique) UsersGraph.GSON.fromJson(streamReader, Clique.class);
                userClique.setUserInfoPath(userInfoPath);
                userClique.setUserFriendlistPath(userFriendlistPath);
                userClique.setClientKey(null);

                userClique.getPlayerInfo().setOnline(false);
                usersGraph.insertClique(client, userClique);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (String client : clients) {
            Clique userClique = usersGraph.getClique(client);

            try (FileReader fileReader = new FileReader(userClique.getUserFriendlistPath());
                 JsonReader jsonReader = new JsonReader(fileReader)) {

                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    jsonReader.beginObject();
                        String friendName = jsonReader.nextString();
                    jsonReader.endObject();

                    userClique.insertFriend(usersGraph.getClique(friendName).getPlayerInfo());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (FileReader fileReader = new FileReader("Words.json");
             JsonReader jsonReader = new JsonReader(fileReader)) {

            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                jsonReader.beginObject();
                String word = jsonReader.nextString();
                jsonReader.endObject();

                italianWords.add(word);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // si occupa di interagire con il disco per la scrittura e lettura dei file json
        ExecutorService diskOperator = Executors.newSingleThreadExecutor();

        // threadpool che si occuperà della lettura delle richieste.
        ExecutorService acceptorsOperator = Executors.newFixedThreadPool(2);

        // threadpool che si occuperà di svolgere le richieste.
        ExecutorService workersOperator = Executors.newFixedThreadPool(2);

        // threadpool che si occuperà di rispondere ai client.
        ExecutorService answerersOperator = Executors.newFixedThreadPool(3);

        WorkersThreadpool workersThreadpool = new WorkersThreadpool(usersGraph, workersOperator, diskOperator,
                answerersOperator, italianWords);

        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(PORT));

            serverSelector = Selector.open();
            serverChannel.register(serverSelector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                serverSelector.select();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Set<SelectionKey> selectedKeys = serverSelector.selectedKeys();
            Iterator<SelectionKey> keysIterator = selectedKeys.iterator();

            while (keysIterator.hasNext()) {
                SelectionKey key = keysIterator.next();
                keysIterator.remove();

                if (key.isAcceptable()) {
                    // nuova richiesta di connessione.
                    try {
                        SocketChannel client = serverChannel.accept();
                        client.configureBlocking(false);
                        client.register(serverSelector, SelectionKey.OP_READ);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if (key.isReadable()) {
                    // nuova richiesta servizio di Word Quizzle.
                    key.interestOps(0);
                    acceptorsOperator.execute(new TaskAcceptor(workersThreadpool, key));
                }
            }
        }
    }
}
