package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 2020-07-14
 *
 * @author Evan Wang
 * @date 2020-07-14
 */
public class TodoCompleteRequest extends NewRequestBase {
    private Listener listener;

    public TodoCompleteRequest(Context ctx, Listener listener) {
        super(ctx, ApiPath.todoComplete);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            if (JsonHelper.getInstance().has(jsonObject, "id", "status")) {
                String id = jsonObject.getString("id");
                long updateTime = jsonObject.has("updateTime") ? jsonObject.getLong("updateTime") : -1L;
                String status = jsonObject.getString("status");
                this.listener.onSuccess(id, updateTime, status);
            }
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e(errorMessage);
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }


    public interface Listener {

        void onSuccess(String todoId, long updateTime, String status);

        void onFailed(String errorMessage);

    }
}
