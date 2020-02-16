package server;

public enum ResponseMessages {
    USER_ALREADY_REGISTERED(0, "L'utente è già registrato a Word Quizzle"),

    USER_NOT_EXISTS(1, "Il nickname specificato non esiste"),
    WRONG_PASSWORD(2, "La password specificata è errata"),
    USER_ALREADY_LOGGED(3, "L'utente ha già effettuato il login a Word Quizzle"),

    USER_NOT_EXISTS_ADD(4, "Il nickname specificato non esiste"),
    FRIEND_ALREADY_ADDED(5, "L'utente è già negli amici"),

    FRIEND_NOT_EXISTS(6, "L'utente non è presente nella lista amici"),
    CHALLENGE_REFUSED(7, "Sfida rifiutata"),
    FRIEND_NOT_ONLINE(8, "L'amico non è online"),
    FRIEND_IN_GAME(9, "L'amico è già in partita"),

    CHALLENGE_TIMEOUTED(10, "Tempo per accettare la sfida scaduto"),
    USER_CRASHED(11, "L'utente che ha inviato la sfida si è disconnesso"),

    USER_REGISTERED(50, "L'utente è stato registrato con successo"),

    USER_LOGGED(51, "Login effettuato con successo"),

    USER_LOGOUTED(52, "Logout effettuato con successo"),

    FRIEND_ADDED(53, "Amico aggiunto con successo"),

    CHALLENGE_ACCEPTED(54, "Sfida accettata"),

    FRIENDLIST(55, ""),

    RANKS(56, ""),

    USERSCORE(57, ""),

    CHALLENGE_REQUEST_ACCEPTED(58, "Richiesta di sfida accettata"),

    WORD(59, "WORD"),

    RESULT(60, "RESULT");

    private final int code;
    private final String description;

    ResponseMessages(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code + ":" + description;
    }
}
