package tw.com.chainsea.ce.sdk.socket.ce.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import tw.com.aile.sdk.bean.message.MessageEntity;

/**
 * current by evan on 2020-10-28
 *
 * @author Evan Wang
 * date 2020-10-28
 */
public class MessageBean {
    @SerializedName("items")
    private List<MessageEntity> items;
    @SerializedName("data")
    private MessageEntity data;

    public List<MessageEntity> getItems() {
        return items;
    }

    public MessageEntity getData() {
        return data;
    }
}
