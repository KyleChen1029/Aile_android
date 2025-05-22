package tw.com.chainsea.chat.qrcode.module;

public class TransTenantInfo {
    private final String tenantId;
    private final String creator;

    public TransTenantInfo(String tenantId, String creator) {
        this.tenantId = tenantId;
        this.creator = creator;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getCreator() {
        return creator;
    }
}
