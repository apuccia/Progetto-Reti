package server;

public enum RequestMessages {
    REGISTER("REGISTER"),
    LOGIN("LOGIN"),
    LOGOUT("LOGOUT"),
    LOGOUT_DISCONNECT("LOGOUT_DISCONNECT"),
    ADD_FRIEND("ADD_FRIEND"),
    SHOW_FRIENDS("SHOW_FRIENDS"),
    SHOW_RANKS("SHOW_RANKS"),
    SHOW_USERSCORE("SHOW_USERSCORE"),
    CHALLENGE_FROM("CHALLENGE_FROM"),
    CHALLENGE_ACCEPTED("CHALLENGE_ACCEPTED"),
    CHALLENGE_REFUSED("CHALLENGE_REFUSED"),
    TRANSLATION("TRANSLATION");

    private final String description;

    RequestMessages(String description) {
        this.description = description;
    }

    public static String[] parseRequestMessage(String request) {
        assert request != null : "richiesta nulla";

        return request.split(" ");
    }
}
