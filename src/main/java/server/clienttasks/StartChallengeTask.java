package server.clienttasks;

import server.*;

import java.io.IOException;
import java.net.*;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class StartChallengeTask implements Callable<Challenge> {
    private final String userNickname;
    private final String friendNickname;
    private final UsersGraph usersGraph;
    private final SelectionKey clientKey;
    private final WorkersThreadpool workersThreadpool;
    private final ConcurrentHashMap<String, InetSocketAddress> onlineUsers;
    private final static int CHALLENGE_TIMER_T1 = 10000;

    /**
     * Costruisce un nuovo oggetto StartChallengeTask che si occupa dei controlli iniziali e dell'inoltro della sfida
     * all'utente sfidato.
     *
     * @param userNickname nickname dell'utente che origina la sfida.
     * @param friendNickname nickname dell'utente sfidato.
     * @param usersGraph riferimento alla struttura dati contenente gli utenti di Word Quizzle.
     * @param workersThreadpool riferimento all'oggetto che incapsula i principali worker di Word Quizzle.
     */
    public StartChallengeTask(String userNickname, String friendNickname,
                              UsersGraph usersGraph, WorkersThreadpool workersThreadpool,
                              ConcurrentHashMap<String, InetSocketAddress> onlineUsers,
                              SelectionKey clientKey) {
        this.userNickname = userNickname;
        this.friendNickname = friendNickname;
        this.clientKey = clientKey;
        this.usersGraph = usersGraph;
        this.onlineUsers = onlineUsers;
        this.workersThreadpool = workersThreadpool;
    }

    @Override
    public Challenge call() {
        Clique userClique = usersGraph.getClique(userNickname);
        Player friend = userClique.getFriend(friendNickname);

        // controllo che l'utente sfidato sia un amico.
        if (friend == null) {
            workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.FRIEND_NOT_EXISTS.toString(),
                    clientKey);
            return null;
        }

        // controllo che l'utente sfidato sia online
        InetSocketAddress friendAddress;
        if ((friendAddress = onlineUsers.get(friendNickname)) == null) {
            workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.FRIEND_NOT_ONLINE.toString(),
                    clientKey);
            return null;
        }

        // controllo che l'utente sfidato non sia già in partita.
        if (friend.isGaming()) {
            workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.FRIEND_IN_GAME.toString(),
                    clientKey);
            return null;
        }

        Challenge challenge = new Challenge(userNickname, friendNickname);
        challenge.setPlayerKey(userNickname, clientKey);

        // inoltro della sfida via UDP.
        DatagramSocket serverSocket;
        try {
            serverSocket = new DatagramSocket();
            byte[] requestBytes = (RequestMessages.CHALLENGE_FROM.toString() + " " + userNickname + " " +
                    System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8);
            DatagramPacket challengeRequestPacket = new DatagramPacket(requestBytes, requestBytes.length);
            // invio il pacchetto contenente la notifica di sfida.
            challengeRequestPacket.setAddress(friendAddress.getAddress());
            challengeRequestPacket.setPort(friendAddress.getPort());
            serverSocket.send(challengeRequestPacket);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            // l'utente è andato offline nel frattempo.
            workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.FRIEND_NOT_ONLINE.toString(),
                    clientKey);
            e.printStackTrace();

            return null;
        }

        final DatagramSocket finalServerSocket = serverSocket;
        // task che andrà ad effettuare il cleanup della sfida, nel caso in cui l'utente sfidato non abbia risposto
        // entro il timer T1.
        workersThreadpool.executeTimeoutTask(() -> {
            workersThreadpool.challengeRefusedTask(userNickname, challenge);
            finalServerSocket.close();
        }, CHALLENGE_TIMER_T1);

        return challenge;
    }
}
