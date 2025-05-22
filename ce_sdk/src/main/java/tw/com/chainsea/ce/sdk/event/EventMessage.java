package tw.com.chainsea.ce.sdk.event;

public class EventMessage {
    private String message;
    private Object data;

    public EventMessage(String message) {
        this.message = message;
    }

    public EventMessage(String message, Object data) {
        this.message = message;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
