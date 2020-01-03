package server;

public enum RequestMessages {
    LOGIN("LOGIN"),
    ADD_FRIEND("ADD_FRIEND"),
    SHOW_FRIENDS("SHOW_FRIENDS"),
    SHOW_RANKS("SHOW_RANKS"),
    CHALLENGE("CHALLENGE");

    private final String description;

    RequestMessages(String description) {
        this.description = description;
    }

    public static String[] parseRequestMessage(String request) {
        return request.split(" ");
    }
}
