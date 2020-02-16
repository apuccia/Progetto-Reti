package server.iotasks;

import server.Player;
import server.ResponseMessages;
import server.UsersGraph;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

public class SendRanksTask implements Runnable {
    private final Player[] players;
    private final SelectionKey clientKey;

    /**
     * Costruisce un nuovo oggetto SendRanksTask che si occupa di inviare la classifica dei punteggi.
     *
     * @param players array di oggetti che incapsulano le statistiche di ogni utente.
     * @param clientKey SelectionKey che identifica l'utente.
     */
    public SendRanksTask(Player[] players, SelectionKey clientKey) {
        this.players = players;
        this.clientKey = clientKey;
    }

    @Override
    public void run() {
        String ranks = UsersGraph.GSON.toJson(players, Player[].class);
        String response = ResponseMessages.RANKS.toString() + ranks;

        SocketChannel clientChannel = (SocketChannel) clientKey.channel();
        ByteBuffer buffer = (ByteBuffer) clientKey.attachment();

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        int offset = 0, maxWrite;

        while (offset != bytes.length) {
            maxWrite = Math.min(bytes.length - offset, 1024);

            buffer.put(bytes, offset, maxWrite);
            buffer.flip();
            try {
                offset += clientChannel.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            buffer.clear();
        }
    }
}
