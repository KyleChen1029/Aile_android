package tw.com.chainsea.ce.sdk.socket.cp.model;

public class TransTenantJoinContent {
    private String tenantId;
    private String ownerId;
    private String memberId;
    private String memberName;
    private int memberCount;

    public String getTenantId() {
        return tenantId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public int getMemberCount() {
        return memberCount;
    }
}
