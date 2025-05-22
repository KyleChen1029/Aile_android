package tw.com.chainsea.ce.sdk.service;

import static tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_SERVICE_NUMBER_ACTIVE_LIST;
import static tw.com.chainsea.ce.sdk.http.ce.model.LastMessageType.ContentType.TEXT;
import static tw.com.chainsea.ce.sdk.service.ChatServiceNumberService.ServicedTransferType.TRANSFER_SNATCH;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.SdkLib;
import tw.com.chainsea.ce.sdk.bean.BadgeDataModel;
import tw.com.chainsea.ce.sdk.bean.CrowdEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType;
import tw.com.chainsea.ce.sdk.config.AppConfig;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.model.LastMessageType;
import tw.com.chainsea.ce.sdk.http.ce.model.message.AtContent;
import tw.com.chainsea.ce.sdk.http.ce.request.SyncContactRequest;
import tw.com.chainsea.ce.sdk.http.ce.response.RoomRecentRsp;
import tw.com.chainsea.ce.sdk.network.NetworkManager;
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse;
import tw.com.chainsea.ce.sdk.network.model.common.RequestHeader;
import tw.com.chainsea.ce.sdk.network.model.request.RobotChatRoomSnatchRequest;
import tw.com.chainsea.ce.sdk.network.model.request.ServiceNumberSnatchRequest;
import tw.com.chainsea.ce.sdk.network.model.response.RobotServiceResponse;
import tw.com.chainsea.ce.sdk.network.services.RobotService;
import tw.com.chainsea.ce.sdk.network.services.ServiceNumberTransfer;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.reference.MessageReference;
import tw.com.chainsea.ce.sdk.service.listener.ApiCallback;
import tw.com.chainsea.ce.sdk.service.listener.RoomRecentCallBack;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;

/**
 * current by evan on 2020-02-18
 * <p>
 * 1、Check the local database first, and call back according to the source.
 * 2、If it is an API, compare the data with local data
 * 3、The API responds first to update the UI.
 * 4、When saving local data, update the UI one by one.
 * 5、If the API fails, the local data will be directly called back.
 * </p>
 *
 * @version 1.10.0
 */
public class ChatRoomService {

    private static ChatRoomService INSTANCE;

