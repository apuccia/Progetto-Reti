package server.clienttasks;

import server.RequestMessages;
import server.WorkersThreadpool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class TaskAcceptor implements Runnable {
    private final SelectionKey clientKey; // channel per l'accettazione delle connessioni.
    private final ByteBuffer input;
    private final WorkersThreadpool workersOperator;

    /**
     * Costruisce un nuovo oggetto TaskAcceptor che riceverà le richieste dai client e inoltrerà i relativi task al threadpool.
     */
    public TaskAcceptor(WorkersThreadpool workersOperator, SelectionKey clientKey) {
        this.clientKey = clientKey;
        input = ByteBuffer.allocateDirect(64);
        this.workersOperator = workersOperator;
    }

    @Override
    public void run() {
        try {
            int bytesRead;
            byte[] message = new byte[64];
            StringBuilder request = new StringBuilder();
            SocketChannel clientChannel = (SocketChannel) clientKey.channel();

            bytesRead = clientChannel.read(input);
            while (bytesRead != -1) {
                input.flip();
                input.get(message, 0, bytesRead);
                request.append(new String(message, 0, bytesRead, StandardCharsets.UTF_8));
                input.flip();

                bytesRead = clientChannel.read(input);
            }

            input.clear();

            String[] tokens = RequestMessages.parseRequestMessage(request.toString());

            // ottengo il tipo di richiesta.
            RequestMessages type = RequestMessages.valueOf(tokens[0]);
            Runnable task;

            switch (type) {
                // richiesta di login
                case LOGIN:
                    // creo il task passando nickname e password.
                    workersOperator.executeLoginTask(tokens[1], tokens[2], clientKey);

                    clientKey.interestOps(SelectionKey.OP_WRITE);
                    break;
                case LOGOUT:
                    // creo il task passando il nickname.
                    workersOperator.executeLogoutTask(tokens[1]);

                    clientKey.interestOps(SelectionKey.OP_WRITE);
                    break;
                // richiesta aggiunta amico.
                case ADD_FRIEND:
                    // creo il task passando il nickname dell'amico.
                    workersOperator.executeAddFriendTask(tokens[1], tokens[2]);

                    clientKey.interestOps(SelectionKey.OP_WRITE);
                    break;
                case SHOW_FRIENDS:
                    // richiesta visualizzazione lista amici.
                    workersOperator.executeShowFriendsTask(tokens[1]);

                    clientKey.interestOps(SelectionKey.OP_WRITE);
                    break;
                case SHOW_RANKS:
                    // richiesta visualizzazione classifica.
                    workersOperator.executeShowRanksTask(tokens[1]);

                    clientKey.interestOps(SelectionKey.OP_WRITE);
                    break;
                case CHALLENGE:
                    // richiesta sfida.
                    workersOperator.executeChallenge(tokens[0], tokens[1]);

                    break;
                default:
                    task = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
