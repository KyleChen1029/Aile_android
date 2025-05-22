package tw.com.chainsea.chat.service;

import static android.Manifest.permission.CAMERA;
import static tw.com.chainsea.chat.App.getContext;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserType;
import tw.com.chainsea.ce.sdk.bean.msg.content.BusinessContent;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.reference.MessageReference;
import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
import tw.com.chainsea.ce.sdk.service.ChatRoomService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.config.InvitationType;
import tw.com.chainsea.chat.config.ScannerType;
import tw.com.chainsea.chat.keyboard.media.MediaSelectorPreviewActivity;
import tw.com.chainsea.chat.lib.ActivityManager;
import tw.com.chainsea.chat.lib.ChatService;
import tw.com.chainsea.chat.searchfilter.view.activity.MemberInvitationActivity;
import tw.com.chainsea.chat.ui.activity.ChatActivity;
import tw.com.chainsea.chat.ui.activity.ChatNormalActivity;
import tw.com.chainsea.chat.ui.activity.FileExplorerActivity;
import tw.com.chainsea.chat.view.base.HomeActivity;
import tw.com.chainsea.chat.view.enlarge.EnLargeMessageActivity;
import tw.com.chainsea.chat.view.gallery.PhotoGalleryActivity;
import tw.com.chainsea.chat.view.gallery.ScreenshotsPreviewActivity;
import tw.com.chainsea.chat.view.homepage.EmployeeInformationHomepageActivity;
import tw.com.chainsea.chat.view.homepage.SelfInformationHomepageActivity;
import tw.com.chainsea.chat.view.homepage.SubscribeInformationHomepageActivity;
import tw.com.chainsea.chat.view.homepage.VisitorHomepageActivity;
import tw.com.chainsea.chat.view.service.ServiceBroadcastEditorActivity;
import tw.com.chainsea.chat.view.service.ServiceNumberAgentsManageActivity;
import tw.com.chainsea.chat.view.service.ServiceNumberManageActivity;
import tw.com.chainsea.chat.view.service.ServiceNumberManageMoreSettingsActivity;
import tw.com.chainsea.chat.view.vision.VisionType;
import tw.com.chainsea.chat.view.vision.barcode.ScannerActivity;

public class ActivityTransitionsControl {
    public static void navigationToBase(Context ctx, String whereCome, String roomId, String messageId, boolean isBindAile, String bindUrl, boolean isCollectInfo, CallBack<Intent, String> callBack) {
        callBack.complete(new Intent(ctx, HomeActivity.class)
            .putExtra(BundleKey.IS_BIND_AILE.key(), isBindAile)
            .putExtra(BundleKey.IS_COLLECT_INFO.key(), isCollectInfo)
            .putExtra(BundleKey.BIND_URL.key(), bindUrl), "");
    }

    /**
     * Navigate to picture picker previewF
     */
    public static void navigateToMediaSelectorPreview(Context ctx, boolean isOriginal, String type, String current, int maxCount, TreeMap<String, String> data, CallBack<Intent, String> callBack) {
        callBack.complete(
            new Intent(ctx, MediaSelectorPreviewActivity.class)
                .putExtra(BundleKey.IS_ORIGINAL.key(), isOriginal)
                .putExtra(BundleKey.MAX_COUNT.key(), maxCount)
                .putExtra(BundleKey.TYPE.key(), type)
                .putExtra(BundleKey.CURRENT.key(), current)
                .putExtra(BundleKey.DATA.key(), JsonHelper.getInstance().toJson(data))
            , "");
    }

    /**
     * Navigate to screenshot preview
     */
    public static void navigateToScreenshotsPreview(Context ctx, ChatRoomEntity entity, String filePath, String action, CallBack<Intent, String> callBack) {
        callBack.complete(
            new Intent(ctx, ScreenshotsPreviewActivity.class)
                .putExtra(BundleKey.ACTION.key(), action)
//                        .putExtra(BundleKey.IS_TASK.key(), isTask)
                .putExtra(BundleKey.ROOM_ID.key(), entity.getId())
                .putExtra(BundleKey.FILE_PATH.key(), filePath)
            , "");
    }