    public static ChatRoomService getInstance() {
        if (INSTANCE == null) {
            synchronized (ChatRoomService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ChatRoomService();
                }
            }
        }
        return INSTANCE;
    }

    long useTime = System.currentTimeMillis();

    public List<ChatRoomEntity> filterByChatRoomSource(Context context, ChatRoomSource source, List<ChatRoomEntity> entities) {
        List<ChatRoomEntity> mainRoomEntities = Lists.newArrayList();
        List<ChatRoomEntity> serviceRoomEntities = Lists.newArrayList();
        switch (source) {
            case ALL:
                useTime = System.currentTimeMillis();
                Collections.sort(entities);
                return entities;
            case SERVICE:
            case MAIN:
                for (ChatRoomEntity entity : entities) {
                    if (!ChatRoomType.broadcast.equals(entity.getType())) {
                        if (!entity.isService(context) && !ChatRoomType.serviceMember.equals(entity.getType())) {
                            mainRoomEntities.add(entity);
                        } else {
                            if (ChatRoomType.serviceMember.equals(entity.getType()) && ChatRoomSource.MAIN.equals(entity.getListClassify())) {
                                mainRoomEntities.add(entity);
                            } else {
                                serviceRoomEntities.add(entity);
                            }
                        }
                    }
                }
                break;
        }

        if (ChatRoomSource.MAIN.equals(source)) {
            useTime = System.currentTimeMillis();
            Collections.sort(mainRoomEntities);
            return mainRoomEntities;
        } else if (ChatRoomSource.SERVICE.equals(source)) {
            useTime = System.currentTimeMillis();
            Collections.sort(serviceRoomEntities);
            return serviceRoomEntities;
        }

        Collections.sort(entities);
        return entities;
    }


    /**
     * TODO 取得聊天室 Cell 未讀
     * Take all unread quantity
     */
    public Map<ChatRoomSource, BadgeDataModel> getBadge(Context context) {
        return getBadge(context, "");
    }

    public Map<ChatRoomSource, BadgeDataModel> getBadge(Context context, String roomId) {
        String selfId = TokenPref.getInstance(context).getUserId();
        Map<ChatRoomSource, BadgeDataModel> badgeData = ChatRoomReference.getInstance().getRoomUnreadNumber(selfId);
        TodoService.getExpiredCount(context);
        badgeData.get(ChatRoomSource.MAIN).setRoomId(roomId);
        badgeData.get(ChatRoomSource.SERVICE).setRoomId(roomId);
        // TodoReference
//        UserPref.getInstance(context).setBrand(badgeData.get(ChatRoomSource.ALL).getUnReadNumber());
        EventBusUtils.sendEvent(new EventMsg(MsgConstant.UPDATE_ALL_BADGE_NUMBER_EVENT, badgeData.get(ChatRoomSource.ALL)));
        EventBusUtils.sendEvent(new EventMsg(MsgConstant.UPDATE_MAIN_BADGE_NUMBER_EVENT, badgeData.get(ChatRoomSource.MAIN)));
        EventBusUtils.sendEvent(new EventMsg(MsgConstant.UPDATE_SERVICE_BADGE_NUMBER_EVENT, badgeData.get(ChatRoomSource.SERVICE)));
        return badgeData;
    }


    /**
     * Retrieve all chat rooms of related objects
     */
    public void getBusinessMeRelRoomList(Context context, String userId, String businessId, RefreshSource source, @Nullable ServiceCallBack<List<ChatRoomEntity>, RefreshSource> callBack) {
        if (RefreshSource.ALL_or_LOCAL.contains(source)) {
            ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
                List<ChatRoomEntity> localEntities = ChatRoomReference.getInstance().findAllBusinessRoomByBusinessId(userId, businessId);
                if (callBack != null) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(localEntities, RefreshSource.LOCAL));
                }
            });
        }
    }


    public void tailAllLocalRoomEntitiesInvoke(Context context, boolean isDown, String userId, int page, int limit, @Nullable ServiceCallBack<List<ChatRoomEntity>, RefreshSource> callBack) {
        if (isDown) {
            return;
        }
        List<ChatRoomEntity> localEntities = ChatRoomReference.getInstance().findAllByChatRoomsAndExcludeType(userId, ChatRoomType.broadcast, page, limit, true, false, true);
        boolean down = localEntities.isEmpty();
        callBack.complete(localEntities, RefreshSource.REMOTE);
        tailAllLocalRoomEntitiesInvoke(context, down, userId, page + 1, limit, callBack);
    }


    /**
     * 從 Local DataBase 先取現有聊天室列表的資料
     *
     * @param context
     * @param roomSource
     * @param callBack
     */
    public void getChatRoomEntitiesFromDb(Context context, ChatRoomSource roomSource, @Nullable ServiceCallBack<List<ChatRoomEntity>, RefreshSource> callBack) {

        String userId = TokenPref.getInstance(context).getUserId();
        getBadge(context);
        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
            List<ChatRoomEntity> localEntities = ChatRoomReference.getInstance().findAllChatRoomSource(roomSource, userId);
            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(localEntities, RefreshSource.LOCAL));
        });
