package tw.com.chainsea.chat.view.gallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.client.helper.ClientsHelper;
import tw.com.chainsea.android.common.client.type.Media;
import tw.com.chainsea.android.common.file.FileHelper;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent;
import tw.com.chainsea.ce.sdk.reference.MessageReference;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.ActivityPhotoGalleryBinding;
import tw.com.chainsea.chat.lib.ToastUtils;
import tw.com.chainsea.chat.ui.activity.ParentActivity;
import tw.com.chainsea.chat.util.DownloadUtil;
import tw.com.chainsea.custom.view.alert.AlertView;

/**
 * 畫廊預覽
 */
public class PhotoGalleryActivity extends ParentActivity implements PhotoGalleryFragment.OnLongClickListener, PhotoGalleryFragment.OnClickListener {
    private static final String TAG = PhotoGalleryActivity.class.getSimpleName();

    //    File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures/Aile/");
    File storageDir = new File(DownloadUtil.INSTANCE.getDownloadImageDir());

    ActivityPhotoGalleryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = this.getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        binding = (ActivityPhotoGalleryBinding) viewDataBinding;
    }

    @Override
    protected int createView() {
        return R.layout.activity_photo_gallery;
    }

    @Override
    protected void findView() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        MessageEntity message = (MessageEntity) getIntent().getSerializableExtra(BundleKey.PHOTO_GALLERY_MESSAGE.key());
        if (message != null && !Strings.isNullOrEmpty(message.getRoomId())) {
            List<MessageEntity> messages = MessageReference.findMessageByRoomIdAndMessageType(message.getRoomId(), MessageType.IMAGE);
            filter(messages);
            Collections.sort(messages);
            int position = messages.indexOf(message);
            // 如果是點擊主題訊息內圖片
            if (!MessageType.IMAGE.equals(message.getType()) && message.nearMessageContent() != null && message.nearMessageContent() instanceof ImageContent) {
                String nearId = message.getNearMessageId();
                MessageEntity nearMessage = MessageReference.findById(nearId);
                if (nearMessage != null) position = messages.indexOf(nearMessage);
            }
            setup(position, messages);
        } else {
            String url = getIntent().getStringExtra(BundleKey.PHOTO_GALLERY_URL.key());
            String thumbnailUrl = getIntent().getStringExtra(BundleKey.PHOTO_GALLERY_THUMBNAIL_URL.key());
            List<MessageEntity> messages = Lists.newArrayList(
                new MessageEntity.Builder()
                    .type(MessageType.IMAGE)
                    .content(ImageContent.Build()
                        .url(url).thumbnailUrl(thumbnailUrl).height(45).width(45).build().toStringContent()
                    ).build());
            filter(messages);
            setup(0, messages);
        }
    }

    private void filter(List<MessageEntity> messages) {
        Iterator<MessageEntity> iterator = messages.iterator();
        while (iterator.hasNext()) {
            MessageEntity msg = iterator.next();
            if (msg.content() != null && msg.content() instanceof ImageContent) {
                ImageContent format = ((ImageContent) msg.content());
                String url = format.getUrl();
                String thumbnailUrl = format.getThumbnailUrl();
                if (!Strings.isNullOrEmpty(url) && url.contains("qs/file/download")) {
                    format.setUrl(url.replaceAll("qs/file/download", "base/file/api-upload/download"));
                }
                if (MessageFlag.RETRACT.equals(msg.getFlag()) || Strings.isNullOrEmpty(url) || Strings.isNullOrEmpty(thumbnailUrl) || url.endsWith("heic") || thumbnailUrl.endsWith("heic")) {
                    iterator.remove();
                }
            }
        }
    }

