package server.clienttasks;

import server.RequestMessages;
import server.WorkersThreadpool;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TaskAcceptor implements Runnable {
    private final String request;
    private final WorkersThreadpool workersOperator;
    private final SelectionKey clientKey;
    private final static HashMap<SelectionKey, Future<String>> connectedUsers = new HashMap<>();

    /**
     * Costruisce un nuovo oggetto TaskAcceptor che riceverà la richiesta di un client e inoltrerà il task
     * appropriato al threadpool.
     *
     * @param request Richiesta da tokenizzare.
     * @param workersOperator Oggetto che fa un wrap attorno ai threadpool che si occupano dello svolgimento della richiesta,
     *                        modifica del file json dell'utente e risposta al client.
     * @param clientKey SelectionKey associata al client nel caso di login.
     */
    public TaskAcceptor(WorkersThreadpool workersOperator, String request, SelectionKey clientKey) {
        this.request = request;
        this.workersOperator = workersOperator;
        this.clientKey = clientKey;
    }

    @Override
    public void run() {
        String[] tokens = RequestMessages.parseRequestMessage(request);

        // ottengo il tipo di richiesta.
        RequestMessages type = RequestMessages.valueOf(tokens[0]);
        Runnable task = null;

        switch (type) {
            case LOGIN:
                // creo il task di login passando nickname e password dell'utente.
                // tokens[1]: nickname dell'utente.
                // tokens[2]: password dell'utente.
                connectedUsers.put(clientKey, workersOperator.executeLoginTask(tokens[1], tokens[2], clientKey));

                break;
            case LOGOUT:
                // creo il task di logout passando il nickname dell'utente.
                // tokens[1]: nickname dell'utente.
                workersOperator.executeLogoutTask(tokens[1], clientKey);

                break;
            case LOGOUT_DISCONNECT:
                // creo il task per gestire la disconnessione dal server.
                Future<String> futureNickname = connectedUsers.remove(clientKey);

                if (futureNickname != null && !futureNickname.cancel(false)) {
                    try {
                        String nickname = futureNickname.get();
                        if (nickname != null) {
                            workersOperator.setOffline(nickname);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                break;
            case ADD_FRIEND:
                // creo il task per creare una nuova amicizia passando il nickname dell'utente di partenza e il
                // nickname dell'amico.
                // tokens[1]: nickname dell'utente.
                // tokens[2]: nickname dell'amico da aggiungere.
                workersOperator.executeAddFriendTask(tokens[1], tokens[2], clientKey);

                break;
            case SHOW_USERSCORE:
                // creo il task per la visualizzazione del punteggio utente.
                // tokens[1]: nickname dell'utente.
                workersOperator.executeShowUserScoreTask(tokens[1], clientKey);

                break;
            case SHOW_FRIENDS:
                // creo il task per la richiesta visualizzazione lista amici passando il nickname dell'utente.
                // tokens[1]: nickname dell'utente.
                workersOperator.executeShowFriendsTask(tokens[1], clientKey);

                break;
            case SHOW_RANKS:
                // creo il task per la richiesta visualizzazione classifica passando il nickname dell'utente.
                // tokens[1]: nickname dell'utente.
                workersOperator.executeShowRanksTask(tokens[1], clientKey);

                break;
            case CHALLENGE_FROM:
                // creo il task per la sfida passando il nickname dell'utente sfidante e dello sfidato.
                // tokens[1]: nickname dell'utente che origina la sfida.
                // tokens[2]: nickname dell'utente sfidato.
                workersOperator.executeStartChallengeTask(tokens[1], tokens[2], clientKey);

                break;
            case CHALLENGE_ACCEPTED:
                // creo il task per il setup della partita.
                // tokens[1]: nickname dell'utente che ha originato la sfida.
                // tokens[2]: nickname dell'utente sfidato.
                workersOperator.executeSetupChallengeTask(tokens[1], tokens[2], clientKey);

                break;
            case CHALLENGE_REFUSED:
                // elimino la partita.
                // tokens[1]: nickname dell'utente che aveva proposto la sfida.
                workersOperator.challengeRefusedTask(tokens[1]);

                break;
            case TRANSLATION:
                // creo il task per il controllo della parola.
                // tokens[1]: nickname dell'utente che ha inviato la traduzione
                // tokens[2]: parola italiana da tradurre
                // tokens[3]: traduzione proposta.
                workersOperator.executeWordTask(tokens[1], tokens[2], tokens[3]);

                break;
            default:
                task = null;
        }

        assert task != null : "Task nullo";
    }
}
