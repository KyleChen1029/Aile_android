package tw.com.chainsea.chat.view.service.model;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.client.type.Media;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.broadcast.BroadcastMessageBean;
import tw.com.chainsea.ce.sdk.bean.broadcast.TopicEntity;
import tw.com.chainsea.ce.sdk.bean.msg.BroadcastFlag;
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.SourceType;
import tw.com.chainsea.ce.sdk.bean.msg.content.BroadcastContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.VoiceContent;
import tw.com.chainsea.ce.sdk.bean.parameter.Sort;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.request.ServiceNumberBroadcastDeleteRequest;
import tw.com.chainsea.ce.sdk.http.ce.request.ServiceNumberBroadcastSendRequest;
import tw.com.chainsea.ce.sdk.http.ce.request.ServiceNumberBroadcastUpdateRequest;
import tw.com.chainsea.ce.sdk.http.ce.request.UploadManager;
import tw.com.chainsea.ce.sdk.reference.MessageReference;
import tw.com.chainsea.ce.sdk.reference.TopicReference;
import tw.com.chainsea.ce.sdk.service.ChatMessageService;
import tw.com.chainsea.ce.sdk.service.FileService;
import tw.com.chainsea.ce.sdk.service.RepairMessageService;
import tw.com.chainsea.ce.sdk.service.TopicService;
import tw.com.chainsea.ce.sdk.service.listener.AServiceCallBack;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.keyboard.emoticon.view.RecordLayout;
import tw.com.chainsea.chat.messagekit.lib.AudioLib;
import tw.com.chainsea.chat.util.TimeUtil;
import tw.com.chainsea.chat.view.service.BroadcastEditorLayout;
import tw.com.chainsea.chat.view.service.ServiceBroadcastEditorActivity;
import tw.com.chainsea.chat.widget.popup.MyPopupWindow;
import tw.com.chainsea.custom.view.alert.AlertView;
import tw.com.chainsea.custom.view.floating.DragImageFloatingButton;

/**
 * current by evan on 2020-07-29
 *
 * @author Evan Wang
 * date 2020-07-29
 */
public class BroadcastEditorViewModel {
    WeakReference<ServiceBroadcastEditorActivity> activityWeak;
    MyPopupWindow popupWindow;

    UserProfileEntity self;
    ChatRoomEntity roomEntity;
    ServiceNumberEntity serviceNumberEntity;
    List<MessageEntity> messageEntities = Lists.newArrayList();

    private static SimpleDateFormat messageTimeLineFormat = new SimpleDateFormat("MMMdd日(EEE)", Locale.TAIWAN);
    private String currentDate;

    Runnable timeBoxTarget;

