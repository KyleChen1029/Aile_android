package tw.com.chainsea.ce.sdk.http.cp.respone;

import tw.com.chainsea.ce.sdk.http.cp.base.BaseResponse;

public class LoginResponse extends BaseResponse {
    private int validSecond;
    private String onceToken;

    public int getValidSecond() {
        return validSecond;
    }

    public String getOnceToken() {
        return onceToken;
    }
}
