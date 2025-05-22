package tw.com.chainsea.chat.view.gallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.List;

import tw.com.chainsea.chat.util.DaVinci;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.client.type.Media;
import tw.com.chainsea.android.common.image.BitmapHelper;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.base.MsgBuilder;
import tw.com.chainsea.ce.sdk.bean.MsgStatusBean;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.request.MessageSendRequest;
import tw.com.chainsea.ce.sdk.http.ce.request.UploadManager;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.service.ChatRoomService;
import tw.com.chainsea.ce.sdk.service.FileService;
import tw.com.chainsea.ce.sdk.service.listener.AServiceCallBack;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.base.Constant;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.config.InvitationType;
import tw.com.chainsea.chat.databinding.ActivityScreenshotsPreviewBinding;
import tw.com.chainsea.chat.lib.PictureParse;
import tw.com.chainsea.chat.lib.ToastUtils;
import tw.com.chainsea.chat.lib.Tools;
import tw.com.chainsea.chat.messagekit.enums.RichMenuBottom;
import tw.com.chainsea.chat.searchfilter.view.activity.MemberInvitationActivity;
import tw.com.chainsea.chat.util.DownloadUtil;
import tw.com.chainsea.chat.util.IntentUtil;
import tw.com.chainsea.chat.util.ThemeHelper;
import tw.com.chainsea.chat.view.BaseActivity;
import tw.com.chainsea.chat.widget.photoview.PhotoViewAttacher;