    public Runnable getTimeBoxTarget(TextView tvFloatTimeBox) {
        if (timeBoxTarget == null) {
            timeBoxTarget = new Runnable() {
                @Override
                public void run() {
                    if (tvFloatTimeBox != null) {
                        tvFloatTimeBox.animate()
                            .alpha(0.0f)
                            .setDuration(300)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    tvFloatTimeBox.setVisibility(View.GONE);
                                }
                            });
                    }
                }
            };
        }
        return timeBoxTarget;
    }


    public BroadcastEditorViewModel(ServiceBroadcastEditorActivity activity) {
        this.activityWeak = new WeakReference<ServiceBroadcastEditorActivity>(activity);
        TopicService.getTopicEntities(activity, null);
    }

    public void onBind(UserProfileEntity self) {
        this.self = self;
    }

    public UserProfileEntity getSelf() {
        return this.self;
    }

    public void onBind(ChatRoomEntity roomEntity) {
        this.roomEntity = roomEntity;
        this.loadLocalMessageEntities(roomEntity.getId());
    }

    public void onBind(ServiceNumberEntity serviceNumberEntity) {
        this.serviceNumberEntity = serviceNumberEntity;
    }

    public ChatRoomEntity getRoomEntity() {
        return this.roomEntity;
    }

    public ServiceNumberEntity getServiceNumberEntity() {
        return this.serviceNumberEntity;
    }

    public List<MessageEntity> getMessageEntities() {
        return this.messageEntities;
    }

    /**
     * 拿出本地訊息資料
     * 刪減法檢查 previous Message
     * 進 Queue 取得實體
     */
    public void loadLocalMessageEntities(String broadcastRoomId) {
        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
            List<MessageEntity> metadata = MessageReference.findByBroadcastRoomId(null, broadcastRoomId);
            Collections.sort(metadata);

            String lastMessageId = metadata.isEmpty() ? "" : metadata.get(metadata.size() - 1).getId();

            // EVAN_FLAG 2020-08-23 (1.12.0) 待認證Flag的 message
            long now = System.currentTimeMillis();
            List<String> unfinishedIdSet = Lists.newArrayList();

            // EVAN_FLAG 2020-08-06 (1.12.0) 補訊息檢查點
            List<String> previousMessageIdSet = Lists.newArrayList();

            for (int i = metadata.size() - 1; i >= 0; i--) {
                MessageEntity entity = metadata.get(i);
                if (BroadcastFlag.BOOKING_of_DISPATCHING.contains(entity.getBroadcastFlag()) && MessageStatus.getValids().contains(entity.getStatus()) && MessageType.BROADCAST.equals(entity.getType())) {
//                if (!BroadcastFlag.DOME_or_DELETED.contains(entity.getBroadcastFlag()) && entity.getBroadcastTime() > 0 && entity.getBroadcastTime() > now && MessageStatus.getValids().contains(entity.getStatus())) {
                    if (!SourceType.SYSTEM.equals(entity.getSourceType())) { // 過濾是系統統訊息
                        unfinishedIdSet.add(entity.getId());
                    }
                }

                if (i > 0 && !Strings.isNullOrEmpty(entity.getPreviousMessageId())) {
                    previousMessageIdSet.add(entity.getPreviousMessageId());
                }
                previousMessageIdSet.remove(entity.getId());
            }

            if (!previousMessageIdSet.isEmpty()) {
                CELog.i("待補齊訊息處理 ::" + previousMessageIdSet);
                // EVAN_FLAG 2020-08-26 (1.12.0) 刪除的訊息被補回
                RepairMessageService.setQueue(activityWeak.get(), roomEntity.getId(), Sets.newHashSet(previousMessageIdSet), true);
            }

            // filter
            Iterator<MessageEntity> iterator = metadata.iterator();
            while (iterator.hasNext()) {
                if (!MessageType.BROADCAST.equals(iterator.next().getType())) {
                    iterator.remove();
                }
            }

            // sort
            sort(metadata);

            assemblyMessageEntities(true, metadata);
            findRemoteMessageList(true, broadcastRoomId, lastMessageId, Sort.ALL);

            // EVAN_FLAG 2020-08-23 (1.12.0) 從本地取出若為完成，足一檢查 message 狀態
            if (!unfinishedIdSet.isEmpty()) {
                RepairMessageService.setQueue(activityWeak.get(), roomEntity.getId(), Sets.newHashSet(unfinishedIdSet), false);
            }
        });

    }


    /**
     * 從Server上加載更多
     * ASC 拿更舊
     * DESC 拿更新
     */
    public void loadMoreOnTheRemote(Sort sort) {
        MessageEntity firstEntity = MessageReference.findMessageByRoomIdAndStatusAndLimitOne(null, this.roomEntity.getId(), MessageStatus.getValidStatus(), MessageReference.Sort.ASC);
        MessageEntity lastEntity = MessageReference.findMessageByRoomIdAndStatusAndLimitOne(null, this.roomEntity.getId(), MessageStatus.getValidStatus(), MessageReference.Sort.DESC);
        String boundaryMessageId = Sort.DESC.equals(sort) ? firstEntity != null ? firstEntity.getId() : "" : lastEntity != null ? lastEntity.getId() : "";
        findRemoteMessageList(Sort.ASC.equals(sort), this.roomEntity.getId(), boundaryMessageId, sort);
    }

    /**
     * 依照 lastMessageId ASC & DESC 50
     */
    public void findRemoteMessageList(boolean isNew, String broadcastRoomId, String lastMessageId, Sort sort) {
        int count = Sort.ALL.equals(sort) ? 2 : 1;
        Set<MessageEntity> entities = Sets.newHashSet();
        ChatMessageService.getBroadcastMessageEntities(activityWeak.get(), broadcastRoomId, lastMessageId, sort, new ServiceCallBack<List<MessageEntity>, RefreshSource>() {
            int findCount = 0;

            @Override
            public void complete(List<MessageEntity> messageEntities, RefreshSource source) {
                findCount++;
                entities.addAll(messageEntities);
                if (findCount == count) {
                    List<MessageEntity> list = Lists.newArrayList(entities);
                    sort(list);
                    assemblyMessageEntities(isNew, list);
                }
            }

            @Override
            public void error(String message) {
                CELog.e("");
            }
        });
    }

    private static synchronized void sort(List<MessageEntity> list) {
        Collections.sort(list, (o1, o2) -> ComparisonChain.start()
            .compare(o1.getBroadcastWeights(), o2.getBroadcastWeights())
            .compare(o1.getBroadcastTime(), o2.getBroadcastTime())
            .result());
    }


    /**
     * 輸入完成顯示編輯畫面(update)
     * 有 messageId or 沒有
     */
    public void showUpdateBroadcastEditor(boolean isNew, IMessageContent<MessageType> iContent, List<TopicEntity> topicEntities, BroadcastEditorLayout bel_editor) {
        if (bel_editor.getEntity() == null) {
            bel_editor.broadcastTime = 0L;
            CELog.e("" + topicEntities.size());
            topicEntities.remove(TopicEntity.newHardCode());
            CELog.e("" + topicEntities.size());
            MessageEntity entity = new MessageEntity.Builder()
                .type(iContent.getType())
                .senderId(self.getId())
                .topicArray(topicEntities)
                .senderName(self.getNickName())
                .content(iContent.toStringContent())
                .flag(MessageFlag.OWNER)
                .status(MessageStatus.FAILED)
                .build();
            bel_editor.bind(entity);
            bel_editor.setSelectData(Lists.newArrayList(), true);
        } else {
            bel_editor.getEntity().setType(iContent.getType());
            bel_editor.getEntity().setContent(iContent.toStringContent());
        }
        bel_editor.refresh();
        bel_editor.setVisibility(View.VISIBLE);
    }

    /**
     * 輸入完成顯示編輯畫面
     * 有 messageId or 沒有
     */
    public void showBroadcastEditor(boolean isNew, IMessageContent<MessageType> iContent, BroadcastEditorLayout bel_editor) {
        if (bel_editor.getEntity() == null) {
            bel_editor.broadcastTime = 0L;
            MessageEntity entity = new MessageEntity.Builder()
                .type(iContent.getType())
                .senderId(self.getId())
                .senderName(self.getNickName())
                .content(iContent.toStringContent())
                .flag(MessageFlag.OWNER)
                .status(MessageStatus.FAILED)
                .build();
            bel_editor.bind(entity);
            bel_editor.setSelectData(Lists.newArrayList(), true);
        } else {
            bel_editor.getEntity().setType(iContent.getType());
            bel_editor.getEntity().setContent(iContent.toStringContent());
        }
        bel_editor.refresh();
        bel_editor.setVisibility(View.VISIBLE);
    }

    /**
     * message item  click to this function
     */
    public void showBroadcastEditor(boolean isNew, MessageEntity entity, BroadcastEditorLayout bel_editor) {
        if (bel_editor.getEntity() != null && bel_editor.getVisibility() == View.VISIBLE) {
            bel_editor.setSelectData(Lists.newArrayList(entity.getTopicArray()), entity.isCanEditBroadcast());
            bel_editor.bind(entity);
        } else {
            bel_editor.setSelectData(Lists.newArrayList(entity.getTopicArray()), entity.isCanEditBroadcast());
            bel_editor.bind(entity);
            bel_editor.setVisibility(View.VISIBLE);
        }
        bel_editor.refresh();
        // 修改者
    }

    /**
     * 置底按鈕事件
     */
    public void setFloatingToBottomAction(RecyclerView recyclerView, DragImageFloatingButton difb) {
        float scale = 0.0f;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
            int lastItemPosition = linearManager.findLastVisibleItemPosition();

            int firstItemPosition = linearManager.findFirstVisibleItemPosition();
            if (messageEntities.size() > firstItemPosition && firstItemPosition > -1) {
                scale = 1.0f;
            }

            //获取最后一个可见view的位置
            if (!messageEntities.isEmpty() && lastItemPosition == messageEntities.size() - 1) {
                scale = 0.0f;
            }
        }
        hiedOrShowFloatingButton(scale, difb);
    }

    public void hiedOrShowFloatingButton(float scale, View v) {
        int bottomDistance = UiHelper.dip2px(activityWeak.get(), 10);
        v.animate().scaleX(scale).scaleY(scale).translationY(scale == 1.0f ? 0 : v.getHeight() + bottomDistance).setInterpolator(new LinearInterpolator()).start();
        v.setVisibility(scale > 0 ? View.VISIBLE : View.GONE);
    }

    public void hideOrShowTimeToast(RecyclerView recyclerView, TextView tvFloatTimeBox) {
        if (true) {
            return;
        }
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
            int index = linearManager.findFirstVisibleItemPosition();

            if (messageEntities.size() > index && index > -1) {
                MessageEntity message = messageEntities.get(index);
                String dateTime = TimeUtil.INSTANCE.getDateShowString(message.getSendTime(), true);
                tvFloatTimeBox.setText(dateTime);
                tvFloatTimeBox.setVisibility(View.VISIBLE);
                tvFloatTimeBox.setAlpha(1.0f);
                tvFloatTimeBox.removeCallbacks(getTimeBoxTarget(tvFloatTimeBox));
                tvFloatTimeBox.postDelayed(getTimeBoxTarget(tvFloatTimeBox), 1500L);
            }
        }
    }

    /**
     * 預覽貼圖 popup on edit text view
     * // R.layout.popup_sticker_preview
     */
