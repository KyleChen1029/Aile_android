package tw.com.chainsea.ce.sdk.http.ce.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Result  implements Serializable {
    private long timeCost;
    @SerializedName("success")
    private boolean isSuccess;
    private String errorMessage;

    public long getTimeCost() {
        return timeCost;
    }

    public void setTimeCost(long timeCost) {
        this.timeCost = timeCost;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}