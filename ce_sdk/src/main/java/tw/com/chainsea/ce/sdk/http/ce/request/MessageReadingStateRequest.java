package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.response.base.ResponseBean;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;

/**
 * current by evan on 2020-04-30
 *
 * @author Evan Wang
 * date 2020-04-30
 */
public class MessageReadingStateRequest extends NewRequestBase {
    private ApiListener<List<Resp.Item>> listener;

    public MessageReadingStateRequest(Context ctx, ApiListener<List<Resp.Item>> listener) {
        super(ctx, ApiPath.messageReadingstate);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            if (jsonObject.has("items")) {
                Resp response = JsonHelper.getInstance().from(s, Resp.class);
                this.listener.onSuccess(response.getItems());
            }
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

    public static class Resp extends ResponseBean {
        private static final long serialVersionUID = -1834640053126788850L;

        @SerializedName("items")
        List<Item> items;

        @Override
        public void close() throws Exception {

        }

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }

        public static class Item {
            String id;
            String roomId;
            int sendNum;
            int receivedNum;
            int readedNum;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getRoomId() {
                return roomId;
            }

            public void setRoomId(String roomId) {
                this.roomId = roomId;
            }

            public int getSendNum() {
                return sendNum;
            }

            public void setSendNum(int sendNum) {
                this.sendNum = sendNum;
            }

            public int getReceivedNum() {
                return receivedNum;
            }

            public void setReceivedNum(int receivedNum) {
                this.receivedNum = receivedNum;
            }

            public int getReadedNum() {
                return readedNum;
            }

            public void setReadedNum(int readedNum) {
                this.readedNum = readedNum;
            }
        }
    }
}
