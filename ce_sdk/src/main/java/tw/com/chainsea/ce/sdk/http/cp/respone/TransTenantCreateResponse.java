package tw.com.chainsea.ce.sdk.http.cp.respone;

import tw.com.chainsea.ce.sdk.http.cp.base.BaseResponse;

public class TransTenantCreateResponse extends BaseResponse {
    private String transTenantUrl;
    private String tenantId;
    private String action;

    public String getTransTenantUrl() {
        return transTenantUrl;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getAction() {
        return action;
    }
}
