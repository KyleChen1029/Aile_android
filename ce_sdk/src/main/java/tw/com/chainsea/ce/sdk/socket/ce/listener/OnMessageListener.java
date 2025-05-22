package tw.com.chainsea.ce.sdk.socket.ce.listener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.socket.client.Ack;
import io.socket.emitter.Emitter;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.socket.ce.bean.MessageBean;
import tw.com.chainsea.ce.sdk.socket.ce.bean.NoticeBean;
import tw.com.chainsea.ce.sdk.socket.ce.bean.ReceiveMessageBean;
import tw.com.chainsea.ce.sdk.socket.ce.code.NoticeCode;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 2020-10-22
 *
 * @author Evan Wang
 * @date 2020-10-22
 */
public abstract class OnMessageListener implements Emitter.Listener {

    boolean ackEnable = true;

    public OnMessageListener(boolean ackEnable) {
        this.ackEnable = ackEnable;
    }

    protected abstract void confirm(Ack ack, MessageEntity item);

    protected abstract void notice(Ack ack, NoticeCode noticeCode, JSONObject content);

    protected abstract void messageNew(Ack ack, List<MessageEntity> items);

    protected abstract void messageOffline(Ack ack, List<MessageEntity> items);

    protected abstract void callEvent(Ack ack, String data);

    protected abstract void socketError(Ack ack, String data);

    @Override
    public void call(Object... args) {
        ThreadExecutorHelper.getSocketExecutor().execute(() -> {
            Ack ack = null;
            Object obj = null;
            try {
                if (ackEnable) {
                    ack = (Ack) args[args.length - 1];
                }
            } catch (Exception e) {
//                throw new RuntimeException(e);
            }

            try {
                obj = args[0];
            } catch (Exception e) {
//                throw new RuntimeException(e);
            }

//            if (ack == null) {
//                throw new RuntimeException("ack is null");
//            }

//            if (obj == null) {
//                throw new RuntimeException("data is null");
//            }

            if (obj instanceof JSONObject) {
                try {
                    JSONObject json = (JSONObject) obj;
                    ReceiveMessageBean receiveBean = ReceiveMessageBean.socketSpecialFrom(json);
                    String data = receiveBean.getData();
                    if (data != null && !data.isEmpty() && receiveBean.getName() != null) {
                        switch (receiveBean.getName()) {
                            case MESSAGE_NEW:
                                MessageBean messageBean = JsonHelper.getInstance().from(data, MessageBean.class);
                                if (messageBean != null) {
                                    messageNew(ack, messageBean.getItems());
                                }
                                break;
                            case CONFIRM:
                                MessageEntity entity = JsonHelper.getInstance().from(data, MessageEntity.class);
                                if (entity != null) {
                                    confirm(ack, entity);
                                }
                                break;
                            case NOTICE:
                                NoticeBean noticeBean = JsonHelper.getInstance().socketSpecialFrom(data, NoticeBean.class);
                                if (noticeBean != null) {
                                    if (noticeBean.getCode() != null && noticeBean.getContent() != null) {
                                        notice(ack, noticeBean.getCode(), noticeBean.getContent());
                                    }
                                } else {
                                    CELog.e("[CE Socket] UnHandle notice:" + receiveBean.getName());
                                }
                                break;
                            case CALL_EVENT:
                                callEvent(ack, "");
                                break;
                            case SOCKET_ERROR:
                                socketError(ack, data);
                                break;
                            case MESSAGE_OFFLINE:
                                MessageBean messageOfflineBean = JsonHelper.getInstance().from(data, MessageBean.class);
                                if (messageOfflineBean != null) {
                                    messageOffline(ack, messageOfflineBean.getItems());
                                }
                                break;
                            default:
                                break;
                        }
                    } else {
                        throw new JSONException("");
                    }
                } catch (Exception e) {
                    CELog.e(e.getMessage());
                }
            } else {
                throw new RuntimeException("data instance not JSONObject ");
            }
        });
    }
}
