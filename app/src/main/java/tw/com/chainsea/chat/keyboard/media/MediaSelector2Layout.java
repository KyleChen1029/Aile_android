package tw.com.chainsea.chat.keyboard.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.multimedia.AMediaBean;
import tw.com.chainsea.android.common.multimedia.FileBean;
import tw.com.chainsea.android.common.multimedia.ImageBean;
import tw.com.chainsea.android.common.multimedia.MediaContentObserver;
import tw.com.chainsea.android.common.multimedia.MultimediaHelper;
import tw.com.chainsea.android.common.multimedia.VideoBean;
import tw.com.chainsea.android.common.text.StringHelper;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.LayoutKeyboardBottomMediaBinding;
import tw.com.chainsea.chat.keyboard.listener.OnNewKeyboardListener;
import tw.com.chainsea.chat.keyboard.media.adatper.MediaSelectorAdapter;
import tw.com.chainsea.chat.keyboard.media.adatper.PicDragCallback;
import tw.com.chainsea.chat.keyboard.media.adatper.PicItemDecoration;
import tw.com.chainsea.chat.style.RoomThemeStyle;
import tw.com.chainsea.chat.ui.activity.ChatNormalActivity;
import tw.com.chainsea.chat.ui.fragment.ChatFragment;
import tw.com.chainsea.chat.util.ThemeHelper;
import tw.com.chainsea.custom.view.alert.AlertView;

/**
 * Current by evan on 2020-06-01
 *
 * @author Evan Wang
 * @date 2020-06-01
 */
public class MediaSelector2Layout extends ConstraintLayout implements MediaSelectorAdapter.OnMediaSelectorListener, MediaContentObserver.Listener {
    private LayoutKeyboardBottomMediaBinding binding;
    private CheckBox cbOriginal;

    private PicDragCallback picDragCallback;
    private MediaSelectorAdapter adapter;
    private ChatFragment.KeyBoardBarListener keyBoardBarListener;
    private ChatNormalActivity.KeyBoardBarListener chatNormalActivityKeyBoardListener;
    private OnNewKeyboardListener onNewKeyboardListener;

    private MultimediaHelper.Type type = MultimediaHelper.Type.IMAGE;

    String currentTag = "ALL";
    ListMultimap<String, AMediaBean> sectionPhotoData = ArrayListMultimap.create();

    List<AMediaBean> mediaPhotoBeans = Lists.newArrayList();
    List<AMediaBean> mediaFileBeans = Lists.newArrayList();
    List<AMediaBean> mediaVideoBeans = Lists.newArrayList();
    List<AMediaBean> mediaAllBeans = Lists.newArrayList();

    RoomThemeStyle themeStyle = RoomThemeStyle.UNDEF;

    int requestCode = -1;

    MediaContentObserver imageObserver;
    //    MediaContentObserver fileObserver;
    boolean isGreenTheme = false;

    public MediaSelector2Layout(@NonNull Context context) {
        super(context);
    }

