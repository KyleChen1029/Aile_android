package tw.com.chainsea.chat.presenter;

import android.content.Context;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference;
import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
import tw.com.chainsea.chat.lib.ChatService;
import tw.com.chainsea.chat.lib.Tools;
import tw.com.chainsea.chat.ui.ife.IComplaintView;

public class ComplaintPresenter {

    private final IComplaintView iView;
    private final UserProfileEntity profile;

    public ComplaintPresenter(IComplaintView iView, String userId) {
        this.iView = iView;
        profile = UserProfileReference.findById(null, userId);
    }
    public void complaintToPlatform(Context ctx, String userId, String type, String content) {
        ApiManager.doRoomComplaint(ctx, userId, type, content, new ApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                iView.dismissLoadingView();
                if(profile!=null)
                    if(!profile.isBlock())
                        iView.alertBlockDialog();
            }

            @Override
            public void onFailed(String errorMessage) {
                iView.dismissLoadingView();
                iView.showToast(errorMessage);
            }
        });
    }

    public void complaintToTeam(String content) {
        UserProfileEntity userProfileEntity = DBManager.getInstance().queryFriend(profile.getId());
        ServiceNumberEntity entity = ServiceNumberReference.findManageServiceNumber(); //get manage service number info
        if(entity != null){
            if(userProfileEntity!=null) {
                ChatService.getInstance().sendMessage(entity.getRoomId(), Tools.generateMessageId(), MessageType.TEXT, userProfileEntity.getNickName() + content, "");
                iView.alertNavigateToChatRoom(entity.getRoomId());
            }else
                iView.showToast("找不到該用戶");
        }else
            iView.showToast("找不到管理服務號");
        iView.dismissLoadingView();
    }
    public void doBlock(Context ctx) {
        if (profile != null) {
            String id = profile.getId();
            boolean isBlock = !profile.isBlock();
            ApiManager.doUserBlock(ctx, id, isBlock, new ApiListener<String>() {
                @Override
                public void onSuccess(String s) {
                    DBManager.getInstance().setFriendBlock(id, isBlock);
                    if (profile != null) {
                        profile.setBlock(isBlock);
                    }
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.ACCOUNT_REFRESH_FILTER, profile));

                    iView.showTipToast(isBlock);
                }

                @Override
                public void onFailed(String errorMessage) {
                    CELog.e(errorMessage);
                }
            });
        }else
            iView.showToast("找不到該用戶");
    }
}
