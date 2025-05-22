package tw.com.chainsea.ce.sdk.socket.cp.model;

public class GuarantorJoinAgreeContent {
    private String accountId;
    private String tenantName;
    private String name;
    private String serviceUrl;
    private String tenantCode;

    private String userId;
    public String getAccountId() {
        return accountId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public String getName() {
        return name;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
