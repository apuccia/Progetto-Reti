package server.iotasks;

import java.nio.channels.SelectionKey;

public class SendSimpleResponse implements Runnable {
    private final String response;
    private final SelectionKey key;

    public SendSimpleResponse(String response, SelectionKey key) {
        this.response = response;
        this.key = key;
    }

    @Override
    public void run() {

    }
}
