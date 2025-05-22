package tw.com.chainsea.chat.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.MimeTypeFilter;
import androidx.databinding.DataBindingUtil;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import tw.com.chainsea.chat.util.DaVinci;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.client.type.Media;
import tw.com.chainsea.android.common.file.FileHelper;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.video.IVideoSize;
import tw.com.chainsea.android.common.video.VideoSizeFromVideoFile;
import tw.com.chainsea.ce.sdk.base.MsgBuilder;
import tw.com.chainsea.ce.sdk.bean.MsgStatusBean;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.response.AileTokenApply;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.request.MessageSendRequest;
import tw.com.chainsea.ce.sdk.http.ce.request.UploadManager;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.reference.MessageReference;
import tw.com.chainsea.ce.sdk.service.FileService;
import tw.com.chainsea.ce.sdk.service.listener.AServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.config.InvitationType;
import tw.com.chainsea.chat.databinding.ActivityOutSideShareInBinding;
import tw.com.chainsea.chat.lib.ActivityManager;
import tw.com.chainsea.chat.lib.PictureParse;
import tw.com.chainsea.chat.lib.ToastUtils;
import tw.com.chainsea.chat.lib.Tools;
import tw.com.chainsea.chat.messagekit.enums.FileType;
import tw.com.chainsea.chat.refactor.welcomePage.WelcomeActivity;
import tw.com.chainsea.chat.searchfilter.view.activity.MemberInvitationActivity;
import tw.com.chainsea.chat.ui.activity.ChatActivity;
import tw.com.chainsea.chat.ui.activity.ChatNormalActivity;
import tw.com.chainsea.chat.util.IntentUtil;
import tw.com.chainsea.chat.view.base.HomeActivity;

/**
 * 接收外部分享仲介處理
 */
public class OutSideShareInActivity extends BaseActivity {
    private static final String TAG = OutSideShareInActivity.class.getSimpleName();
    private ActivityResultLauncher<Intent> outSideShareSend = null;
    private ActivityOutSideShareInBinding binding = null;
    private boolean isMultipleShare = false;
    private String orginal_roomId = "";

    enum MimeType {
        ALL("*/*"),
        UNKNOWN("unknown"),
        TEXT_PLAIN("text/plain"),
        IMAGE("image/*");

        private final String name;

        MimeType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static String[] getValues() {
            List<MimeType> mimeTypes = value();
            String[] values = new String[mimeTypes.size()];
            for (int i = 0; i < mimeTypes.size(); i++) {
                values[i] = mimeTypes.get(i).name;
            }
            return values;
        }

        public static List<MimeType> value() {
            return Lists.newArrayList(TEXT_PLAIN, IMAGE, ALL);
        }

