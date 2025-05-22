package tw.com.chainsea.ce.sdk.service;

import android.content.Context;

import androidx.annotation.Nullable;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.android.common.text.StringHelper;
import tw.com.chainsea.ce.sdk.SdkLib;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.ServiceNumberPrivilege;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.model.Member;
import tw.com.chainsea.ce.sdk.http.ce.model.ServiceNumber;
import tw.com.chainsea.ce.sdk.http.ce.request.FromAppointRequest;
import tw.com.chainsea.ce.sdk.http.ce.request.ServiceNumberChatroomAgentServicedRequest;
import tw.com.chainsea.ce.sdk.http.ce.request.ServiceNumberConsultListRequest;
import tw.com.chainsea.ce.sdk.http.ce.request.UserItemRequest;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.reference.ServiceNumberAgentRelReference;
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference;
import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;

/**
 * current by evan on 2020-08-19
 *
 * @author Evan Wang
 * date 2020-08-19
 */
public class ChatServiceNumberService {


    public enum ServicedType {
        AGENT_SERVICED,
        APPOINT,
        ROBOT_SERVICED,
        ROBOT_SERVICE_STOP,
    }

    public enum ServicedTransferType {
        TRANSFER(""),
        TRANSFER_COMPLETE("/complete"),
        TRANSFER_CANCEL("/cancel"),
        TRANSFER_SNATCH("/snatch");

        private final String path;

        ServicedTransferType(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }


    public static void consult(Context context , String srcRoomId , String consultRoomId , ServiceCallBack<String, RefreshSource> callBack) {
        ApiManager.doServiceNumberConsult(context, srcRoomId, consultRoomId, new ApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                if (callBack != null) {
                    callBack.complete(consultRoomId, RefreshSource.REMOTE);
                }
            }

            @Override
            public void onFailed(String errorMessage) {
                if (callBack != null) {
                    callBack.error(errorMessage);
                }
            }
        });

    }

    public static void findConsultList(Context context, Set<String> blacklist, RefreshSource source, ServiceCallBack<List<ServiceNumberEntity>, RefreshSource> callBack) {
        ApiManager.doServiceNumberConsultList(context, new ApiListener<ServiceNumberConsultListRequest.Resp>() {
            @Override
            public void onSuccess(ServiceNumberConsultListRequest.Resp resp) {
//                List<ChatRoomEntity> consultationEntities = Lists.newArrayList();
                Iterator<ServiceNumberEntity> iterator = resp.getItems().iterator();
                while (iterator.hasNext()) {
                    ServiceNumberEntity e = iterator.next();
                    if (blacklist.contains(e.getServiceNumberId()) || blacklist.contains(e.getRoomId())) {
                        iterator.remove();
                    }
//                    else {
//                        consultationEntities.add(ChatRoomEntity.Build()
//                                .id(e.getRoomId())
//                                .serviceNumberId(e.getServiceNumberId())
//                                .serviceNumberAvatarId(e.getAvatarId())
//                                .serviceNumberName(e.getName())
//                                .type(ChatRoomType.SERVICE)
//                                .build());
//                    }
                }

                if (callBack != null) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                        callBack.complete(resp.getItems(), RefreshSource.REMOTE);
                    });
                }
            }

            @Override
            public void onFailed(String errorMessage) {

            }
        });
    }

    public static void findServicedStatusAndAppoint(Context context, String roomId, ServiceCallBack<ServiceNumberChatroomAgentServicedRequest.Resp, ServicedType> callBack) {
        ApiManager.doServiceNumberChatroomAgentServiced(context, roomId, new ApiListener<ServiceNumberChatroomAgentServicedRequest.Resp>() {
            @Override
            public void onSuccess(ServiceNumberChatroomAgentServicedRequest.Resp resp) {
                //CELog.d("Kyle1 stop = "+resp.isServiceNumberOwnerStop()+", status="+resp.getServiceNumberStatus());
                if(resp.getServiceNumberStatus().equals(ServiceNumberStatus.ROBOT_SERVICE)) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(resp, ServicedType.ROBOT_SERVICED));
                }else if(resp.getServiceNumberStatus().equals(ServiceNumberStatus.ROBOT_STOP)) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(resp, ServicedType.ROBOT_SERVICE_STOP));
                } else {
                    if (ServiceNumberStatus.ON_LINE_or_TIME_OUT.contains(resp.getServiceNumberStatus())) {
                        ChatRoomReference.getInstance().updateServiceNumberAgentIdById(roomId, String.valueOf(StringHelper.getString(resp.getServiceNumberAgentId(), "")));
                    } else {
                        ChatRoomReference.getInstance().updateServiceNumberAgentIdById(roomId, "");
                    }
                    ChatRoomReference.getInstance().updateServiceNumberStatusById(roomId, resp.getServiceNumberStatus());
                    ChatRoomReference.getInstance().updateServiceNumberOwnerStopById(roomId, resp.isServiceNumberOwnerStop());
                    if (callBack != null) {
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(resp, ServicedType.AGENT_SERVICED));
                    }

                    if (ServiceNumberStatus.OFF_LINE_or_TIME_OUT.contains(resp.getServiceNumberStatus())) {
                        findAppoint(context, resp, callBack);
                    }
                }
            }

            @Override
            public void onFailed(String errorMessage) {
                if(Objects.equals(errorMessage, "聊天室已被刪除") || Objects.equals(errorMessage, "你已不是聊天室成員")) {
                    ChatRoomReference.getInstance().deleteById(roomId);
                }
                findAppoint(context, new ServiceNumberChatroomAgentServicedRequest.Resp(roomId, ServiceNumberStatus.OFF_LINE), callBack);
            }
        });
    }

    public static void findAppoint(Context context, ServiceNumberChatroomAgentServicedRequest.Resp servicedResp, ServiceCallBack<ServiceNumberChatroomAgentServicedRequest.Resp, ServicedType> callBack) {
        ApiManager.doFromAppoint(context, servicedResp.getRoomId(), new ApiListener<FromAppointRequest.Resp>() {
            @Override
            public void onSuccess(FromAppointRequest.Resp resp) {
                if (resp == null) {
                    return;
                }
                ServiceNumberChatroomAgentServicedRequest.Resp newServicedResp = new ServiceNumberChatroomAgentServicedRequest.Resp(
                        resp.getStatus(),
                        resp.getOtherFroms(),
                        resp.getLastFrom()
                );
                if (callBack != null) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(newServicedResp, ServicedType.APPOINT));
                }
            }

            @Override
            public void onFailed(String errorMessage) {

            }
        });
    }

    public static void findAppoint(Context context, String roomId, ServiceCallBack<ServiceNumberChatroomAgentServicedRequest.Resp, ServicedType> callBack) {
        ApiManager.doFromAppoint(context, roomId, new ApiListener<FromAppointRequest.Resp>() {
            @Override
            public void onSuccess(FromAppointRequest.Resp resp) {
                if (resp == null) {
                    return;
                }
                ServiceNumberChatroomAgentServicedRequest.Resp servicedResp = new ServiceNumberChatroomAgentServicedRequest.Resp(
                        resp.getRoomId(),
                        resp.getStatus(),
                        resp.getOtherFroms(),
                        resp.getLastFrom()
                );
                if (callBack != null) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(servicedResp, ServicedType.APPOINT));
                }
            }

            @Override
            public void onFailed(String errorMessage) {

            }
        });
    }

    public static void servicedTransferHandle(Context context, ServicedTransferType type, String roomId, String reason, @Nullable ServiceCallBack<String, ServicedTransferType> callBack) {
        ApiListener<String> listener = new ApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                if (callBack != null) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete("", type));
                }
            }

            @Override
            public void onFailed(String errorMessage) {
                if (callBack != null) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.error(errorMessage));
                }
            }
        };

        switch (type) {
            case TRANSFER:
                ApiManager.doServiceNumberTransfer(context, roomId, reason, listener);
                break;
            case TRANSFER_CANCEL:
                ApiManager.doServiceNumberTransferCancel(context, roomId, listener);
                break;
            case TRANSFER_COMPLETE:
                ApiManager.doServiceNumberTransferComplete(context, roomId, listener);
                break;
            case TRANSFER_SNATCH:
                ApiManager.doServiceNumberTransferSnatch(context, roomId, listener);
                break;
        }
    }

    public static void findServiceNumber(Context context, String serviceNumberId, RefreshSource source, ServiceCallBack<ServiceNumberEntity, RefreshSource> callBack) {
        if (RefreshSource.ALL_or_LOCAL.contains(source)) {
            if (callBack != null) {
                ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
                    //ServiceNumberEntity entity = ServiceNumberReference.findBroadcastServiceNumberById(null, serviceNumberId);
                    ServiceNumberEntity entity = ServiceNumberReference.findServiceNumberById(serviceNumberId);
                    if (entity != null) {
                        callBack.complete(entity, RefreshSource.LOCAL);
                    }
                });
            }
        }

        if (RefreshSource.ALL_or_REMOTE.contains(source)) {
            ApiManager.doServiceNumberItem(context, serviceNumberId, new ApiListener<ServiceNumberEntity>() {
                @Override
                public void onSuccess(ServiceNumberEntity entity) {
                    String selfId = TokenPref.getInstance(context).getUserId();
                    Set<String> memberIds = getMemberIds(selfId, entity);
                    Iterator<String> memberIdIterator = memberIds.iterator();
                    while (memberIdIterator.hasNext()) {
                        if (UserProfileReference.hasLocalData(null, memberIdIterator.next())) {
                            memberIdIterator.remove();
                        }
                    }

                    ServiceNumberReference.save(null, entity);
                    if (!memberIds.isEmpty()) {
                        handleStranger(context, Queues.newLinkedBlockingDeque(memberIds), entity, callBack);
                    } else {
                        handling(context, entity, callBack);
                    }
                }

                @Override
                public void onFailed(String errorMessage) {
                    if (callBack != null) {
                        callBack.error(errorMessage);
                    }
                }
            });
        }
    }

    public static void updateServiceNumberMember(String serviceNumberId) {
        ApiManager.doServiceNumberItem(SdkLib.getAppContext(), serviceNumberId, new ApiListener<ServiceNumberEntity>() {
            @Override
            public void onSuccess(ServiceNumberEntity entity) {
                String selfId = TokenPref.getInstance(SdkLib.getAppContext()).getUserId();
//                Set<String> memberIds = getMemberIds(selfId, entity);
                // 如果自己不在此服務號就不要存了，在此之前已經刪除本地資料，會造成永遠會有一筆自己不在服務號的資料
                List<Member> selfProfile = entity.getMemberItems().stream().filter(userProfile -> userProfile.getId().equals(selfId)).collect(Collectors.toList());
                if (selfProfile.isEmpty()) return;
                for (Member member : entity.getMemberItems()) {
                    if (selfId.equals(member.getId())) {
                        if (ServiceNumber.PrivilegeType.OWNER.equals(member.getPrivilege().name())) {
                            entity.setOwner(true);
                        } else if (ServiceNumber.PrivilegeType.MANAGER.equals(member.getPrivilege().name())) {
                            entity.setManager(true);
                        } else if (ServiceNumber.PrivilegeType.COMMON.equals(member.getPrivilege().name())) {
                            entity.setCommon(true);
                        }
                    }
                }
//                entity.getMemberItems().removeIf(userProfileEntity -> UserProfileReference.hasLoadlData(null, userProfileEntity.getId()));
                ServiceNumberReference.save(null, entity);
                ServiceNumberAgentRelReference.saveAgentsRelByServiceNumber(null, entity);
            }

            @Override
            public void onFailed(String errorMessage) {
            }
        });
    }

    protected static Set<String> getMemberIds(String selfId, ServiceNumberEntity entity) {
        List<Member> memberItems = entity.getMemberItems();
        if (memberItems == null || memberItems.isEmpty()) {
            return Sets.newHashSet();
        }
        Set<String> ids = Sets.newHashSet();
        for (Member member : memberItems) {
            String id = member.getId();
            if (!selfId.equals(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static synchronized void handleStranger(Context context, Queue<String> senderIdQueue, ServiceNumberEntity entity, ServiceCallBack<ServiceNumberEntity, RefreshSource> callBack) {
        CELog.w("MessageNewQueueController.handleStranger() ");
        if (senderIdQueue.isEmpty()) {
            ServiceNumberReference.save(null, entity);
            handling(context, entity, callBack);
            return;
        }

        String senderId = senderIdQueue.remove();
        ApiManager.getInstance().doUserItem2(context, senderId, new UserItemRequest.Listener() {
            @Override
            public void onSuccess(UserProfileEntity account) {
                DBManager.getInstance().insertFriends(account);
                handleStranger(context, senderIdQueue, entity, callBack);
            }

            @Override
            public void onFailed(String errorMessage) {
                handleStranger(context, senderIdQueue, entity, callBack);
            }
        });
    }

    private static void handling(Context context, ServiceNumberEntity entity, ServiceCallBack<ServiceNumberEntity, RefreshSource> callBack) {
        if (callBack != null) {
            ServiceNumberEntity localEntity = ServiceNumberReference.findBroadcastServiceNumberById(null, entity.getServiceNumberId());
            localEntity.setServiceNumberStat(entity.getServiceNumberStat());
//            if (entity.getMemberItems() != null) {
//                for (UserProfileEntity r : localEntity.getMemberItems()) {
//                    entity.getMemberItems().remove(r);
//                }
//                localEntity.getMemberItems().addAll(entity.getMemberItems());
//            }


            Set<String> ownerIdSet = Sets.newHashSet();
            for (Member profile : entity.getMemberItems()) {
                if (ServiceNumberPrivilege.OWNER.equals(profile.getPrivilege())) {
                    ownerIdSet.add(profile.getId());
                }
            }
            for (Member profile : localEntity.getMemberItems()) {
                if (ownerIdSet.contains(profile.getId())) {
                    profile.setPrivilege(ServiceNumberPrivilege.OWNER);
                }
            }
            callBack.complete(localEntity, RefreshSource.REMOTE);
        }
    }

    // Filter no broadcast chat room Id
    private static List<ServiceNumberEntity> filter(List<ServiceNumberEntity> list) {
        return list;
    }

//    private static void handleServiceMemberChatRoom(Context context, List<ServiceNumberEntity> entities) {
//        if (entities == null || entities.isEmpty()) {
//            return;
//        }
//        String selfId = TokenPref.getInstance(context).getUserId();
//        for (ServiceNumberEntity e : entities) {
//            if (!Strings.isNullOrEmpty(e.getServiceMemberRoomId())) {
//                String roomId = e.getServiceMemberRoomId();
//
//                ApiManager.doRoomItemInIoThread(context, roomId, selfId, new ApiListener<ChatRoomEntity>() {
//                    @Override
//                    public void onSuccess(ChatRoomEntity entity) {
//                        ChatRoomReference.getInstance().save(entity);
//                    }
//
//                    @Override
//                    public void onFailed(String errorMessage) {
//                        CELog.e(errorMessage);
//                    }
//                });
//            }
//        }
//    }
}
