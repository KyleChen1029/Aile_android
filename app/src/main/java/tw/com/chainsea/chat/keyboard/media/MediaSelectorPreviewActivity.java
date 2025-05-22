package tw.com.chainsea.chat.keyboard.media;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.multimedia.AMediaBean;
import tw.com.chainsea.android.common.multimedia.MultimediaHelper;
import tw.com.chainsea.android.common.multimedia.VideoBean;
import tw.com.chainsea.android.common.text.StringHelper;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.ActivityMediaSelectorPreviewBinding;
import tw.com.chainsea.chat.keyboard.media.adatper.MediaSelectorPreviewAdapter;
import tw.com.chainsea.chat.keyboard.media.adatper.MediaSelectorThumbnailPreviewAdapter;
import tw.com.chainsea.chat.keyboard.media.adatper.PicItemDecoration;
import tw.com.chainsea.chat.lib.ToastUtils;
import tw.com.chainsea.chat.util.ThemeHelper;
import tw.com.chainsea.chat.widget.photoview.PhotoViewAttacher;

/**
 * 圖片選擇器，大版面預覽功能
 */
public class MediaSelectorPreviewActivity extends AppCompatActivity implements MediaSelectorThumbnailPreviewAdapter.OnMediaSelectorThumbnailPreviewListener<AMediaBean> {


    MediaSelectorPreviewAdapter previewAdapter;
    MediaSelectorThumbnailPreviewAdapter thumbnailPreviewAdapter;
    boolean isThumbnailViewShow = true;

    int currentPosition = -1;
    ListMultimap<String, AMediaBean> sectionData = ArrayListMultimap.create();
    List<AMediaBean> mediaBeans = Lists.newArrayList();
    private int maxCount = 9;
    List<AMediaBean> selectList = Lists.newArrayList();

    private ActivityMediaSelectorPreviewBinding binding;

    @Override
    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.INSTANCE.setTheme(MediaSelectorPreviewActivity.this);
        super.onCreate(savedInstanceState);
        binding = ActivityMediaSelectorPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        boolean isGreenTheme = ThemeHelper.INSTANCE.isGreenTheme();
        binding.cbOriginal.setButtonDrawable(isGreenTheme ? R.drawable.green_gray_checkbox_style : R.drawable.white_black_checkbox_style);
        Window w = this.getWindow();
        w.setStatusBarColor(0xFF404040);

        /* 取出意圖內資料 */
        String type = getIntent().getStringExtra(BundleKey.TYPE.key());
        boolean isOriginal = getIntent().getBooleanExtra(BundleKey.IS_ORIGINAL.key(), false);
        String dataJson = getIntent().getStringExtra(BundleKey.DATA.key());
        String current = getIntent().getStringExtra(BundleKey.CURRENT.key());
        this.maxCount = getIntent().getIntExtra(BundleKey.MAX_COUNT.key(), 9);

        /* 資料處理，注意有序以選取的資訊 */


        Map<String, String> data = JsonHelper.getInstance().<String, String>fromToMap(dataJson);
        TreeMap<String, String> treeData = Maps.newTreeMap((o1, o2) -> ComparisonChain.start()
            .compare(o1, o2)
            .result());
        treeData.putAll(data);


        this.mediaBeans = composeSectionBeans(type);
        int currentIndex = getCurrentIndex(mediaBeans, current);
        onChangeSelected(mediaBeans, treeData);


