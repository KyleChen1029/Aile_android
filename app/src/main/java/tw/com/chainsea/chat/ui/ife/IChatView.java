package tw.com.chainsea.chat.ui.ife;


import java.util.List;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.QuickReplyItem;
import tw.com.chainsea.ce.sdk.service.listener.AgentSnatchCallback;
import tw.com.chainsea.chat.base.IBaseView;
import tw.com.chainsea.chat.keyboard.view.HadEditText;

/**
 * IChatView
 * Created by 90Chris on 2016/4/21.
 */
public interface IChatView extends IBaseView {
    void initMessageList();

    void initKeyboard();

    HadEditText.SendData getInputAreaContent();

    void clearTypedMessage();

    void displayMainMessage(boolean isNew, boolean isSendMsgDisplay, boolean scrollToBottom, MessageEntity message, boolean isFriend);

    void onRefreshMore(List<MessageEntity> msgs, MessageEntity lastMsg);

    void updateMsgStatus(String messageId, MessageStatus status);

    void updateMsgProgress(String messageId, int progress);

    void updateMsgStatus(String messageId, int sendNum, long sendTime);

    void updateMsgNotice(String messageId, int receivedNum, int readNum, int sendNum);

    void deleteMessage(String messageId);


    void highLightUnReadLine(boolean needProcess);

    void updateSendVideoProgress(boolean isCovert, String progress);




//    boolean isReplyListIsShow();


    void displayThemeMessage(String themeId, boolean isSendMsgDisplay, MessageEntity message);
    MessageEntity getThemeMessage();
    MessageEntity getNearMessage();
    void setThemeOpen(boolean isThemeOpen);
    boolean isThemeOpen();


    String getChildRoomId();
    boolean isChildRoomOpen();
    void displayChildRoomMessage(String roomId, MessageEntity message);





    void displayLogos(List<Integer> logoBeans);

    MessageEntity getFirstMsgId();

    void cancel();

    void onCleanMsgs();

    void refreshPager(ChatRoomEntity entity);

    // EVAN_FLAG 2020-02-18 1.10.0 暫時拔除 linphone
//    CallView getCallView();

    void refreshUnReadNum();

    void refreshUI();

    MessageEntity getLastMessage();

//    List<MessageEntity> sortIMessageData(List<MessageEntity> iMessageList);

    void updateSenderName(String senderName, String senderId);

    void onLoadMoreMsg(List<MessageEntity> messages);

    void transferModeDisplay(List<MessageEntity> messages);

    void scrollToTop();

    long getLastMsgTime();

    void finishActivity();

    void doChannelOnLineStatus(String roomId, MessageEntity message);

    void doChangeAgentStatus();

    void doChatRoomSnatchByAgent(boolean isSuccess, HadEditText.SendData sendData);
    void onCompleteProvisionalMemberList(List<UserProfileEntity> entities, List<String> newMemberIds);
    void onAddProvisionalMember(List<String> addNewMemberIds);
    void showErrorMsg(String msg);

    void showSendVideoProgress(String message);
    void dismissSendVideoProgress();

    void updateSendVideoProgress();
    void updateSendVideoProgress(int progress);
    void setServicedGreenStatus();

    void doServiceNumberServicedStatus(ChatRoomEntity roomEntity, AgentSnatchCallback agentSnatchCallback);

    void showErrorToast(String errorMessage);
    void showNoMoreMessage();
    void showIsNotMemberMessage(String errorMessage);
    void setSearchKeyWord(String keyWord);

    void setQuickReply(List<QuickReplyItem> quickReplyItemList);
    void onSendFacebookImageReplySuccess();
    void onSendFacebookImageReplyFailed(String errorMessage);
    void showFacebookOverTimeView();
    void setFacebookKeyboard();
    void updateFacebookStatus(MessageEntity message);
    void moveToFacebookReplyMessage(int position);
}
