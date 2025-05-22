package tw.com.chainsea.ce.sdk.bean;


public class GroupUpgradeBean {
    String sessionId;
    String title;

    public GroupUpgradeBean(String sessionId, String title) {
        this.sessionId = sessionId;
        this.title = title;
    }


    public String getSessionId() {
        return sessionId;
    }

    public String getTitle() {
        return title;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
