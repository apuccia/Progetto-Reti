package clientCLI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import server.IRegistrationService;
import server.Player;
import server.RequestMessages;
import server.ResponseMessages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class CLIClientMainClass {
    private static PrintWriter clientWriter = new PrintWriter(System.out, true);
    private static Scanner clientScanner = new Scanner(System.in);
    private static ClientSelector clientSelector;
    private static Thread selectorThread;
    private static Gson gson = new GsonBuilder().create();

    private static IRegistrationService serverObject = null;

    public static void main(String[] args) {
        int operation;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> selectorThread.interrupt()));

        external_loop: while (true) {
            clientWriter.println("+-----------------+\n" +
                                 "| 1) ACCEDI       |\n" +
                                 "| 2) REGISTRATI   |\n" +
                                 "| 3) ESCI         |\n" +
                                 "+-----------------+");

            String s = clientScanner.nextLine();

            if (!s.isEmpty()) {
                operation = Integer.parseInt(s);
            }
            else {
                continue;
            }
            String clientNickname, password;
            switch (operation) {
                case 1:
                    clientWriter.println("Inserisci nickname: ");
                    clientNickname = clientScanner.nextLine();
                    if (clientNickname.contains(" ") || clientNickname.isEmpty()) {
                        clientWriter.println("Non sono ammessi spazi");
                        break;
                    }
                    clientWriter.println("Inserisci password: ");
                    password = clientScanner.nextLine();
                    if (password.contains(" ") || password.isEmpty()) {
                        clientWriter.println("Non sono ammessi spazi");
                        break;
                    }

                    clientSelector = new ClientSelector();
                    selectorThread = new Thread(clientSelector);
                    selectorThread.start();

                    clientSelector.putRequest(RequestMessages.LOGIN + " " + clientNickname + " " + password);
                    String response = clientSelector.getResponse();
                    if (response.equals(ResponseMessages.USER_LOGGED.toString())) {
                        clientWriter.println(response);
                        internalLoop(clientNickname);
                    }
                    else if (response.equals(ClientSelector.CRASH)) {
                        clientWriter.println("Il server è offline");
                        selectorThread.interrupt();
                    }
                    else {
                        clientWriter.println(response);
                    }
                    break;
                case 2:
                    clientWriter.println("Inserisci nickname: ");
                    clientNickname = clientScanner.nextLine();
                    if (clientNickname.contains(" ") || clientNickname.isEmpty()) {
                        clientWriter.println("Non sono ammessi spazi");
                        break;
                    }
                    clientWriter.println("Inserisci password: ");
                    password = clientScanner.nextLine();
                    if (password.contains(" ") || password.isEmpty()) {
                        clientWriter.println("Non sono ammessi spazi");
                        break;
                    }

                    clientWriter.println(register(clientNickname, password));
                    break;
                case 3:
                    break external_loop;
            }
        }
    }

    public static void internalLoop(String clientNickname) {
        String response;
        int operation = -1;

        while (operation != 7) {
            clientWriter.println("+------------------------------+\n" +
                                 "| 1) AGGIUNGI AMICO            |\n" +
                                 "| 2) LISTA AMICI               |\n" +
                                 "| 3) CLASSIFICA                |\n" +
                                 "| 4) MOSTRA PUNTEGGIO UTENTE   |\n" +
                                 "| 5) SFIDA AMICO               |\n" +
                                 "| 6) RICHIESTE DI SFIDA        |\n" +
                                 "| 7) LOGOUT                    |\n" +
                                 "+------------------------------+");

            String s = clientScanner.nextLine();

            if (!s.isEmpty()) {
                operation = Integer.parseInt(s);
            }
            else {
                continue;
            }
            switch (operation) {
                case 1:
                    clientWriter.println("Nome amico:");

                    String friendNickname = clientScanner.nextLine();

                    if (friendNickname.equals(clientNickname)) {
                        clientWriter.println("Valore non valido");
                        break;
                    }
                    if (friendNickname.contains(" ") || friendNickname.isEmpty()) {
                        clientWriter.println("Non sono ammessi spazi");
                        break;
                    }

                    clientSelector.putRequest(RequestMessages.ADD_FRIEND + " " + clientNickname + " "
                            + friendNickname);
                    response = clientSelector.getResponse();

                    if (response.equals(ClientSelector.CRASH)) {
                        clientWriter.println("Il server è offline");
                        selectorThread.interrupt();
                        return;
                    }
                    else {
                        clientWriter.println(response);
                    }
                    break;
                case 2:
                    clientSelector.putRequest(RequestMessages.SHOW_FRIENDS + " " + clientNickname);
                    response = clientSelector.getResponse();

                    if (response.equals(ClientSelector.CRASH)) {
                        clientWriter.println("Il server è offline");
                        selectorThread.interrupt();
                        return;
                    }

                    String[] friends = gson.fromJson(response.split(":", 2)[1], String[].class);

                    for (String friend: friends) {
                        clientWriter.println(friend);
                    }
                    break;
                case 3:
                    clientSelector.putRequest(RequestMessages.SHOW_RANKS + " " + clientNickname);
                    response = clientSelector.getResponse();

                    if (response.equals(ClientSelector.CRASH)) {
                        clientWriter.println("Il server è offline");
                        selectorThread.interrupt();
                        return;
                    }

                    Player[] players = gson.fromJson(response.split(":", 2)[1], Player[].class);
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

                    for (int i = 0; i < players.length; i++) {
                        clientWriter.println("Nickname: " + players[i].getNickname() +
                                             "  Punteggio utente: " + players[i].getUserScore() +
                                             "  Vittorie:" + players[i].getWins() +
                                             "  Sconfitte: " + players[i].getLosses() +
                                             "  Rateo: " + players[i].getRateo());
                    }
                    break;
                case 4:
                    clientSelector.putRequest(RequestMessages.SHOW_USERSCORE + " " + clientNickname);
                    response = clientSelector.getResponse();

                    if (response.equals(ClientSelector.CRASH)) {
                        clientWriter.println("Il server è offline");
                        selectorThread.interrupt();
                        return;
                    }

                    Player player = gson.fromJson(response.split(":",2)[1], Player.class);
                    clientWriter.println("Punteggio utente: " + player.getUserScore() + " Vittorie: " + player.getWins()
                        + " Sconfitte: " + player.getLosses() + " Rateo: " + player.getRateo());
                    break;
                case 5:
                    clientWriter.println("Nome amico:");

                    String challengedNickname = clientScanner.nextLine();

                    if (challengedNickname.equals(clientNickname)  || challengedNickname.isEmpty()) {
                        clientWriter.println("Valore non valido");
                        break;
                    }
                    if (challengedNickname.contains(" ")) {
                        clientWriter.println("Non sono ammessi spazi");
                        break;
                    }
                    clientSelector.putRequest(RequestMessages.CHALLENGE_FROM + " " + clientNickname + " " +
                            challengedNickname);

                    clientWriter.println("Aspettando una risposta...");
                    response = clientSelector.getResponse();

                    if (response.equals(ResponseMessages.CHALLENGE_ACCEPTED.toString())) {
                        clientWriter.println(response);
                        challengeHandler(clientNickname);
                    }
                    else if (response.equals(ClientSelector.CRASH)) {
                        clientWriter.println("Il server è offline");
                        selectorThread.interrupt();
                        return;
                    }
                    else {
                        clientWriter.println(response);
                    }

                    break;
                case 6:
                    ArrayList<String> challengers = clientSelector.getChallenges();
                    clientWriter.println("LISTA SFIDANTI");
                    int i = 0;

                    clientWriter.println(" " + i + ") Indietro");

                    for (i = 0; i < challengers.size(); i++) {
                        clientWriter.println(" " + (i+1) + ") " + challengers.get(i));
                    }

                    operation = Integer.parseInt(clientScanner.nextLine());

                    if (operation <= i && operation > 0) {
                        clientWriter.println("0) Indietro\n1) Accetta\n2) Rifiuta");

                        int choose = Integer.parseInt(clientScanner.nextLine());
                        switch (choose) {
                            case 0:
                                break;
                            case 1:
                                clientSelector.putRequest(RequestMessages.CHALLENGE_ACCEPTED + " " +
                                        challengers.get(operation - 1) + " " + clientNickname);


                                response = clientSelector.getResponse();

                                if (response.equals(ResponseMessages.CHALLENGE_REQUEST_ACCEPTED.toString())) {
                                    clientWriter.println(response);
                                    challengeHandler(clientNickname);
                                }
                                else if (response.equals(ClientSelector.CRASH)) {
                                    clientWriter.println("Il server è offline");
                                    selectorThread.interrupt();
                                    return;
                                }
                                else {
                                    clientWriter.println(response);
                                }
                                break;
                            case 2:
                                clientSelector.putRequest(RequestMessages.CHALLENGE_REFUSED + " " +
                                        challengers.get(operation - 1) + " " + clientNickname);
                                break;
                        }
                    }

                    break;
                case 7:
                    clientSelector.putRequest(RequestMessages.LOGOUT + " " + clientNickname);
                    response = clientSelector.getResponse();

                    if (response.equals(ClientSelector.CRASH)) {
                        clientWriter.println("Il server è offline");
                    }
                    else {
                        clientWriter.println(response);
                    }
                    selectorThread.interrupt();
                    break;
            }
        }
    }

    public static void challengeHandler(String clientNickname) {
        String italianWord = null;

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            String response = clientSelector.getChallengeString();
            if (response != null) {
                String tokens[] = response.substring(3).split(" ");

                if (tokens[0].equals(ResponseMessages.WORD.getDescription())) {
                    clientWriter.println("> " + tokens[1]);
                    italianWord = tokens[1];

                }
                else if (tokens[0].equals(ResponseMessages.RESULT.getDescription())) {
                    clientWriter.println("Hai tradotto correttamente " + tokens[3] + " parole, ne hai sbagliate " +
                            tokens[4] + " e non risposto a " + tokens[5] + ".\nHai totalizzato " + tokens[2] + " punti.\n" +
                            "Il tuo avversario ha totalizzato " + tokens[6] + " punti.");

                    if (tokens[1].equals("WIN")) {
                        clientWriter.println("Hai vinto. Hai guadagnato 3 punti extra, per un totale di " +
                                (Integer.parseInt(tokens[2]) + 3) + " punti");
                    }
                    else if (tokens[1].equals("LOSS")) {
                        clientWriter.println("Hai perso.");
                    }
                    else if (tokens[1].equals("TIE")) {
                        clientWriter.println("Pareggio.");
                    }

                    break;
                }
                else if (tokens[0].equals(ClientSelector.CRASH)) {
                    break;
                }
            }
            else if (italianWord == null) {
                continue;
            }

            String englishWord = null;
            try {
                if (!br.ready()) {
                    Thread.sleep(200);
                    continue;
                }
                else {
                    englishWord = br.readLine();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            if (englishWord != null) {
                clientSelector.putRequest(RequestMessages.TRANSLATION.toString() + " " + clientNickname + " " +
                        italianWord + " " + englishWord);
                italianWord = null;
            }
        }
    }

    public static String register(String nickname, String password) {
        Remote remoteObject;

        try {
            Registry registry = LocateRegistry.getRegistry(6789);
            remoteObject = registry.lookup("REGISTRATION-SERVICE");

            serverObject = (IRegistrationService) remoteObject;
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        try {
            if (serverObject != null) {
                return serverObject.registra_utente(nickname, password);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            serverObject = null;
        }

        return "Il server è offline";
    }
}
