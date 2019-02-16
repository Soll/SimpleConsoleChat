package chat;

import java.io.Serializable;

public class Message implements Serializable {

    private MessageType type;
    private String data;

    public Message(MessageType type) {
        this.type = type;
    }

    public Message(MessageType type, String data) {
        this.type = type;
        this.data = data;
    }

    public MessageType getType() {
        return type;
    }

    public String getData() {
        return data;
    }
}
