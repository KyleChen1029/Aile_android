package tw.com.chainsea.ce.sdk.bean;

/**
 * Created by sunhui on 2018/4/24.
 */

public class Recommend {
    private String name;
    private String avatarUrl;
    private String accountId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getId() {
        return accountId;
    }

    public void setId(String accountId) {
        this.accountId = accountId;
    }
}