    /**
     * Navigate to the subscription account homepage
     * SUBSCRIBE_NUMBER_ID("SUBSCRIBE_NUMBER_ID"),
     * IS_SUBSCRIBE("IS_SUBSCRIBE"),
     */
    public static void navigateToSubscribePage(Context context, String subscribeNumberId, String roomId, boolean isSubscribe, CallBack<Intent, String> callBack) {
        String name = context.getClass().getSimpleName();
        if (ChatActivity.class.getSimpleName().equals(name)) {
            ActivityManager.addActivity((AppCompatActivity) context);
        }
        callBack.complete(
            new Intent(context, SubscribeInformationHomepageActivity.class)
                .putExtra(BundleKey.SUBSCRIBE_NUMBER_ID.key(), subscribeNumberId)
                .putExtra(BundleKey.ROOM_ID.key(), roomId)
                .putExtra(BundleKey.IS_SUBSCRIBE.key(), isSubscribe)
                .putExtra(BundleKey.WHERE_COME.key(), name)
            , "");
    }

    /**
     * Navigate to the create object page
     * deprecated api and page
     */
    public static void navigateToCreateBusinessTask(Context ctx, String action, ChatRoomEntity entity, BusinessContent businessContent, String[] messageIds, String path, CallBack<Intent, String> callBack) {
//        callBack.complete(
//                new Intent(ctx, CreateBusinessTaskActivity.class)
//                        .putExtra(BundleKey.ROOM_TYPE.key(), entity.getType().getName())
//                        .putExtra(BundleKey.ACTION.key(), action)
//                        .putExtra(BundleKey.MESSAGE_IDS.key(), messageIds)
//                        .putExtra(BundleKey.BUSINESS_ITEM.key(), businessContent)
//                        // 服務號用
////                .putExtra(BundleKey.TITLE.key(), entity.getName())
//                        .putExtra(BundleKey.ROOM_ID.key(), entity.getId())
//                        .putExtra(BundleKey.SERVICE_NUMBER_ID.key(), entity.getServiceNumberId())
//                        .putExtra(BundleKey.FILE_PATH.key(), path)
////                .putExtra(BundleKey.ROOM_TYPE.key(), entity.getTodoOverviewType().getName())
//
//                , ""
//        );
        callBack.complete(null, "");
    }

    public static void navigateToChat(Context ctx, ChatRoomEntity entity, String whereCome, CallBack<Intent, String> callBack) {
        boolean hasData = ChatRoomReference.getInstance().hasLocalData(entity.getId());
        if (hasData) {
            if (entity.getServiceNumberStatus().equals(ServiceNumberStatus.ROBOT_SERVICE))
                navigateToRobotChat(ctx, entity.getId(), entity, null, whereCome, callBack);
            else
                navigateToChat(ctx, entity.getId(), null, whereCome, callBack);
        } else {
            String selfId = TokenPref.getInstance(ctx).getUserId();
            ChatRoomService.getInstance().getChatRoomItem(ctx, selfId, entity.getId(), RefreshSource.REMOTE, new ServiceCallBack<ChatRoomEntity, RefreshSource>() {
                @Override
                public void complete(ChatRoomEntity entity, RefreshSource refreshSource) {
                    if (!entity.getServiceNumberStatus().equals(ServiceNumberStatus.ROBOT_SERVICE))
                        navigateToChat(ctx, entity.getId(), null, whereCome, callBack);
                    else
                        navigateToRobotChat(ctx, entity.getId(), entity, null, whereCome, callBack);
                }

                @Override
                public void error(String message) {

                }
            });
        }
    }

