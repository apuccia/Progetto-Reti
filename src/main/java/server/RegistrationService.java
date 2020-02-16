package server;

import server.iotasks.WriteUserInfoTask;

import java.io.*;
import java.rmi.server.RemoteServer;
import java.util.concurrent.ExecutorService;

public class RegistrationService extends RemoteServer implements IRegistrationService {
    private final UsersGraph usersGraph;            // struttura dati contenente le informazioni degli utenti di
                                                    //      Word Quizzle + informazioni di supporto.
    private final WorkersThreadpool workersThreadpool;

    /**
     * Costruisce un nuovo oggetto RegistrationService per fornire via RMI il servizio di registrazione a Word Quizzle.
     * @param usersGraph Struttura dati di Word Quizzle.
     */
    public RegistrationService(UsersGraph usersGraph, WorkersThreadpool workersThreadpool) {
        this.usersGraph = usersGraph;
        this.workersThreadpool = workersThreadpool;
    }

    /**
     * Metodo che permette ad un client di registrarsi a Word Quizzle.
     * @param nickname nickname dell'utente che si vuole registrare a Word Quizzle.
     * @param password password dell'utente che si vuole registrare a Word Quizzle.
     * @return se non ci sono stati errori un messaggio di avvenuta registrazione, altrimenti un messaggio di errore.
     */
    public String registra_utente(String nickname, String password) {
        assert nickname != null : "nickname nullo";
        assert password != null : "password nulla";

        String clientPath = usersGraph.MAIN_PATH + "/" + nickname;
        File userDirectory = new File(clientPath);

        // verifico che l'utente non si sia già registrato precedentemente a Word Quizzle.
        if (!userDirectory.mkdirs()) {
            // restituisco messaggio di errore.
            return ResponseMessages.USER_ALREADY_REGISTERED.toString();
        }

        // creo l'utente.
        Clique user = new Clique(nickname, password, clientPath);

        // inserisco la cerchia di amicizie.
        if (usersGraph.insertClique(nickname, user) != null) {
            // un'altro client nel frattempo si era registrato con lo stesso nickname.
            return ResponseMessages.USER_ALREADY_REGISTERED.toString();
        }

        workersThreadpool.executeWriteUserInfoTask(new Clique(nickname, user.getHash(),
                        0, 0, 0, 0),
                user.getUserInfoPath());

        // restituisco messaggio positivo di avvenuta registrazione.
        return ResponseMessages.USER_REGISTERED.toString();
    }
}
