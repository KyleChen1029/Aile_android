package tw.com.chainsea.ce.sdk.bean.servicenumber;

public class FacebookFansPages {

    private String fansPageId;
    private String fansPageCode;
    private String name;
    private String fansAvatarURL;
    private String fansPageAccessToken;


    public String getFansPageId() {
        return fansPageId;
    }

    public void setFansPageId(String fansPageId) {
        this.fansPageId = fansPageId;
    }

    public String getFansPageCode() {
        return fansPageCode;
    }

    public void setFansPageCode(String fansPageCode) {
        this.fansPageCode = fansPageCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFansAvatarURL() {
        return fansAvatarURL;
    }

    public void setFansAvatarURL(String fansAvatarURL) {
        this.fansAvatarURL = fansAvatarURL;
    }

    public String getFansPageAccessToken() {
        return fansPageAccessToken;
    }

    public void setFansPageAccessToken(String fansPageAccessToken) {
        this.fansPageAccessToken = fansPageAccessToken;
    }
}
