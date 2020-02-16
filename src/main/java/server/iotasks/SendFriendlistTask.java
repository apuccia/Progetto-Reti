package server.iotasks;

import server.ResponseMessages;
import server.UsersGraph;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class SendFriendlistTask implements Runnable {
    private final String[] friends;
    private final SelectionKey clientKey;

    /**
     * Costruisce un nuovo oggetto SendFriendlistTask che si occuperà dell'invio della lista amici di un client nel
     * formato json.
     *
     * @param friends array contenente i nickname degli amici.
     * @param clientKey SelectionKey che identifica l'utente.
     */
    public SendFriendlistTask(String[] friends, SelectionKey clientKey) {
        this.friends = friends;
        this.clientKey = clientKey;
    }

    @Override
    public void run() {
        String friendlist = UsersGraph.GSON.toJson(friends, String[].class);
        String response = ResponseMessages.FRIENDLIST.toString() + friendlist;

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
