package tw.com.chainsea.chat.view.service;

import static tw.com.chainsea.chat.keyboard.NewKeyboardLayout.NewKeyboardFun.FUN_FACIAL;
import static tw.com.chainsea.chat.keyboard.NewKeyboardLayout.NewKeyboardFun.FUN_MEDIA;
import static tw.com.chainsea.chat.keyboard.NewKeyboardLayout.NewKeyboardFun.FUN_PHOTO;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hjq.permissions.XXPermissions;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.client.type.FileMedia;
import tw.com.chainsea.android.common.event.KeyboardHelper;
import tw.com.chainsea.android.common.file.FileHelper;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.multimedia.AMediaBean;
import tw.com.chainsea.android.common.multimedia.ImageBean;
import tw.com.chainsea.android.common.multimedia.MultimediaHelper;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.android.common.video.IVideoSize;
import tw.com.chainsea.android.common.video.VideoSizeFromVideoFile;
import tw.com.chainsea.android.common.voice.VoiceHelper;
import tw.com.chainsea.ce.sdk.bean.InputLogBean;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.broadcast.BroadcastMessageBean;
import tw.com.chainsea.ce.sdk.bean.broadcast.TopicEntity;
import tw.com.chainsea.ce.sdk.bean.msg.BroadcastFlag;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.content.BroadcastContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.FileContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.StickerContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.VoiceContent;
import tw.com.chainsea.ce.sdk.bean.parameter.Sort;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.bean.sticker.StickerItemEntity;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference;
import tw.com.chainsea.ce.sdk.service.ChatRoomService;
import tw.com.chainsea.ce.sdk.service.ChatServiceNumberService;
import tw.com.chainsea.ce.sdk.service.RepairMessageService;
import tw.com.chainsea.ce.sdk.service.UserProfileService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.ActivityServiceBroadcastEditorBinding;
import tw.com.chainsea.chat.keyboard.NewKeyboardLayout;
import tw.com.chainsea.chat.keyboard.emoticon.NewEmoticonLayout;
import tw.com.chainsea.chat.keyboard.view.HadEditText;
import tw.com.chainsea.chat.messagekit.lib.AudioLib;
import tw.com.chainsea.chat.messagekit.listener.OnMainMessageControlEventListener;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.style.RoomThemeStyle;
import tw.com.chainsea.chat.util.GlideEngine;
import tw.com.chainsea.chat.view.service.listener.OnServiceBroadcastListener;
import tw.com.chainsea.chat.view.service.model.BroadcastEditorViewModel;
import tw.com.chainsea.chat.widget.xrefreshlayout.XRefreshLayout;
import tw.com.chainsea.custom.view.alert.AlertView;

/**
 * current by evan on 2020-07-28
 *
 * @author Evan Wang
 * @date 2020-07-28
 * 預約未發送，可再次編輯，正在發送中不可編輯，發送完成不可編輯。
 * 編輯為完成取消或點擊新的MessageItem 需提示正在編輯中是否取消當前編輯。
 * 在 show 編輯畫面前要upload 的資訊要先上傳完成，(除了文字與貼圖)。
 * 在 show 編輯畫面上的 被選取 Topic 可點擊取消。
 * <p>
 * BroadcastEditorEventHandlers
 */

public class ServiceBroadcastEditorActivity extends AppCompatActivity implements XRefreshLayout.OnLoadMoreListener,
        XRefreshLayout.OnRefreshListener, XRefreshLayout.OnBackgroundClickListener, OnServiceBroadcastListener<ChatRoomEntity>,
        BroadcastEditorLayout.OnBroadcastEditorListener<MessageEntity>, NewEmoticonLayout.OnEmoticonSelectListener<StickerItemEntity>{
    public ActivityServiceBroadcastEditorBinding vb;
    public ServiceBroadcastEditorView binding;
    private Context context;
    BroadcastEditorViewModel model;

    String userId;
    RoomThemeStyle themeStyle = RoomThemeStyle.SERVICES;

    public static final int MEDIA_SELECTOR_REQUEST_CODE = 9989;
    public static final int FILE_EXPLORER_SELECTOR_REQUEST_CODE = 9990;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
        vb = ActivityServiceBroadcastEditorBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());
