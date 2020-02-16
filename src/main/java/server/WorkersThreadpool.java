package server;

import server.clienttasks.*;
import server.iotasks.*;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.concurrent.*;

public class WorkersThreadpool {
    // threadpool (singolo) che si occuperà di effettuare le modifiche nei file json.
    private final ExecutorService diskOperator;
    // threadpool che si occuperà di svolgere le richieste.
    private final ExecutorService workersOperator;
    // threadpool che si occuperà di spedire le risposte.
    private final ExecutorService answererOperator;
    // threadpool (singolo) che si occuperà di notificare il timeout per il termine delle partite.
    private final ScheduledExecutorService timeoutOperator;
    // riferimento alla struttura dati contenenti gli utenti di Word Quizzle.
    private final UsersGraph usersGraph;
    // struttura dati contenente le parole italiane da utilizzare durante le sfide.
    private final ArrayList<String> italianWords;
    // struttura dati contenente i mapping tra le parole italiane e le traduzioni in inglese.
    private final ConcurrentHashMap<String, String> translatedWords;
    // struttura dati contenente le partite correnti.
    private final ConcurrentHashMap<String, Future<Challenge>> currentGames;
    // struttura dati che mantiene gli utenti che sono online
    private final ConcurrentHashMap<String, InetSocketAddress> onlineUsers;

    /**
     * Costruisce un nuovo oggetto WorkersThreadpool che incapsula tutti gli oggetti necessari per svolgere le richieste
     * dei client.
     *
     * @param usersGraph struttura dati contenente gli utenti di Word Quizzle.
     * @param italianWords struttura dati contenente le parole italiane da utilizzare durante le sfide.
     */
    public WorkersThreadpool(UsersGraph usersGraph, ArrayList<String> italianWords) {
        this.diskOperator = Executors.newSingleThreadExecutor();
        this.workersOperator = Executors.newFixedThreadPool(2);
        this.answererOperator = Executors.newSingleThreadExecutor();
        timeoutOperator = Executors.newSingleThreadScheduledExecutor();

        this.usersGraph = usersGraph;
        this.italianWords = italianWords;
        translatedWords = new ConcurrentHashMap<>();
        currentGames = new ConcurrentHashMap<>();
        onlineUsers = new ConcurrentHashMap<>();
    }

    /**
     * Metodo che spedisce per l'esecuzione un nuovo task per effettuare il login di un utente.
     *
     * @param nickname nickname dell'utente che vuole fare il login.
     * @param password password dell'utente che vuole fare il login.
     * @param clientKey SelectionKey dell'utente per inviare la risposta.
     */
    public Future<String> executeLoginTask(String nickname, String password, SelectionKey clientKey) {
        return workersOperator.submit(new LoginTask(nickname, password, usersGraph, this, onlineUsers, clientKey));
    }

    /**
     * Metodo che spedisce per l'esecuzione un nuovo task per effettuare il logout di un utente.
     *
     * @param nickname nickname dell'utente che vuole fare il logout.
     * @param clientKey SelectionKey dell'utente per inviare la risposta.
     */
    public void executeLogoutTask(String nickname, SelectionKey clientKey) {
        workersOperator.execute(new LogoutTask(nickname, usersGraph, this, onlineUsers, clientKey));
    }

    /**
     * Metodo che spedisce per l'esecuzione un nuovo task per effettuare l'aggiunta di un amico.
     *
     * @param nickname nickname dell'utente che ha richiesto l'amicizia.
     * @param friendNickname nickname dell'utente da aggiungere come amico.
     * @param clientKey SelectionKey dell'utente per inviare la risposta.
     */
    public void executeAddFriendTask(String nickname, String friendNickname, SelectionKey clientKey) {
        workersOperator.execute(new AddFriendTask(nickname, friendNickname, usersGraph, this, clientKey));
    }

    /**
     * Metodo che spedisce per l'esecuzione un nuovo task per la lettura delle statistiche dell'utente.
     *
     * @param nickname nickname dell'utente che ha richiesto il punteggio utente.
     * @param clientKey SelectionKey dell'utente per inviare la risposta.
     */
    public void executeShowUserScoreTask(String nickname, SelectionKey clientKey) {
        workersOperator.execute(new ShowUserscoreTask(nickname, usersGraph, this, clientKey));
    }

    /**
     * Metodo che spedisce per l'esecuzione un nuovo task per la lettura della lista amici di un utente.
     *
     * @param nickname nickname dell'utente che vuole visualizzare la lista amici.
     * @param clientKey SelectionKey dell'utente per inviare la risposta.
     */
    public void executeShowFriendsTask(String nickname, SelectionKey clientKey) {
        workersOperator.execute(new ShowFriendsTask(nickname, usersGraph, this, clientKey));
    }

