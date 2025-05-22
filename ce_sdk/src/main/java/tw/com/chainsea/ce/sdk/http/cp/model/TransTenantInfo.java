package tw.com.chainsea.ce.sdk.http.cp.model;

import java.util.List;

public class TransTenantInfo {
    private List<TransMember> transMemberArray;
    private String tenantId;

    public List<TransMember> getTransMembers() {
        return transMemberArray;
    }

    public String getTenantId() {
        return tenantId;
    }
}
