package tw.com.chainsea.chat.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.loader.content.CursorLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import tw.com.chainsea.chat.util.DaVinci;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.base.Constant;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.lib.Tools;
import tw.com.chainsea.chat.messagekit.lib.FileUtil;
import tw.com.chainsea.chat.widget.photoview.PhotoView;
import tw.com.chainsea.chat.widget.photoview.PhotoViewAttacher;


/**
 * ImageSendFragment
 * Created by 90Chris on 2014/11/18.
 */
public class ImageConfirmFragment extends BaseFragment {
    final int BIG_PIC_SIZE = 600;
    final int SMALL_PIC_SIZE = 450;
    PhotoView mPhotoView = null;
    ImageView ivPhotoFrame = null;
    final int REQUEST_CODE_CAMERA = 1;
    final int REQUEST_CODE_ALBUM = 2;
    final String IMAGE_FILE_NAME = "pic.jpg";
    File picTempFile = null;

    float photoTop;
    float photoLeft;
    float frameRate;
    int mType;

    public static ImageConfirmFragment newInstance(int way, int type) {
        ImageConfirmFragment fragment = new ImageConfirmFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.INTENT_IMAGE_CONFIRM_TYPE, type);
        bundle.putInt(Constant.INTENT_IMAGE_GET_WAY, way);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int which = getArguments().getInt(Constant.INTENT_IMAGE_GET_WAY);
        if (which == Constant.INTENT_CODE_CAMERA) {
//            Intent openCameraIntent = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ? new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE).putExtra("return-data", true) : new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            picTempFile = FileUtil.getCacheFile(IMAGE_FILE_NAME, getActivity().getPackageName());
            //适配7.0
            Uri uri;
            uri = FileProvider.getUriForFile(requireContext(), requireActivity().getPackageName() + ".fileprovider", picTempFile);
            openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(openCameraIntent, REQUEST_CODE_CAMERA);
        } else {
            Intent intentFromGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentFromGallery, REQUEST_CODE_ALBUM);
        }
        mType = getArguments().getInt(Constant.INTENT_IMAGE_CONFIRM_TYPE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_send, container, false);
        mPhotoView = (PhotoView) view.findViewById(R.id.image_send_photo);

        if (mType == Constant.INTENT_CODE_CROP) {
            ivPhotoFrame = (ImageView) view.findViewById(R.id.image_send_frame);
            ivPhotoFrame.setVisibility(View.VISIBLE);
            mPhotoView.setOnMatrixChangeListener(new PhotoViewAttacher.OnMatrixChangedListener() {
                @Override
                public void onMatrixChanged(RectF rect) {
                    photoTop = rect.top;
                    photoLeft = rect.left;
                }
            });
        }
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_ALBUM:
                    displayImage(data.getData());
                    break;
                case REQUEST_CODE_CAMERA:
                    Log.e("pic_url", "name:" + picTempFile);
                    displayImage(Uri.fromFile(picTempFile));
                    break;
            }
        } else {
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    Bitmap big_pic;

    private void displayImage(Uri uri) {
        try {
            //do not save photo to memory, but calculate the size
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getActivity().getContentResolver()
                .openInputStream(uri), null, op);
            op.inSampleSize = Tools.calculateInSampleSize(op, BIG_PIC_SIZE, BIG_PIC_SIZE);
            //get the photo
            op.inJustDecodeBounds = false;
            big_pic = BitmapFactory.decodeStream(getActivity().getContentResolver()
                .openInputStream(uri), null, op);

            if (picTempFile != null) {
                big_pic = applyOrientation(big_pic, resolveBitmapOrientation(picTempFile));
                mPhotoView.setImageBitmap(big_pic);
            } else {
                big_pic = applyOrientation(big_pic, resolveBitmapOrientation(getRealPathFromUri(getContext(), uri)));
                mPhotoView.setImageBitmap(big_pic);
            }


            if (mType == Constant.INTENT_CODE_CROP) {
                ivPhotoFrame.post(new Runnable() {
                    @Override
                    public void run() {
                        int height = (int) mPhotoView.getDisplayRect().height();
                        int width = (int) mPhotoView.getDisplayRect().width();
                        int frameSize = width > height ? height : width;
                        frameSize -= 10;   //for 10px boarder
                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(frameSize, frameSize);
                        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                        ivPhotoFrame.setLayoutParams(lp);

                        frameRate = (float) width / (float) big_pic.getWidth();
                    }
                });
            }

        } catch (Exception e) {
            CELog.e("bitmapOptionFailed" + e);
        }
    }

    public void confirmImage() {
        String key = Tools.createName("uid_photo", MessageType.IMAGE);
        String key_small = "small" + key;

        if (mType == Constant.INTENT_CODE_CROP) {
            /*crop begin*/
            float scaleRate = mPhotoView.getScale() * frameRate;
            int cropX = (int) ((ivPhotoFrame.getLeft() - photoLeft) / scaleRate);
            if (cropX < 0) {
                cropX = 0;
            }
            int cropY = (int) ((ivPhotoFrame.getTop() - photoTop) / scaleRate);
            if (cropY < 0) {
                cropY = 0;
            }
            int cropSize = (int) (ivPhotoFrame.getHeight() / scaleRate);
            big_pic = Bitmap.createBitmap(big_pic, cropX, cropY, cropSize, cropSize);
            /*crop end*/
        }
        /*compress the pic to stream*/
        ByteArrayOutputStream baoBig = new ByteArrayOutputStream();
        /*try {
            big_pic = applyOrientation(big_pic, resolveBitmapOrientation(picTempFile));
        } catch (IOException ignored) {
        }*/
        /*if (big_pic.getRowBytes() * big_pic.getHeight() / 1024 < 600) {
        } else {
            big_pic.compress(Bitmap.CompressFormat.JPEG, 80, baoBig);
        }*/
        big_pic.compress(Bitmap.CompressFormat.JPEG, 100, baoBig);

        /*before uploading, cache the pic*/
        DaVinci.with(getActivity()).getImageLoader().putImage(key, baoBig.toByteArray());

        ByteArrayOutputStream baoSmall = new ByteArrayOutputStream();
        zoomImage(big_pic, SMALL_PIC_SIZE).compress(Bitmap.CompressFormat.JPEG, 50, baoSmall);
        DaVinci.with(getActivity()).getImageLoader().putImage(key_small, baoSmall.toByteArray());

        Intent it = new Intent();
        it.putExtra(BundleKey.RESULT_PIC_URI.key(), key);
        it.putExtra(BundleKey.RESULT_PIC_SMALL_URI.key(), key_small);

        getActivity().setResult(Activity.RESULT_OK, it);
        getActivity().finish();
    }

    /**
     * zoom in image
     *
     * @param bitmap  bitmap
     * @param maxSize size
     * @return bitmap
     */
    public Bitmap zoomImage(Bitmap bitmap, int maxSize) {
        int outWidth;
        int outHeight;
        int inWidth = bitmap.getWidth();
        int inHeight = bitmap.getHeight();

        if (inWidth > inHeight) {
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }

        return Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);
    }

    /**
     * 处理bitmap图像的方向
     *
     * @param bitmapFile file
     * @return
     * @throws IOException
     */
    private int resolveBitmapOrientation(File bitmapFile) throws IOException {
        ExifInterface exif;
        if (bitmapFile != null) {
            exif = new ExifInterface(bitmapFile.getAbsolutePath());
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } else {
            return 0;
        }
    }

    private int resolveBitmapOrientation(String absolutePath) throws IOException {
        ExifInterface exif;
        if (absolutePath != null) {
            exif = new ExifInterface(absolutePath);
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } else {
            return 0;
        }
    }

    /**
     * 将图片旋转为正确的方向
     *
     * @param bitmap      bitmap
     * @param orientation orientation
     * @return bitmap
     */
    private Bitmap applyOrientation(Bitmap bitmap, int orientation) {
        int rotate;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
            default:
                return bitmap;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix mtx = new Matrix();
        mtx.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, mtx, true);
    }

    /**
     * 根据图片的Uri获取图片的绝对路径(已经适配多种API)
     *
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    public String getRealPathFromUri(Context context, Uri uri) {
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion < 11) {
            // SDK < Api11
            return getRealPathFromUri_BelowApi11(context, uri);
        }
        if (sdkVersion < 19) {
            // SDK > 11 && SDK < 19
            return getRealPathFromUri_Api11To18(context, uri);
        }
        // SDK > 19
        return getRealFilePath(context, uri);
    }

    /**
     * 适配api19以上,根据uri获取图片的绝对路径
     *
     * @param context
     * @param uri
     * @return
     */
    public String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 适配api19以上,根据uri获取图片的绝对路径
     */
    private String getRealPathFromUri_AboveApi19(Context context, Uri uri) {
        String filePath = null;
        String wholeID = DocumentsContract.getDocumentId(uri);

        // 使用':'分割
        String id = wholeID.split(":")[1];

        String[] projection = {MediaStore.Images.Media.DATA};
        String selection = MediaStore.Images.Media._ID + "=?";
        String[] selectionArgs = {id};

        Cursor cursor = context.getContentResolver().query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
            selection, selectionArgs, null);
        int columnIndex = cursor.getColumnIndex(projection[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    /**
     * 适配api11-api18,根据uri获取图片的绝对路径
     */
    @SuppressLint("Range")
    private String getRealPathFromUri_Api11To18(Context context, Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};

        CursorLoader loader = new CursorLoader(context, uri, projection, null,
            null, null);
        Cursor cursor = loader.loadInBackground();

        if (cursor != null) {
            cursor.moveToFirst();
            filePath = cursor.getString(cursor.getColumnIndex(projection[0]));
            cursor.close();
        }
        return filePath;
    }

    /**
     * 适配api11以下(不包括api11),根据uri获取图片的绝对路径
     */
    @SuppressLint("Range")
    private String getRealPathFromUri_BelowApi11(Context context, Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection,
            null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            filePath = cursor.getString(cursor.getColumnIndex(projection[0]));
            cursor.close();
        }
        return filePath;
    }
}