    /**
     * Metodo che spedisce per l'esecuzione un nuovo task per la lettura della classifica di un utente con i suoi amici.
     *
     * @param nickname nickname dell'utente che vuole visualizzare la classifica.
     * @param clientKey SelectionKey dell'utente per inviare la risposta.
     */
    public void executeShowRanksTask(String nickname, SelectionKey clientKey) {
        workersOperator.execute(new ShowRanksTask(nickname, usersGraph, this, clientKey));
    }

    /**
     * Metodo che spedisce per l'esecuzione un nuovo task che si occuperà dei controlli iniziali e dell'inoltro della sfida.
     *
     * @param userNickname nickname dell'utente da cui parte la sfida.
     * @param friendNickname nickname dell'utente sfidato.
     * @param clientKey SelectionKey dell'utente per inviare la risposta.
     */
    public void executeStartChallengeTask(String userNickname, String friendNickname, SelectionKey clientKey) {
        currentGames.put(userNickname, workersOperator.submit(new StartChallengeTask(userNickname, friendNickname, usersGraph, this,
                onlineUsers, clientKey)));
    }

    /**
     * Metodo che spedisce per l'esecuzione un nuovo task che si occuperà del setup iniziale a seguito dell'accettazione
     * della sfida.
     *
     * @param userNickname nickname dell'utente che ha originato la sfida.
     * @param friendNickname nickname dell'utente che era stato sfidato.
     * @param clientKey SelectionKey dell'utente a cui inviare la risposta.
     */
    public void executeSetupChallengeTask(String userNickname, String friendNickname, SelectionKey clientKey) {
        Future futureChallenge;

        if ((futureChallenge = currentGames.get(userNickname)) != null) {
            Challenge challenge;
            try {
                challenge = (Challenge) futureChallenge.get();
                if (challenge.acceptChallenge()) {
                    // sfida accettata, aggiungo anche il mapping tra l'utente sfidato e l'oggetto Challenge.
                    challenge.setPlayerKey(friendNickname, clientKey);
                    currentGames.put(friendNickname, workersOperator.submit(new SetupChallengeTask(italianWords,
                            translatedWords, usersGraph, challenge, this)));
                    executeSendSimpleResponseTask(ResponseMessages.CHALLENGE_ACCEPTED.toString(),
                            challenge.getPlayerKey(userNickname));
                    executeSendSimpleResponseTask(ResponseMessages.CHALLENGE_REQUEST_ACCEPTED.toString(), clientKey);
                }
                else {
                    executeSendSimpleResponseTask(ResponseMessages.CHALLENGE_TIMEOUTED.toString(), clientKey);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        else {
            executeSendSimpleResponseTask(ResponseMessages.USER_CRASHED.toString(), clientKey);
        }
    }

    /**
     * Metodo che spedisce per l'esecuzione un nuovo task che si occuperà di effettuare il controllo della traduzione di
     * una parola spedita da uno dei due utenti.
     *
     * @param nickname nickname dell'utente che ha inviato la traduzione.
     * @param word parola originale.
     * @param translation traduzione in inglese della parola.
     */
    public void executeWordTask(String nickname, String word, String translation) {
        Future futureChallenge;

        if ((futureChallenge = currentGames.get(nickname)) != null) {
            Challenge challenge;
            try {
                if ((challenge = (Challenge) futureChallenge.get()) != null && !challenge.isEnded()) {
                    // controllo che la partita non sia finita a seguito del timeout T2 o a causa di un crash.
                    workersOperator.execute(new WordTask(challenge, nickname, word, translation, translatedWords,
                            usersGraph, this));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Metodo che spedisce per l'esecuzione un nuovo task che si occupa di terminare una sfida.
     *
     * @param playerA nickname di uno dei due utenti che partecipano alla sfida.
     * @param playerB nickname di uno dei due utenti che partecipano alla sifda.
     */
    public void executeEndChallengeTask(String playerA, String playerB) {
        assert !playerA.equals(playerB);

        Future<Challenge> challengeA = null, challengeB = null;
        challengeA = currentGames.remove(playerA);
        challengeB = currentGames.remove(playerB);

        if (challengeA != null) {
            try {
                workersOperator.execute(new EndChallengeTask(challengeA.get(), usersGraph, this));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        else if (challengeB != null){
            try {
                workersOperator.execute(new EndChallengeTask(challengeB.get(), usersGraph, this));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Metodo che spedisce per l'esecuzione un nuovo task che si occupa di inviare una risposta affermativa/negativa al
     * client.
     *
     * @param response stringa di risposta.
     * @param clientKey SelectionKey dell'utente a cui inviare la risposta.
     */
    public void executeSendSimpleResponseTask(String response, SelectionKey clientKey) {
        workersOperator.execute(new SendSimpleResponse(response, clientKey));
    }

    /**
     * Metodo che spedisce per l'esecuzione un nuovo task che si occupa di inviare una risposta affermativa/negativa al
     * client.
     *
     * @param player oggetto che incapsula le statistiche dell'utente.
     * @param clientKey SelectionKey dell'utente a cui inviare la risposta.
     */
    public void executeSendUserscoreTask(Player player, SelectionKey clientKey) {
        workersOperator.execute(new SendUserscoreTask(player, clientKey));
    }

    /**
     * Metodo che spedisce per l'esecuzione un nuovo task che si occupa di creare/modificare il file json contenente
     * le informazioni di un utente.
     *
     * @param clique oggetto da serializzare in json.
     * @param userInfoPath path del file contenente le informazioni dell'utente.
     */
    public void executeWriteUserInfoTask(Clique clique, String userInfoPath) {
        diskOperator.execute(new WriteUserInfoTask(clique, userInfoPath));
    }

    /**
     * Metodo che spedisce per l'esecuzione un nuovo task che si occupa di creare/modificare il file json contenente
     * la lista amici di un'utente.
     *
     * @param userNickname nickname dell'utente di cui modificare la lista amici.
     * @param friends nickname dell'utente da aggiungere al file json.
     * @param userFriendlistPath path del file contenente la lista amici.
     */
    public void executeWriteUserFriendlistTask(String userNickname, String[] friends, String userFriendlistPath) {
        diskOperator.execute(new WriteFriendlistTask(userNickname, friends, userFriendlistPath));
    }

    /**
     * Metodo che spedisce per l'esecuzione un nuovo task che si occupa di inviare a un client la sua lista amici.
     *
     * @param friends Array contenente i nomi degli amici.
     * @param clientKey SelectionKey dell'utente a cui inviare la risposta.
     */
    public void executeSendFriendlistTask(String[] friends, SelectionKey clientKey) {
        answererOperator.execute(new SendFriendlistTask(friends, clientKey));
    }

    /**
     * Metodo che spedisce per l'esecuzione un nuovo task che si occupa di inviare a un client la classifica.
     *
     * @param players Array contenente le informazioni sugli amici + utente principale.
     * @param clientKey SelectionKey dell'utente a cui inviare la risposta.
     */
    public void executeSendRanksTask(Player[] players, SelectionKey clientKey) {
        answererOperator.execute(new SendRanksTask(players, clientKey));
    }

    /**
     * Metodo che spedisce per l'esecuzione un nuovo task che si occuperà di gestire i casi di scadenza del timer T1 o T2.
     *
     * @param task Runnable che implementa l'interruzione della partita.
     * @param time tempo in cui dovrà essere eseguito il task (T1 oppure T2).
     */
    public void executeTimeoutTask(Runnable task, long time) {
        timeoutOperator.schedule(task, time, TimeUnit.MILLISECONDS);
    }

    /**
     * Metodo che spedisce per l'esecuzione un nuovo task che effettua il cleanup della sfida a seguito della scadenza
     * del timeout T1.
     *
     * @param nickname nickname dell'utente che aveva originato la sfida.
     * @param challenge oggetto che incapsula le informazioni della sfida.
     */
    public void challengeRefusedTask(String nickname, Challenge challenge) {
        if (challenge.declineChallenge()) {
            currentGames.remove(nickname);

            answererOperator.execute(new SendSimpleResponse(ResponseMessages.CHALLENGE_REFUSED.toString(),
                    challenge.getPlayerKey(nickname)));
        }
    }

    /**
     * Metodo che spedisce per l'esecuzione un nuovo task che effettua il cleanup della sfida nel caso in cui
     * l'utente sfidato rifiuti.
     *
     * @param nickname nickname dell'utente che aveva originato la sfida.
     */
    public void challengeRefusedTask(String nickname) {
        Future<Challenge> futureChallenge;
        if ((futureChallenge = currentGames.get(nickname)) != null) {
            try {
                Challenge challenge = futureChallenge.get();

                if (challenge.declineChallenge()) {
                    currentGames.remove(nickname);

                    answererOperator.execute(new SendSimpleResponse(ResponseMessages.CHALLENGE_REFUSED.toString(),
                            challenge.getPlayerKey(nickname)));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Metodo che effettua il cleanup a seguito della disconnessione dal server.
     *
     * @param nickname nickname dell'utente che si è disconnesso.
     */
    public void setOffline(String nickname) {
        final String finalNickname = nickname;
        workersOperator.execute(() -> {
            // l'utente non è più online.
            onlineUsers.remove(finalNickname);

            // controllo se erano in corso delle partite.
            Future futureChallenge = currentGames.remove(finalNickname);

            if (futureChallenge != null) {
                try {
                    // nel caso in cui era in corso la fase di start o di setup aspetto
                    Challenge challenge = (Challenge) futureChallenge.get();

                    if (challenge != null && !challenge.declineChallenge()) {
                        // la partita è già nella fase di setup o in una fase avanzata.

                        if (challenge.timeoutEnd() < 2) {
                            // se la partita non era già stata terminata.
                            executeEndChallengeTask(challenge.getPlayerANickname(), challenge.getPlayerBNickname());
                        }
                    }

                    // l'utente non è più in gioco.
                    usersGraph.getClique(nickname).getPlayerInfo().setGaming(false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
