package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.response.base.ResponseBean;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 2020-08-18
 *
 * @author Evan Wang
 * date 2020-08-18
 */
public class ServiceNumberBroadcastDeleteRequest extends NewRequestBase {
    private final Listener listener;

    public ServiceNumberBroadcastDeleteRequest(Context ctx, Listener listener) {
        super(ctx, ApiPath.serviceNumberBroadcastDelete);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        try {
            ResponseBean response = JsonHelper.getInstance().from(s, ResponseBean.class);
            if (this.listener != null && response != null && response.get_header_() != null && response.get_header_().isSuccess()) {
                String roomId = mJSONObject.getString("roomId");
                String messageId = mJSONObject.getString("messageId");
                this.listener.onSuccess(roomId, messageId);
            }
        } catch (Exception e) {
            failed(ErrCode.JSON_PARSE_FAILED, e.getMessage());
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e("");
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

    public interface Listener {
        void onSuccess(String broadcastRoomId, String messageId);

        void onFailed(String errorMessage);
    }
}
