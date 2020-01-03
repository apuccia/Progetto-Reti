package server;

public enum ResponseMessages {
    USER_ALREADY_REGISTERED(0, "L'utente è già registrato a Word Quizzle"),
    USER_NOT_EXISTS(1, "Il nickname specificato non esiste"),
    WRONG_PASSWORD(2, "La password specificata è errata"),
    REGISTRATION_ERROR(10, "Errore nella registrazione"),

    USER_REGISTERED(50, "L'utente è stato registrato con successo"),
    USER_LOGGED(51, "Login effettuato con successo"),
    FRIEND_ADDED(52, "Amico aggiunto con successo");

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