//        vb = DataBindingUtil.setContentView(this, R.layout.activity_service_broadcast_editor);
        binding = new ServiceBroadcastEditorView(this);
        model = new BroadcastEditorViewModel(this);
        String broadcastRoomId = getIntent().getStringExtra(BundleKey.BROADCAST_ROOM_ID.key());
        String serviceNumberId = getIntent().getStringExtra(BundleKey.SERVICE_NUMBER_ID.key());
        getServiceNumberEntity(serviceNumberId, broadcastRoomId);


        // 浮動按鈕設置
        model.hiedOrShowFloatingButton(0.0f, vb.difb);

        EventBusUtils.register(this);
        UserProfileService.getSelfProfile(this, RefreshSource.LOCAL, new ServiceCallBack<UserProfileEntity, RefreshSource>() {
            @Override
            public void complete(UserProfileEntity profile, RefreshSource source) {

                model.onBind(profile);

            }

            @Override
            public void error(String message) {

            }
        });


        vb.rlRecorder.setFinishCallBack((s, integer) -> {
            onSendRecord();
            return null;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bundle bundle = getIntent().getBundleExtra("PREVIEW");
        if (bundle != null) {
            String type = bundle.getString(BundleKey.TYPE.key());
            boolean isOriginal = bundle.getBoolean(BundleKey.IS_ORIGINAL.key());
            String dataJson = bundle.getString(BundleKey.DATA.key());
            Map<String, String> data = JsonHelper.getInstance().fromToMap(dataJson);
            TreeMap<String, String> treeData = Maps.newTreeMap((o1, o2) -> ComparisonChain.start()
                    .compare(o1, o2)
                    .result());
            treeData.putAll(data);
            vb.funMedia.setSelectData(type, isOriginal, treeData);
            vb.nklInput.clearFocus();
            onOpenPhotoSelectorFun(true);
            getIntent().removeExtra("PREVIEW");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
//                case TOPIC_SELECTOR_REQUEST_CODE:
//                    String json = data.getStringExtra(BundleKey.TOPIC_SELECT_IDS.key());
//                    List<TopicEntity> selectTopicEntities = JsonHelper.getInstance().fromToList(json, TopicEntity[].class);
//                    vb.belEditor.setSelectData(selectTopicEntities);
//                    CELog.e("");
//                    break;
                case FILE_EXPLORER_SELECTOR_REQUEST_CODE:
                    List<String> filePaths = data.getStringArrayListExtra(BundleKey.FILE_PATH_LIST.key());
                    for (String filePath : filePaths) {
                        String type = FileHelper.getFileTyle(filePath);
                        if (FileMedia.isImage(type)) {
                            model.showBroadcastEditor(true, ImageContent.Build().thumbnailUrl(filePath).url(filePath).width(1).height(1).build(), vb.belEditor);
                        } else if (FileMedia.isVideo(type)) {
                            IVideoSize iVideoSize = new VideoSizeFromVideoFile(filePath);
                            String path = iVideoSize.path();
                            String name = iVideoSize.name();
                            int width = iVideoSize.width();
                            int height = iVideoSize.height();
                            model.showBroadcastEditor(true, new VideoContent(height, width, name, path), vb.belEditor);
                        } else {
                            model.showBroadcastEditor(true, new FileContent(filePath, filePath), vb.belEditor);
                        }
                    }
                    break;
                case MEDIA_SELECTOR_REQUEST_CODE:
                    CELog.e(data + "");
                    getIntent().putExtra("PREVIEW", data.getExtras());
                    break;
                case PictureConfig.CHOOSE_REQUEST:
//                    List<LocalMedia> mLocalMedias = PictureSelector.obtainMultipleResult(data);
//                    for (LocalMedia localMedia : mLocalMedias) {
//                        String pictureType = localMedia.getPictureType();
//                        if (Strings.isNullOrEmpty(pictureType)) {
//                            return;
//                        }
//
//                        if ("image/png".equalsIgnoreCase(pictureType) || "image/jpeg".equalsIgnoreCase(pictureType) || "image/gif".equalsIgnoreCase(pictureType)) {
//                            model.showBroadcastEditor(true, ImageContent.Build().thumbnailUrl(localMedia.getPath()).url(localMedia.getPath()).width(1).height(1).build(), vb.belEditor);
//                            CELog.w("");
//                        }
//
//                        if ("video/mp4".equals(pictureType)) {
//                            IVideoSize iVideoSize = new VideoSizeFromVideoFile(localMedia.getPath());
//                            String path = iVideoSize.path();
//                            String name = iVideoSize.name();
//                            int width = iVideoSize.width();
//                            int height = iVideoSize.height();
//                            model.showBroadcastEditor(true, new VideoContent(height, width, name, path), vb.belEditor);
//                            CELog.w("");
//                        }
////                        model.callKeyBack(500L);
//                    }
                    break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        InputLogBean bean = vb.nklInput.getUnfinishedEditBean();
        if (model.getRoomEntity() != null) {
            String roomId = model.getRoomEntity().getId();
            String content = Strings.isNullOrEmpty(bean.getText().trim()) ? "" : bean.getText();
            bean.setText(content);
            ChatRoomReference.getInstance().updateUnfinishedEditedAndTimeById(roomId, bean.toJson());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        EventBusUtils.unregister(this);
    }

    public void refresh(boolean isNew) {
        vb.rvMessageList.refreshToBottom(isNew);
    }

    /**
     * 檢查數據(服務號實體)
     *
     * @param serviceNumberId
     */
    private void getServiceNumberEntity(String serviceNumberId, String broadcastRoomId) {
        if (this.userId == null) {
            this.userId = TokenPref.getInstance(this).getUserId();
        } else {
            String newUserId = TokenPref.getInstance(this).getUserId();
            this.userId = this.userId.equals(newUserId) ? this.userId : newUserId;
        }

        ServiceNumberEntity entity = ServiceNumberReference.findBroadcastRoomByIdAndServiceNumberId(null, broadcastRoomId, serviceNumberId);
        if (entity != null) {
            getBroadcastRoomEntity(entity);
        } else {
            ChatServiceNumberService.findServiceNumber(this, serviceNumberId, RefreshSource.REMOTE, new ServiceCallBack<ServiceNumberEntity, RefreshSource>() {
                @Override
                public void complete(ServiceNumberEntity entity, RefreshSource source) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> getBroadcastRoomEntity(entity));
                }

                @Override
                public void error(String message) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                        model.toast(message, false);
                        finish();
                    });
                }
            });
        }
    }

    /**
     * 檢查數據 (ChatRoomEntity)
     *
     * @param serviceNumberEntity
     */
    private void getBroadcastRoomEntity(ServiceNumberEntity serviceNumberEntity) {
        model.onBind(serviceNumberEntity);


        ChatRoomEntity entity = ChatRoomReference.getInstance().findByRoomIdAndServiceNumberId(serviceNumberEntity.getBroadcastRoomId(), serviceNumberEntity.getServiceNumberId());
        if (entity != null) {
            if (!ChatRoomType.broadcast.equals(entity.getType())) {
                model.toast("非廣播聊天室", false);
                finish();
                return;
            }
            onBindEntity(userId, entity);
        } else {
            ChatRoomService.getInstance().getChatRoomItem(ServiceBroadcastEditorActivity.this, userId, serviceNumberEntity.getBroadcastRoomId(), RefreshSource.REMOTE, new ServiceCallBack<ChatRoomEntity, RefreshSource>() {
                @Override
                public void complete(ChatRoomEntity entity, RefreshSource source) {
                    if (!ChatRoomType.broadcast.equals(entity.getType())) {
                        model.toast("非廣播聊天室", false);
                        finish();
                        return;
                    }
                    entity.setServiceNumberId(serviceNumberEntity.getServiceNumberId());
                    entity.setServiceNumberAvatarId(serviceNumberEntity.getAvatarId());
                    entity.setServiceNumberName(serviceNumberEntity.getName());
                    entity.setUnReadNum(0);
                    entity.setServiceNumberName(serviceNumberEntity.getName());
                    ChatRoomReference.getInstance().save(entity);
                    onBindEntity(userId, entity);
                }

                @Override
                public void error(String message) {
                    model.toast(message, false);
                    finish();
                }
            });
        }
    }

    /**
     * 綁定數據
     *
     * @param userId
     * @param entity
     */
    private void onBindEntity(String userId, ChatRoomEntity entity) {
        model.onBind(entity);
        vb.ncrtlToolbar.bind(model.getRoomEntity());
        vb.ncrtlToolbar.refresh();
        vb.nklInput.bind(model.getRoomEntity());
        vb.nklInput.refresh();

        themeStyle(themeStyle);

        vb.rvMessageList.setContainer(model.getMessageEntities(), entity, null);
        vb.rvMessageList.setOnMessageControlEventListener(onMainMessageControlEventListener);

        String unfinishedEdited = ChatRoomReference.getInstance().getUnfinishedEdited(entity.getId());
        if (Strings.isNullOrEmpty(unfinishedEdited)) {
        } else {
            vb.nklInput.setUnfinishedEdited(InputLogBean.from(unfinishedEdited));
        }

        // 設定黑名單
//        Set<MessageType> blackList = Sets.newHashSet(MessageType.values());
//        blackList.remove(MessageType.BROADCAST);
//        vb.rvMessageList.setBlackList(blackList);
//
//        model.findMessageList(entity.getId());
    }

    /**
     * 聊天室 樣式設定
     *
     * @param themeStyle
     */
    private void themeStyle(RoomThemeStyle themeStyle) {
        getWindow().setStatusBarColor(themeStyle.getMainColor());
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().setNavigationBarColor(themeStyle.getKeyboardColor());
//        }
    }

    @Override
    public void onBackClick(ChatRoomEntity entity, View view) {
        finish();
    }

    @Override
    public void onTitleBoxClick(ChatRoomEntity entity, View view) {

    }

    @Override
    public void onPenClick(ChatRoomEntity entity, View view) {

    }

    @Override
    public void onChannelClick(ChatRoomEntity entity, View view) {

    }

    @Override
    public void onSearchClick(ChatRoomEntity entity, View view) {

    }

    @Override
    public void onInviteClick(ChatRoomEntity entity, View view) {

    }

    @Override
    public void onCallClick(ChatRoomEntity entity, View view) {

    }

    @Override
    public void onDropDownClick(ChatRoomEntity entity, View view) {

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }

    /**
     * 打開錄影
     */
    @Override
    public void onOpenVideoFun() {
        // 选择拍摄
        PictureSelector.create(this)
                .openCamera(SelectMimeType.ofVideo()) // 影片
                .setRecordVideoMaxSecond(120)
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        for (LocalMedia localMedia : result) {
                            IVideoSize iVideoSize = new VideoSizeFromVideoFile(localMedia.getRealPath());
                            String path = iVideoSize.path();
                            String name = iVideoSize.name();
                            int width = iVideoSize.width();
                            int height = iVideoSize.height();
                            model.showBroadcastEditor(true, new VideoContent(height, width, name, path), vb.belEditor);
                            CELog.w("");
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    /**
     * 打開錄音
     */
    @Override
    public void onOpenRecordFun() {
        XXPermissions.with(context).permission(Manifest.permission.RECORD_AUDIO).request((permissions, all) -> {
            if (vb.belEditor.getEntity() != null && !BroadcastFlag.BOOKING.equals(vb.belEditor.getEntity().getBroadcastFlag())) {
                model.toast("不可編輯", false);
                return;
            }
            model.showCountdownTimer(getWindow().getDecorView(), 2, vb.rlRecorder);
        });
    }

    /**
     * 打開相機
     */
    @Override
    public void onOpenCameraFun() {
        PictureSelector.create(this)
                .openCamera(SelectMimeType.ofImage()) //图片
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        for (LocalMedia localMedia : result) {
                            model.showBroadcastEditor(true, ImageContent.Build().thumbnailUrl(localMedia.getRealPath()).url(localMedia.getRealPath()).width(1).height(1).build(), vb.belEditor);
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    /**
     * 打開多媒體選擇器（File）
     */
    @Override
    public void onOpenMediaSelectorFun() {
        vb.funEmoticon.setVisibility(View.GONE);
        vb.funMedia.setType(this, MultimediaHelper.Type.FILE, themeStyle, -1);
        if (vb.funMedia.getVisibility() == View.GONE) {
            vb.funMedia.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 打開底部圖片選擇器
     *
     * @param isChange
     */
    @Override
    public void onOpenPhotoSelectorFun(boolean isChange) {
        vb.funEmoticon.setVisibility(View.GONE);
        if (isChange) {
            vb.funMedia.setChangeVisibility();
        } else {
            vb.funMedia.setType(this, MultimediaHelper.Type.IMAGE, themeStyle, MEDIA_SELECTOR_REQUEST_CODE);
        }

        if (vb.funMedia.getVisibility() == View.GONE) {
            vb.funMedia.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 打開相簿
     *
     * @param mode
     */
    @Override
    public void onOpenGallery(int mode) {
        model.callKeyBack(-1L);
        PictureSelector.create(this)
                .openGallery(SelectMimeType.ofImage())
                .setMaxSelectNum(1)
                .setMinSelectNum(1)
                .isPreviewImage(true)
                .isDisplayCamera(false)
                .isOriginalSkipCompress(false)
                .isGif(true)
                .isOpenClickSound(false)
                .setImageEngine(GlideEngine.Companion.createGlideEngine())
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        for (LocalMedia localMedia : result) {
                            model.showBroadcastEditor(true, ImageContent.Build().thumbnailUrl(localMedia.getRealPath()).url(localMedia.getRealPath()).width(1).height(1).build(), vb.belEditor);

                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    /**
     * 打開資料夾
     *
     * @param limit
     */
    @Override
    public void onOpenFileFolder(int limit) {
        model.callKeyBack(-1L);
        ActivityTransitionsControl.navigateToFileExplorer(this, 1, (intent, s) -> startActivityForResult(intent, FILE_EXPLORER_SELECTOR_REQUEST_CODE));
    }

    /**
     * 導航到圖片選擇器預覽器
     */
    @Override
    public void toMediaSelectorPreview(boolean isOriginal, String type, String current, TreeMap<String, String> data, int maxCount) {
        ActivityTransitionsControl.navigateToMediaSelectorPreview(this, isOriginal, type, current, maxCount, data, (intent, s) -> {
            startActivityForResult(intent, MEDIA_SELECTOR_REQUEST_CODE);
        });
    }

    /**
     * 圖片,多媒體（File) 選擇器，選取完成事件處理
     */
    @Override
    public void onMediaSelector(MultimediaHelper.Type type, List<AMediaBean> list, boolean isOriginal) {
        switch (type) {
            case IMAGE:
                for (AMediaBean bean : list) {
                    model.showBroadcastEditor(true, ImageContent.Build().thumbnailUrl(bean.getPath()).url(bean.getPath()).width(1).height(1).build(), vb.belEditor);
                }
                break;
            case FILE:
                for (AMediaBean bean : list) {
                    String filePath = bean.getPath();
                    String fileType = FileHelper.getFileTyle(filePath);
                    if (FileMedia.isImage(fileType)) {
                        model.showBroadcastEditor(true, ImageContent.Build().thumbnailUrl(filePath).url(filePath).width(1).height(1).build(), vb.belEditor);
                    } else if (FileMedia.isVideo(fileType)) {
                        IVideoSize iVideoSize = new VideoSizeFromVideoFile(filePath);
                        String path = iVideoSize.path();
                        String name = iVideoSize.name();
                        int width = iVideoSize.width();
                        int height = iVideoSize.height();
                        model.showBroadcastEditor(true, new VideoContent(height, width, name, path), vb.belEditor);
                    } else {
                        model.showBroadcastEditor(true, new FileContent(filePath, filePath), vb.belEditor);
                    }
                }
//                for (AMediaBean bean : list) {
//                    model.showBroadcastEditor(true, FileContent.Build().name(bean.getFileName()).android_local_path(bean.getPath()).build(), vb.belEditor);
//                }
                break;
        }

        model.callKeyBack(500L);
    }

    /**
     * 打開表情，貼圖功能
     */
    @Override
    public void onOpenFacialFun() {
        vb.funMedia.setVisibility(View.GONE);
        if (vb.funEmoticon.getVisibility() != View.VISIBLE) {
            vb.funEmoticon.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 選取了表情事件處理
     *
     * @param entity
     */
    @Override
    public void onEmoticonSelect(StickerItemEntity entity) {
        vb.nklInput.append(entity.getName(), true);
    }

    /**
     * 選取了貼圖事件處理
     *
     * @param entity
     * @param drawable
     */
    @Override
    public void onStickerSelect(StickerItemEntity entity, Drawable drawable) {
        model.showBroadcastEditor(true,
                new StickerContent(entity.getId(), entity.getStickerPackageId()), vb.belEditor);
        model.callKeyBack(-1L);

    }

    /**
     * 關閉底部功能事件處理
     *
     * @param type
     */
    @Override
    public void onCloseFun(NewKeyboardLayout.NewKeyboardFun type) {
        if (type == null) {
            vb.funMedia.setVisibility(View.GONE);
            vb.funEmoticon.setVisibility(View.GONE);
        }
    }

    /**
     * 送出事件
     */
    @Override
    public void onSendAction(HadEditText.SendData sendData, boolean enableSend) {
        if (sendData == null || Strings.isNullOrEmpty(sendData.getContent())) {
            return;
        }

        MessageEntity entity = vb.belEditor.getEntity();
        if (entity != null && !BroadcastFlag.BOOKING.equals(entity.getBroadcastFlag())) {
            model.toast("不可編輯", false);
            return;
        }

        KeyboardHelper.hide(vb.nklInput);

        if (entity != null && entity.isCanEditBroadcast()) {
            model.showUpdateBroadcastEditor(true, new TextContent(sendData.getContent()), vb.belEditor.getEntity().getTopicArray(), vb.belEditor);
            vb.nklInput.setText("", false);
            onCloseFun(null);
            return;
        }

        model.showBroadcastEditor(true, new TextContent(sendData.getContent()), vb.belEditor);
        vb.nklInput.setText("", false);
        onCloseFun(null);
    }


    @Override
    public void onStopScrolling(RecyclerView recyclerView) {
//        model.setFloatingToBottomAction(recyclerView, vb.difb);
    }

    @Override
    public void onDragScrolling(RecyclerView recyclerView) {
        model.hideOrShowTimeToast(recyclerView, vb.tvFloatTimeBox);
    }

    @Override
    public void onAutoScrolling(RecyclerView recyclerView) {

    }

    public void onSendRecord() {
        String path = AudioLib.getInstance(this).currentPath();
        File voiceFile = new File(path);
        if (voiceFile.exists()) {
            double duration = VoiceHelper.getVoiceDuration(voiceFile);
            model.showBroadcastEditor(true, new VoiceContent(duration, path), vb.belEditor);
            vb.nklInput.setFunctionBtnUnSelect(NewKeyboardLayout.NewKeyboardFun.FUN_RECORD);
        }
        model.callKeyBack(-1L);
    }

    /**
     * 群發內容編輯器取消事件處理
     */
    @Override
    public void onEditorCancel(MessageEntity entity) {
        if (entity != null && Strings.isNullOrEmpty(entity.getId()) && MessageStatus.FAILED_or_ERROR.contains(entity.getStatus())) {
            new AlertView.Builder()
                    .setContext(this)
                    .setStyle(AlertView.Style.Alert)
                    .setMessage("有訊息正在編輯中，是否退出編輯")
                    .setOthers(new String[]{"取消", "確定"})
                    .setOnItemClickListener((o, position) -> {
                        if (position == 1) {
                            vb.belEditor.bind(null);
                            vb.belEditor.setVisibility(View.GONE);
                        }
                    }).build()
                    .show();
        } else {
            vb.belEditor.bind(null);
            vb.belEditor.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEditorDelete(MessageEntity entity) {
        model.delete(entity);
        vb.belEditor.bind(null);
        vb.belEditor.setVisibility(View.GONE);
    }

    @Override
    public void onEditorSend(BroadcastMessageBean.BroadcastMessageBeanBuilder build, MessageEntity entity) {
        if (build.build().topicIds().isEmpty()) {
            model.toast("請至少選擇一個發送對象", false);
            return;
        }
        vb.belEditor.setVisibility(View.GONE);
        model.sendOrUpdateBroadcast(build, entity);
    }

    @Override
    public void onEditorEdit(MessageEntity entity) {
        if (MessageType.TEXT.equals(entity.getType())) {
            vb.funMedia.setVisibility(View.GONE);
            vb.funEmoticon.setVisibility(View.GONE);
            vb.nklInput.setFunctionBtnSelectStatus(null);
            vb.nklInput.setText(entity.content().simpleContent(), true);
            vb.nklInput.openKeyboard();
        }
    }

    @Override
    public void onEditorVisibilityChange(MessageEntity entity, int visibility) {

    }

    private void setVisibility(View view, int visibility) {
        view.setVisibility(visibility);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (vb.funMedia.getVisibility() == View.VISIBLE) {
                vb.nklInput.setFunctionBtnUnSelect(FUN_PHOTO);
                vb.nklInput.setFunctionBtnUnSelect(FUN_MEDIA);
                setVisibility(vb.funMedia, View.GONE);
                return true;
            }

            if (vb.rlRecorder.getVisibility() == View.VISIBLE) {
//                model.onCancelRecord(vb.rlRecorder);
//                vb.nklInput.setFunctionBtnUnSelect(NewKeyboardLayout.NewKeyboardFun.FUN_RECORD);
                return true;
            }

            if (vb.funEmoticon.getVisibility() == View.VISIBLE) {
                vb.nklInput.setFunctionBtnUnSelect(FUN_FACIAL);
                setVisibility(vb.funEmoticon, View.GONE);
                return true;
            }
            if (vb.belEditor.getVisibility() == View.VISIBLE) {
                vb.belEditor.setVisibility(View.GONE);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    // 下拉刷新，上划加載
    @Override
    public void onLoadMore() {
        vb.xRefreshLayout.completeRefresh();
        model.loadMoreOnTheRemote(Sort.ASC);
    }

    @Override
    public void onRefresh() {
        vb.xRefreshLayout.completeRefresh();
        model.loadMoreOnTheRemote(Sort.DESC);
    }

    @Override
    public void onBackgroundClick(XRefreshLayout refreshLayout) {
        CELog.e("");
    }

    OnMainMessageControlEventListener<MessageEntity> onMainMessageControlEventListener = new OnMainMessageControlEventListener<MessageEntity>() {
        @Override
        public void onItemClick(MessageEntity entity) {
            if (MessageType.BROADCAST.equals(entity.getType())) {
                BroadcastContent broadcastContent = (BroadcastContent) entity.content();
                entity.setType(broadcastContent.getInsideType());
                entity.setContent(broadcastContent.getContent());
                model.showBroadcastEditor(false, entity, vb.belEditor);
            }
        }

        @Override
        public void makeUpMessages(MessageEntity current, MessageEntity previous) {

        }

        @Override
        public void doRangeSelection(MessageEntity entity) {

        }

        @Override
        public void onItemChange(MessageEntity entity) {

        }

        @Override
        public void onInvalidAreaClick(MessageEntity entity) {

        }

        @Override
        public void onImageClick(MessageEntity entity) {
        }

        @Override
        public void onLongClick(MessageEntity msg, int pressX, int pressY) {

        }

        @Override
        public void onTipClick(MessageEntity entity) {

        }

        @Override
        public void onAvatarClick(String senderId) {

        }

        @Override
        public void onSubscribeAgentAvatarClick(String senderId) {

        }

        @Override
        public void onAtSpanClick(String userId) {

        }

        @Override
        public void onAvatarLoad(ImageView iv, String senderId) {

        }

        @Override
        public void onSendNameClick(String sendId) {

        }

        @Override
        public void onContentUpdate(String msgId, String formatName, String formatContent) {

        }

        @Override
        public void updateReplyMessageWhenVideoDownload(String messageId) {

        }

        @Override
        public void copyText(MessageEntity entity) {

        }

        @Override
        public void replyText(MessageEntity entity) {

        }

        @Override
        public void tranSend(MessageEntity entity) {

        }

        @Override
        public void retry(MessageEntity entity) {

        }

        @Override
        public void cellect(MessageEntity entity) {

        }

        @Override
        public void shares(MessageEntity entity, View image) {

        }

        @Override
        public void choice() {

        }

        @Override
        public void delete(MessageEntity entity) {

        }

        @Override
        public void enLarge(MessageEntity entity) {

        }

        @Override
        public void onPlayComplete(MessageEntity entity) {

        }

        @Override
        public void onStopOtherVideoPlayback(MessageEntity entity) {

        }

        @Override
        public void retractMsg(MessageEntity entity) {

        }

        @Override
        public void showRePlyPanel(MessageEntity entity) {

        }

        @Override
        public void locationMsg(MessageEntity entity) {

        }

        @Override
        public void findReplyMessage(String messageId) {

        }

        @Override
        public void onVideoClick(MessageEntity entity) {

        }
    };


    /**
     * EventBus 全局事件處理
     *
     * @param eventMsg
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(EventMsg eventMsg) {
        switch (eventMsg.getCode()) {
            case MsgConstant.UI_NOTICE_BROADCAST_EDIT_TO_DEFAULT: // Broadcast 編輯器還回預設狀態
                vb.belEditor.bind(null);
                vb.belEditor.setVisibility(View.GONE);
                break;
            case MsgConstant.INTERNET_STSTE_FILTER: //網路監聽
                boolean isInternet = (boolean) eventMsg.getData();
                if (isInternet) {
                    model.loadMoreOnTheRemote(Sort.ASC);
                }
                break;
            case MsgConstant.MSG_RECEIVED_FILTER:
                MessageEntity receiverMessage = JsonHelper.getInstance().from(eventMsg.getString(), MessageEntity.class);
                if (receiverMessage != null) {
                    model.appendMessageEntity(receiverMessage);
                }
                break;
            case MsgConstant.NOTICE_TOPIC_SELECTOR:
                String json = eventMsg.getString();
                List<TopicEntity> selectTopicEntities = JsonHelper.getInstance().fromToList(json, TopicEntity[].class);
                vb.belEditor.setSelectData(selectTopicEntities);
                break;
            case MsgConstant.NOTICE_BROADCAST_MESSAGE_DELETE:
                MessageEntity deleteEntity = JsonHelper.getInstance().from(eventMsg.getString(), MessageEntity.class);
                model.removeMessageEntity(deleteEntity);
                break;
            case MsgConstant.NOTICE_APPEND_MESSAGE: // Queue Message Item Response
                MessageEntity appendMessage = JsonHelper.getInstance().from(eventMsg.getString(), MessageEntity.class);
                if (appendMessage != null) {
                    model.appendMessageEntity(appendMessage);
                    boolean isShow = false;
                    if (BroadcastFlag.DELETED.equals(appendMessage.getBroadcastFlag())) {
                        model.toast("群發訊息已刪除", false);
                        isShow = true;
                    }
                    if (vb.belEditor.getVisibility() == View.VISIBLE) {
//                        if (vb.belEditor.getEntity() != null && appendMessage.getId().equals(vb.belEditor.getEntity().getId())) {
//                            vb.belEditor.setVisibility(View.GONE);
//                            if (!isShow) {
//                                model.toast("資料被更新", false);
//                            }
//                        }
                    }
                }
                break;
            case MsgConstant.NOTICE_BROADCAST_MESSAGE_UPDATE:
                MessageEntity updateEntity = JsonHelper.getInstance().from(eventMsg.getString(), MessageEntity.class);
                RepairMessageService.setQueue(this, updateEntity.getRoomId(), Sets.newHashSet(updateEntity.getId()), false);
                break;
            case MsgConstant.NOTICE_BROADCAST_FLAG_STATUS:
                MessageEntity statusMessage = JsonHelper.getInstance().from(eventMsg.getString(), MessageEntity.class);
                model.appendMessageEntity(statusMessage);
                break;
//            case MsgConstant.SEND_PHOTO_MEDIA_SELECTOR:
//                String selectorJson = eventMsg.getString();
//                Map<String, String> selectorData = JsonHelper.getInstance().fromToMap(selectorJson);
//                String isOriginalStr = selectorData.get("isOriginal");
//                String listStr = selectorData.get("list");
//                List<ImageBean> list = JsonHelper.getInstance().fromToList(listStr, ImageBean[].class);
//                CELog.e(selectorJson);
//                for (ImageBean bean : list) {
//                    model.showBroadcastEditor(true, ImageContent.Build().thumbnailUrl(bean.getPath()).url(bean.getPath()).width(1).height(1).build(), vb.belEditor);
//                }
//                model.callKeyBack(500L);
//                break;
        }
    }
}