//        int size = ChatRoomReference.getInstance().getChatRoomTableSize(roomSource);
//        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> tailLocalRoomEntitiesInvoke(context, size,false, roomSource, userId, 0, callBack));
    }

    public void snatchRobotServicingAPI(Context ctx, String roomId, @Nullable RoomRecentCallBack<RobotServiceResponse, RefreshSource> callBack) {
        NetworkManager.INSTANCE.provideRetrofit(ctx).create(RobotService.class)
            .snatchRobotServicing(
                new RobotChatRoomSnatchRequest(
                    SyncContactRequest.PAGE_SIZE,
                    0L,
                    new RequestHeader(
                        AppConfig.tokenForNewAPI,
                        AppConfig.LANGUAGE
                    ),
                    roomId
                )
            ).enqueue(new Callback<RobotServiceResponse>() {
                @Override
                public void onResponse(@NonNull Call<RobotServiceResponse> call, @NonNull Response<RobotServiceResponse> response) {
                    if (response.body() != null)
                        if (callBack != null)
                            callBack.complete(response.body(), RefreshSource.ROBOT);
                }

                @Override
                public void onFailure(@NonNull Call<RobotServiceResponse> call, @NonNull Throwable t) {
                    if (callBack != null)
                        callBack.error(t.getMessage());
                }
            });
    }

    public void snatchAgentServicing(Context context, String roomId, RoomRecentCallBack<CommonResponse<Object>, ChatServiceNumberService.ServicedTransferType> callBack) {
        NetworkManager.INSTANCE.provideRetrofit(context).create(ServiceNumberTransfer.class).snatchAgentServicing(new ServiceNumberSnatchRequest(roomId)).enqueue(
            new Callback<CommonResponse<Object>>() {
                @Override
                public void onResponse(@NonNull Call<CommonResponse<Object>> call, @NonNull Response<CommonResponse<Object>> response) {
                    if (response.body() != null) {
                        callBack.complete(response.body(), TRANSFER_SNATCH);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CommonResponse<Object>> call, @NonNull Throwable t) {
                    callBack.error(t.getMessage());
                }
            }
        );
    }

    List<ChatRoomEntity> servicingItems = Lists.newArrayList();

    private void getRobotChatServiceList(Context context, @Nullable ApiCallback<List<ChatRoomEntity>> callback) {
        ApiManager.getInstance().doSyncRobotServiceList(context, callback);
    }

    public void getServiceMemberActiveList(Context context, List<String> serviceNumberIds, long refreshTime, @Nullable ApiCallback<List<ChatRoomEntity>> callBack) {

        ApiManager.getInstance().doSyncServiceNumberActiveList(context, serviceNumberIds, refreshTime, new ApiCallback<RoomRecentRsp>() {
            @Override
            public void error(String message) {
                CELog.e("getServiceMemberActiveList error=" + message);
            }

            @Override
            public void complete(RoomRecentRsp rsp) {
                if (rsp != null && rsp.getItems() != null && !rsp.getItems().isEmpty()) {
                    String userId = TokenPref.getInstance(context).getUserId();
                    List<ChatRoomEntity> items = new ArrayList<>();
                    for (ChatRoomEntity entity : rsp.getItems()) {
                        setChatType(entity, userId);
                        //聊天室或服務號分類
//                        if (ChatRoomType.SERVICE_MEMBER.equals(entity.getType())) {
//                            if (ServiceNumberType.BOSS.equals(entity.getServiceNumberType()) && userId.equals(entity.getServiceNumberOwnerId())) { //服務號成員聊天室且是商務號擁有者
//                                entity.setListClassify(ChatRoomSource.MAIN);
//                            } else {
//                                entity.setListClassify(ChatRoomSource.SERVICE);
//                            }
//                        } else if (ChatRoomType.SERVICES.equals(entity.getType())) {
//                            if (entity.getOwnerId().equals(userId)) { //我自己進線的服務號聊天室，服務號並且是擁有者
//                                entity.setType(ChatRoomType.SUBSCRIBE);
//                                entity.setListClassify(ChatRoomSource.MAIN);
//                            } else if (ServiceNumberType.BOSS.equals(entity.getServiceNumberType()) && userId.equals(entity.getServiceNumberOwnerId())) { //我的商務號聊天室
//                                entity.setListClassify(ChatRoomSource.MAIN);
//                            } else {
//                                entity.setListClassify(ChatRoomSource.SERVICE);
//                            }
//                        } else {
//                            entity.setListClassify(ChatRoomSource.MAIN);
//                        }

                        items.add(entity);
                    }
                    DBManager.getInstance().updateOrInsertApiInfoField(SYNC_SERVICE_NUMBER_ACTIVE_LIST, rsp.getRefreshTime());

                    ChatRoomReference.getInstance().save(items);

                    //更新未讀數
                    getBadge(context);

                    if (callBack != null)
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(items));
                    if (rsp.getHasNextPage() != null && rsp.getHasNextPage()) {
                        getServiceMemberActiveList(context, serviceNumberIds, rsp.getRefreshTime(), callBack);
                    } else {
                        if (callBack != null)
                            ThreadExecutorHelper.getMainThreadExecutor().execute(callBack::finish);
                    }
                } else {
                    if (callBack != null)
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.error("no data"));
                }
            }

            @Override
            public void finish() {
            }
        });
    }

    /**
     * Get chat room entity
     */
    public void getChatRoomItem(Context context, String userId, String roomId, RefreshSource source, @Nullable ServiceCallBack<ChatRoomEntity, RefreshSource> callBack) {
        if (RefreshSource.ALL_or_LOCAL.contains(source)) {
            ChatRoomEntity localEntity = ChatRoomReference.getInstance().findById2(userId, roomId, true, true, true, true, true);
            if (localEntity != null) {
//                List<ChatRoomEntity> entities = Lists.newArrayList(localEntity);
                // 1、Check if it is a service account agent
//                isServiceNumberAgent(context, entities);
                // 2、Complete message processing
//                handleEntities(context, entities, RefreshSource.LOCAL);
                // 3、Callback update UI // Filter data according to the required data source
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(localEntity, RefreshSource.LOCAL));
                // 4、Calculate unread
                getBadge(context);
            } else {
                source = RefreshSource.REMOTE;
            }
        }

        if (RefreshSource.ALL_or_REMOTE.contains(source)) {
            ApiManager.doRoomItem(context, roomId, userId, new ApiListener<ChatRoomEntity>() {
                @Override
                public void onSuccess(ChatRoomEntity entity) {
                    List<ChatRoomEntity> entities = Lists.newArrayList(entity);
                    // 1、Check if it is a service account agent
//                    isServiceNumberAgent(context, entities);
                    doPerfectMessageEntities(context, entities, RefreshSource.REMOTE);
                    // 3、Callback update UI // Filter data according to the required data source
//                    ChatRoomEntity localEntity = ChatRoomReference.getInstance().findById2( userId, roomId, true, true, true, true, true);

                    if (ChatRoomReference.getInstance().save(entities)) {
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(entity, RefreshSource.REMOTE));
                    } else {
                        onFailed("save room data failed");
                    }
                    // 4、Calculate unread
                    getBadge(context);

                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.REFRESH_ROOM_BY_LOCAL));
                }

                @Override
                public void onFailed(String errorMessage) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.error(errorMessage));
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.REFRESH_ROOM_BY_LOCAL)); //不是聊天室成員，更新DB將聊天室從列表中移除
                }
            });
        }
    }

    /**
     * If there is dftTime and it is a friend chat room, delete the chat room and not display it on the screen
     */
    public static List<ChatRoomEntity> filterByDelete(List<ChatRoomEntity> entities) {
        List<ChatRoomEntity> list = Lists.newArrayList();
        for (ChatRoomEntity e : entities) {
            // 過濾如果是滑動刪除的聊天室
            if (e.getType() != ChatRoomType.person) {
                if (e.getLastMessage() != null) {
                    if (e.getDfrTime() > e.getLastMessage().getSendTime()) continue;
                } else {
                    if (e.getDfrTime() > 0) continue;
                }
            }

            if (ChatRoomType.serviceMember.equals(e.getType())) {
                if (e.isDeleted()) continue;
                list.add(e);
            } else if (e.isDeleted() || e.isMember_deleted() || (e.getLastMessage() != null && e.getDfrTime() > e.getLastMessage().getSendTime())) {
                // delete room
                ChatRoomReference.getInstance().deleteById(e.getId());
            } else {
                list.add(e);
            }
        }
        return list;
    }

    /**
     * Get group chat room
     */
    public void getGroupRoomList(Context context, RefreshSource source, @Nullable ServiceCallBack<List<CrowdEntity>, RefreshSource> callBack) {
        if (RefreshSource.LOCAL.equals(source)) {
            List<CrowdEntity> list = DBManager.getInstance().findAllCrowds();
            if (list != null) {
                callBack.complete(list, source);
            }
        } else {
            ApiManager.doRoomList(context, new ApiListener<List<CrowdEntity>>() {
                @Override
                public void onSuccess(List<CrowdEntity> crowdEntities) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(crowdEntities, RefreshSource.REMOTE));
                }

                @Override
                public void onFailed(String errorMessage) {
                    CELog.w("API Get My CrowdEntity List Error ", errorMessage);
                }
            });
        }
    }

    /**
     * Perfect Message processing
     */
    private void doPerfectMessageEntities(Context context, List<ChatRoomEntity> entities, RefreshSource source) {
        String userId = TokenPref.getInstance(context).getUserId();
        List<ChatRoomEntity> mainRoomEntities = Lists.newArrayList();
        List<ChatRoomEntity> serviceRoomEntities = Lists.newArrayList();

        if (RefreshSource.REMOTE.equals(source)) {
            class PendingData {
                final String roomId;
                final int unreadNumber;

                public PendingData(String roomId, int unreadNumber) {
                    this.roomId = roomId;
                    this.unreadNumber = unreadNumber;
                }
            }

            List<ChatRoomEntity> unreadHandleEntities = Lists.newArrayList();
            List<ChatRoomEntity> defaultHandleEntities = Lists.newArrayList();
            Map<PendingData, ChatRoomEntity> pendingProfessionalServiceUnreadNumberData = Maps.newHashMap();

            if (entities != null && !entities.isEmpty()) {
                for (ChatRoomEntity item : entities) {
                    if (item.isService(context)) {
                        serviceRoomEntities.add(item);
                    } else {
                        mainRoomEntities.add(item);
                    }

                    if (item.getUnReadNum() > 0) {
                        unreadHandleEntities.add(item);
                    } else {
                        defaultHandleEntities.add(item);
                    }
                }
            }

            // Handling the service number and sending out the read logic
            for (ChatRoomEntity entity : serviceRoomEntities) {
                if (ServiceNumberType.PROFESSIONAL.equals(entity.getServiceNumberType())
                    && Strings.isNullOrEmpty(entity.getServiceNumberAgentId())
                    && entity.getUnReadNum() > 0) {
                    // Do not process professional service account reading logic temporarily
                } else if (ServiceNumberType.PROFESSIONAL.equals(entity.getServiceNumberType())
                    && !userId.equals(entity.getServiceNumberAgentId()) // The connection person is not for me
                    && entity.getUnReadNum() > 0) { // Is a professional service number
//                    entity.setLastEndServiceTime(-1);
                    // Do not process professional service account reading logic temporarily
                    // pendingProfessionalServiceUnreadNumberData.put(new PendingData(entity.getId(), entity.getUnReadNum()), entity);
//                    entity.setUnReadNum(0);
                } else {
                    // Do not process professional service account reading logic temporarily
//                    if (entity.getLastMessage() != null) {
//                        MessageReference.save(entity.getId(), entity.getLastMessage());
//                    }
                }
            }

            // Fix the progress bar to load the data listener correctly, use EventBus instead
//            OnToolBarProgressListener listener = (roomId, progress) -> {
//                ChatRoomEntity entity = ChatRoomReference.getInstance().findById2( userId, roomId, false, false, false, true, true);
//                if (entity != null && entity.getUnReadNum() > 0 && entity.isAtMe()) {
//                    // Determine if there is an unread chat room of At Me
//                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.OUTCROP_MENTION_UNREAD_ROOM, JsonHelper.getInstance().toJson(entity)));
//                }
//                EventBusUtils.sendEvent(new EventMsg(MsgConstant.CHAT_ROOM_DATA_LOADING_PROGRESS_EVENT, 1));
//            };


            // Correct the logic of loading the data correctly in the progress bar. Give the max value to handle the number of unread chat rooms. Use EventBus instead
            EventBusUtils.sendEvent(new EventMsg(MsgConstant.CHAT_ROOM_DATA_LOADING_MAX_EVENT, unreadHandleEntities.size() + defaultHandleEntities.size()));


            // Open thread asynchronous supplementary message logic, process all
            for (ChatRoomEntity entity : unreadHandleEntities) {
                ChatMessageService.doCompleteMessageEntitiesProcessing(context, entity, true);
//                ChatMessageService.messageListRequestRecursiveDesc(context, entity, true, RefreshSource.LOCAL.equals(source) ? null : listener);
            }

            // Due to the change of jocket mechanism, only the information within 30 minutes of disconnection will be kept, and will not be pushed again if it exceeds that.
            // After the multi-vehicle reads the chat room, unReadNumber is reset to zero and cannot enter the supplementary message mechanism and Jocket.Message.New is no longer pushed.
            // process
            // 1. Enter the logic and save the last message.
            // 2. If there is no local last, get message list by asc.
            // 3. If there is last locally, get message list by id to id.
            for (ChatRoomEntity entity : defaultHandleEntities) {
                ChatMessageService.doCompleteMessageEntitiesProcessing(context, entity, true);
            }

            // Logic for processing the unread quantity of professional service account
            for (Map.Entry<PendingData, ChatRoomEntity> entry : pendingProfessionalServiceUnreadNumberData.entrySet()) {
                ChatRoomEntity entity = entry.getValue();
                PendingData data = entry.getKey();
                entity.setUnReadNum(data.unreadNumber);
                // Cancel local check
                boolean isReceived = true;
//                boolean isReceived = entity.getLastEndServiceTime() < 0;
                ChatMessageService.messageListRequestRecursiveDesc(context, entity, isReceived, (roomId, progress) -> {
                    entity.setUnReadNum(0);
                    ChatRoomReference.getInstance().save(entity);
                    if (entity.getLastEndServiceTime() > 0) {
                        List<String> messageIds = MessageReference.findUnreadByRoomIdAndSendTime(null, entity.getId(), entity.getLastEndServiceTime());
                        ApiManager.doMessagesRead(context, entity.getId(), messageIds, null);
                    }
                });
            }
        }
//        getBadge(context);
    }


    /**
     * Change the mute setting
     */
    public void changeMute(Context context, ChatRoomEntity entity, ServiceCallBack<ChatRoomEntity, Enum> callBack) {
        if (entity.isMute()) {
            ApiManager.getInstance().doRoomMuteCancel(context, entity.getId(), new ApiListener<String>() {
                @Override
                public void onSuccess(String roomId) {
                    boolean updateStatus = ChatRoomReference.getInstance().updateMuteById(roomId, !entity.isMute());
                    if (updateStatus) {
                        entity.setMute(!entity.isMute());
                        if (callBack != null) {
                            callBack.complete(entity, null);
                        }
                    }
                }

                @Override
                public void onFailed(String errorMessage) {
                    if (callBack != null) {
                        callBack.error(errorMessage);
                    }
                }
            });
        } else {
            ApiManager.getInstance().doRoomMute(context, entity.getId(), new ApiListener<String>() {
                @Override
                public void onSuccess(String roomId) {
                    boolean updateStatus = ChatRoomReference.getInstance().updateMuteById(roomId, !entity.isMute());
                    if (updateStatus) {
                        entity.setMute(!entity.isMute());
                        if (callBack != null) {
                            callBack.complete(entity, null);
                        }
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

    //    switch
    public void changeTop(Context context, ChatRoomEntity entity, ServiceCallBack<ChatRoomEntity, Enum> callBack) {
        if (entity.isTop()) {
            ApiManager.getInstance().doRoomTopCancel(context, entity.getId(), new ApiListener<String>() {
                @Override
                public void onSuccess(String s) {
                    boolean updateStatus = ChatRoomReference.getInstance().updateTopAndTopTimeById(entity.getId(), !entity.isTop(), 0L);
                    if (updateStatus) {
                        entity.setTop(false);
                        entity.setTopTime(0L);
                        callBack.complete(entity, null);
//                        EventBusUtils.sendEvent(new EventMsg(MsgConstant.SESSION_UPDATE_FILTER, entity));
                    }
                }

                @Override
                public void onFailed(String errorMessage) {
                    callBack.error(errorMessage);
//                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.API_ON_FAILED_MESSAGE_EVENT, errorMessage));
                }
            });
        } else {
            ApiManager.getInstance().doRoomTop(context, entity.getId(), new ApiListener<String>() {
                @Override
                public void onSuccess(String s) {
                    boolean updateStatus = ChatRoomReference.getInstance().updateTopAndTopTimeById(entity.getId(), !entity.isTop(), 0L);
                    if (updateStatus) {
                        entity.setTop(true);
                        entity.setTopTime(System.currentTimeMillis());
                        callBack.complete(entity, null);
//                        EventBusUtils.sendEvent(new EventMsg(MsgConstant.SESSION_UPDATE_FILTER, entity));
                    }
                }

                @Override
                public void onFailed(String errorMessage) {
                    callBack.error(errorMessage);
//                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.API_ON_FAILED_MESSAGE_EVENT, errorMessage));
                }
            });
        }
    }

    /**
     * Delete the chat room, if successful, update the unread number of corner tags
     */
    public void delete(Context context, ChatRoomEntity entity, ServiceCallBack<ChatRoomEntity, Enum> callBack) {
        ApiManager.getInstance().doRoomRecentDelete(context, entity.getId(), new ApiListener<String>() {
            @Override
            public void onSuccess(String roomId) {
                boolean deleteStatus = ChatRoomReference.getInstance().deleteById(entity.getId());
                if (deleteStatus) {
                    getBadge(context);
                    callBack.complete(entity, null);
                }
            }

            @Override
            public void onFailed(String errorMessage) {
                callBack.error(errorMessage);
            }
        });
    }


    public enum RelieveStatus {
        UPDATE,
        DELETE
    }


    public static SpannableStringBuilder getAtContent(String input) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        Gson gson = new Gson();
        try {
            List<AtContent> contents = gson.fromJson(input, new TypeToken<List<AtContent>>() {
            }.getType());
            String selfUserId = TokenPref.getInstance(SdkLib.getAppContext()).getUserId();
            for (AtContent content : contents) {
                if (LastMessageType.ObjectType.USER.equals(content.getObjectType())) {
                    for (String id : content.getUserIds()) {
                        UserProfileEntity userProfile = DBManager.getInstance().queryFriend(id);
                        if (!Strings.isNullOrEmpty(userProfile.getNickName())) {
                            int start = builder.length();
                            builder.append("@").append(userProfile.getNickName());
                            int end = builder.length();
                            builder.append(" ");
                            // 如果不是 at 自己 不用換字體顏色
                            if (id.equals(selfUserId)) {
                                ForegroundColorSpan span = new ForegroundColorSpan(0xFF4A90E2);
                                builder.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }
                    }
                } else if (LastMessageType.ObjectType.ALL.equals(content.getObjectType())) {
                    Spannable spannable = new SpannableString("@ALL ");
                    spannable.setSpan(new ForegroundColorSpan(0xFF4A90E2), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.append(spannable);
                }
                if (TEXT.equals(content.getType())) {
                    builder.append(content.getContent().getText());
                }
            }
        } catch (Exception e) {
            builder.append(input);
        }
        return builder;
    }

    /**
     * Check the chat room and get the chat room entity
     */
    int count = 0;

    public void checkRoomEntities(Context context, String[] roomIds, ServiceCallBack<List<String>, Enum> callBack) {
        count = roomIds.length;
        String userId = TokenPref.getInstance(context).getUserId();
        List<ChatRoomEntity> roomEntities = ChatRoomReference.getInstance().findByIds(userId, Lists.newArrayList(roomIds), true, true, true);
        for (String roomId : roomIds) {
            if (roomEntities.contains(ChatRoomEntity.Build().id(roomId).build())) {
                count--;
                if (count == 0) {
                    callBack.complete(Lists.newArrayList(roomIds), null);
                }
            } else {
                ChatRoomService.getInstance().getChatRoomItem(context, userId, roomId, RefreshSource.REMOTE, new ServiceCallBack<ChatRoomEntity, RefreshSource>() {
                    @Override
                    public void complete(ChatRoomEntity entity, RefreshSource source) {
                        count--;
                        if (count == 0) {
                            callBack.complete(Lists.newArrayList(roomIds), null);
                        }
                    }

                    @Override
                    public void error(String message) {
                        count--;
                        if (count == 0) {
                            callBack.complete(Lists.newArrayList(roomIds), null);
                        }
                    }
                });
            }
        }
    }

    //聊天室&服務號分類
    private void setChatType(ChatRoomEntity chatRoomEntity, String userId) {
        //聊天室或服務號分類
        if (ChatRoomType.serviceMember.equals(chatRoomEntity.getType())) {
            if (ServiceNumberType.BOSS.equals(chatRoomEntity.getServiceNumberType()) && userId.equals(chatRoomEntity.getServiceNumberOwnerId())) { //服務號成員聊天室且是商務號擁有者
                chatRoomEntity.setListClassify(ChatRoomSource.MAIN);
            } else {
                chatRoomEntity.setListClassify(ChatRoomSource.SERVICE);
            }
        } else if (ChatRoomType.services.equals(chatRoomEntity.getType())) {
            if (chatRoomEntity.getOwnerId().equals(userId)) { //我自己進線的服務號聊天室，服務號並且是擁有者
                chatRoomEntity.setType(ChatRoomType.subscribe);
                chatRoomEntity.setListClassify(ChatRoomSource.MAIN);
            } else if (ServiceNumberType.BOSS.equals(chatRoomEntity.getServiceNumberType()) && userId.equals(chatRoomEntity.getServiceNumberOwnerId())) { //我的商務號聊天室
                chatRoomEntity.setListClassify(ChatRoomSource.MAIN);
            } else if (chatRoomEntity.getProvisionalIds().contains(userId)) {
                chatRoomEntity.setListClassify(ChatRoomSource.MAIN); //臨時成員聊天室顯示在一般聊天列表
            } else {
                chatRoomEntity.setListClassify(ChatRoomSource.SERVICE);
            }
        } else {
            chatRoomEntity.setListClassify(ChatRoomSource.MAIN);
        }
    }
}