    public MediaSelector2Layout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaSelector2Layout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            setTag("VISIBLE");
        }

        if (visibility == View.GONE) {
            setTag("GONE");
        }

        if (visibility == View.VISIBLE && adapter != null && !this.isChange) {
//            setData(Lists.newArrayList());
//            switch (type) {
//                case IMAGE:
//                    setData(sectionPhotoData.get(currentTag));
//                    break;
//                case VIDEO:
//                    setData(mediaVideoBeans);
//                    break;
//                case FILE:
//                    setData(mediaFileBeans);
//                    break;
//            }
            binding.rvMedia.getLayoutManager().scrollToPosition(0);
        }
        this.isChange = false;
        super.setVisibility(visibility);
    }

    @Override
    public void setTag(Object tag) {
        super.setTag(tag);
    }

    boolean isChange = false;

    public void setChangeVisibility() {
        this.isChange = true;
    }

    @SuppressLint("SetTextI18n")
    public void init(Context context) {
        try {
            binding = LayoutKeyboardBottomMediaBinding.inflate(LayoutInflater.from(context), this, false);
            addView(binding.getRoot());
            binding.rvMedia.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
            binding.rvMedia.addItemDecoration(new PicItemDecoration(4));
            binding.rvMedia.setHasFixedSize(false);
            adapter = new MediaSelectorAdapter().setOnMediaSelectorListener(this);
            binding.rvMedia.setAdapter(adapter);
            mediaAllBeans.clear();
            refresh();
            binding.tvNumber.setText(currentTag + " ");
            setData(sectionPhotoData.get(currentTag));

            imageObserver = new MediaContentObserver(MultimediaHelper.Type.IMAGE, new Handler(), this);
//        fileObserver = new MediaContentObserver(MultimediaHelper.Type.FILE, new Handler(), this);
            context.getContentResolver()
                .registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, imageObserver);

            picDragCallback = new PicDragCallback(adapter, binding.vTriggerBoundary)
                .setAlpha(0.9f)
                .setScale(1.3f);//1.3f
            ItemTouchHelper helper = new ItemTouchHelper(picDragCallback);
            helper.attachToRecyclerView(binding.rvMedia);


            picDragCallback.setDragListener(new PicDragCallback.PicDragListener() {
                @Override
                public void onDragStart() {
                }

                @Override
                public void onDragFinish(boolean isInside) {
                }

                @Override
                public void onDragAreaChange(boolean isInside, boolean isIdle) {
                }

                @Override
                public void onTriggerBoundary(int position) {
                    if (position != -1) {
                        AMediaBean bean = adapter.getData(position);
//                    setVisibility(GONE);
                        if (keyBoardBarListener != null) {
                            if (bean instanceof ImageBean) {
                                keyBoardBarListener.onSlideUpSendImage(MultimediaHelper.Type.IMAGE, Lists.newArrayList(bean), (cbOriginal != null && cbOriginal.isChecked()));
                            }
                            if (bean instanceof FileBean) {
                                keyBoardBarListener.onSlideUpSendImage(MultimediaHelper.Type.FILE, Lists.newArrayList(bean), (cbOriginal != null && cbOriginal.isChecked()));
                            }
                            if (bean instanceof VideoBean) {
                                keyBoardBarListener.onSlideUpSendImage(MultimediaHelper.Type.VIDEO, Lists.newArrayList(bean), (cbOriginal != null && cbOriginal.isChecked()));
                            }
                        }

                        if (chatNormalActivityKeyBoardListener != null) {
                            if (bean instanceof ImageBean) {
                                chatNormalActivityKeyBoardListener.onSlideUpSendImage(MultimediaHelper.Type.IMAGE, Lists.newArrayList(bean), (cbOriginal != null && cbOriginal.isChecked()));
                            }
                            if (bean instanceof FileBean) {
                                chatNormalActivityKeyBoardListener.onSlideUpSendImage(MultimediaHelper.Type.FILE, Lists.newArrayList(bean), (cbOriginal != null && cbOriginal.isChecked()));
                            }
                            if (bean instanceof VideoBean) {
                                chatNormalActivityKeyBoardListener.onSlideUpSendImage(MultimediaHelper.Type.VIDEO, Lists.newArrayList(bean), (cbOriginal != null && cbOriginal.isChecked())); //上傳影片檔
                            }
                        }

                        if (onNewKeyboardListener != null) {
                            if (bean instanceof ImageBean) {
                                onNewKeyboardListener.onMediaSelector(MultimediaHelper.Type.IMAGE, Lists.newArrayList(bean), (cbOriginal != null && cbOriginal.isChecked()));
                            }
                            if (bean instanceof FileBean) {
                                onNewKeyboardListener.onMediaSelector(MultimediaHelper.Type.FILE, Lists.newArrayList(bean), (cbOriginal != null && cbOriginal.isChecked()));
                            }
                            if (bean instanceof VideoBean) {
                                onNewKeyboardListener.onMediaSelector(MultimediaHelper.Type.VIDEO, Lists.newArrayList(bean), (cbOriginal != null && cbOriginal.isChecked()));
                            }
                        }
                    }
                }
            });

            initListener();
        } catch (Exception e) {
            CELog.e("MediaSelector2Layout init error: " + e.getMessage());
        }
    }

    private void initListener() {
        binding.tvNumber.setOnClickListener(this::doNumberAction);
        binding.ivArrow.setOnClickListener(this::doNumberAction);
        binding.tvSubmit.setOnClickListener(this::doSubmitAction);
        binding.ivFolder.setOnClickListener(this::doOpenFolderAction);

    }

    public void setMaxCount(int maxCount) {
        this.adapter.setMaxCount(maxCount);
    }

    public int getMaxCount() {
        return this.adapter.getMaxCount();
    }

    public void setSelectData(String type, boolean isOriginal, TreeMap<String, String> data) {
        currentTag = type;
        if (cbOriginal != null) {
            cbOriginal.setChecked(isOriginal);
        }
        adapter.setData(sectionPhotoData.get(currentTag)).onChangeSelected(data);
    }

    @SuppressLint("SetTextI18n")
    public void setType(Context context, MultimediaHelper.Type type, RoomThemeStyle themeStyle, int requestCode) {
        this.requestCode = requestCode;
        isGreenTheme = ThemeHelper.INSTANCE.isGreenTheme();
        if (this.type.equals(type) && getVisibility() == View.VISIBLE) {

        } else {
            this.type = type;
            setVisibility(VISIBLE);
            if (adapter == null) {
                init(context);
            }
            adapter.refreshData();
        }

        if (MultimediaHelper.Type.FILE.equals(this.type)) {
            binding.tvNumber.setText("");
            binding.ivArrow.setVisibility(GONE);
        } else {
            binding.tvNumber.setText(currentTag + " ");
            binding.ivArrow.setVisibility(VISIBLE);
        }
        this.themeStyle = themeStyle;

        binding.llCheckContainer.removeAllViews();
        binding.ivFolder.setImageResource(MultimediaHelper.Type.FILE.equals(this.type) ? R.drawable.ic_folder_20dp : R.drawable.pic_white);
        if (RoomThemeStyle.SERVICES_or_BUSINESS_or_BROADCAST_or_SERVICE_MEMBER.contains(this.themeStyle)) {
            binding.tvSubmit.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF256D67")));
            binding.tvNumber.setTextColor(Color.WHITE);
            ImageViewCompat.setImageTintList(binding.ivArrow, ColorStateList.valueOf(Color.WHITE));
        } else {
            binding.tvNumber.setTextColor(Color.WHITE);
            ImageViewCompat.setImageTintList(binding.ivArrow, ColorStateList.valueOf(Color.WHITE));
        }

        adapter.setThemeStyle(this.themeStyle);
        if (MultimediaHelper.Type.IMAGE.equals(this.type)) {
            binding.llCheckContainer.addView(newCheckBox(context, this.themeStyle));
        }
        binding.clBottom.setBackgroundColor(isGreenTheme && this.themeStyle != RoomThemeStyle.SERVICES ? 0xFF015F57 : this.themeStyle.getKeyboardColor());
        binding.tvSubmit.setBackgroundResource(isGreenTheme && this.themeStyle != RoomThemeStyle.SERVICES ? R.drawable.btn_send_pic_bg_green : R.drawable.btn_send_pic_bg);
    }

    @SuppressLint("SetTextI18n")
    private CheckBox newCheckBox(Context context, RoomThemeStyle themeStyle) {
        @StyleRes int style = R.style.DefaultCheckboxTheme;
        @ColorInt int textColor;

        if (isGreenTheme) {
            if (themeStyle.equals(RoomThemeStyle.SERVICES)) {
                style = R.style.GreenWhiteCheckboxTheme;
            } else {
                style = R.style.CheckboxThemeWithGreen;
            }
        } else {
            if (themeStyle.equals(RoomThemeStyle.SERVICES)) {
                style = R.style.GreenWhiteCheckboxTheme;
            }
        }
        textColor = 0xFFFFFFFF;

        cbOriginal = new CheckBox(context, null, 0, style);
        cbOriginal.setText(" " + getContext().getString(R.string.warning_original_photo));
        cbOriginal.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        cbOriginal.setTextColor(textColor);
        return cbOriginal;
    }

    private void setData(List<AMediaBean> mediaBeans) {
//        CELog.d("Kyle2 setData size="+mediaBeans.size());
//        for(AMediaBean aMediaBean: mediaBeans) {
//            CELog.d("Kyle2 "+aMediaBean.getFileName());
//        }
        adapter.clearSelect().setData(mediaBeans).refreshData();
//        binding.rvMedia.setAdapter(adapter);
    }

    @SuppressLint("SetTextI18n")
    public void refreshData() {
        try {
            Thread.sleep(50);
            adapter = new MediaSelectorAdapter().setOnMediaSelectorListener(this);
            binding.rvMedia.setAdapter(adapter);
            mediaAllBeans.clear();
            refresh();
            binding.tvNumber.setText(currentTag + " ");
            setData(sectionPhotoData.get(currentTag));
        } catch (Exception ignored) {
        }
    }

    // 刷新資料
    public void refresh() {
        long useTime = System.currentTimeMillis();
        mediaPhotoBeans = MultimediaHelper.query(getContext(), MultimediaHelper.Type.IMAGE);
        //mediaFileBeans = MultimediaHelper.query(getContext(), MultimediaHelper.Type.FILE);
        mediaVideoBeans = MultimediaHelper.query(getContext(), MultimediaHelper.Type.VIDEO);
        mediaAllBeans.addAll(mediaPhotoBeans);
        mediaAllBeans.addAll(mediaVideoBeans);
        Collections.sort(mediaAllBeans);
        sectionPicturePath();
        sectionVideoPath();
        CELog.d("system media resource query use time :: " + ((System.currentTimeMillis() - useTime) / 1000.0d));
    }


    private synchronized void sectionPicturePath() {
        sectionPhotoData.clear();
        Iterator<AMediaBean> iterator = mediaPhotoBeans.iterator();
        //CELog.d("Kyle2 mediaAllBeans size="+mediaAllBeans.size()+", photo size="+mediaPhotoBeans.size()+", video size="+mediaVideoBeans.size());
        sectionPhotoData.putAll("ALL", mediaAllBeans);
        while (iterator.hasNext()) {
            AMediaBean bean = iterator.next();
            sectionPhotoData.put(bean.getFolderName(), bean);
        }
    }

    private void sectionVideoPath() {
        for (AMediaBean bean : mediaVideoBeans) {
            //CELog.d("Kyle2 "+bean.getFolderName()+", "+bean.getFileName()+", "+bean.getPath()+", "+bean.getThumbnailBitmap());
            sectionPhotoData.put(bean.getFolderName() == null ? "Video" : bean.getFolderName(), bean);
        }
    }

    public void setKeyBoardBarListener(ChatFragment.KeyBoardBarListener keyBoardBarListener) {
        this.keyBoardBarListener = keyBoardBarListener;
    }

    public void setChatNormalKeyBoardBarListener(ChatNormalActivity.KeyBoardBarListener chatNormalActivityKeyBoardListener) {
        this.chatNormalActivityKeyBoardListener = chatNormalActivityKeyBoardListener;
    }

    public void setOnNewKeyboardListener(OnNewKeyboardListener onNewKeyboardListener) {
        this.onNewKeyboardListener = onNewKeyboardListener;
    }



    /*  --------------------  Event binding  -------------------------*/


    /**
     * 提交按鈕
     */
    public void doSubmitAction(View view) {
        if (onNewKeyboardListener != null) {
            binding.tvNumber.setTag(null);
            onNewKeyboardListener.onMediaSelector(this.type, adapter.getSelect(), cbOriginal != null && cbOriginal.isChecked());
            adapter.clearSelect().refreshData();
            onSelected(Lists.newArrayList());
        }

        //聊天室下方多媒體選擇器發送
        if (keyBoardBarListener != null) {
            binding.tvNumber.setTag(null);
            keyBoardBarListener.onMediaSelector(this.type, adapter.getSelect(), cbOriginal != null && cbOriginal.isChecked());
            adapter.clearSelect().refreshData();
            onSelected(Lists.newArrayList());
        } else if (chatNormalActivityKeyBoardListener != null) {
            binding.tvNumber.setTag(null);
            chatNormalActivityKeyBoardListener.onMediaSelector(this.type, adapter.getSelect(), cbOriginal != null && cbOriginal.isChecked());
            adapter.clearSelect().refreshData();
            onSelected(Lists.newArrayList());
        }
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    void doNumberAction(View view) {
        if (!MultimediaHelper.Type.FILE.equals(this.type)) {
            if (!sectionPhotoData.isEmpty()) {
                view.getTag();
            }
            String[] options = sectionPhotoData.keySet().toArray(new String[sectionPhotoData.keySet().size()]);
            int displayHeight = UiHelper.getDisplayHeight(getContext());
            binding.ivArrow.animate().rotation(0f);

            new AlertView.Builder()
                .setContext(getContext()).setStyle(AlertView.Style.ActionSheet).setMaxHeight(displayHeight / 2).setOthers(options).setCancelText(getContext().getString(R.string.alert_cancel))
                .setOnItemClickListener((o, position) -> {
                    if (position != -1) {
                        String key = options[position];
                        currentTag = key;
                        binding.tvNumber.setText(key + " ");
                        adapter.setData(sectionPhotoData.get(key)).refreshData();
                        adapter.notifyDataSetChanged();
                        adapter.clearSelect();
                        binding.rvMedia.getLayoutManager().scrollToPosition(0);
                    }
                })
                .build()
                .setCancelable(true)
                .setOnDismissListener(o -> binding.ivArrow.animate().rotation(180f)).show();
        }
    }


    /**
     * 打開資料夾
     */
    public void doOpenFolderAction(View view) {
        if (keyBoardBarListener != null) {
            if (MultimediaHelper.Type.FILE.equals(this.type)) {
                keyBoardBarListener.onOpenFolders();
            } else {
                keyBoardBarListener.onOpenGallery();
            }
        }

        if (chatNormalActivityKeyBoardListener != null) {
            if (MultimediaHelper.Type.FILE.equals(this.type)) {
                chatNormalActivityKeyBoardListener.onOpenFolders();
            } else {
                chatNormalActivityKeyBoardListener.onOpenGallery();
            }
        }

        if (onNewKeyboardListener != null) {
            if (MultimediaHelper.Type.FILE.equals(this.type)) {
                onNewKeyboardListener.onOpenFileFolder(1);
            } else {
                onNewKeyboardListener.onOpenGallery(1);
            }
        }
        setVisibility(GONE);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSelected(List<AMediaBean> selectlist) {
        if (selectlist.isEmpty() && (MultimediaHelper.Type.IMAGE.equals(this.type) || MultimediaHelper.Type.VIDEO.equals(this.type))) {
            binding.tvNumber.setText(currentTag + " ");
        } else {
            String unit = getContext().getString(MultimediaHelper.Type.IMAGE.equals(this.type) ? R.string.unit_sheet : R.string.unit_item);
            binding.tvNumber.setText(StringHelper.autoNewSpace(getContext().getString(R.string.warning_launch), selectlist.size() + "", unit));
        }
        binding.tvNumber.setTag(selectlist.isEmpty() ? null : "hasSelect");
        binding.ivArrow.setVisibility(selectlist.isEmpty() ? MultimediaHelper.Type.FILE.equals(this.type) ? GONE : VISIBLE : GONE);
    }


    @Override
    public void toMediaSelectorPreview(AMediaBean currentBean, TreeMap<String, String> data, int position) {
        if (keyBoardBarListener != null) {
            keyBoardBarListener.toMediaSelectorPreview((cbOriginal != null && cbOriginal.isChecked()), currentTag, currentBean.getPath(), data, position);
//            if(currentBean instanceof ImageBean)
//                keyBoardBarListener.toMediaSelectorPreview(cbOriginal.isChecked(), currentTag, currentBean.getPath(), data);
//            else if(currentBean instanceof VideoBean)
//                keyBoardBarListener.toVideoSelectorPreview(currentBean.getPath());
        }

        if (chatNormalActivityKeyBoardListener != null) {
            chatNormalActivityKeyBoardListener.toMediaSelectorPreview((cbOriginal != null && cbOriginal.isChecked()), currentTag, currentBean.getPath(), data, position);
        }

        if (onNewKeyboardListener != null) {
            onNewKeyboardListener.toMediaSelectorPreview((cbOriginal != null && cbOriginal.isChecked()), currentTag, currentBean.getPath(), data, this.getMaxCount());
        }
    }

    @Override
    public void onChange(MultimediaHelper.Type type, boolean selfChange, Uri uri) {
        List<AMediaBean> beans = MultimediaHelper.query(getContext(), type, 1);
        if (beans == null || beans.isEmpty()) {
            return;
        }
        switch (type) {
            case IMAGE:
                for (AMediaBean b : beans) {
                    mediaPhotoBeans.remove(b);
                    mediaPhotoBeans.add(0, b);
                }
                Collections.sort(mediaPhotoBeans);
                break;
            case VIDEO:
                for (AMediaBean b : beans) {
                    mediaVideoBeans.remove(b);
                    mediaVideoBeans.add(0, b);
                }
                Collections.sort(mediaVideoBeans);
                break;
//            case FILE:
//                for (AMediaBean b:beans) {
//                    mediaFileBeans.remove(b);
//                    mediaFileBeans.add(0, b);
//                }
//                Collections.sort(mediaFileBeans);
//                break;
        }
        sectionPicturePath();
        sectionVideoPath();
    }

    public void onDestroy(Context context) {
        try {
            if (imageObserver != null) {
                context.getContentResolver().unregisterContentObserver(imageObserver);
                imageObserver = null;
            }
        } catch (Exception ignored) {
        }
    }
}
