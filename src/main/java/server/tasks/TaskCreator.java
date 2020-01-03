package server.tasks;

import server.RequestMessages;

import java.nio.channels.SelectionKey;

// factory di task.
public class TaskCreator {
    public TaskCreator() {

    }

    /**
     * Factory Method che si occupa della creazione dei task.
     * @param request messaggio di richiesta inviato dal client.
     * @param clientKey chiave associata al client.
     * @return task da sottomettere al threadpool.
     */
    public static Runnable createTask(String request, SelectionKey clientKey) {
        // effettuo il parsing della richiesta.
        String[] tokens = RequestMessages.parseRequestMessage(request);
        // ottengo il tipo di richiesta.
        RequestMessages type = RequestMessages.valueOf(tokens[0]);
        Runnable task;

        switch (type) {
            // richiesta di login
            case LOGIN:
                // creo il task passando nickname e password.
                task = new LoginTask(tokens[1], tokens[2], clientKey);
                break;
            case LOGOUT:
                // creo il task passando il nickname.
                task = new LogoutTask(tokens[1], clientKey);
                break;
            // richiesta aggiunta amico.
            case ADD_FRIEND:
                // creo il task passando il nickname dell'amico.
                task = new AddFriendTask(tokens[1], clientKey);
                break;
            case SHOW_FRIENDS:
            // richiesta visualizzazione lista amici.
                ;
            case SHOW_RANKS:
            // richiesta visualizzazione classifica.
                ;
            case CHALLENGE:
            // richiesta sfida.
                ;
            default:
                task = null;
        }

        return task;
    }
}
