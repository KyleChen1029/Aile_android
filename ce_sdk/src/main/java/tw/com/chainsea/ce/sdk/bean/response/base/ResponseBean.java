package tw.com.chainsea.ce.sdk.bean.response.base;

import androidx.annotation.NonNull;

import java.io.Serializable;

import tw.com.chainsea.android.common.json.JsonHelper;

public abstract class ResponseBean implements AutoCloseable, Serializable {
    private static final long serialVersionUID = -6587789984908057598L;

    private _Header_ _header_;

    public _Header_ get_header_() {
        return _header_;
    }

    public void set_header_(_Header_ _header_) {
        this._header_ = _header_;
    }


    @NonNull
    @Override
    public String toString() {
        return JsonHelper.getInstance().toJson(this);
    }

    public static class _Header_ implements Serializable {
        private static final long serialVersionUID = -5447852822814394464L;
        private boolean success = false;
        private int timeCost;
        private String errorCode;
        private String errorMessage;
        private String stackTrace;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getStackTrace() {
            return stackTrace;
        }

        public void setStackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
        }

        @NonNull
        @Override
        public String toString() {
            return JsonHelper.getInstance().toJson(this);
        }
    }
}
