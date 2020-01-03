package server;

import server.tasks.TaskAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class MainClass {
    private final static int PORT = 8888;

    public static void main(String[] args) {
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

        TaskAcceptor taskAcceptor = new TaskAcceptor(serverChannel, serverSelector);
    }
}
