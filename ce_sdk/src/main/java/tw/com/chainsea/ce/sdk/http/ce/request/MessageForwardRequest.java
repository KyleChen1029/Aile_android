package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;

/**
 * RoomCreateRequest to create a group
 * Created by Fleming on 2016/8/8.
 */
public class MessageForwardRequest extends NewRequestBase {
    private ApiListener<List<MessageEntity>> listener;

    public MessageForwardRequest(Context ctx, ApiListener<List<MessageEntity>> listener) {
        super(ctx, ApiPath.messageForward);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            JSONArray items = jsonObject.getJSONArray("forwardDatas");
            List<MessageEntity> list = JsonHelper.getInstance().fromToList(items.toString(), MessageEntity[].class);
            this.listener.onSuccess(list);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
//        CELog.e("group create failed:" + code.getValue());
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }
}
