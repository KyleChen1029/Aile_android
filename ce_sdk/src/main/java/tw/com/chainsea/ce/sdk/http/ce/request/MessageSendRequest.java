package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

/**
 * MessageSendRequest to send message to server
 * Created by 90Chris on 2016/6/20.
 */
// 1、Send Message  —>>  各三次 5 , 10 , 30 ,60  ，Android 重送機制過慢，需要檢查
public class MessageSendRequest extends NewRequestBase {
    //    private static final String TAG = MessageSendRequest.class.getSimpleName();
    private final Listener<MessageEntity> listener;
//    private int reTryCount = 0;
//    private Map<Integer, Long> reSendTimeTable = Maps.newHashMap(ImmutableMap.of(
//            0, 5000L,
//            1, 10000L,
//            2, 30000L,
//            3, 60000L));


    public MessageSendRequest(Context ctx, Listener<MessageEntity> listener) {
        super(ctx, "/" + ApiPath.messageSend);
        this.listener = listener;
//        if (BuildConfig.DEBUG) {
//            this.reSendTimeTable = Maps.newHashMap(ImmutableMap.of(
//                    0, 10L,
//                    1, 11L,
//                    2, 12L,
//                    3, 13L));
//        }
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        int sendNum = jsonObject.getInt("sendNum");
        long sendTime = jsonObject.getLong("sendTime");
        String messageId = mJSONObject.getString("messageId");
        String roomId = mJSONObject.getString("roomId");
        if (this.listener != null) {
            this.listener.onSuccess(new MessageEntity.Builder()
                .id(messageId)
                .roomId(roomId)
                .sendNum(sendNum)
                .sendTime(sendTime)
                .build());

//            this.listener.onSuccess(messageId, sendNum, sendTime, roomId);
        }
    }

    @Override
    protected void failed(ErrCode reason, String errorMessage) {
        if (this.listener != null) {
            try {
                String messageId = mJSONObject.getString("messageId");
                String roomId = mJSONObject.getString("roomId");
                this.listener.onFailed(new MessageEntity.Builder()
                    .id(messageId)
                    .roomId(roomId)
                    .build(), errorMessage);
            } catch (Exception ignored) {

            }
        }


//        if (this.reTryCount < 12) {
//            long delayedTime = this.reSendTimeTable.get(this.reTryCount / 3);
//            CELog.msg("reTrySendMessage: " + mJSONObject.toString() + "\ndelayedTime: " + delayedTime + "\nreTryDateTime: " + System.currentTimeMillis());
//            reSend(delayedTime);
//        }
//        if (this.reTryCount == 12 && this.listener != null) {
//            try {
//                String messageId = mJSONObject.getString("messageId");
//                String roomId = mJSONObject.getString("messageId");
////                this.listener.onFailed(roomId, messageId, errorMessage);
//                this.listener.onFailed(new MessageEntity.Builder()
//                        .id(messageId)
//                        .roomId(roomId)
//                        .build(), errorMessage);
//            } catch (Exception e) {
//
//            }
//
//
//
////            this.listener.onFailed(this.roomId, this.messageId, errorMessage);
//        }

    }

//    private void reSend(long delayedTime) {
//        this.reTryCount++;
//        new Handler().postDelayed(() -> this.request(getReqData()), delayedTime);
//    }

    public abstract static class Listener<T> implements ApiListener<T> {


        @Override
        public void onFailed(String errorMessage) {

        }

        public abstract void onFailed(T t, String errorMessage);
//        public abstract void onSuccess(String messageId, int sendNum, long sendTime, String roomId);
//
//        public abstract void onFailed(String roomId, String messageId, String errorMessage);

    }
}
