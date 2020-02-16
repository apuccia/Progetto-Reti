package server;

import com.google.gson.stream.JsonReader;
import server.clienttasks.TaskAcceptor;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainClass {
    private final static int PORT = 8888;
    private final static int RMI_PORT = 6789;

    public static void main(String[] args) {
        PrintWriter serverWriter = new PrintWriter(System.out, true);
        UsersGraph usersGraph = new UsersGraph();
        ArrayList<String> italianWords = new ArrayList<>();

        File file = new File(UsersGraph.MAIN_PATH);
        String clients[] = file.list();
        long start;

        // carico gli utenti di Word Quizzle.
        if (clients != null) {
            start = System.currentTimeMillis();
            for (String client : clients) {
                String userInfoPath = UsersGraph.MAIN_PATH + "/" + client + "/UserInfo.json";
                String userFriendlistPath = UsersGraph.MAIN_PATH + "/" + client + "/UserFriendlist.json";

                try (FileInputStream fileInput = new FileInputStream(userInfoPath);
                     BufferedInputStream bufferedInput = new BufferedInputStream(fileInput);
                     InputStreamReader streamReader = new InputStreamReader(bufferedInput)) {

                    Clique userClique = UsersGraph.GSON.fromJson(streamReader, Clique.class);
                    userClique.setUserInfoPath(userInfoPath);
                    userClique.setUserFriendlistPath(userFriendlistPath);

                    userClique.getPlayerInfo().setGaming(false);
                    usersGraph.insertClique(client, userClique);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            serverWriter.println("UTENTI CARICATI, tempo impiegato: " + (System.currentTimeMillis() - start));

            // carico le amicizie degli utenti.
            start = System.currentTimeMillis();
            for (String client : clients) {
                Clique userClique = usersGraph.getClique(client);

                try (FileReader fileReader = new FileReader(userClique.getUserFriendlistPath());
                     JsonReader jsonReader = new JsonReader(fileReader)) {

                    jsonReader.beginArray();
                    while (jsonReader.hasNext()) {
                        String friendName = jsonReader.nextString();

                        userClique.insertFriend(usersGraph.getClique(friendName).getPlayerInfo());
                    }

                } catch (IOException e) {
                    // l'utente non ha amicizie, il file che le contiene viene creato solamente alla prima aggiunta
                    // di un amico.
                    e.printStackTrace();
                }
            }
            serverWriter.println("AMICIZIE CARICATE, tempo impiegato: " + (System.currentTimeMillis() - start));
        }

        // carico le parole italiane da utilizzare durante le sfide.
        start = System.currentTimeMillis();
        try (FileReader fileReader = new FileReader("Words.json");
             JsonReader jsonReader = new JsonReader(fileReader)) {

            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                String word = jsonReader.nextString();

                italianWords.add(word);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverWriter.println("PAROLE CARICATE, tempo impiegato: " + (System.currentTimeMillis() - start));

        // threadpool che si occuperà della lettura delle richieste.
        ExecutorService acceptorOperator = Executors.newSingleThreadExecutor();
        WorkersThreadpool workersThreadpool = new WorkersThreadpool(usersGraph, italianWords);

        // inizializzazione rmi.
        RegistrationService registrationService = new RegistrationService(usersGraph, workersThreadpool);
        IRegistrationService stub = null;
        try {
            stub = (IRegistrationService)
                    UnicastRemoteObject.exportObject(registrationService, 0);
            LocateRegistry.createRegistry(RMI_PORT);
            Registry registry = LocateRegistry.getRegistry(RMI_PORT);
            registry.rebind("REGISTRATION-SERVICE", stub);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // inizializzazione del selettore.
        ServerSocketChannel serverChannel = null;
        Selector serverSelector = null;

        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(PORT));

            serverSelector = Selector.open();
            serverChannel.register(serverSelector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverWriter.println("SERVER PRONTO");
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
                    System.out.println("NUOVA RICHIESTA DI CONNESSIONE");
                    try {
                        SocketChannel client = serverChannel.accept();
                        client.configureBlocking(false);

                        client.register(serverSelector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if (key.isReadable()) {
                    // nuova richiesta servizio di Word Quizzle.
                    int bytesRead = -1;
                    StringBuilder request = new StringBuilder();
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = (ByteBuffer) key.attachment();

                    try {
                        while ((bytesRead = clientChannel.read(buffer)) > 0) {
                            buffer.flip();
                            request.append(new String(buffer.array(), 0, bytesRead, StandardCharsets.UTF_8));
                            buffer.clear();
                        }
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (bytesRead == -1) {
                        System.out.println("DISCONNESSIONE DA WORD QUIZZLE");
                        key.cancel();
                        acceptorOperator.execute(new TaskAcceptor(workersThreadpool,
                                RequestMessages.LOGOUT_DISCONNECT.toString(), key));
                        continue;
                    }

                    System.out.println("NUOVA RICHIESTA DI SERVIZIO");
                    buffer.clear();
                    System.out.println("-> " + request.toString());
                    acceptorOperator.execute(new TaskAcceptor(workersThreadpool, request.toString(), key));
                }
            }
        }
    }
}
