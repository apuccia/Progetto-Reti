package clientGUI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import server.IRegistrationService;
import server.Player;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class GUIClientMainClass {
    public static Selector clientSelector;

    private static int REGISTRY_PORT = 6789;
    private static int SERVER_TCP_PORT = 8888;
    private static int CHALLENGE_TIMER_T1 = 10000;

    // struttura che conterrà le richieste da inviare tramite TCP.
    private static ArrayBlockingQueue<String> request = new ArrayBlockingQueue<>(1);
    // struttura che conterrà le richieste di registrazione.
    private static ArrayBlockingQueue<String> registrationRequest = new ArrayBlockingQueue<>(1);
    // struttura che conterrà la richiesta di eliminazione di una sfida rifiutata.
    private static ArrayBlockingQueue<String> challengeUpdateRequest = new ArrayBlockingQueue<>(1);
    // struttura che conterrà le sfide che sono state ricevute dal client.
    private static HashMap<String, Long> challengersRequests = new HashMap<>();

    private static MainFrame clientGui;
    private static boolean logged = false;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final SocketAddress address = new InetSocketAddress("Localhost", SERVER_TCP_PORT);
    private static IRegistrationService serverObject = null;
    private static SelectionKey clientKey = null;
    private static SocketChannel client = null;
    private static DatagramChannel challengesChannel = null;

    public static void main(String[] args) {
        client = null;
        challengesChannel = null;
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        SwingUtilities.invokeLater(() -> clientGui = new MainFrame("Word Quizzle", registrationRequest,
                request, challengeUpdateRequest));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (client != null) {
                    client.close();
                }
                if (challengesChannel != null) {
                    challengesChannel.close();
                }

                clientSelector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        try {
            clientSelector = Selector.open();
        } catch (IOException e) {
            System.exit(1);
        }

        while (true) {
            int selected = 0;

            try {
                if ((selected = clientSelector.select(5000)) >= 0) {
                    String clientRequest;

                    if ((clientRequest = request.poll()) != null) {
                        if (!logged) {
                            // connessione solamente alla richiesta della prima operazione (login).
                            connect();
                        }

                        byte[] bytes = clientRequest.getBytes(StandardCharsets.UTF_8);
                        int offset = 0, maxWrite;

                        while (offset != bytes.length) {
                            maxWrite = Math.min(bytes.length - offset, 1024);

                            buffer.put(bytes, offset, maxWrite);
                            buffer.flip();
                            offset += client.write(buffer);

                            buffer.clear();
                        }

                        clientKey.interestOps(SelectionKey.OP_READ);
                    }
                    if ((clientRequest = registrationRequest.poll()) != null) {
                        register(clientRequest);
                    }

                    if ((clientRequest = challengeUpdateRequest.poll()) != null) {
                        challengersRequests.remove(clientRequest);
                    }
                }
            } catch (IOException e) {
                // server crash
                SwingUtilities.invokeLater(() -> clientGui.notifyCrash());
                challengersRequests.clear();
                logged = false;
                try {
                    challengesChannel.close();
                    client.close();

                    challengesChannel = null;
                    client = null;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }

            if (selected != 0) {
                Set<SelectionKey> selectedKeys = clientSelector.selectedKeys();
                Iterator<SelectionKey> keysIterator = selectedKeys.iterator();
                while (keysIterator.hasNext()) {
                    SelectionKey key = keysIterator.next();

                    keysIterator.remove();
                    Channel channel = key.channel();

                    if (channel == client && key.isReadable()) {
                        // ricevuto esito dal server.
                        int read = -1;
                        try {
                            read = client.read(buffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (read == -1) {
                            key.cancel();
                            SwingUtilities.invokeLater(() -> clientGui.notifyCrash());
                            challengersRequests.clear();
                            logged = false;
                            try {
                                challengesChannel.close();
                                client.close();

                                challengesChannel = null;
                                client = null;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            continue;
                        }

                        String response = new String(buffer.array(), 0, read, StandardCharsets.UTF_8);
                        buffer.clear();
                        System.out.println(response);
                        parser(response);
                    }
                    else if (channel == challengesChannel && key.isReadable()) {
                        // nuova richiesta di sfida.
                        try {
                            challengesChannel.receive(buffer);
                        } catch (IOException e) {
                            SwingUtilities.invokeLater(() -> clientGui.notifyCrash());
                            challengersRequests.clear();
                            logged = false;

                            try {
                                challengesChannel.close();
                                client.close();

                                challengesChannel = null;
                                client = null;
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            e.printStackTrace();
                        }

                        String[] challengerRequest = new String(buffer.array(), 0, buffer.position()
                                , StandardCharsets.UTF_8).split(" ");
                        challengersRequests.put(challengerRequest[1], Long.parseLong(challengerRequest[2]));
                        ArrayList<String> challengers = new ArrayList<>();

                        for (String challenger : challengersRequests.keySet()) {
                            long time = challengersRequests.get(challenger);

                            if (System.currentTimeMillis() - time <= CHALLENGE_TIMER_T1) {
                                challengers.add(challenger);
                            } else {
                                challengersRequests.remove(challenger);
                            }
                        }

                        SwingUtilities.invokeLater(() -> {
                            clientGui.notifyChallenges(challengers.toArray(new String[challengers.size()]));
                            clientGui.notifyNewChallenge();
                        });
                        buffer.clear();
                    }
                }
            }
            else {
                // aggiorno le sfide che sono scadute secondo il timeout t1.
                updateChallenges();
            }
        }
    }

    // implementa le operazioni di connessione.
    public static void connect() {
        try {
            client = SocketChannel.open(address);
            client.configureBlocking(false);

            challengesChannel = DatagramChannel.open();
            challengesChannel.configureBlocking(false);
            System.out.println(client.getLocalAddress());
            challengesChannel.socket().bind(client.getLocalAddress());

            clientKey = client.register(clientSelector, 0);
            challengesChannel.register(clientSelector, SelectionKey.OP_READ);
        }
        catch (IOException e) {
            e.printStackTrace();
            clientGui.notifyCrash();
        }
    }

    // implementa il recupero dello stub per la registrazione, se si è alla prima registrazione dall'avvio del client
    // oppure se il server è crashato.
    public static void register(String registrationRequest) {
        String[] tokens = registrationRequest.split(" ");
        Remote remoteObject;

        if (serverObject == null) {
            try {
                Registry registry = LocateRegistry.getRegistry(REGISTRY_PORT);
                remoteObject = registry.lookup("REGISTRATION-SERVICE");

                serverObject = (IRegistrationService) remoteObject;
            } catch (ConnectException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
        }

        if (serverObject == null) {
            SwingUtilities.invokeLater(() -> clientGui.notifyCrash());
        }
        else {
            try {
                String result = serverObject.registra_utente(tokens[1], tokens[2]);
                parser(result);
            } catch (RemoteException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> clientGui.notifyCrash());
                serverObject = null;
            }
        }
    }

    // controlla nella struttura dati contenente le sfide ricevute quali sono quelle ancora valide.
    public static void updateChallenges() {
        ArrayList<String> challengers = new ArrayList<>();
        int validChallenges = 0;

        for (String challenger : challengersRequests.keySet()) {
            long time = challengersRequests.get(challenger);

            if (System.currentTimeMillis() - time <= CHALLENGE_TIMER_T1) {
                challengers.add(challenger);
                validChallenges++;
            } else {
                challengersRequests.remove(challenger);
            }
        }

        int finalValidChallenges = validChallenges;
        SwingUtilities.invokeLater(() -> {
            clientGui.notifyChallenges(challengers.toArray(new String[challengers.size()]));
            if (finalValidChallenges != 0) {
                clientGui.notifyNewChallenge();
            }
            else {
                clientGui.notifyNoChallenge();
            }
        });
    }

    public static void parser(String message) {
        String[] tokens = message.split(":", 2);
        int responseCode = Integer.parseInt(tokens[0]);

        switch (responseCode) {
            // USER_ALREADY_REGISTERED(0, "L'utente è già registrato a Word Quizzle")
            case 0:
                SwingUtilities.invokeLater(() -> clientGui.notifyRegistrationResponse(false, message));
                break;
            // USER_NOT_EXISTS(1, "Il nickname specificato non esiste")
            // WRONG_PASSWORD(2, "La password specificata è errata")
            // USER_ALREADY_LOGGED(3, "L'utente ha già effettuato il login a Word Quizzle")
            case 1:
            case 2:
            case 3:
                SwingUtilities.invokeLater(() -> clientGui.notifyLoginResponse(false, message));
                break;
            // USER_NOT_EXISTS_ADD(4, "Il nickname specificato non esiste"),
            // FRIEND_ALREADY_ADDED(5, "L'utente è già negli amici"),
            case 4:
            case 5:
                SwingUtilities.invokeLater(() -> clientGui.notifyAddFriendResponse(false, message));
                break;
            // FRIEND_NOT_EXISTS(6, "L'utente non è presente nella lista amici"),
            // CHALLENGE_REFUSED(7, "Sfida rifiutata"),
            // FRIEND_NOT_ONLINE(8, "L'amico non è online"),
            // FRIEND_IN_GAME(9, "L'amico è già in partita"),
            case 6:
            case 7:
            case 8:
            case 9:
                SwingUtilities.invokeLater(() -> clientGui.notifyChallengeRequest(false, message));
                break;
            // CHALLENGE_TIMEOUTED(10, "Tempo per accettare la sfida scaduto")
            // USER_CRASHED(11, "L'utente che ha inviato la sfida si è disconnesso")
            case 10:
            case 11:
                SwingUtilities.invokeLater(() -> clientGui.notifyAccept(false, message));
                break;
            // USER_REGISTERED(50, "L'utente è stato registrato con successo")
            case 50:
                SwingUtilities.invokeLater(() -> clientGui.notifyRegistrationResponse(true, message));
                break;
            // USER_LOGGED(51, "Login effettuato con successo")
            case 51:
                logged = true;

                SwingUtilities.invokeLater(() -> clientGui.notifyLoginResponse(true, message));
                break;
            // USER_LOGOUTED(52, "Logout effettuato con successo")
            case 52:
                logged = false;
                challengersRequests.clear();
                try {
                    client.close();
                    challengesChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                SwingUtilities.invokeLater(() -> clientGui.notifyLogoutResponse(message));
                break;
            // FRIEND_ADDED(53, "Amico aggiunto con successo")
            case 53:
                SwingUtilities.invokeLater(() -> clientGui.notifyAddFriendResponse(true, message));
                break;
            // CHALLENGE_ACCEPTED(54, "Sfida accettata")
            case 54:
                SwingUtilities.invokeLater(() -> clientGui.notifyChallengeRequest(true, message));
                break;
            // FRIENDLIST(55, "")
            case 55:
                String[][] nicknamesMatrix;
                String[] nicknames = gson.fromJson(tokens[1], String[].class);

                nicknamesMatrix = new String[nicknames.length][1];

                for (int i = 0; i < nicknames.length; i++) {
                    nicknamesMatrix[i][0] = nicknames[i];
                }

                SwingUtilities.invokeLater(() -> clientGui.notifyFriendlistResponse(nicknamesMatrix));
                break;
            // RANKS(56, "")
            case 56:
                String[][] playersMatrix;
                Player[] players = gson.fromJson(tokens[1], Player[].class);

                Arrays.sort(players, (playerA, playerB) -> {
                    if (playerA.getUserScore() == playerB.getUserScore()) {
                        if (playerA.getWins() == playerB.getWins()) {
                            if (playerA.getLosses() == playerB.getLosses()) {
                                return 0;
                            }
                            else {
                                return Long.compare(playerB.getLosses(), playerA.getLosses());
                            }
                        }
                        else {
                            return Long.compare(playerB.getWins(), playerA.getWins());
                        }
                    }
                    else {
                        return Long.compare(playerB.getUserScore(), playerA.getUserScore());
                    }
                });
                
                playersMatrix = new String[players.length][5];

                for (int i = 0; i < players.length; i++) {
                    playersMatrix[i][0] = players[i].getNickname();
                    playersMatrix[i][1] = Long.toString(players[i].getUserScore());
                    playersMatrix[i][2] = Long.toString(players[i].getWins());
                    playersMatrix[i][3] = Long.toString(players[i].getLosses());
                    playersMatrix[i][4] = Float.toString(players[i].getRateo());
                }

                SwingUtilities.invokeLater(() -> clientGui.notifyRanksResponse(playersMatrix));
                break;
            // USERSCORE(57, "")
            case 57:
                Player player = gson.fromJson(tokens[1], Player.class);

                SwingUtilities.invokeLater(() -> clientGui.notifyUserscoreResponse(player.getUserScore(),
                        player.getWins(), player.getLosses(), player.getRateo()));
                break;
            // CHALLENGE_REQUEST_ACCEPTED(58, "Richiesta di sfida accettata")
            case 58:
                SwingUtilities.invokeLater(() -> clientGui.notifyAccept(true, message));
                break;
            // WORD(59, "WORD")
            case 59:
                System.out.println(tokens[1]);
                String word = tokens[1].split(" ")[1];

                SwingUtilities.invokeLater(() -> clientGui.notifyWord(word));
                break;
            // RESULT(60, "RESULT")
            case 60:
                String[] resultTokens = tokens[1].split(" ");
                StringBuilder result = new StringBuilder();

                result.append("Hai tradotto correttamente " + resultTokens[3] + " parole, " +
                        "ne hai sbagliate " + resultTokens[4] + " e non risposto a " + resultTokens[5] +
                        ".\nHai totalizzato " + resultTokens[2] + " punti.\nIl tuo avversario" +
                        " ha totalizzato " + resultTokens[6] + " punti.\n");

                if (resultTokens[1].equals("WIN")) {
                    result.append("Hai vinto. Hai guadagnato 3 punti extra, per un totale di " +
                            (Integer.parseInt(resultTokens[2]) + 3) + " punti");
                }
                else if (resultTokens[1].equals("LOSS")) {
                    result.append("Hai perso.");
                }
                else if (resultTokens[1].equals("TIE")) {
                    result.append("Pareggio");
                }

                SwingUtilities.invokeLater(() -> clientGui.notifyEndChallenge(result.toString()));
                break;
        }
    }
}
