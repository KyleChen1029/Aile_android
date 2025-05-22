package tw.com.chainsea.ce.sdk.service.listener;

import java.util.List;

import tw.com.aile.sdk.bean.message.MessageEntity;

/**
 * current by evan on 2020-03-18
 *
 * @author Evan Wang
 * @date 2020-03-18
 */
public interface RefreshMsgCallBack {
    void refreshFinish(List<MessageEntity> msgIds);
}