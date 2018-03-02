package watershine;

public class Message {

    public enum MessageLevel {
        INFO,
        ERROR,
    }

    private MessageLevel level;
    private String message;

    public Message(MessageLevel level, String message) {
        this.level = level;
        this.message = message;
    }

    public MessageLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }
}
