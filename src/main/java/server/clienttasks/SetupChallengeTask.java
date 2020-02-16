package server.clienttasks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import server.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class SetupChallengeTask implements Callable<Challenge> {
    private static final String MYMEMORY = "https://api.mymemory.translated.net";
    private final static int CHALLENGE_TIMER_T2 = 60000;

    private final ArrayList<String> italianWords;
    private final ConcurrentHashMap<String, String> translatedWords;
    private final UsersGraph usersGraph;
    private final Challenge challenge;
    private final WorkersThreadpool workersThreadpool;

    /**
     * Costruisce un nuovo oggetto SetupChallengeTask che si occupa della scelta delle parole da utilizzare durante la
     * sfida e l'invio della prima parola.
     *
     * @param italianWords riferimento alla struttura dati contenente le parole in italiano.
     * @param translatedWords riferimento alla struttura dati contenente i mapping tra parole italiane e traduzioni.
     * @param usersGraph riferimento alla struttura dati contenente gli utenti di Word Quizzle.
     * @param challenge riferimento all'oggetto che incapsula lo stato della partita.
     * @param workersThreadpool riferimento all'oggetto che incapsula i principali worker di Word Quizzle.
     */
    public SetupChallengeTask(ArrayList<String> italianWords, ConcurrentHashMap<String, String> translatedWords,
                              UsersGraph usersGraph, Challenge challenge, WorkersThreadpool workersThreadpool) {
        this.italianWords = italianWords;
        this.translatedWords = translatedWords;
        this.usersGraph = usersGraph;
        this.challenge = challenge;
        this.workersThreadpool = workersThreadpool;
    }

    @Override
    public Challenge call() {
        String playerANickname = challenge.getPlayerANickname();
        String playerBNickname = challenge.getPlayerBNickname();

        Clique cliqueA = usersGraph.getClique(playerANickname);
        Clique cliqueB = usersGraph.getClique(playerBNickname);

        // entrambi gli utenti sono da questo momento considerati in gioco.
        cliqueA.getPlayerInfo().setGaming(true);
        cliqueB.getPlayerInfo().setGaming(true);

        String[] words = new String[10];
        Random randomChooser = new Random();

        for (int i = 0; i < words.length; i++) {
            // scelgo le parole italiane da spedire agli utenti.
            words[i] = italianWords.get(randomChooser.nextInt(italianWords.size()));

            if (!translatedWords.containsKey(words[i])) {
                // se la traduzione non è ancora presente nella struttura dati la richiedo al servizio esterno.
                try {
                    URL myMemoryURL = new URL(MYMEMORY + "/get?q=" + words[i] + "&langpair=it|en");
                    URLConnection connection = myMemoryURL.openConnection();

                    BufferedReader buffered = new BufferedReader(new
                            InputStreamReader(connection.getInputStream()));

                    String line;
                    StringBuilder builder = new StringBuilder();

                    while ((line = buffered.readLine()) != null) {
                        builder.append(line);
                    }

                    // ottengo la parola tradotta dal json inviato.
                    JsonElement jsonElement = JsonParser.parseString(builder.toString());
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    jsonObject = jsonObject.getAsJsonObject("responseData");

                    String translatedWord = jsonObject.get("translatedText").getAsString();
                    System.out.println("PAROLA INGLESE: " + translatedWord.toLowerCase());
                    translatedWords.putIfAbsent(words[i], translatedWord.toLowerCase());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        challenge.setWords(words);

        // spedisco la prima parola ai giocatori.
        workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.WORD + " " + words[0],
                challenge.getPlayerKey(playerANickname));
        workersThreadpool.executeSendSimpleResponseTask(ResponseMessages.WORD + " " + words[0],
                challenge.getPlayerKey(playerBNickname));

        // creo un task che andrà a settare flag di terminazione della sfida nel caso in cui scada il timer T2.
        workersThreadpool.executeTimeoutTask(() -> {
            if (challenge.timeoutEnd() < 2) {
                workersThreadpool.executeEndChallengeTask(playerANickname, playerANickname);
            }
        }, CHALLENGE_TIMER_T2);

        return challenge;
    }
}
