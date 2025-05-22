package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

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
public class TodoUpdateRequest extends NewRequestBase {
    private Listener listener;

    public TodoUpdateRequest(Context ctx, Listener listener) {
        super(ctx, ApiPath.todoUpdate);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        long updateTime = jsonObject.getLong("updateTime");
        String id = jsonObject.getString("id");
        if (this.listener != null) {
            this.listener.onSuccess(id, updateTime);
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
        void onSuccess(String todoId, long updateTime);

        void onFailed(String errorMessage);
    }
}
