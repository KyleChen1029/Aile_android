package tw.com.chainsea.ce.sdk.bean;

public class UpdateProfileBean {
    private String title;
    private String userId;
    private String roomId;

    public UpdateProfileBean(String title, String userId, String roomId) {
        this.title = title;
        this.userId = userId;
        this.roomId = roomId;
    }

    public String getTitle() {
        return title;
    }

    public String getUserId() {
        return userId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
