package tw.com.chainsea.chat.messagekit.listener;

import android.view.View;
import android.widget.ImageView;

import tw.com.aile.sdk.bean.message.MessageEntity;


/**
 * current by evan on 2020-02-11
 */
public interface OnMessageControlEventListener<T extends MessageEntity> {


    void onItemClick(T entity);

    void onItemChange(T entity);

    void onInvalidAreaClick(T entity);

    void onImageClick(T entity);

    void onLongClick(T msg, int pressX, int pressY);

    void onTipClick(T entity);

    void onAvatarClick(String senderId);

    void onSubscribeAgentAvatarClick(String senderId);

    void onAtSpanClick(String userId);

    void onAvatarLoad(ImageView iv, String senderId);

    void onSendNameClick(String sendId);

    void onContentUpdate(String msgId, String formatName, String formatContent);


    void copyText(T entity);

    void replyText(T entity);

    void tranSend(T entity);

    void retry(T entity);

    void cellect(T entity);

    void shares(T entity, View image);

    void choice();

    void delete(T entity);

    void enLarge(T entity);

    void onPlayComplete(T entity);

    void onStopOtherVideoPlayback(T entity);

    void retractMsg(T entity);

    void showRePlyPanel(T entity);

    void locationMsg(T entity);

    void findReplyMessage(String messageId);

    void onVideoClick(MessageEntity entity);

    void updateReplyMessageWhenVideoDownload(String messageId);
}
