package tw.com.chainsea.ce.sdk.http.ce.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import tw.com.chainsea.ce.sdk.http.ce.model.Result;

public class BusinessCardUrlResponse implements Serializable {
    @SerializedName("_header_")
    private Result result;
    @SerializedName("businessCardUrl")
    private String businessCardUrl;

    public Result getResult() {
        return result;
    }

    public String getBusinessCardUrl() {
        return businessCardUrl;
    }
}