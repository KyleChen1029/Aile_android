package tw.com.chainsea.ce.sdk.http.cp.respone;

import java.util.List;

import tw.com.chainsea.ce.sdk.http.cp.base.BaseResponse;
import tw.com.chainsea.ce.sdk.http.cp.model.Configuration;
import tw.com.chainsea.ce.sdk.http.cp.model.TransTenantInfo;

public class AccountLoginDeviceCheckResponse extends BaseResponse {
    private String loginStatus;
    private String deviceId;
    private String accountId;
    private String name;
    private String tokenId;
    private int tokenValidSeconds;
    private String refreshTokenId;
    private TransTenantInfo tenantInfo;
    private Configuration configuration;
    private TransTenantInfo transTenantInfo;
    private String authToken;
    private List<RelationTenant> relationTenantArray;
    private int createTenant;
    private int joinTenant;

    public String getLoginStatus() {
        return loginStatus;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public String getTokenId() {
        return tokenId;
    }

    public int getTokenValidSeconds() {
        return tokenValidSeconds;
    }

    public String getRefreshTokenId() {
        return refreshTokenId;
    }

    public TransTenantInfo getTenantInfo() {
        return tenantInfo;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public TransTenantInfo getTransTenantInfo() {
        return transTenantInfo;
    }

    public String getAuthToken() {
        return authToken;
    }

    public List<RelationTenant> getRelationTenantArray() {
        return relationTenantArray;
    }
    public int getCreateTenant() {
        return createTenant;
    }
    public int getJoinTenant() {
        return joinTenant;
    }
}
