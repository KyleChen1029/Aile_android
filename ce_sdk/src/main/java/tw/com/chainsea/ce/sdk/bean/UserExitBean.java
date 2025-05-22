package tw.com.chainsea.ce.sdk.bean;

public class UserExitBean {
    private String roomId;
    private String title;
    private String imageUrl;
    private String userId;

    public UserExitBean(String roomId, String title, String imageUrl, String userId) {
        this.roomId = roomId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.userId = userId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
