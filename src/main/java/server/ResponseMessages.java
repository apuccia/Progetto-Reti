package server;

public enum ResponseMessages {
    USER_ALREADY_REGISTERED(0, "L'utente è già registrato a Word Quizzle"),
    USER_NOT_EXISTS(1, "Il nickname specificato non esiste"),
    WRONG_PASSWORD(2, "La password specificata è errata"),
    USER_ALREADY_LOGGED(3, "L'utente ha già effettuato il login a Word Quizzle"),
    FRIEND_ALREADY_ADDED(4, "L'utente è già negli amici"),
    FRIEND_NOT_EXISTS(5, "L'utente non è presente nella lista amici"),
    CHALLENGE_REFUSED(6, "Sfida rifiutata"),
    REGISTRATION_ERROR(10, "Errore nella registrazione"),

    USER_REGISTERED(50, "L'utente è stato registrato con successo"),
    USER_LOGGED(51, "Login effettuato con successo"),
    USER_LOGOUTED(52, "Logout effettuato con successo"),
    FRIEND_ADDED(53, "Amico aggiunto con successo"),
    CHALLENGE_ACCEPTED(54, "Sfida accettata");

    private final int code;
    private final String description;

    ResponseMessages(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String toString() {
        return code + ": " + description;
    }
}