    public static void navigateToChat(Context ctx, String roomId, CallBack<Intent, String> callBack) {
        boolean hasData = ChatRoomReference.getInstance().hasLocalData(roomId);
        if (hasData) {
            navigateToChat(ctx, roomId, null, null, callBack);
        } else {
            String selfId = TokenPref.getInstance(ctx).getUserId();
            ChatRoomService.getInstance().getChatRoomItem(ctx, selfId, roomId, RefreshSource.REMOTE, new ServiceCallBack<ChatRoomEntity, RefreshSource>() {
                @Override
                public void complete(ChatRoomEntity entity, RefreshSource refreshSource) {
                    navigateToChat(ctx, roomId, null, null, callBack);
                }

                @Override
                public void error(String message) {
                    if (message.contains("聊天室不")) { // server 回傳聊天室錯誤的訊息（租戶對應錯亂），在本地刪除
                        ChatRoomReference.getInstance().deleteById(roomId);
                        Toast.makeText(ctx, "從資料庫移除：" + message, Toast.LENGTH_SHORT).show();
                    } else Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static void groupNavigateToChatRoom(Context ctx, String roomId, String whereCome, CallBack<Intent, String> callBack) {
        boolean hasData = ChatRoomReference.getInstance().hasLocalData(roomId);
        if (hasData) {
            navigateToChat(ctx, roomId, null, whereCome, callBack);
        } else {
            String selfId = TokenPref.getInstance(ctx).getUserId();
            ChatRoomService.getInstance().getChatRoomItem(ctx, selfId, roomId, RefreshSource.REMOTE, new ServiceCallBack<ChatRoomEntity, RefreshSource>() {
                @Override
                public void complete(ChatRoomEntity entity, RefreshSource refreshSource) {
                    navigateToChat(ctx, roomId, null, whereCome, callBack);
                }

                @Override
                public void error(String message) {
                    if (message.contains("聊天室不")) { // server 回傳聊天室錯誤的訊息（租戶對應錯亂），在本地刪除
                        ChatRoomReference.getInstance().deleteById(roomId);
                        Toast.makeText(ctx, "從資料庫移除：" + message, Toast.LENGTH_SHORT).show();
                    } else Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static void navigateToChat(Context ctx, String roomId, MessageEntity messageEntity, CallBack<Intent, String> callBack) {
        boolean hasData = ChatRoomReference.getInstance().hasLocalData(roomId);
        if (hasData) {
            navigateToChat(ctx, roomId, messageEntity, null, callBack);
        } else {
            String selfId = TokenPref.getInstance(ctx).getUserId();
            ChatRoomService.getInstance().getChatRoomItem(ctx, selfId, roomId, RefreshSource.REMOTE, new ServiceCallBack<ChatRoomEntity, RefreshSource>() {
                @Override
                public void complete(ChatRoomEntity entity, RefreshSource refreshSource) {
                    navigateToChat(ctx, entity.getId(), messageEntity, null, callBack);
                }

                @Override
                public void error(String message) {

                }
            });
        }
    }

    private static void navigateToChat(Context ctx, String roomId, MessageEntity entity, String whereCome, CallBack<Intent, String> callBack) {
        String unreadId = MessageReference.findUnreadFirstMessageIdByRoomId(null, roomId);
        ChatRoomEntity entity1 = ChatRoomReference.getInstance().findById(roomId);
        Intent intent = new Intent(ctx,
            (entity1.getType().equals(ChatRoomType.services) ||
                entity1.getType().equals(ChatRoomType.serviceMember) ||
                entity1.getType().equals(ChatRoomType.bossSecretary)) ? ChatActivity.class : ChatNormalActivity.class)
            .putExtra(BundleKey.EXTRA_SESSION_ID.key(), roomId)
            .putExtra(BundleKey.UNREAD_MESSAGE_ID.key(), unreadId);

        if (entity != null) {
            intent.putExtra(BundleKey.EXTRA_MESSAGE.key(), entity);
        }

        if (!Strings.isNullOrEmpty(whereCome)) {
            intent.putExtra(BundleKey.WHERE_COME.key(), whereCome);
        }
        callBack.complete(intent, "");
    }

    private static void navigateToRobotChat(Context ctx, String roomId, ChatRoomEntity crEntity, MessageEntity entity, String whereCome, CallBack<Intent, String> callBack) {
        Intent intent = new Intent(ctx, ChatActivity.class)
            .putExtra(BundleKey.EXTRA_SESSION_ID.key(), roomId);
        if (entity != null) {
            intent.putExtra(BundleKey.EXTRA_MESSAGE.key(), entity);
        }
        if (!Strings.isNullOrEmpty(whereCome)) {
            intent.putExtra(BundleKey.WHERE_COME.key(), whereCome);
        }
        if (crEntity != null)
            intent.putExtra(BundleKey.EXTRA_ROOM_ENTITY.key(), crEntity);
        callBack.complete(intent, "");

    }

    /**
     * Navigate to the message zoom function
     */
    public static void navigateToEnLargeMessage(Context ctx, MessageEntity entity, CallBack<Intent, String> callBack) {
        callBack.complete(
            new Intent(ctx, EnLargeMessageActivity.class)
                .putExtra(BundleKey.ROOM_ID.key(), entity.getRoomId())
                .putExtra(BundleKey.MESSAGE_ID.key(), entity.getId())
            , ""
        );
    }

    /**
     * Navigate to service account member management
     */
    public static void navigateToServiceAgentsManage(Context ctx, String broadcastRoomId, String serviceNumberId, CallBack<Intent, String> callBack) {
        callBack.complete(
            new Intent(ctx, ServiceNumberAgentsManageActivity.class)
                .putExtra(BundleKey.BROADCAST_ROOM_ID.key(), broadcastRoomId)
                .putExtra(BundleKey.SERVICE_NUMBER_ID.key(), serviceNumberId)
            , ""
        );
    }

    /**
     * New friend selection page
     */
    public static void navigateToAddMember(Context ctx, String roomId, List<UserProfileEntity> members, CallBack<Intent, String> callBack) {
        ArrayList<String> accountIds = Lists.newArrayList();
        for (int i = 0; i < members.size(); i++) {
            accountIds.add(members.get(i).getId());
        }
        callBack.complete(
            new Intent(ctx, MemberInvitationActivity.class)
                .putExtra(BundleKey.ROOM_ID.key(), roomId)
                .putExtra(BundleKey.ACCOUNT_IDS.key(), accountIds)
                .putExtra(BundleKey.ROOM_TYPE.key(), InvitationType.GroupRoom.name())
            , ""
        );
    }

    /**
     * New Provisional Member selection page
     */
    public static void navigateToAddProvisionalMember(Context ctx, String roomId, String serviceNumberId, ArrayList<String> provisionalIds, String serviceAgentId, CallBack<Intent, String> callBack) {
        callBack.complete(
            new Intent(ctx, MemberInvitationActivity.class)
                .putExtra(BundleKey.ROOM_ID.key(), roomId)
                .putExtra(BundleKey.SERVICE_NUMBER_ID.key(), serviceNumberId)
                .putExtra(BundleKey.SUBSCRIBE_AGENT_ID.key(), serviceAgentId)
                .putExtra(BundleKey.ROOM_TYPE.key(), InvitationType.ProvisionalMember.name())
                .putStringArrayListExtra(BundleKey.PROVISIONAL_MEMBER_IDS.key(), provisionalIds)
            , ""
        );
    }

    /**
     * Forward multiple messages to multiple destinations
     */
    public static void toTransfer(Context ctx, List<MessageEntity> messages, List<String> sessionIds, CallBack<Intent, String> callBack) {
        String[] messageIds = new String[]{};
        String[] roomIds = new String[]{};
        if (messages != null && !messages.isEmpty()) {
            messageIds = new String[messages.size()];
            for (int i = 0; i < messages.size(); i++) {
                messageIds[i] = messages.get(i).getId();
            }
        }

        if (sessionIds != null && !sessionIds.isEmpty()) {
            roomIds = new String[sessionIds.size()];
            for (int i = 0; i < sessionIds.size(); i++) {
                roomIds[i] = sessionIds.get(i);
            }
        }

        callBack.complete(
            new Intent(ctx, MemberInvitationActivity.class)
                .putExtra(BundleKey.TRANSEND_MSG_IDS.key(), messageIds)
                .putExtra(BundleKey.FROM_ROOM_IDS.key(), roomIds)
                .putExtra(BundleKey.ROOM_TYPE.key(), InvitationType.MessageToTransfer.name())
            , ""
        );
    }

    /**
     * Navigate to the service account homepage
     */
    public static void navigateToServiceNumberManage(Context ctx, String roomId, String serviceNumberId, CallBack<Intent, String> callBack) {
        callBack.complete(
            new Intent(ctx, ServiceNumberManageActivity.class)
                .putExtra(BundleKey.SERVICE_NUMBER_ID.key(), serviceNumberId), "");
    }

    /**
     * Navigate to the service account management more settings page
     */
    public static void navigateToServiceNumberMoreSettings(Context ctx, String serviceNumberId, CallBack<Intent, String> callBack) {
        callBack.complete(new Intent(ctx, ServiceNumberManageMoreSettingsActivity.class)
//                .putExtra(BundleKey.BROADCAST_ROOM_ID.key(), roomId)
            .putExtra(BundleKey.SERVICE_NUMBER_ID.key(), serviceNumberId), "");
    }

    /**
     * Navigate to the service account management more settings page
     * ServiceBroadcastEditorActivity
     */
    public static void navigateToServiceBroadcastEditor(Context ctx, String broadcastRoomId, String serviceNumberName, String serviceNumberId, CallBack<Intent, String> callBack) {
        callBack.complete(new Intent(ctx, ServiceBroadcastEditorActivity.class)
            .putExtra(BundleKey.TITLE.key(), serviceNumberName)
            .putExtra(BundleKey.BROADCAST_ROOM_ID.key(), broadcastRoomId)
            .putExtra(BundleKey.SERVICE_NUMBER_ID.key(), serviceNumberId), "");
    }

    /**
     * Navigate to the service account to edit the list of topics
     */
    public static void navigateToServiceTopicSelector(Context ctx, String[] topicIds, CallBack<Intent, String> callBack) {
        callBack.complete(new Intent(ctx, ServiceTopicSelectorActivity.class)
            .putExtra(BundleKey.TOPIC_IDS.key(), topicIds), "");
    }

    /**
     * Navigate to the file folder
     */
    public static void navigateToFileExplorer(Context context, int limit, CallBack<Intent, String> callBack) {
        callBack.complete(new Intent(context, FileExplorerActivity.class)
            .putExtra(BundleKey.LIMIT_MAX.key(), limit), "");
    }

    /**
     * Go to the opportunity details page
     */
    public static void navigateToBusinessDetail(Context context, String businessId, CallBack<Intent, String> callBack) {
//        Toast.makeText(context, "尚未準備完成", Toast.LENGTH_SHORT).show();
//        long max = BusinessReference.maxInteractionTime(null, businessId);
//        callBack.complete(new Intent(context, BusinessDetailActivity.class)
//                .putExtra(BundleKey.BUSINESS_ID.key(), businessId), "");
    }

    public static void navigateToSelfPage(Context context, CallBack<Intent, String> callBack) {
        String selfId = TokenPref.getInstance(context).getUserId();
        String name = context.getClass().getSimpleName();
        callBack.complete(new Intent(context, SelfInformationHomepageActivity.class)
                .putExtra(BundleKey.ACCOUNT_TYPE.key(), UserType.EMPLOYEE.name())
                .putExtra(BundleKey.ACCOUNT_ID.key(), selfId)
                .putExtra(BundleKey.WHERE_COME.key(), name)
            , "");
    }

    /**
     * Navigate to the user’s homepage, which will be split according to user type
     */
    public static void navigateToEmployeeHomePage(Context context, String accountId, UserType userType, CallBack<Intent, String> callBack) {
        String name = context.getClass().getSimpleName();
        String selfId = TokenPref.getInstance(context).getUserId();
        Intent intent = new Intent();
        intent.putExtra(BundleKey.ACCOUNT_TYPE.key(), userType.name())
            .putExtra(BundleKey.ACCOUNT_ID.key(), accountId)
            .putExtra(BundleKey.WHERE_COME.key(), name);
        if (selfId.equals(accountId)) {
            intent.setClass(context, SelfInformationHomepageActivity.class);
            callBack.complete(intent, "");
        } else {
            intent.setClass(context, EmployeeInformationHomepageActivity.class);
            String roomId = UserProfileReference.findRoomIdByAccountId(null, accountId);
            if (roomId != null) {
                callBack.complete(intent, "");
            } else { //代表還沒加好友, 先加好友在導入
                String userName = UserProfileReference.findAccountName(null, accountId);
                ChatService.getInstance().addContact(new ApiListener<String>() {
                    @Override
                    public void onSuccess(final String roomId) {
                        callBack.complete(intent, "");
                    }

                    @Override
                    public void onFailed(String errorMessage) {
                        callBack.complete(intent, "");
                    }
                }, accountId, userName);
            }
        }
    }

    /**
     * Navigate to the Visitor’s homepage, which will be split according to user type
     */
    public static void navigateToVisitorHomePage(Context context, String accountId, String roomId, UserType userType, String nickName, CallBack<Intent, String> callBack) {
        callBack.complete(new Intent(context, VisitorHomepageActivity.class)
            .putExtra(BundleKey.ACCOUNT_TYPE.key(), userType.name())
            .putExtra(BundleKey.ACCOUNT_ID.key(), accountId)
            .putExtra(BundleKey.ROOM_ID.key(), roomId)
            .putExtra(BundleKey.USER_NICKNAME.key(), nickName)
            .putExtra(BundleKey.WHERE_COME.key(), context.getClass().getSimpleName()), "");
    }

    /**
     * Navigate to the picture gallery
     */
    public static void navigateToPhotoGallery(Context context, String url, String turl, CallBack<Intent, String> callBack) {
        callBack.complete(new Intent(context, PhotoGalleryActivity.class)
            .putExtra(BundleKey.PHOTO_GALLERY_URL.key(), url)
            .putExtra(BundleKey.PHOTO_GALLERY_THUMBNAIL_URL.key(), turl), "");
    }

    public static void navigateScanner(Context context) {
        navigateScanner(context, context.getString(R.string.barcode_detector_scan_title), ScannerType.Scanner, "");
    }

    public static void navigateScannerJoinGroup(Context context) {
        navigateScanner(context, context.getString(R.string.join_group), ScannerType.JoinTenant, "");
    }

    public static void navigateScannerReScanGuarantor(Context context, String data) {
        navigateScanner(context, context.getString(R.string.join_group), ScannerType.ReScanGuarantor, data);
    }

    public static void navigateFirstJoinGroup(Context context) {
        navigateScanner(context, context.getString(R.string.join_group), ScannerType.FirstJoinTenant, "");
    }

    public static void navigateScanner(Context context, String title, ScannerType type, String data) {
        XXPermissions.with(context).permission(CAMERA).request(new OnPermissionCallback() {
            @Override
            public void onGranted(List<String> permissions, boolean all) {
                Intent intent = new Intent(getContext(), ScannerActivity.class)
                    .putExtra(BundleKey.VISION_TYPE.key(), VisionType.SCAN_BAR_CODE.name())
                    .putExtra(BundleKey.TITLE.key(), title)
                    .putExtra(BundleKey.SCANNER_TYPE.key(), type.name())
                    .setAction(Intent.ACTION_VIEW)
                    .putExtra("action", "ACTION");
                if (!Strings.isNullOrEmpty(data)) {
                    intent.putExtra(BundleKey.RE_SCAN_GUARANTOR.key(), data);
                }
                context.startActivity(intent);
            }

            @Override
            public void onDenied(List<String> permissions, boolean never) {
                Toast.makeText(context, context.getString(R.string.text_need_camera_permission), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 導航進入虛擬聊天室
     */
    public static void navigationToVirtualChat(Context context, String nickName, String userId, CallBack<Intent, String> callBack) {
        callBack.complete(new Intent(context, ChatNormalActivity.class)
            .putExtra(BundleKey.USER_NICKNAME.key(), nickName)
            .putExtra(BundleKey.USER_ID.key(), userId), "");
    }

    public interface CallBack<I extends Intent, T> {
        void complete(I i, T t);
    }
}