//    public void showStickerPreviewPopup(View anchor, EmoticonBean bean, Drawable drawable) {
//        final View contentView = LayoutInflater.from(activityWeak.get()).inflate(R.layout.popup_sticker_preview, null);
//        ImageView iv = contentView.findViewById(R.id.iv_sticker);
//        iv.setImageDrawable(drawable);
//        contentView.setOnClickListener(v -> dismissPopup());
//
//        int height = UiHelper.dip2px(activityWeak.get(), 150);
//        popupWindow = new MyPopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, height);
//        popupWindow.setBackgroundDrawable(new ColorDrawable(0x10101010));
//        popupWindow.setOutsideTouchable(false);
//        popupWindow.setFocusable(true);
//        popupWindow.showAsDropDown(anchor, 0, -(height + anchor.getMeasuredHeight()));
//    }

    /**
     * 錄音倒數計時UI
     */
    public void showCountdownTimer(View anchor, int countDown, RecordLayout rlRecorder) {
        if (AudioLib.getInstance(activityWeak.get()).isPlaying()) {
            toast("麥克風正在被佔用", false);
            return;
        }

        // 正在錄音判斷
        if (rlRecorder.getVisibility() == View.VISIBLE) {
            toast("正在錄音", false);
            return;
        }


        // 通知禁止休眠
        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_KEEP_SCREEN_ON));

        startRecord(rlRecorder);
    }

    private void startRecord(RecordLayout rlRecorder) {
        rlRecorder.setVisibility(View.VISIBLE);
    }

    public void toast(String tip, boolean needUiThread) {
        if (needUiThread) {
            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> Toast.makeText(activityWeak.get(), tip, Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(activityWeak.get(), tip, Toast.LENGTH_SHORT).show();
        }
    }

    public void alert(String tip) {
        new AlertView.Builder()
            .setContext(activityWeak.get())
            .setStyle(AlertView.Style.Alert)
            .setMessage(tip)
            .setOthers(new String[]{"取消", "確定"})
            .setOnItemClickListener((o, position) -> {

            })
            .build()
            .setCancelable(true)
            .show();
    }

    /**
     * 送出廣播訊息內容
     */
    public void sendOrUpdateBroadcast(BroadcastMessageBean.BroadcastMessageBeanBuilder build, MessageEntity entity) {
        build.roomId(serviceNumberEntity.getBroadcastRoomId())
            .serviceNumberId(serviceNumberEntity.getServiceNumberId())
            .iContent(entity.content())
            .type("Broadcast");
//        build.iContent(entity.content());
        // 有無 messageId 是否走 update
        boolean isUpdate = !Strings.isNullOrEmpty(entity.getId()) && !MessageStatus.FAILED_or_ERROR.contains(entity.getStatus());
        if (!isUpdate) {
            isUpdate = MessageStatus.UPDATE_ERROR.equals(entity.getStatus());
        }

        MessageEntity sendEntity = null;
        MessageEntity updateEntity = null;
        if (isUpdate) {
            updateEntity = buildUpdateMessageEntity(build, entity);
        } else {
            if (Strings.isNullOrEmpty(entity.getId())) {
                entity.setId(String.valueOf(UUID.randomUUID()));
            }
            build.messageId(entity.getId());
            sendEntity = buildSendMessageEntity(build, entity);
            boolean status = MessageReference.save(roomEntity.getId(), sendEntity);
        }

        assemblyMessageEntity(true, sendEntity);
        if (MessageType.NON_UPLOAD_TYPES.contains(entity.getType())) {
            if (isUpdate) {
                update(build, updateEntity);
            } else {
                send(build, sendEntity);
            }
        } else {
            upload(build, entity, isUpdate);
        }
    }

    public MessageEntity buildSendMessageEntity(BroadcastMessageBean.BroadcastMessageBeanBuilder build, MessageEntity entity) {
        BroadcastContent broadcastContent = new BroadcastContent(entity.getType(),
            MessageType.TEXT.equals(entity.getType()) ? entity.content().simpleContent() : entity.content().toStringContent()
        );
        entity.setTopicArray(build.build().getValidTopicItems());
        entity.setType(MessageType.BROADCAST);
        entity.setRoomId(roomEntity.getId());
        entity.setSenderName(this.self.getNickName());
        entity.setSenderId(this.self.getId());
        entity.setContent(broadcastContent.toStringContent());
        entity.setSendTime(System.currentTimeMillis());
        entity.setStatus(MessageStatus.SENDING);
        entity.setBroadcastFlag(BroadcastFlag.BOOKING);
        entity.setUpdateUserId(this.self.getId());
        entity.setBroadcastTime(build.build().getBroadcastTime());
        return entity;
    }

    public MessageEntity buildUpdateMessageEntity(BroadcastMessageBean.BroadcastMessageBeanBuilder build, MessageEntity entity) {
        BroadcastContent broadcastContent = new BroadcastContent(entity.getType(),
            MessageType.TEXT.equals(entity.getType()) ? entity.content().simpleContent() : entity.content().toStringContent()
        );
        entity.setTopicArray(build.build().getValidTopicItems());
        entity.setType(MessageType.BROADCAST);
        entity.setRoomId(roomEntity.getId());
        entity.setSenderName(this.self.getNickName());
        entity.setSenderId(this.self.getId());
        entity.setContent(broadcastContent.toStringContent());
        entity.setSendTime(System.currentTimeMillis());
        entity.setStatus(MessageStatus.SENDING);
        entity.setBroadcastFlag(BroadcastFlag.BOOKING);
        entity.setUpdateUserId(this.self.getId());
        entity.setBroadcastTime(build.build().getBroadcastTime());
        return entity;
    }

    public void upload(BroadcastMessageBean.BroadcastMessageBeanBuilder build, MessageEntity entity, boolean isUpdate) {
        String filePath = entity.content().getFilePath();
        if (Strings.isNullOrEmpty(filePath) || filePath.startsWith("http")) {
            if (isUpdate) {
                MessageEntity updateEntity = buildSendMessageEntity(build, entity);
                assemblyMessageEntity(false, updateEntity);
                update(build, updateEntity);
            } else {
                MessageEntity sendEntity = buildSendMessageEntity(build, entity);
                send(build, sendEntity);
            }
            return;
        }

        String tokenId = TokenPref.getInstance(activityWeak.get()).getTokenId();
        FileService.uploadFile(activityWeak.get(), false, tokenId, Media.findByFileType(filePath), filePath, new AServiceCallBack<UploadManager.FileEntity, RefreshSource>() {
            @Override
            public void complete(UploadManager.FileEntity fileEntity, RefreshSource source) {
                if (fileEntity != null && entity.content() instanceof VideoContent) {
                    fileEntity.setWidth(((VideoContent) entity.content()).getWidth());
                    fileEntity.setHeight(((VideoContent) entity.content()).getHeight());
                }

                if (fileEntity != null && entity.content() instanceof VoiceContent) {
                    fileEntity.setDuration(((VoiceContent) entity.content()).getDuration());
                }

                String content = JsonHelper.getInstance().toJson(fileEntity);
                entity.setContent(content);
                build.iContent(entity.content());
                if (isUpdate) {
                    MessageEntity updateEntity = buildSendMessageEntity(build, entity);
                    assemblyMessageEntity(false, updateEntity);
                    update(build, updateEntity);
                } else {
                    MessageEntity sendEntity = buildSendMessageEntity(build, entity);
                    send(build, sendEntity);
                }
            }

            @Override
            public void onProgress(float progress, long total) {

            }

            @Override
            public void error(String message) {
                MessageEntity localEntity = MessageReference.findByIdAndBroadcastRoomId(null, entity.getId(), roomEntity.getId());
                if (isUpdate) {
                    assemblyMessageEntity(true, localEntity);
                } else {
                    // delete
                    removeMessageEntity(localEntity);
                    MessageReference.deleteByRoomIdAndMessageIds(roomEntity.getId(), new String[]{localEntity.getId()});
                }
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_BROADCAST_EDIT_TO_DEFAULT));
                alert(message);
            }
        });
    }

    public void send(BroadcastMessageBean.BroadcastMessageBeanBuilder build, MessageEntity sendEntity) {
        ApiManager.doServiceNumberBroadcastSend(activityWeak.get(), build.build(), new ServiceNumberBroadcastSendRequest.Listener() {
            @Override
            public void onSuccess(String broadcastRoomId, String messageId, String updateUserId, String createUserId, BroadcastFlag flag, long broadcastTime) {
                sendEntity.setId(messageId);
                sendEntity.setUpdateUserId(updateUserId);
                sendEntity.setRoomId(broadcastRoomId);
                sendEntity.setBroadcastFlag(flag);
                sendEntity.setStatus(MessageStatus.SUCCESS);
                sendEntity.setBroadcastTime(broadcastTime);
                boolean status = MessageReference.save(roomEntity.getId(), sendEntity);
                MessageEntity entity = MessageReference.findByIdAndBroadcastRoomId(null, messageId, roomEntity.getId());
                assemblyMessageEntity(true, entity);
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_BROADCAST_EDIT_TO_DEFAULT));
                RepairMessageService.setQueue(activityWeak.get(), roomEntity.getId(), Sets.newHashSet(messageId), false);
            }

            @Override
            public void onFailed(String errorMessage) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    removeMessageEntity(sendEntity);
                    alert(errorMessage);
                });
                MessageReference.deleteByRoomIdAndMessageIds(roomEntity.getId(), new String[]{sendEntity.getId()});
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_BROADCAST_EDIT_TO_DEFAULT));
            }
        });
    }

    /**
     * 送出更新
     */
    public void update(BroadcastMessageBean.BroadcastMessageBeanBuilder build, MessageEntity sendEntity) {
        ApiManager.doServiceNumberBroadcastUpdate(activityWeak.get(), build.build(), new ServiceNumberBroadcastUpdateRequest.Listener() {
            @Override
            public void onSuccess(String broadcastRoomId, String messageId, String createUserId, String updateUserId, BroadcastFlag flag, long broadcastTime) {
                ThreadExecutorHelper.getHandlerExecutor().execute(() -> {
                    RepairMessageService.setQueue(activityWeak.get(), roomEntity.getId(), Sets.newHashSet(sendEntity.getId()), false);
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_BROADCAST_EDIT_TO_DEFAULT));
                }, 300L);

            }

            @Override
            public void onFailed(String errorMessage) {
                ThreadExecutorHelper.getHandlerExecutor().execute(() -> {
                    alert(errorMessage);
                    RepairMessageService.setQueue(activityWeak.get(), roomEntity.getId(), Sets.newHashSet(sendEntity.getId()), false);
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_BROADCAST_EDIT_TO_DEFAULT));
                }, 300L);
            }
        });
    }

    /**
     * 刪除廣播訊息
     */
    public void delete(MessageEntity entry) {
        if (MessageStatus.FAILED_or_ERROR.contains(entry.getStatus())) {
            MessageReference.deleteByRoomIdAndMessageIds(roomEntity.getId(), new String[]{entry.getId()});
            TopicReference.deleteTopicRelByRelIdAndType(null, roomEntity.getId(), TopicReference.TopicRelType.MESSAGE);
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_BROADCAST_MESSAGE_DELETE, JsonHelper.getInstance().toJson(entry)));
            return;
        }

        ApiManager.doServiceNumberBroadcastDelete(activityWeak.get(), roomEntity.getId(), entry.getId(), new ServiceNumberBroadcastDeleteRequest.Listener() {
            @Override
            public void onSuccess(String broadcastRoomId, String messageId) {
                // delete Success 不能刪除 Local data 改下 DELETE FLAG
                entry.setBroadcastFlag(BroadcastFlag.DELETED);
                entry.setType(MessageType.BROADCAST);
                MessageReference.updateBroadcastFlag(null, broadcastRoomId, messageId, BroadcastFlag.DELETED);
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_APPEND_MESSAGE, JsonHelper.getInstance().toJson(entry)));
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_BROADCAST_EDIT_TO_DEFAULT));
            }

            @Override
            public void onFailed(String errorMessage) {
                CELog.e("");
            }
        });
    }

    public void assemblyMessageEntities(boolean isNew, List<MessageEntity> messageEntities) {
        for (MessageEntity entity : messageEntities) {
            displayMessageEntity(isNew, entity);
        }
        if (!messageEntities.isEmpty()) {
            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> BroadcastEditorViewModel.this.activityWeak.get().refresh(isNew));
        }
    }

    public void assemblyMessageEntity(boolean isNew, MessageEntity entity) {
        displayMessageEntity(isNew, entity);
        if (entity != null) {
            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> BroadcastEditorViewModel.this.activityWeak.get().refresh(isNew));
        }
    }

    public void removeMessageEntity(MessageEntity entity) {
        if (roomEntity.getId().equals(entity.getRoomId())) {
            this.messageEntities.remove(entity);
            BroadcastEditorViewModel.this.activityWeak.get().refresh(false);
        }
    }

    public void appendMessageEntity(MessageEntity entity) {
        if (!this.roomEntity.getId().equals(entity.getRoomId())) {
            return;
        }
        if (this.messageEntities.isEmpty()) {
            displayMessageEntity(true, entity);
            return;
        }

        int index = this.messageEntities.indexOf(entity);
        boolean isNew = false;
        if (index != -1 && index != this.messageEntities.size() - 1) {
        } else {
            isNew = true;
        }
        displayMessageEntity(isNew, entity);
        if (!messageEntities.isEmpty()) {
            BroadcastEditorViewModel.this.activityWeak.get().refresh(isNew);
        }
    }

    public void displayMessageEntity(boolean isNew, MessageEntity entity) {
        if (entity == null) {
            return;
        }
        if (!MessageType.BROADCAST.equals(entity.getType())) {
            return;
        }

        if (this.messageEntities.contains(entity)) {
            int index = this.messageEntities.indexOf(entity);
            this.messageEntities.remove(index);
        }
        this.messageEntities.remove(entity);
        this.messageEntities.add(entity);
    }

    private void dismissPopup() {
        if (popupWindow != null) {
            if (popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
        }
    }

    public void callKeyBack(long delayMillis) {
        if (this.activityWeak.get() != null && delayMillis < 0) {
            this.activityWeak.get().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
        } else if (this.activityWeak.get() != null && delayMillis > 0) {
            ThreadExecutorHelper.getHandlerExecutor().execute(() -> this.activityWeak.get().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK)), delayMillis);
        }
    }
}
