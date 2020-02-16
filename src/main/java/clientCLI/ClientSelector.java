package clientCLI;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ClientSelector implements Runnable {
    private final static int CHALLENGE_TIMER_T1 = 10000;
    public final static String CRASH = "CRASH";

    private SocketAddress address;
    private SocketChannel client;
    private SelectionKey clientKey;
    private DatagramChannel challengesChannel;
    private ByteBuffer buffer;
    private ArrayBlockingQueue<String> request, responses;
    private ConcurrentHashMap<String, Long> challenges;
    private Selector clientSelector;

    public ClientSelector() {
        address = new InetSocketAddress("Localhost", 8888);
        buffer = ByteBuffer.allocate(1024);
        request = new ArrayBlockingQueue<>(1);
        responses = new ArrayBlockingQueue<>(3);
        challenges = new ConcurrentHashMap<>();

        try {
            client = SocketChannel.open(address);
            client.configureBlocking(false);
            challengesChannel = DatagramChannel.open();
            challengesChannel.configureBlocking(false);
            challengesChannel.socket().bind(client.getLocalAddress());
            clientSelector = Selector.open();
            clientKey = client.register(clientSelector, SelectionKey.OP_READ);
            challengesChannel.register(clientSelector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (clientSelector == null) {
            responses.add(CRASH);
            return;
        }

        loop: while (!Thread.interrupted()) {
            try {
                if (clientSelector.select() >= 0) {
                    try {
                        String message = request.poll();
                        if (message != null) {
                            byte[] bytes = message.getBytes();
                            int offset = 0, maxWrite;

                            while (offset != bytes.length) {
                                maxWrite = Math.min(bytes.length - offset, 1024);

                                buffer.put(bytes, offset, maxWrite);
                                buffer.flip();
                                offset += client.write(buffer);

                                buffer.clear();
                            }

                            clientKey.interestOps(SelectionKey.OP_READ);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                responses.add(CRASH);
                break loop;
            }

            Set<SelectionKey> selectedKeys = clientSelector.selectedKeys();
            Iterator<SelectionKey> keysIterator = selectedKeys.iterator();

            while (keysIterator.hasNext()) {
                SelectionKey key = keysIterator.next();

                keysIterator.remove();
                Channel channel = key.channel();

                if (channel == client && key.isReadable()) {
                    int read = -1;
                    try {
                        read = client.read(buffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (read == -1) {
                        key.cancel();
                        responses.add(CRASH);
                        break loop;
                    }

                    String response = new String(buffer.array(), 0, read, StandardCharsets.UTF_8);
                    buffer.clear();
                    responses.add(response);
                }
                else if (channel == challengesChannel && key.isReadable()) {
                    try {
                        challengesChannel.receive(buffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                        responses.add(CRASH);
                        break loop;
                    }

                    String[] challengerRequest = new String(buffer.array(), 0, buffer.position(),
                            StandardCharsets.UTF_8).split(" ");
                    challenges.put(challengerRequest[1], Long.parseLong(challengerRequest[2]));

                    buffer.clear();
                }
            }
        }

        Set<SelectionKey> keys = clientSelector.keys();
        Iterator<SelectionKey> keysIterator = keys.iterator();

        while (keysIterator.hasNext()) {
            SelectionKey key = keysIterator.next();

            try {
                key.channel().close();
            } catch (IOException e) {
                e.printStackTrace();
                responses.add(CRASH);
            }
        }

        try {
            clientSelector.close();
        } catch (IOException e) {
            e.printStackTrace();
            responses.add(CRASH);
        }
    }

    public void putRequest(String message) {
        request.add(message);

        if (clientSelector != null) {
            clientSelector.wakeup();
        }
    }

    public String getResponse(){
        try {
            return responses.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getChallengeString() {
        try {
            return responses.poll(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<String> getChallenges() {
        ArrayList<String> availableChallenges = new ArrayList<>();

        for (String challenger : challenges.keySet()) {
            long time = challenges.get(challenger);

            if (System.currentTimeMillis() - time <= CHALLENGE_TIMER_T1) {
                availableChallenges.add(challenger);
            }
            else {
                challenges.remove(challenger);
            }
        }

        return availableChallenges;
    }
}
