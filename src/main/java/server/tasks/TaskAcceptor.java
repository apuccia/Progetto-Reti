package server.tasks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class TaskAcceptor implements Runnable {
    private final ServerSocketChannel serverChannel; // channel per l'accettazione delle connessioni.
    private final Selector serverSelector; // selettore per le tutti i tipi di richieste.

    /**
     * Costruisce un nuovo oggetto TaskAcceptor che riceverà le richieste dai client e inoltrerà i relativi task al threadpool.
     * @param serverChannel channel su cui verranno inviate le richieste di connessione.
     * @param serverSelector selettore per la scelta dei channel pronti alla lettura.
     */
    public TaskAcceptor(ServerSocketChannel serverChannel, Selector serverSelector) {
        this.serverChannel = serverChannel;
        this.serverSelector = serverSelector;
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
                    // richiesta di connessione.
                    try {
                        SocketChannel client = serverChannel.accept();
                        client.configureBlocking(false);
                        client.register(serverSelector, SelectionKey.OP_READ);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if (key.isReadable()) {
                    // richiesta di un servizio di Word Quizzle.
                    SocketChannel client = (SocketChannel) key.channel();

                    try {
                        int bytesRead;
                        byte[] message = new byte[64];
                        StringBuilder request = new StringBuilder();

                        bytesRead = client.read(input);

                        while (bytesRead != -1) {
                            input.flip();
                            input.get(message, 0, bytesRead);
                            request.append(new String(message, 0, bytesRead, StandardCharsets.UTF_8));
                            input.flip();

                            bytesRead = client.read(input);
                        }

                        input.clear();
                        // ottengo il task
                        Runnable task = TaskCreator.createTask(request.toString(), key);

                        key.interestOps(SelectionKey.OP_WRITE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
