package tw.com.chainsea.ce.sdk.http.cp.respone;

import tw.com.chainsea.ce.sdk.http.cp.base.BaseResponse;
import tw.com.chainsea.ce.sdk.http.cp.model.TenantInfo;

public class TransTenantActiveResponse extends BaseResponse {
    private TenantInfo tenantInfo;

    public TenantInfo getTenantInfo() {
        return tenantInfo;
    }
}