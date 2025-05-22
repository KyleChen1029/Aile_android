package tw.com.chainsea.ce.sdk.bean;

public class UpdateAvatarBean {
    private String type;
    private String roomId;
    private String userId;
    private String avatar;

    public UpdateAvatarBean(String type, String roomId, String userId, String avatar) {
        this.type = type;
        this.roomId = roomId;
        this.userId = userId;
        this.avatar = avatar;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
