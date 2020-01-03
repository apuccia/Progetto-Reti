package server.tasks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TaskAcceptor implements Runnable {
    private final ServerSocketChannel serverChannel;
    private final Selector serverSelector;
    private final TaskCreator taskCreator;

    public TaskAcceptor(ServerSocketChannel serverChannel, Selector serverSelector) {
        this.serverChannel = serverChannel;
        this.serverSelector = serverSelector;
        taskCreator = new TaskCreator();
    }

    @Override
    public void run() {
        while (true) {
            try {
                serverSelector.select();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Set<SelectionKey> selectedKeys = serverSelector.selectedKeys();
            Iterator<SelectionKey> keysIterator = selectedKeys.iterator();
            ByteBuffer input = ByteBuffer.allocateDirect(64);

            while (keysIterator.hasNext()) {
                SelectionKey key = keysIterator.next();
                keysIterator.remove();

                if (key.isAcceptable()) {
                    try {
                        SocketChannel client = serverChannel.accept();
                        client.configureBlocking(false);
                        client.register(serverSelector, SelectionKey.OP_READ);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();

                    try {
                        int bytesRead;
                        byte[] message = new byte[64];
                        StringBuilder request = new StringBuilder();

                        bytesRead = client.read(input);

                        while (bytesRead != -1) {
                            input.flip();
                            input.get(message, 0, bytesRead);
                            request.append(new String(message));
                            input.flip();

                            bytesRead = client.read(input);
                        }

                        input.clear();
                        taskCreator.createTask(request.toString(), key);

                        key.interestOps(SelectionKey.OP_WRITE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
