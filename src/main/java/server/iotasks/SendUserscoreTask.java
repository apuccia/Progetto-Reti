package server.iotasks;

import server.Player;
import server.ResponseMessages;
import server.UsersGraph;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class SendUserscoreTask implements Runnable {
    Player player;
    SelectionKey clientKey;

    public SendUserscoreTask(Player player, SelectionKey clientKey) {
        this.player = player;
        this.clientKey = clientKey;
    }

    @Override
    public void run() {
        String userscore = UsersGraph.GSON.toJson(player, Player.class);
        String response = ResponseMessages.USERSCORE.toString() + userscore;

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
