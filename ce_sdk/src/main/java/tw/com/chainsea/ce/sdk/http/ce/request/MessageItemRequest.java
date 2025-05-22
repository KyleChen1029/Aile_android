package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;

/**
 * current by evan on 2020-01-02
 */
public class MessageItemRequest extends NewRequestBase {
    private ApiListener<MessageEntity> listener;

    public MessageItemRequest(Context ctx, ApiListener<MessageEntity> listener) {
        super(ctx, "/" + ApiPath.messageItem);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            try {
                MessageEntity entity = JsonHelper.getInstance().from(jsonObject.toString(), MessageEntity.class);
                this.listener.onSuccess(entity);
            } catch (Exception e) {
                this.listener.onFailed(e.getMessage());
            }
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    @EqualsAndHashCode(callSuper = false)
//    @Builder(builderMethodName = "Build", toBuilder = true)
//    public static class Resp extends ResponseBean implements Serializable {
//
//
//        @Override
//        public void close() throws Exception {
//
//        }
//    }

}
