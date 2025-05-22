package tw.com.chainsea.chat.qrcode.module;

public class UserInfo {
    private String tenantCode;
    private String accountId;
    private String tenantName;
    private String userId;
    private String name;

    public UserInfo(String tenantCode, String accountId, String tenantName, String userId, String nickName) {
        this.tenantCode = tenantCode;
        this.accountId = accountId;
        this.tenantName = tenantName;
        this.userId = userId;
        this.name = nickName;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
