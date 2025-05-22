//package tw.com.chainsea.ce.sdk.http.ce.request;
//
//import android.content.Context;
//import android.os.Handler;
//
//import com.google.common.collect.ImmutableMap;
//import com.google.common.collect.Maps;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.Map;
//
//import tw.com.chainsea.ce.sdk.BuildConfig;
//import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
//import tw.com.aile.sdk.bean.message.MessageEntity;
//import tw.com.chainsea.ce.sdk.lib.ErrCode;
//import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
//import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
//import tw.com.chainsea.android.common.log.CELog;
//
///**
// * current by evan on 11/18/20
// *
// * @author Evan Wang
// * @date 11/18/20
// */
//public class NewMessageSendRequest extends NewRequestBase {
//    private static final String TAG = MessageSendRequest.class.getSimpleName();
//    private Listener<MessageEntity> listener;
//    private int reTryCount = 0;
//    private Map<Integer, Long> reSendTimeTable = Maps.newHashMap(ImmutableMap.of(
//            0, 5000L,
//            1, 10000L,
//            2, 30000L,
//            3, 60000L));
//
//
//    public NewMessageSendRequest(Context ctx, Listener<MessageEntity> listener) {
//        super(ctx, ApiPath.messageSend);
//        this.listener = listener;
//
////        if (BuildConfig.DEBUG) {
////            this.reSendTimeTable = Maps.newHashMap(ImmutableMap.of(
////                    0, 10L,
////                    1, 11L,
////                    2, 12L,
////                    3, 13L));
////        }
//    }
//
//    @Override
//    protected void success(JSONObject jsonObject, String s) throws JSONException {
//        int sendNum = jsonObject.getInt("sendNum");
//        long sendTime = jsonObject.getLong("sendTime");
//
//        String messageId = mJSONObject.getString("messageId");
//        String roomId = mJSONObject.getString("messageId");
//
//        if (this.listener != null) {
////            MessageEntity entity = new MessageEntity.Builder().id(messageId)
////                    .sendNum(sendNum)
////                    .sendTime(sendTime)
////                    .roomId(roomId)
////                    .build();
////            this.listener.onSuccess(messageId, sendNum, sendTime, roomId);
//            this.listener.onSuccess(new MessageEntity.Builder()
//                    .id(messageId)
//                    .sendNum(sendNum)
//                    .sendTime(sendTime)
//                    .roomId(roomId)
//                    .build());
//        }
//    }
//
//    @Override
//    protected void failed(ErrCode code, String errorMessage) {
//        if (this.reTryCount < 12) {
//            long delayedTime = this.reSendTimeTable.get(this.reTryCount / 3);
//
//            CELog.msg("reTrySendMessage: " + mJSONObject.toString() + "\ndelayedTime: " + delayedTime + "\nreTryDateTime: " + System.currentTimeMillis());
//            reSend(delayedTime);
//        }
//
//
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
//        }
//    }
//
//    private void reSend(long delayedTime) {
//        this.reTryCount++;
//        new Handler().postDelayed(() -> this.request(getReqData()), delayedTime);
//    }
//
//
//    public abstract class Listener<T> implements ApiListener<T> {
//
//
//        @Override
//        public void onFailed(String errorMessage) {
//
//        }
//
//
//        public abstract void onFailed(T t, String errorMessage);
////        public abstract void onSuccess(String messageId, int sendNum, long sendTime, String roomId);
////
////        public abstract void onFailed(String roomId, String messageId, String errorMessage);
//
//    }
//
//
//}
