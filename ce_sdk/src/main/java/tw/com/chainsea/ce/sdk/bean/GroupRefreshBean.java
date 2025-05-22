package tw.com.chainsea.ce.sdk.bean;

import java.util.List;

public class GroupRefreshBean {
    String sessionId;
    String title;
    List<String> deletedMemberIdsList;

    public GroupRefreshBean(String sessionId, String title, List<String> deletedMemberIdsList) {
        this.sessionId = sessionId;
        this.title = title;
        this.deletedMemberIdsList = deletedMemberIdsList;
    }


    public GroupRefreshBean(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getDeletedMemberIdsList() {
        return deletedMemberIdsList;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDeletedMemberIdsList(List<String> deletedMemberIdsList) {
        this.deletedMemberIdsList = deletedMemberIdsList;
    }
}