        public static MimeType of(String name) {
            for (MimeType type : values()) {
                if (type.name.equals(name)) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_out_side_share_in);
        ActivityManager.addActivity(this);
        // 判斷是否為登入狀態，若未登入 Alert 提示
        String userId = TokenPref.getInstance(this).getUserId();
        if (Strings.isNullOrEmpty(userId)) {
            startActivity(new Intent(this, WelcomeActivity.class));
            return;
        }

        outSideShareSend = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    binding.clProgressBar.setVisibility(View.VISIBLE);
                    Bundle bundle = result.getData().getExtras();
                    MimeType mimeType = MimeType.of(MimeTypeFilter.matches(getIntent().getType(), MimeType.getValues()));
                    Type listType = new TypeToken<List<String>>() {
                    }.getType();
                    assert bundle != null;
                    List<String> accountIds = JsonHelper.getInstance().from(bundle.getString("data"), listType);
                    switch (mimeType) {
                        case ALL:
                            doExecutionSendAllType(accountIds, getIntent());
                            break;
                        case TEXT_PLAIN:
                            String shareText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
                            if (Strings.isNullOrEmpty(shareText))
                                doExecutionSendAllType(accountIds, getIntent());
                            else
                                doExecutionSendText(accountIds, shareText);
                            break;
                        case IMAGE:
                            Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
                            if (uri != null) {
                                doExecutionSendImage(accountIds, uri);
                            }
                            break;
                        case UNKNOWN:
                            break;
                        default:
                            finish();
                    }
                }
            }
        });

        // 取得外部分享進入的 MimeType & Data
        Intent intent = getIntent();
        if (intent != null) {
            try {
                Uri uri = Objects.requireNonNull(intent.getClipData()).getItemAt(0).getUri();
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r");
                assert pfd != null;
                long fileSize = pfd.getStatSize();
                AileTokenApply.Resp.TenantInfo tenantInfo = TokenPref.getInstance(this).getTenantInfo();
//                if(tenantInfo != null && fileSize > tenantInfo.getUploadFileMaxSize()){
                pfd.close();
                if (tenantInfo != null && fileSize > 102400000) { //workround 限制傳輸大小為100MB
                    Toast.makeText(this, "分享的檔案大小超過上限", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            } catch (Exception ignored) {
            }
            orginal_roomId = intent.getStringExtra(BundleKey.ROOM_ID.key());
            String action = intent.getAction();
            String type = intent.getType();
            if (intent.getExtras() != null) {
                isMultipleShare = intent.getExtras().getBoolean(BundleKey.IS_FROM_FILTER.key(), false);
            }
            if (Intent.ACTION_SEND.equals(action) && type != null) {
                MimeType mimeType = MimeType.of(MimeTypeFilter.matches(type, MimeType.getValues()));
                if (mimeType != null) {
                    Intent intent2 = new Intent(this, MemberInvitationActivity.class);
                    intent2.putExtra(BundleKey.ROOM_ID.key(), orginal_roomId)
                        .putExtra(BundleKey.ROOM_TYPE.key(), InvitationType.ShareIn.name());
                    outSideShareSend.launch(intent2);
                }
            } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
                MimeType mimeType = MimeType.of(MimeTypeFilter.matches(type, MimeType.getValues()));
                if (mimeType != null) {
                    Intent intent2 = new Intent(this, MemberInvitationActivity.class);
                    intent2.putExtra(BundleKey.ROOM_ID.key(), orginal_roomId)
                        .putExtra(BundleKey.ROOM_TYPE.key(), InvitationType.ShareIn.name());
                    outSideShareSend.launch(intent2);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

//    @SuppressLint("NewApi")
//    public static String getFilePath(Context context, Uri uri) throws URISyntaxException {
//        String selection = null;
//        String[] selectionArgs = null;
//        // Uri is different in versions after KITKAT (Android 4.4), we need to
//        if (DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
//            if (isExternalStorageDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                return Environment.getExternalStorageDirectory() + "/" + split[1];
//            } else if (isDownloadsDocument(uri)) {
//                final String id = DocumentsContract.getDocumentId(uri);
//                uri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
//            } else if (isMediaDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//                if ("image".equals(type)) {
//                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                } else if ("video".equals(type)) {
//                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//                } else if ("audio".equals(type)) {
//                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//                }
//                selection = "_id=?";
//                selectionArgs = new String[]{
//                        split[1]
//                };
//            }
//        }
//        if ("content".equalsIgnoreCase(uri.getScheme())) {
//            if (isGooglePhotosUri(uri)) {
//                return uri.getLastPathSegment();
//            }
//
//            String e1 = uri.getAuthority();
//            String e2 = uri.getEncodedAuthority();
//
//            String[] projection = {MediaStore.Files.FileColumns.DATA};
//            Cursor cursor = null;
//            try {
//                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
//                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
//                if (cursor.moveToFirst()) {
//                    return cursor.getString(column_index);
//                }
//            } catch (Exception e) {
//                return uri.getPath();
////                e.printStackTrace();
//            }
//        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
//            return uri.getPath();
//        }
//        return null;
//    }


    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


//    private String getRealPathFromURI(Uri contentURI) {
//        String result;
//        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
//        if (cursor == null) {//Source is Dropbox or other similar local file path
//            result = contentURI.getPath();
//        } else {
//            cursor.moveToFirst();
//            int idx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
//            result = cursor.getString(idx);
//            cursor.close();
//        }
//        return result;
//    }


    /**
     * 針對外部分享單個圖片進 CEChat App 到多個聊天室
     */
    private void doExecutionSendImage(List<String> sessionIds, Uri uri) {
        try {
            String[] paths = PictureParse.parseUri(this, uri);
            String token = TokenPref.getInstance(this).getTokenId();
            String path = DaVinci.with().getImageLoader().getAbsolutePath(paths[0]);
            FileService.uploadFile(this, true, token, Media.findByFileType(paths[0]), path, paths[0], new AServiceCallBack<>() {
                @Override
                public void complete(UploadManager.FileEntity entity, RefreshSource refreshSource) {
                    for (int i = 0; i < sessionIds.size(); i++) {
                        String mMessageId = Tools.generateMessageId();
                        String roomId = sessionIds.get(i);
                        ApiManager.doMessageSend(OutSideShareInActivity.this, roomId, mMessageId, "",
                            new MsgBuilder(MessageType.IMAGE)
                                .url(entity.getUrl())
                                .size(entity.getSize())
                                .width(entity.getWidth())
                                .height(entity.getHeight())
                                .thumbnailUrl(entity.getThumbnailUrl())
                                .thumbnailSize(entity.getThumbnailSize())
                                .name("pic")
                            , new SendOutSideShareInListener("[圖片]", i + 1) {
                                @Override
                                public void complete(int count, String msgId, boolean isSuccess) {
//                                        if (isSuccess && mMessageId.equals(msgId)) {
//                                            ChatRoomReference.getInstance().updateLastMessageIdById(roomId, msgId);
//                                        }
                                    OutSideShareInActivity.this.complete(sessionIds, count);
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
                    OutSideShareInActivity.this.complete(sessionIds, sessionIds.size());
                }

            });
        } catch (IOException e) {
            CELog.e("bitmapOptionFailed" + e);
        }
    }

    private static void copyInputStreamToFile(InputStream inputStream, File file)
        throws IOException {

        // append = false
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[8192];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }

    }

    private int uriSize = 0;

    private void doExecutionSendAllType(List<String> roomIds, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            uriSize = 1;
            send(roomIds, intent.getParcelableExtra(Intent.EXTRA_STREAM));
        } else {
            List<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (uris == null) return;
            uriSize = uris.size();
            for (Uri uri : uris) {
                send(roomIds, uri);
            }
        }
    }

    private void send(List<String> roomIds, Uri uri) {
        try {
            String extension = FileHelper.getMimeType(this, uri);
            String fileName = FileHelper.getFileName(this, uri);
            int lastIndex = fileName.lastIndexOf(".");
            if (lastIndex < 0) {
                fileName += "." + extension;
            }
            try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                File tempFile = new File(getExternalFilesDir(Environment.DIRECTORY_DCIM) + "/" + fileName);
                assert inputStream != null;
                copyInputStreamToFile(inputStream, tempFile);

                if (!tempFile.exists()) {
                    CELog.e("tempFile is not exists");
                    finish();
                    return;
                }

                String tokenId = TokenPref.getInstance(this).getTokenId();
                String path = tempFile.getPath();
                Media media = Media.findByFileType(path);
                FileService.uploadFile(this, false, tokenId, media, path, fileName, new AServiceCallBack<>() {
                    @Override
                    public void onProgress(float progress, long total) {
                    }

                    @Override
                    public void complete(UploadManager.FileEntity fileEntity, RefreshSource source) {
                        for (int i = 0; i < roomIds.size(); i++) {
                            String mMessageId = Tools.generateMessageId();
                            String roomId = roomIds.get(i);
                            MsgBuilder messageEntity = builderMessage(fileEntity, path);
                            if (messageEntity == null) {
                                runOnUiThread(() -> {
                                    Toast.makeText(OutSideShareInActivity.this, getString(R.string.text_share_media_failure), Toast.LENGTH_SHORT).show();
                                    OutSideShareInActivity.this.complete(roomIds, roomIds.size());
                                });
                                return;
                            }
                            ApiManager.doMessageSend(OutSideShareInActivity.this, roomId, mMessageId, "",
                                builderMessage(fileEntity, path)
                                , new SendOutSideShareInListener("[圖片]", i + 1) {
                                    @Override
                                    public void complete(int count, String msgId, boolean isSuccess) {
                                        if (isSuccess && mMessageId.equals(msgId)) {
                                            uriSize--;
//                                            ChatRoomReference.getInstance().updateLastMessageIdById(roomId, msgId);
                                        }
                                        OutSideShareInActivity.this.complete(roomIds, count);
                                    }
                                });
                        }
                    }

                    @Override
                    public void error(String message) {
                        super.error(message);
                        CELog.e(message);
                        finish();
                    }
                });
            } catch (Exception e) {
                CELog.e(e.getMessage());
                finish();
            }
        } catch (Exception e) {
            CELog.e(e.getMessage());
            finish();
        }
    }


    private MsgBuilder builderMessage(UploadManager.FileEntity fileEntity, String androidLocalPath) {
        try {
            String url = fileEntity.getUrl();
            String fileType = url.substring(url.lastIndexOf("."));
            FileType type = FileType.of(fileType);
            return switch (type) {
                case VIDEO -> {
                    IVideoSize iVideoSize = new VideoSizeFromVideoFile(androidLocalPath);
                    yield new MsgBuilder(MessageType.VIDEO)
                        .id(Tools.generateMessageId())
                        .name(fileEntity.getName())
                        .size(fileEntity.getSize())
                        .width(iVideoSize.width())
                        .height(iVideoSize.height())
                        .duration(iVideoSize.duration())
                        .thumbnailHeight(fileEntity.getThumbnailHeight())
                        .thumbnailWidth(fileEntity.getThumbnailWidth())
                        .thumbnailUrl(fileEntity.getThumbnailUrl())
                        .md5(fileEntity.getMD5())
                        .androidLocalPath(androidLocalPath)
                        .url(fileEntity.getUrl());
                }
                case IMAGE -> new MsgBuilder(MessageType.IMAGE)
                    .id(Tools.generateMessageId())
                    .name(fileEntity.getName())
                    .size(fileEntity.getSize())
                    .thumbnailHeight(fileEntity.getThumbnailHeight())
                    .thumbnailWidth(fileEntity.getThumbnailWidth())
                    .thumbnailUrl(fileEntity.getThumbnailUrl())
                    .md5(fileEntity.getMD5())
                    .androidLocalPath(androidLocalPath)
                    .url(fileEntity.getUrl());
                default -> new MsgBuilder(MessageType.FILE)
                    .id(Tools.generateMessageId())
                    .name(fileEntity.getName())
                    .size(fileEntity.getSize())
                    .md5(fileEntity.getMD5())
                    .androidLocalPath(androidLocalPath)
                    .url(fileEntity.getUrl());
            };
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 針對外部分享一則文字信息進 CEChat App 到多個聊天室
     */
    private void doExecutionSendText(List<String> roomIds, String content) {
        for (int i = 0; i < roomIds.size(); i++) {
            String messageId = Tools.generateMessageId();
            String roomId = roomIds.get(i);
            ApiManager.doMessageSend(this, roomId, messageId, "", new MsgBuilder(MessageType.TEXT).content(content), new SendOutSideShareInListener(content, i + 1) {
                @Override
                public void complete(int count, String msgId, boolean isSuccess) {
//                    if (isSuccess && messageId.equals(msgId)) {
//                        ChatRoomReference.getInstance().updateLastMessageIdById(roomId, msgId);
//                    }
                    OutSideShareInActivity.this.complete(roomIds, count);
                }
            });
        }
    }

    /**
     * 因為API 限制，無法同一個 messageId 送出到多個聊天室目標，這裡判斷是否完成，並離開App
     */
    private void complete(List<String> sessionIds, int count) {
        binding.progressBar.setMax(sessionIds.size() + uriSize + 1);
        binding.progressBar.setProgress(count + 1);
        if (isMultipleShare) {
            finish();
            return;
        }
        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.REFRESH_FILTER));
        if (Strings.isNullOrEmpty(orginal_roomId)) {
            //從外部資料夾分享檔案，需喚起HomeActivity
            Intent intent = new Intent(OutSideShareInActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        if (sessionIds.size() == 1) {
            //只有分享至1個聊天室，直接喚起該聊天室
            String roomId = sessionIds.get(0);
            ChatRoomEntity currentRoom = ChatRoomReference.getInstance().findById(roomId);
            String unreadId = MessageReference.findUnreadFirstMessageIdByRoomId(null, roomId);
            Intent intent;
            if (currentRoom.getType() == ChatRoomType.services) {
                intent = new Intent(this, ChatActivity.class);
            } else {
                intent = new Intent(this, ChatNormalActivity.class);
            }
            intent.putExtra(BundleKey.EXTRA_SESSION_ID.key(), roomId)
                .putExtra(BundleKey.UNREAD_MESSAGE_ID.key(), unreadId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            IntentUtil.INSTANCE.start(OutSideShareInActivity.this, intent);
        } else {
            //分享多個聊天室，只有toast
            ToastUtils.showToast(OutSideShareInActivity.this, getString(R.string.text_share_to_N_chat_room_already, sessionIds.size()));
        }
        finish();
    }


    abstract static class SendOutSideShareInListener extends MessageSendRequest.Listener<MessageEntity> {
        private final int count;
        private final String content;

        SendOutSideShareInListener(String content, int count) {
            this.count = count;
            this.content = content;
        }

        public abstract void complete(int count, String messageId, boolean isSuccess);


        @Override
        public void onSuccess(MessageEntity success) {
            try {
                DBManager.getInstance().updateMessageStatus(success.getId(), MessageStatus.SUCCESS);
                DBManager.getInstance().updateSendNum(success.getId(), success.getSendNum() != null ? success.getSendNum() : 0);
                DBManager.getInstance().updateSendTime(success.getId(), success.getSendTime());
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.MSG_STATUS_FILTER, new MsgStatusBean(success.getId(), success.getSendNum(), success.getSendTime())));
                // EVAN_FLAG 2019-10-19 送出新回覆訊息，
                //  更新該聊天室交互時間
                ChatRoomReference.getInstance().updateInteractionTimeById(success.getRoomId());
//                if (!Strings.isNullOrEmpty(this.content)) {
//                    ChatRoomReference.getInstance().updateContentById(success.getRoomId(), this.content);
//                    ChatRoomEntity entity = ChatRoomReference.getInstance().findById(success.getRoomId());

//                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.REFRESH_FILTER));
//                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.REFRESH_ROOM_LIST_BY_ENTITY, entity));
//                }
            } catch (Exception e) {
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        outSideShareSend.unregister();
    }
}
