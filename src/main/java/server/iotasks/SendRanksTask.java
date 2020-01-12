package server.iotasks;

import server.Player;
import server.UsersGraph;

import java.nio.channels.SelectionKey;
import java.util.Vector;

public class SendRanksTask implements Runnable {
    private final Player[] players;
    private final SelectionKey clientKey;

    public SendRanksTask(Player[] players, SelectionKey clientKey) {
        this.players = players;
        this.clientKey = clientKey;
    }

    @Override
    public void run() {
        UsersGraph.GSON.toJson(players);
    }
}