public class ScreenshotsPreviewActivity extends BaseActivity {
    ActivityScreenshotsPreviewBinding binding;
    private static final int RESULT_CODE = 0x1389;
    private boolean isGreenTheme = ThemeHelper.INSTANCE.isGreenTheme();
    private boolean isServiceRoomTheme = ThemeHelper.INSTANCE.isServiceRoomTheme();
    int count = -1;
    private ActivityResultLauncher<Intent> shareScreenShotResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_screenshots_preview);
        initBgColor();
        String filePath = getIntent().getStringExtra(BundleKey.FILE_PATH.key());
        Bitmap screenshots = BitmapHelper.getBitmapFromLocal(filePath);
        binding.photoView.setImageBitmap(screenshots);
        binding.photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                CELog.e("");
                openOrClose();
            }

            @Override
            public void onOutsidePhotoTap() {
                CELog.e("");
            }
        });


        String action = getIntent().getStringExtra(BundleKey.ACTION.key());

        if (RichMenuBottom.TASK.name().equals(action)) {
            binding.tvShare.setText(R.string.alert_confirm);
        } else {
            binding.tvShare.setText(binding.tvShare.getText());
        }

        binding.tvSave.setVisibility(RichMenuBottom.TASK.name().equals(action) ? View.GONE : View.VISIBLE);

        initListener();
    }

    private void initBgColor() {
        Window w = this.getWindow();
        w.setStatusBarColor(0xFF000000);
        binding.tvCancel.setBackgroundColor(getColor(isGreenTheme && isServiceRoomTheme ? R.color.color_6BC2BA : isGreenTheme ? R.color.color_015F57 : R.color.colorPrimary));
        binding.tvSave.setBackgroundColor(getColor(isGreenTheme && isServiceRoomTheme ? R.color.color_6BC2BA : isGreenTheme ? R.color.color_015F57 : R.color.colorPrimary));
        binding.tvShare.setBackgroundColor(getColor(isGreenTheme && isServiceRoomTheme ? R.color.color_6BC2BA : isGreenTheme ? R.color.color_015F57 : R.color.colorPrimary));
    }

    private void openOrClose() {
        if (binding.clToolBar.getVisibility() == View.GONE) {
            binding.clToolBar.setVisibility(View.VISIBLE);
            binding.clBottomFunction.setVisibility(View.VISIBLE);
        } else {
            binding.clToolBar.setVisibility(View.GONE);
            binding.clBottomFunction.setVisibility(View.GONE);
        }
    }

    private void initListener() {
        binding.leftAction.setOnClickListener(this::doBackAction);
        binding.tvCancel.setOnClickListener(this::doBackAction);
        binding.tvShare.setOnClickListener(this::dotShareAction);
        binding.tvSave.setOnClickListener(this::doSaveAction);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 因為他有 override onActivityResult 所以結果會到那邊去
        shareScreenShotResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result != null && result.getData() != null) {
                Bundle bundle = result.getData().getExtras();
                if (bundle != null) {
                    Type typeToken = new TypeToken<List<String>>() {
                    }.getType();
                    List<String> ids = JsonHelper.getInstance().from(bundle.getString("data"), typeToken);
                    String filePath = bundle.getString(BundleKey.FILE_PATH.key());
                    doExecutionSendImage(ids, filePath);
                }
            }
        });
    }

    private void doExecutionSendImage(List<String> sessionIds, String path) {
        //            String[] paths = PictureParse.parseUri(requireContext(), uri);
        String token = TokenPref.getInstance(this).getTokenId();
//            String path = DaVinci.with().getImageLoader().getAbsolutePath(paths[0]);
        FileService.uploadFile(this, true, token, Media.findByFileType(path), path, path, new AServiceCallBack<UploadManager.FileEntity, RefreshSource>() {
            @Override
            public void complete(UploadManager.FileEntity entity, RefreshSource refreshSource) {
                for (int i = 0; i < sessionIds.size(); i++) {
                    String mMessageId = tw.com.chainsea.chat.lib.Tools.generateMessageId();
                    String roomId = sessionIds.get(i);
                    ApiManager.doMessageSend(ScreenshotsPreviewActivity.this, roomId, mMessageId, "",
                        new MsgBuilder(MessageType.IMAGE)
                            .url(entity.getUrl())
                            .size(entity.getSize())
                            .width(entity.getWidth())
                            .height(entity.getHeight())
                            .thumbnailUrl(entity.getThumbnailUrl())
                            .thumbnailSize(entity.getThumbnailSize())
                            .name("pic")
                        , new MessageSendRequest.Listener<MessageEntity>() {
                            @Override
                            public void onFailed(MessageEntity message, String errorMessage) {
                                Toast.makeText(ScreenshotsPreviewActivity.this, getString(R.string.text_share_media_failure), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess(MessageEntity message) {
                                Toast.makeText(ScreenshotsPreviewActivity.this, "分享成功", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                }
            }

            @Override
            public void onProgress(float progress, long total) {

            }

            @Override
            public void error(String message) {
                CELog.e(message);
                Toast.makeText(ScreenshotsPreviewActivity.this, getString(R.string.text_share_media_failure), Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_CODE) {
                String userId = TokenPref.getInstance(this).getUserId();
                String filePath = getIntent().getStringExtra(BundleKey.FILE_PATH.key());
                String[] roomIds = data.getStringArrayExtra(Constant.ACTIVITY_RESULT);
                List<ChatRoomEntity> roomEntities = ChatRoomReference.getInstance().findByIds(userId, Lists.newArrayList(roomIds), true, true, true);
                count = roomIds.length;
                for (String roomId : roomIds) {
                    if (roomEntities.contains(ChatRoomEntity.Build().id(roomId).build())) {
                        count--;
                        if (count == 0) {
                            sendImageToRooms(Lists.newArrayList(roomIds), filePath);
                        }
                    } else {
                        ChatRoomService.getInstance().getChatRoomItem(this, userId, roomId, RefreshSource.REMOTE, new ServiceCallBack<ChatRoomEntity, RefreshSource>() {
                            @Override
                            public void complete(ChatRoomEntity entity, RefreshSource source) {
                                count--;
                                if (count == 0) {
                                    sendImageToRooms(Lists.newArrayList(roomIds), filePath);
                                }
                            }

                            @Override
                            public void error(String message) {
                                count--;
                                if (count == 0) {
                                    sendImageToRooms(Lists.newArrayList(roomIds), filePath);
                                }
                            }
                        });
                    }
                }
                // 截圖的分享
            } else {
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        Type typeToken = new TypeToken<List<String>>() {
                        }.getType();
                        List<String> ids = JsonHelper.getInstance().from(bundle.getString("data"), typeToken);
                        String path = bundle.getString(BundleKey.FILE_PATH.key());
                        doExecutionSendImage(ids, path);
                    }
                }
            }
        }
    }


    private void sendImageToRooms(List<String> roomIds, String filePath) {
        String token = TokenPref.getInstance(this).getTokenId();
        String[] paths = PictureParse.parsePath(this, filePath);

        String path = DaVinci.with().getImageLoader().getAbsolutePath(paths[0]);
        FileService.uploadFile(this, true, token, Media.findByFileType(paths[0]), path, paths[0], new AServiceCallBack<UploadManager.FileEntity, RefreshSource>() {
            @Override
            public void complete(UploadManager.FileEntity entity, RefreshSource refreshSource) {
                for (int i = 0; i < roomIds.size(); i++) {
                    String roomId = roomIds.get(i);
                    String mMessageId = Tools.generateMessageId();

                    MsgBuilder msgBuilder = new MsgBuilder(MessageType.IMAGE)
                        .url(entity.getUrl())
                        .size(entity.getSize())
                        .width(entity.getWidth())
                        .height(entity.getHeight())
                        .thumbnailUrl(entity.getThumbnailUrl())
                        .thumbnailSize(entity.getThumbnailSize())
                        .name("pic");

                    ApiManager.doMessageSend(ScreenshotsPreviewActivity.this, roomId, mMessageId, "", msgBuilder, new MessageSendListener("[圖片]", i + 1) {
                        @Override
                        public void complete(int count, String msgId, boolean isSuccess) {
                            if (isSuccess && mMessageId.equals(msgId)) {
//                                ChatRoomReference.getInstance().updateLastMessageIdById(roomId, msgId);
                            }
                            completes(roomIds.size(), count);
                        }
                    });
                }
            }

            @Override
            public void onProgress(float progress, long total) {

            }

            @Override
            public void error(String message) {
                CELog.e(message);
                completes(roomIds.size(), roomIds.size());
            }
        });
    }


    private void completes(int length, int count) {
        if (length == count) {
            new Handler().postDelayed(() -> finish(), 500L);
        }
    }

    abstract static class MessageSendListener extends MessageSendRequest.Listener<MessageEntity> {
        private int count = -1;
        private String content = "";

        MessageSendListener(String content, int count) {
            this.count = count;
            this.content = content;
        }

        public abstract void complete(int count, String messageId, boolean isSuccess);

        @Override
        public void onSuccess(MessageEntity success) {
            try {
                DBManager.getInstance().updateMessageStatus(success.getId(), MessageStatus.SUCCESS);
                DBManager.getInstance().updateSendNum(success.getId(), success.getSendNum());
                DBManager.getInstance().updateSendTime(success.getId(), success.getSendTime());
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.MSG_STATUS_FILTER, new MsgStatusBean(success.getId(), success.getSendNum(), success.getSendTime())));
                // EVAN_FLAG 2019-10-19 Send a new reply message,
                //  Update the chat room interaction time
                boolean dd = ChatRoomReference.getInstance().updateInteractionTimeById(success.getRoomId());
                if (!Strings.isNullOrEmpty(this.content)) {
                    ChatRoomEntity entity = ChatRoomReference.getInstance().findById(success.getRoomId());
                }
            } catch (Exception e) {
            } finally {
                complete(this.count, success.getId(), true);
            }
            CELog.i("OutSide Share Send Success: ", String.format("id: %s , sendNum: %s , sendTime: %s ...", success.getId(), success.getSendNum(), success.getSendTime()));


        }

        @Override
        public void onFailed(MessageEntity failed, String errorMessage) {
            CELog.e("OutSide Share Send Failed: ", String.format("msgId: %s , errorMessage: %s", failed.getId(), errorMessage));
            complete(this.count, failed.getId(), false);
        }
    }

    private void doBackAction(View viw) {
        finish();
    }

    /**
     * Share button event
     *
     * @param view
     */
    private void dotShareAction(View view) {
        String filePath = getIntent().getStringExtra(BundleKey.FILE_PATH.key());
        String action = getIntent().getStringExtra(BundleKey.ACTION.key());
        if (RichMenuBottom.TASK.name().equals(action)) {
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_EXECUTION_BUSINESS_CREATE_ACTION, filePath));
            finish();
            return;
        }

        Bundle bundle = new Bundle();
//        bundle.putString(BundleKey.FROM_ROOM_IDS.key(), chatRoom.getId());
        bundle.putString(BundleKey.FILE_PATH.key(), filePath);
        bundle.putSerializable(BundleKey.ROOM_TYPE.key(), InvitationType.ShareIn.name());
        IntentUtil.INSTANCE.launchIntent(this, MemberInvitationActivity.class, shareScreenShotResult, bundle);
    }


    /**
     * Save button event
     *
     * @param view
     */
    void doSaveAction(View view) {
        String filePath = getIntent().getStringExtra(BundleKey.FILE_PATH.key());
        Bitmap screenshots = BitmapHelper.getBitmapFromLocal(filePath);
        String saveImagePath = null;
        String imageFileName = "JPEG_" + "down" + System.currentTimeMillis() + ".jpg";
        File storageDir = new File(DownloadUtil.INSTANCE.getDownloadImageDir());
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            saveImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fout = new FileOutputStream(imageFile);
                screenshots.compress(Bitmap.CompressFormat.JPEG, 100, fout);
                fout.close();
            } catch (Exception ignored) {

            }

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(saveImagePath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
            ToastUtils.showToast(this, String.format(getString(R.string.bruce_photo_save), saveImagePath));
        }
    }
}