//    private void sort(List<MessageEntity> messages) {
//        Collections.sort(messages);
//    }


    private void setup(int position, List<MessageEntity> messages) {
        position = position < 0 ? 0 : position;
        PhotoGalleryPagerAdapter adapter = new PhotoGalleryPagerAdapter(this, getSupportFragmentManager(), messages);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setCurrentItem(position);
    }

    @Override
    public void onLongClick(MessageEntity message, IMessageContent iContent) {

    }

    @Override
    public void onClick(MessageEntity message, IMessageContent iContent) {

    }

    class PhotoGalleryPagerAdapter extends FragmentPagerAdapter {
        private List<MessageEntity> messages = Lists.newArrayList();
        private Context ctx;

        public PhotoGalleryPagerAdapter(Context ctx, FragmentManager fm, List<MessageEntity> messages) {
            super(fm);
            this.ctx = ctx;
            this.messages = messages;
        }

        @Override
        @NonNull
        public PhotoGalleryFragment getItem(int position) {
            MessageEntity m = this.messages.get(position);
            return PhotoGalleryFragment.newInstance(this.ctx, m)
                .setOnClickListener((message, format) -> finish())
                .setOnLongClickListener((message, format) -> afterPicLongClick((ImageContent) format));
        }

        @Override
        public int getCount() {
            return this.messages.size();
        }
    }


    public void afterPicLongClick(final ImageContent imageContent) {
        String url = imageContent.getUrl();
        new AlertView.Builder()
            .setContext(this)
            .setStyle(AlertView.Style.ActionSheet)
            .setOthers(new String[]{"儲存到相簿"})
            .setCancelText("取消")
            .setOnItemClickListener((o, position) -> {
                if (position == 0) {
                    if (URLUtil.isValidUrl(url)) {
                        downloadImage(url);
                    } else {
                        if (url.endsWith(".gif") && !url.startsWith("http")) {
                            File file = new File(url);
                            if (file.exists()) {
                                try {
                                    String path = saveGif(FileHelper.file2byte(url));
                                    File saveFile = new File(path);
                                    if (saveFile.exists()) {
                                        galleryAddPic(path);
                                    } else {
                                        ToastUtils.showToast(this, "圖片存儲失敗");
                                    }
                                } catch (IOException ignored) {
                                    ToastUtils.showToast(this, "儲存錯誤");
                                }
                            } else {
                            }
                        } else {
                            try {
                                Glide.with(this)
                                    .asBitmap()
                                    .load(url)
                                    .into(new SimpleTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                            saveImage(resource);
                                        }
                                    });
                            } catch (Exception ignored) {
                            }

                        }
                    }
                }
            })
            .build()
            .setCancelable(true)
            .show();
    }

    private void downloadImage(String url) {
        String imageFileName = (url.endsWith(".gif") ? "GIF_" : "JPEG_") + "down" + System.currentTimeMillis() + (url.endsWith(".gif") ? ".gif" : ".jpg");
        File imageFile = new File(storageDir, imageFileName);
        ClientsHelper.post(false).execute(url, Media.OCTET_STREAM.get(), "", new tw.com.chainsea.android.common.client.callback.impl.FileCallBack(storageDir.getPath(), imageFileName, false) {

            @Override
            public void onSuccess(String resp, File file) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> galleryAddPic(file));
            }

            @Override
            public void onFailure(Exception e, String errorMsg) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    Toast.makeText(PhotoGalleryActivity.this, "圖片存儲失敗", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void progress(float progress, long total) {
                super.progress(progress, total);
            }
        });
    }

    private void saveImage(Bitmap image) {
        String saveImagePath = null;
        String imageFileName = "JPEG_" + "down" + System.currentTimeMillis() + ".jpg";
//        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures/cechat/");

        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            saveImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fout = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fout);
                fout.close();
            } catch (Exception ignored) {

            }

            // Add the image to the system gallery
            galleryAddPic(saveImagePath);
        }
//        return saveImagePath;
    }


    private void galleryAddPic(String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        ToastUtils.showToast(this, String.format(getString(R.string.bruce_photo_save), imagePath));
    }


    private void galleryAddPic(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        Toast.makeText(PhotoGalleryActivity.this, String.format(getString(R.string.bruce_photo_save), file.getAbsolutePath()), Toast.LENGTH_SHORT).show();
    }

    private String saveGif(byte[] bytes) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
//        String imageFileName = "GIF_" + "down" + System.currentTimeMillis() + ".gif";
        String path = DownloadUtil.INSTANCE.getDownloadImageDir() + "GIF_" + "down" + System.currentTimeMillis() + ".gif";
        FileOutputStream fileOutputStream = new FileOutputStream(path);
        byte[] buff = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buff)) != -1) {
            fileOutputStream.write(buff, 0, len);
        }
        inputStream.close();
        fileOutputStream.close();
        return path;
    }

}
