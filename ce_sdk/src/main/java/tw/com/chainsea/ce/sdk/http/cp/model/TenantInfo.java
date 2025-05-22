package tw.com.chainsea.ce.sdk.http.cp.model;

public class TenantInfo {
    private int unReadNum;
    private String avatarId;
    private String tenantName;
    private String serviceUrl;
    private boolean isCommon;
    private String tenantCode;

    public int getUnReadNum() {
        return unReadNum;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public boolean isCommon() {
        return isCommon;
    }

    public String getTenantCode() {
        return tenantCode;
    }
}
