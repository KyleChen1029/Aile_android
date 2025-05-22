package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.bean.broadcast.TopicEntity;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;

/**
 * current by evan on 2020-07-31
 *
 * @author Evan Wang
 * @date 2020-07-31
 */
public class TopicListRequest extends NewRequestBase {
    private ApiListener<List<TopicEntity>> listener;

    public TopicListRequest(Context ctx, ApiListener<List<TopicEntity>> listener) {
        super(ctx, ApiPath.topicList);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (jsonObject.has("items")) {
            List<TopicEntity> list = JsonHelper.getInstance().fromToList(jsonObject.getJSONArray("items").toString(), TopicEntity[].class);
            if (this.listener != null) {
                this.listener.onSuccess(list);
            }
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

}
