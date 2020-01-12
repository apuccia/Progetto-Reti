package server.clienttasks;

import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;
import server.*;
import server.iotasks.SendSimpleResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static java.nio.channels.SelectionKey.OP_WRITE;

public class ChallengeTask implements Runnable {
    private final String userNickname;
    private final String friendNickname;
    private final ArrayList<String> italianWords;
    private final ConcurrentHashMap<String, String> translatedWords;
    private final UsersGraph usersGraph;
    private final ExecutorService diskOperator;
    private final ExecutorService answerersOperator;

    private static final int GAME_PORT = 2000;
    private static final int CHALLENGE_TIMER = 10000;
    private static final String MYMEMORY = "https://api.mymemory.translated.net";

    public ChallengeTask(String userNickname, String friendNickname, UsersGraph usersGraph, ExecutorService diskOperator,
                         ExecutorService answerersOperator, ArrayList<String> italianWords,
                         ConcurrentHashMap<String, String> translatedWords) {
        this.userNickname = userNickname;
        this.friendNickname = friendNickname;
        this.usersGraph = usersGraph;
        this.diskOperator = diskOperator;
        this.answerersOperator = answerersOperator;
        this.italianWords = italianWords;
        this.translatedWords = translatedWords;
    }

    @Override
    public void run() {
        Clique userClique = usersGraph.getClique(userNickname);
        Player friend = userClique.getFriend(friendNickname);

        if (friend == null) {
            answerersOperator.execute(new SendSimpleResponse(ResponseMessages.FRIEND_NOT_EXISTS.toString(), userClique.getClientKey()));
            return;
        }

        try (DatagramSocket serverSocket = new DatagramSocket(GAME_PORT);) {
            byte[] requestBytes = (RequestMessages.CHALLENGE.toString() + " FROM " + userNickname).getBytes();
            DatagramPacket challengeRequestPacket = new DatagramPacket(requestBytes, requestBytes.length);

            challengeRequestPacket.setAddress(InetAddress.getByName("Localhost"));
            challengeRequestPacket.setPort(GAME_PORT);

            serverSocket.send(challengeRequestPacket);

            byte[] responseBytes = new byte[64];
            DatagramPacket challengeResponsePacket = new DatagramPacket(responseBytes, responseBytes.length);
            serverSocket.setSoTimeout(CHALLENGE_TIMER);
            serverSocket.receive(challengeResponsePacket);

            String response = new String(challengeResponsePacket.getData(), 0, challengeResponsePacket.getLength(), StandardCharsets.UTF_8);

            if (response == ResponseMessages.CHALLENGE_REFUSED.toString()) {
                answerersOperator.execute(new SendSimpleResponse(ResponseMessages.CHALLENGE_REFUSED.toString(), userClique.getClientKey()));
                return;
            }
            else {
                answerersOperator.execute(new SendSimpleResponse(ResponseMessages.CHALLENGE_ACCEPTED.toString(), userClique.getClientKey()));
            }
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            answerersOperator.execute(new SendSimpleResponse(ResponseMessages.CHALLENGE_REFUSED.toString(), userClique.getClientKey()));
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] words = new String[10];
        Random randomChooser = new Random();

        for (String word : words) {
            word = italianWords.get(randomChooser.nextInt(italianWords.size()));

            if (!translatedWords.containsKey(word)) {
                try {
                    URL myMemoryURL = new URL(MYMEMORY + "/get?q=" + word + "&langpair=en|it");
                    URLConnection connection = myMemoryURL.openConnection();

                    BufferedReader buffered = new BufferedReader(new
                            InputStreamReader(connection.getInputStream()));

                    String line;
                    StringBuilder builder = new StringBuilder();

                    while ((line = buffered.readLine()) != null) {
                        builder.append(line);
                    }
                    JsonStreamParser parser = new JsonStreamParser(builder.toString());
                    JsonObject jsonObject = (JsonObject) parser.next();
                    translatedWords.putIfAbsent(word, jsonObject.get("responseData").getAsJsonObject()
                            .get("translatedText").getAsString());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Selector gameSelector = null;
        try {
            gameSelector = Selector.open();

            usersGraph.getClique(userNickname).getClientKey().channel().register(gameSelector, OP_WRITE);
            usersGraph.getClique(friendNickname).getClientKey().channel().register(gameSelector, OP_WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while (true) {
                gameSelector.select();

                Set<SelectionKey> selectedKeys = gameSelector.selectedKeys();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
