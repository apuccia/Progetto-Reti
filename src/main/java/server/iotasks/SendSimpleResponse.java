package server.iotasks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class SendSimpleResponse implements Runnable {
    private final String response;
    private final SelectionKey key;

    public SendSimpleResponse(String response, SelectionKey key) {
        this.response = response;
        this.key = key;
    }

    @Override
    public void run() {
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        SocketChannel clientChannel = (SocketChannel) key.channel();

        buffer.put(response.getBytes(StandardCharsets.UTF_8));
        buffer.flip();

        try {
            clientChannel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            // l'utente a cui invio va offline.
        }

        buffer.clear();
    }
}
