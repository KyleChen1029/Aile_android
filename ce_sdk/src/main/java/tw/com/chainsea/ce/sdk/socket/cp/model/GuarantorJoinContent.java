package tw.com.chainsea.ce.sdk.socket.cp.model;

public class GuarantorJoinContent {
    private String tenantCode;
    private String tenantName;
    private String name;
    private String onceToken;
    private String userId;

    public String getTenantCode() {
        return tenantCode;
    }

    public String getTenantName() {
        return tenantName;
    }

    public String getName() {
        return name;
    }

    public String getOnceToken() {
        return onceToken;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOnceToken(String onceToken) {
        this.onceToken = onceToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