        // set view data
        previewAdapter = new MediaSelectorPreviewAdapter()
            .setOnPhotoTapListener(new OnPhotoTapListener())
            .setData(mediaBeans);
        binding.vpPreview.setAdapter(previewAdapter);
        binding.vpPreview.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);


        binding.rvSelector.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        int space = UiHelper.dip2px(this, 10);
        binding.rvSelector.addItemDecoration(new PicItemDecoration(space));
        thumbnailPreviewAdapter = new MediaSelectorThumbnailPreviewAdapter(selectList)
            .setOnMediaSelectorThumbnailPreviewListener(this);
        binding.rvSelector.setAdapter(thumbnailPreviewAdapter);

        binding.cbOriginal.setChecked(isOriginal);
        binding.cbOriginal.setText(" " + getString(R.string.warning_original_photo));

        scrollToPositionTo(currentIndex, false);
        initListener();
    }


    public void onChangeSelected(List<AMediaBean> mediaBeans, TreeMap<String, String> selectData) {
        selectList.clear();
        for (Map.Entry<String, String> entry : selectData.entrySet()) {
            String key = entry.getKey();
            String path = entry.getValue();
            for (AMediaBean bean : mediaBeans) {
                if (path.equals(bean.getPath())) {
                    if (selectList.size() >= maxCount) {
                        Toast.makeText(this, getString(R.string.chat_max_send_picture_or_video), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectList.add(bean);
                    bean.setSelectPosition(selectList.size());
                }
            }
        }
    }

    /**
     * 組裝媒體資料 & 取出所要的部件
     */
    private List<AMediaBean> composeSectionBeans(String type) {
        List<AMediaBean> photoMediaBeans = MultimediaHelper.query(this, MultimediaHelper.Type.IMAGE);
        List<AMediaBean> videoMediaBeans = MultimediaHelper.query(this, MultimediaHelper.Type.VIDEO);
        List<AMediaBean> allMediaBeans = Lists.newLinkedList();
        allMediaBeans.addAll(photoMediaBeans);
        allMediaBeans.addAll(videoMediaBeans);
        Collections.sort(allMediaBeans);
        sectionData.clear();
        sectionData.putAll("ALL", allMediaBeans);
        for (AMediaBean bean : allMediaBeans) {
            sectionData.put(bean.getFolderName(), bean);
        }
        if (Strings.isNullOrEmpty(type)) {
            type = "ALL";
        }
        return sectionData.get(type);
    }

    /**
     * 判斷當前位置
     */
    private int getCurrentIndex(List<AMediaBean> mediaBeans, String currentPath) {
        if (Strings.isNullOrEmpty(currentPath)) {
            return 0;
        }
        for (int i = 0; i < mediaBeans.size(); i++) {
            if (mediaBeans.get(i).getPath().equals(currentPath)) {
                return i;
            }
        }
        return 0;
    }


    /**
     * 移動到指定位置
     */
    private void scrollToPositionTo(int position, boolean isSmooth) {
        this.currentPosition = position;

        if (position >= 0 && position < mediaBeans.size()) {
            AMediaBean selectBean = getSelectListPosition(position);
            setSelectNumber(selectBean);
            linkageThumbnailPreviewAdapter(selectBean);
            calculateSelectQuantity();
            binding.vpPreview.setCurrentItem(position, false);
        }
    }

    /**
     * 依照滑動後位置取出以選擇的順號
     */
    private AMediaBean getSelectListPosition(int scrollPosition) {
        try {
            AMediaBean aMediaBean = mediaBeans.get(scrollPosition);
            for (AMediaBean bean : selectList) {
                if (aMediaBean.getPath().equals(bean.getPath())) {
                    return bean;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 設定右上角選取順號
     */
    private void setSelectNumber(AMediaBean selectBean) {
        if (selectBean == null || selectBean.getSelectPosition() <= 0) {
            binding.tvSelect.setText("");
            binding.tvSelect.setSelected(false);
        } else {
            binding.tvSelect.setText(String.valueOf(selectBean.getSelectPosition()));
            binding.tvSelect.setSelected(true);
        }
    }

    private void linkageThumbnailPreviewAdapter(AMediaBean selectBean) {
        thumbnailPreviewAdapter
            .setCurrentBean(selectList, selectBean)
            .refreshData(true);
    }

    private void calculateSelectQuantity() {
        binding.tvNumber.setText(StringHelper.autoNewSpace(getString(R.string.warning_launch), selectList.size() + "", getString(R.string.unit_sheet)));
        binding.rvSelector.setVisibility(this.selectList.size() <= 0 || !this.isThumbnailViewShow ? View.GONE : View.VISIBLE);
        binding.tvNumber.setVisibility(this.selectList.size() <= 0 ? View.GONE : View.VISIBLE);
//        tvSubmit.setVisibility(this.selectList.size() <= 0 ? View.GONE : View.VISIBLE);
    }


    private void subSelectPosition() {
        for (int index = 0, len = this.selectList.size(); index < len; index++) {
            AMediaBean folderBean = this.selectList.get(index);
            folderBean.setSelectPosition(index + 1);
        }
    }

//    /**
//     * 關閉縮略圖預覽
//     *
//     * @param aMediaBean
//     */
//    @Override
//    public void doCloseThumbnailView(AMediaBean aMediaBean) {
//        this.isThumbnailViewShow = false;
//        rvSelector.setVisibility(View.GONE);
//        clToolBar.setVisibility(View.GONE);
//        clBottomTool.setVisibility(View.GONE);
//    }
//
//    /**
//     * 開啟縮略圖預覽
//     *
//     * @param aMediaBean
//     */
//    @Override
//    public void doOpenThumbnailView(AMediaBean aMediaBean) {
//        this.isThumbnailViewShow = true;
//        rvSelector.setVisibility(this.selectList.size() <= 0 ? View.GONE : View.VISIBLE);
//        clToolBar.setVisibility(View.VISIBLE);
//        clBottomTool.setVisibility(View.VISIBLE);
//    }

    /**
     * 變化縮略圖預覽
     */
    public void doChangeThumbnailView() {
        this.isThumbnailViewShow = !this.isThumbnailViewShow;
        if (this.isThumbnailViewShow) {
            binding.clToolBar.setVisibility(View.VISIBLE);
            binding.clBottomTool.setVisibility(View.VISIBLE);
            binding.rvSelector.setVisibility(this.selectList.size() <= 0 ? View.GONE : View.VISIBLE);
        } else {
            binding.clToolBar.setVisibility(View.GONE);
            binding.rvSelector.setVisibility(View.GONE);
            binding.clBottomTool.setVisibility(View.GONE);
        }
    }

    @SuppressLint("DefaultLocale")
    public TreeMap<String, String> getSelectToMap() {
        TreeMap<String, String> data = Maps.newTreeMap((o1, o2) -> ComparisonChain.start()
            .compare(o1, o2)
            .result());
        if (this.selectList == null || this.selectList.isEmpty()) {
            return data;
        }
        for (int i = 0; i < this.selectList.size(); i++) {
            data.put(String.format("%09d", i), this.selectList.get(i).getPath());
        }
        return data;
    }


    class OnPhotoTapListener implements PhotoViewAttacher.OnPhotoTapListener {
        @Override
        public void onPhotoTap(View view, float x, float y) {
            doChangeThumbnailView();
        }

        @Override
        public void onOutsidePhotoTap() {
            doChangeThumbnailView();
        }
    }


    /*  Binding event  */


    /**
     * 左上返回扭
     */
    void doBackAction(View view) {
        TreeMap<String, String> data = getSelectToMap();
        getIntent().putExtra(BundleKey.IS_ORIGINAL.key(), binding.cbOriginal.isChecked())
            .putExtra(BundleKey.DATA.key(), JsonHelper.getInstance().toJson(data));
        setResult(Activity.RESULT_OK, getIntent());
        finish();
    }

    /**
     * 選中 or 反選
     *
     * @param view
     */
    void doSelectAction(View view) {
        AMediaBean bean = this.mediaBeans.get(currentPosition);
        if (this.selectList.contains(bean)) {
            this.selectList.remove(bean);
            bean.setSelectPosition(-1);
            setSelectNumber(null);
            subSelectPosition();
        } else {
            if (this.selectList.size() >= this.maxCount) {
                Toast.makeText(this, getString(R.string.chat_max_send_picture_or_video), Toast.LENGTH_SHORT).show();
                return;
            }
            this.selectList.add(bean);
            bean.setSelectPosition(this.selectList.size());
            setSelectNumber(bean);
        }
        linkageThumbnailPreviewAdapter(bean);
        calculateSelectQuantity();
    }

    /**
     * 縮略圖點擊
     *
     * @param aMediaBean
     */
    @Override
    public void onItemClick(AMediaBean aMediaBean) {
        int index = getCurrentIndex(this.mediaBeans, aMediaBean.getPath());
        scrollToPositionTo(index, false);
    }


    private void initListener() {
        binding.leftAction.setOnClickListener(this::doBackAction);
        binding.tvSelect.setOnClickListener(this::doSelectAction);
        binding.tvSubmit.setOnClickListener(this::doSubmitAction);
        binding.vpPreview.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (previewAdapter != null) {
                    previewAdapter.pauseOtherVideo(position);
                }
                currentPosition = position;
                AMediaBean bean = mediaBeans.get(position);
                setSelectNumber(bean);
                linkageThumbnailPreviewAdapter(bean);
                calculateSelectQuantity();
            }
        });
    }

    @Override
    public void onStop() {
        if (previewAdapter != null) {
            previewAdapter.pausePlayer();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (previewAdapter != null) {
            previewAdapter.releasePlayer();
        }
        super.onDestroy();
    }

    /**
     * 提交
     */
    void doSubmitAction(View view) {
        CELog.e("doSubmitAction");
        Map<String, Object> data;
        if (this.selectList.isEmpty() && this.currentPosition != -1) {
            //this.currentPosition
            AMediaBean bean = this.mediaBeans.get(this.currentPosition);
            if (bean instanceof VideoBean) {
                if (!((VideoBean) bean).name.endsWith("mp4")) {
                    ToastUtils.showToast(this, getString(R.string.text_video_limit_mp4_format));
                    return;
                }
            }
            data = Maps.newHashMap(ImmutableMap.of(
                "list", JsonHelper.getInstance().toJson(Lists.newArrayList(bean)),
                "isOriginal", "" + binding.cbOriginal.isChecked()
            ));
        } else {
            data = Maps.newHashMap(ImmutableMap.of(
                "list", JsonHelper.getInstance().toJson(this.selectList),
                "isOriginal", "" + binding.cbOriginal.isChecked()
            ));
        }
        Intent intent = new Intent();
        intent.putExtra("data",  JsonHelper.getInstance().toJson(data));
//        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.SEND_PHOTO_MEDIA_SELECTOR, JsonHelper.getInstance().toJson(data)));
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        TreeMap<String, String> data = getSelectToMap();
        getIntent().putExtra(BundleKey.IS_ORIGINAL.key(), binding.cbOriginal.isChecked())
            .putExtra(BundleKey.DATA.key(), JsonHelper.getInstance().toJson(data));
        setResult(Activity.RESULT_OK, getIntent());
        return super.onKeyDown(keyCode, event);
    }


}
