package tw.com.chainsea.chat.service.fcm.model;

public class Room {
    private String roomId;
    private String type;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
