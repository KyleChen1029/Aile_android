package tw.com.chainsea.ce.sdk.http.cp.respone;

import tw.com.chainsea.ce.sdk.http.cp.base.BaseResponse;

public class AccountLoginCheckCodeValidateResponse extends BaseResponse {
    private boolean validateResult;

    public boolean isValidateResult() {
        return validateResult;
    }
}

