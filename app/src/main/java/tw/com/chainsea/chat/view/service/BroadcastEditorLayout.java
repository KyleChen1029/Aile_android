package tw.com.chainsea.chat.view.service;

import static tw.com.chainsea.chat.messagekit.main.viewholder.FileMessageView.formatSizeDisplay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import pl.droidsonroids.gif.GifImageView;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.file.FileHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.android.common.voice.VoiceHelper;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.broadcast.BroadcastMessageBean;
import tw.com.chainsea.ce.sdk.bean.broadcast.TopicEntity;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.content.FileContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.StickerContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.VoiceContent;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.http.ce.request.StickerDownloadRequest;
import tw.com.chainsea.ce.sdk.service.PhotoService;
import tw.com.chainsea.ce.sdk.service.StickerService;
import tw.com.chainsea.ce.sdk.service.listener.ProgressServiceCallBack;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.messagekit.enums.FileType;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.view.service.adapter.ServiceTopicAdapter;
import tw.com.chainsea.custom.view.image.CircleImageView;
import tw.com.chainsea.custom.view.image.RoundImageView;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemBaseViewHolder;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemNoSwipeViewHolder;

/**
 * current by evan on 2020-07-29
 *
 * @author Evan Wang
 * @date 2020-07-29
 */
public class BroadcastEditorLayout extends ConstraintLayout implements ServiceTopicAdapter.OnTopicSelectListener {
    BroadcastEditorView bView;
    OnBroadcastEditorListener<MessageEntity> onBroadcastEditorListener;
    ServiceTopicAdapter adapter = new ServiceTopicAdapter(ServiceTopicAdapter.Type.EDIT);
    MessageEntity entity;

//    BroadcastMessageBean.BroadcastMessageBeanBuilder broadcastMessageBeanBuilder = BroadcastMessageBean.Build();

    BroadcastEditorContentAdapter contentAdapter = new BroadcastEditorContentAdapter();

    int bottomToTopTargetId = ConstraintLayout.LayoutParams.PARENT_ID;

    public long broadcastTime = 0L;

    public BroadcastEditorLayout(Context context) {
        super(context);
        init(context);
    }

    public BroadcastEditorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BroadcastEditorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.broadcast_editor_layout, this);
        bView = BroadcastEditorView.bindView(this);
        listener();
//        setupEditorHeight(2.0d / 3.0d);
        bView.rvMessageContent.setAdapter(contentAdapter);
//        bView.llMessageContent.removeAllViews();
        bView.rvTopics.setAdapter(adapter);
        adapter.setOnTopicSelectListener(this);
        bView.dateTimePick.resetPlus(0, 0, 0);
        setClickable(true);
    }

    public void bind(MessageEntity entity) {
        this.entity = entity;
        if (entity != null) {
            this.broadcastTime = entity.getBroadcastTime();
        } else {
            this.broadcastTime = 0L;
        }
    }

    public void setSelectData(List<TopicEntity> topicEntities) {
        if (adapter != null) {
            adapter.setData(topicEntities).refresh();
            if (entity != null) {
                entity.setTopicArray(adapter.getSelectTopics());
            }
//            broadcastMessageBeanBuilder.topicArray(adapter.getSelecSNBtTopics());
        }
    }

    public void setSelectData(List<TopicEntity> topicEntities, boolean isShowAddCell) {
        if (adapter != null) {
            adapter.setShowAddCell(isShowAddCell).setData(topicEntities).refresh();
            if (entity != null) {
                entity.setTopicArray(adapter.getSelectTopics());
            }
//            broadcastMessageBeanBuilder.topicArray(adapter.getSelectTopics());
        }
    }

    public MessageEntity getEntity() {
        return this.entity;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh() {
        setActionStatus();
//        View view = getViewByMessageType(this.entity);
//        setEdieView(view);
        this.requestLayout();

        if (adapter != null) {
            adapter.refresh();
        }
        contentAdapter.bind(this.entity);
        contentAdapter.notifyDataSetChanged();
        contentAdapter.notifyItemChanged(0, this.entity);
        bView.rvMessageContent.requestLayout();

        bView.btnDateTimeClear.setVisibility(GONE);
        if (this.entity != null) {
            if (adapter != null && this.entity.getTopicArray() != null) {
                this.entity.getTopicArray().remove(TopicEntity.newHardCode());
                adapter.setData(Lists.newArrayList(this.entity.getTopicArray())).refresh();
            }
            bView.btnEditor.setVisibility(entity.isCanEditBroadcastAndCheckType() ? View.VISIBLE : View.GONE);
            bView.btnDelete.setVisibility(entity.isCanDeleteBroadcast() ? VISIBLE : GONE);
            bView.btnDateTimeClear.setVisibility(entity.isCanEditBroadcast() && this.broadcastTime > 0 ? VISIBLE : GONE);
            if (Strings.isNullOrEmpty(this.entity.getUpdateUserId())) {
                bView.tvLastEditorName.setText(this.entity.getSenderName());
            } else {
                UserProfileEntity userProfile = DBManager.getInstance().queryFriend(this.entity.getUpdateUserId());
                bView.tvLastEditorName.setText(userProfile != null ? userProfile.getNickName() : "");
            }
        } else {
            bView.tvLastEditorName.setText("");
        }

        entity.setTopicArray(adapter.getSelectTopics());
        Calendar nowCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"), Locale.TAIWAN);
        if (entity != null && broadcastTime > 0) {
//            this.broadcastTime =  entity.getBroadcastTime();
            nowCal.setTimeInMillis(this.broadcastTime);
            bView.tvDatetime.setText(bView.dateTimePick.getFormat().format(nowCal.getTime()));
        } else {
            bView.tvDatetime.setText("預約");
        }

        bView.dateTimePick.setCurrent(nowCal);
    }

//    private void setEdieView(View view) {
//        bView.llMessageContent.removeAllViews();
//        bView.llMessageContent.addView(view, 0);
//        bView.llMessageContent.requestLayout();
//    }

//    private View getViewByMessageType(MessageEntity entity) {
//        if (entity == null) {
//            return null;
//        }
//        switch (entity.getType()) {
//            case TEXT:
//                return buildTextContent(entity);
//            case IMAGE:
//                return buildImageContent(entity);
//            default:
//                return null;
//        }
//    }

//    private View buildTextContent(MessageEntity entity) {
//        View view = LayoutInflater.from(getContext()).inflate(R.layout.broadcast_editor_content_text, this, false);
//        TextView tvText = view.findViewById(R.id.tv_text);
//        tvText.setText(entity.content().simpleContent());
//        tvText.setMovementMethod(ScrollingMovementMethod.getInstance());
//        return view;
//    }
//
//    private View buildImageContent(MessageEntity entity) {
//        //broadcast_editor_content_text
//        View view = LayoutInflater.from(getContext()).inflate(R.layout.broadcast_editor_content_image, this, false);
//        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//        RoundImageView ivPhoto = view.findViewById(R.id.iv_photo);
//        if (entity.content() instanceof ImageContent) {
//            ImageContent imageContent = (ImageContent) entity.content();
//            if (imageContent.getUrl().startsWith("http")) {
//
//            } else {
//                Bitmap bitmap = BitmapHelper.getBitmapFromLocal(imageContent.getUrl());
//                if (imageContent.getUrl().endsWith(".gif")) {
//                    Glide.with(getContext()).asGif().load(imageContent.getUrl()).into(ivPhoto);
//                } else {
//                    ivPhoto.setImageBitmap(bitmap);
//                }
//                int[] zoom = zoomImage(bitmap.getWidth(), bitmap.getHeight());
//                layoutParams.width = zoom[0];
//                layoutParams.height = zoom[1];
//            }
//
//            view.setOnClickListener(v -> ActivityTransitionsControl.navigateToPhotoGallery(getContext(), imageContent.getUrl(), imageContent.getUrl(), new ActivityTransitionsControl.CallBack<Intent, String>() {
//                @Override
//                public void complete(Intent intent, String s) {
//                    getContext().startActivity(intent);
//                }
//            }));
//        }
//        return view;
//    }

    static int SMALL_PIC_SIZE = 460;

    public static int[] zoomImage(int inWidth, int inHeight) {
        if (inWidth <= 0 || inHeight <= 0) {
            return new int[]{100, 100};
        }

        int outWidth;
        int outHeight;

        if (inWidth > inHeight) {
            outWidth = SMALL_PIC_SIZE;
            outHeight = (inHeight * SMALL_PIC_SIZE) / inWidth;
        } else {
            outHeight = SMALL_PIC_SIZE;
            outWidth = (inWidth * SMALL_PIC_SIZE) / inHeight;
        }

        int[] sizes = new int[2];
        sizes[0] = outWidth;
        sizes[1] = outHeight;

        return sizes;
    }

    public void setBottomToTopTargetId(int targetId) {
        this.bottomToTopTargetId = targetId;
    }

    public void setOnBroadcastEditorListener(OnBroadcastEditorListener<MessageEntity> onBroadcastEditorListener) {
        this.onBroadcastEditorListener = onBroadcastEditorListener;
    }

    /**
     * 若 MessageEntity 沒有 id & MessageType == TEXT ? [btn_edit, btn_send] : [btn_send]
     * 若 MessageEntity 有 id [btn_delete]
     */
    public void setActionStatus() {
        bView.btnDelete.setVisibility(GONE);
        bView.btnEditor.setVisibility(GONE);
        bView.btnSend.setVisibility(GONE);
        if (entity != null) {
            // 沒ID
            if (Strings.isNullOrEmpty(entity.getId())) {
                if (MessageType.TEXT.equals(entity.getType())) {
                    bView.btnEditor.setVisibility(VISIBLE);
                    bView.btnSend.setVisibility(VISIBLE);
                } else {
                    bView.btnSend.setVisibility(VISIBLE);
                }
            } else {

                bView.btnSend.setVisibility(entity.isCanEditBroadcast() ? VISIBLE : GONE);
//                if (entity.getBroadcastTime() == 0 || entity.getBroadcastTime() < System.currentTimeMillis()) {
//                    bView.btnSend.setVisibility(GONE);
//                }
//                if (!BroadcastFlag.BOOKING.equals(entity.getBroadcastFlag())) {
//                    bView.btnSend.setVisibility(GONE);
//                }
            }
        }
    }

    /**
     * 編輯區高度編輯
     *
     * @param proportion
     */


    private void setupEditorHeight(double proportion) {
//        ViewGroup.LayoutParams spParams = bView.space.getLayoutParams();
//        spParams.height = proportion == 0.0d ?  LayoutParams.PARENT_ID : LayoutParams.WRAP_CONTENT;
        post(() -> {
            int max = getMaxHeight();
            CELog.d("" + max);
        });

//        ConstraintLayout.LayoutParams layoutParams = (LayoutParams) bView.llMessageContent.getLayoutParams();
//        layoutParams.height = proportion == 0.0d ? ConstraintLayout.LayoutParams.PARENT_ID : LayoutParams.WRAP_CONTENT;
//        ConstraintLayout.LayoutParams layoutParams = (LayoutParams) bView.rvMessageContent.getLayoutParams();
//        layoutParams.height = proportion == 0.0d ? ConstraintLayout.LayoutParams.PARENT_ID : LayoutParams.WRAP_CONTENT;
//        topToBottom = layoutParams.topToBottom;
//        bottomToTop = layoutParams.bottomToTop;
//        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
//        layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
//        bView.rvMessageContent.setLayoutParams(layoutParams);
//        bView.rvMessageContent.requestLayout();


        int paramsHeight = proportion == 0.0d ? ConstraintLayout.LayoutParams.PARENT_ID : ConstraintLayout.LayoutParams.WRAP_CONTENT;
        ConstraintLayout.LayoutParams params = (LayoutParams) this.getLayoutParams();
        params.height = paramsHeight;
//        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, paramsHeight);
//        params.verticalBias = proportion == 0.0d ? 0.0f : 1.0f;
//        params.bottomToTop = bottomToTopTargetId;
//        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
//        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        params.topToTop = proportion == 0.0d ? ConstraintLayout.LayoutParams.PARENT_ID : -1;
//        this.setLayoutParams(params);
        int toolbarHeight = UiHelper.dip2px(getContext(), 44.0f);
        int inputHeight = UiHelper.dip2px(getContext(), 45.0f);
        int displayHeight = UiHelper.getDisplayHeight(getContext());
        int roomHeight = displayHeight - (toolbarHeight + inputHeight);
//        this.setMaxHeight(proportion != 0.0d ? 2147483647 : (int) (roomHeight * proportion));


//        CELog.e("");
//        layoutParams.topToTop = proportion == 0.0d ?
        if (entity != null) {
            refresh();
        }

//        ConstraintLayout.LayoutParams layoutParams = (LayoutParams) bView.flMessageContent.getLayoutParams();
//        layoutParams.height = proportion == 0.0d ? ConstraintLayout.LayoutParams.PARENT_ID : ConstraintLayout.LayoutParams.WRAP_CONTENT;
//        bView.flMessageContent.requestLayout();

//        CELog.e("");


//        return (int) (roomHeight * proportion);
    }

    public void listener() {
        bView.ivExpansion.setOnClickListener(v -> {
            if (v.getTag() == null) { // 放大
                v.setTag("change height");
                bView.ivExpansion.setImageResource(R.drawable.collapse_white);
                setupEditorHeight(0.0d);
            } else { // 縮小至最高2/3
                v.setTag(null);
                setupEditorHeight(2.0d / 3.0d);
                bView.ivExpansion.setImageResource(R.drawable.expand_white);
            }
        });

        bView.ivCancel.setOnClickListener(v -> {
            if (onBroadcastEditorListener != null) {

                onBroadcastEditorListener.onEditorCancel(entity != null ? entity : null);
            }
        });

        bView.dateTimePick.setOnDateTimePickerListener((current, dateTime, millis) -> {
            bView.tvDatetime.setText(dateTime);
            bView.btnDateTimeClear.setVisibility(VISIBLE);
            this.broadcastTime = millis;
        });

        bView.ivCalendar.setOnClickListener(v -> {
            if (entity != null && entity.isCanEditBroadcast()) {
                bView.dateTimePick.setVisibility(bView.dateTimePick.getVisibility() == View.VISIBLE ? GONE : VISIBLE);
            }
        });

        bView.tvDatetime.setOnClickListener(v -> {
            if (entity != null && entity.isCanEditBroadcast()) {
                bView.dateTimePick.setVisibility(bView.dateTimePick.getVisibility() == View.VISIBLE ? GONE : VISIBLE);
            }
        });

        bView.btnDateTimeClear.setOnClickListener(v -> {
            bView.tvDatetime.setText("預約");
            v.setVisibility(GONE);
            this.broadcastTime = 0L;
            bView.dateTimePick.setVisibility(GONE);
            bView.dateTimePick.resetPlus(0, 0, 0);
        });

        // 刪除
        bView.btnDelete.setOnClickListener(v -> {
            if (onBroadcastEditorListener != null && entity.isCanDeleteBroadcast()) {
                onBroadcastEditorListener.onEditorDelete(entity);
            }
        });

        // 編輯 限制Text
        bView.btnEditor.setOnClickListener(v -> {
            if (onBroadcastEditorListener != null) {
                onBroadcastEditorListener.onEditorEdit(entity);
            }
//            setVisibility(GONE);
        });

        bView.btnSend.setOnClickListener(v -> {
            if (onBroadcastEditorListener != null) {
                entity.setBroadcastTime(this.broadcastTime);
                entity.setTopicArray(adapter.getSelectTopics());
                BroadcastMessageBean.BroadcastMessageBeanBuilder build = BroadcastMessageBean.Build()
                    .messageId(entity.getId())
                    .topicArray(entity.getTopicArray())
                    .broadcastTime(entity.getBroadcastTime())
                    .iContent(entity.content());
//                broadcastMessageBeanBuilder.iContent(entity.content());
                onBroadcastEditorListener.onEditorSend(build, entity);
            }
        });
    }


    @Override
    public void setVisibility(int visibility) {
        if (visibility != VISIBLE) {
            bView.ivExpansion.setTag(null);
            setupEditorHeight(2.0d / 3.0d);
            bView.ivExpansion.setImageResource(R.drawable.expand_white);
            bView.dateTimePick.setVisibility(GONE);
        }

        super.setVisibility(visibility);
        if (onBroadcastEditorListener != null) {
            onBroadcastEditorListener.onEditorVisibilityChange(entity, visibility);
        }

        if (visibility == GONE) {
            adapter.restart();
            bView.tvLastEditorName.setText("");
        }
    }

    @Override
    public void onTopicSelect(TopicEntity topicEntity) {
        adapter.remove(topicEntity).refresh();
    }

    @Override
    public void onTopicAdd(TopicEntity topicEntity) {
        ActivityTransitionsControl.navigateToServiceTopicSelector(getContext(), adapter.getSelectIds(), (intent, s) -> {
            getContext().startActivity(intent);
        });
    }


    class BroadcastEditorContentAdapter extends RecyclerView.Adapter<ItemBaseViewHolder<MessageEntity>> {
        MessageEntity entity;

        public void bind(MessageEntity entity) {
            this.entity = entity;
        }

        @NonNull
        @Override
        public ItemBaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 2) {
                return new ImageItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.broadcast_editor_content_image, parent, false));
            }

            if (viewType == 3) {
                return new StickerItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.broadcast_editor_content_image, parent, false));
            }

            if (viewType == 4) {
                return new FileItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.msgkit_file, parent, false));
            }

            if (viewType == 5) {
                return new VideoItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.msgkit_video, parent, false));
            }

            if (viewType == 6) {
                return new VoiceItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.msgkit_voice, parent, false));
            }

            return new TextItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.broadcast_editor_content_text, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ItemBaseViewHolder holder, int position) {
            holder.onBind(entity, 0, position);
        }

        @Override
        public int getItemCount() {
            return entity == null ? 0 : 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (entity != null) {
                switch (entity.getType()) {
                    case TEXT:
                        return 1;
                    case IMAGE:
                        return 2;
                    case STICKER:
                        return 3;
                    case FILE:
                        return 4;
                    case VIDEO:
                        return 5;
                    case VOICE:
                        return 6;
                    default:
                        return -1;
                }
            } else {
                return -1;
            }
        }

        static class TextItemViewHolder extends ItemNoSwipeViewHolder<MessageEntity> {
            TextView tvText;

            public TextItemViewHolder(View itemView) {
                super(itemView);
                tvText = itemView.findViewById(R.id.tv_text);
            }

            @Override
            public void onBind(MessageEntity entity, int section, int position) {
                tvText.setText(entity.content().simpleContent());
            }
        }

        class ImageItemViewHolder extends ItemNoSwipeViewHolder<MessageEntity> {

            RoundImageView ivPhoto;

            public ImageItemViewHolder(View itemView) {
                super(itemView);
                ivPhoto = itemView.findViewById(R.id.iv_photo);
            }

            @Override
            public void onBind(MessageEntity entity, int section, int position) {
                if (entity.content() instanceof ImageContent) {
                    ImageContent imageContent = (ImageContent) entity.content();
                    ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();

                    String url = !Strings.isNullOrEmpty(imageContent.getUrl()) ? imageContent.getUrl() : imageContent.getThumbnailUrl();
                    PhotoService.post(getContext(), url, null, R.drawable.image_load_error, new ProgressServiceCallBack<Drawable, RefreshSource>() {
                        @Override
                        public void progress(float progress, long total) {

                        }

                        @Override
                        public void complete(Drawable drawable, RefreshSource source) {
                            int width = drawable.getIntrinsicWidth();
                            int height = drawable.getIntrinsicHeight();
                            int[] zoom = zoomImage(width, height);
                            layoutParams.width = zoom[0];
                            layoutParams.height = zoom[1];
                            ivPhoto.setImageDrawable(drawable);
                        }

                        @Override
                        public void error(String message) {
                            layoutParams.width = 100;
                            layoutParams.height = 100;
                            ivPhoto.setImageResource(R.drawable.image_load_error);
                        }
                    });
                }
            }
        }

        class StickerItemViewHolder extends ItemNoSwipeViewHolder<MessageEntity> {
            GifImageView ivPhoto;

            public StickerItemViewHolder(View itemView) {
                super(itemView);
                ivPhoto = itemView.findViewById(R.id.iv_photo);
                ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
                layoutParams.height = 300;
                layoutParams.width = 300;
            }

            @Override
            public void onBind(MessageEntity entity, int section, int position) {
                super.onBind(entity, section, position);
                if (entity.content() instanceof StickerContent) {
                    StickerContent stickerContent = (StickerContent) entity.content();
                    String packageId = stickerContent.getPackageId();
                    String stickerId = stickerContent.getId();
                    ivPhoto.setVisibility(View.VISIBLE);
                    StickerService.postSticker(getContext(), stickerContent.getPackageId(), stickerContent.getId(), StickerDownloadRequest.Type.PICTURE, new ServiceCallBack<Drawable, RefreshSource>() {
                        @Override
                        public void complete(Drawable drawable, RefreshSource source) {
                            ivPhoto.setImageDrawable(drawable);
                        }

                        @Override
                        public void error(String message) {
                            ivPhoto.setImageResource(R.drawable.image_load_error);
                        }
                    });
//                    if (StringHelper.isValidUUID(stickerId) && !StringHelper.isValidUUID(packageId)) {
//                        ivPhoto.setImageResource(R.drawable.image_load_error);
//                    } else if (StringHelper.isValidUUID(stickerId) && StringHelper.isValidUUID(packageId)) {
//                        StickerService.postSticker(getContext(), stickerContent.getPackageId(), stickerContent.getId(), StickerDownloadRequest.Type.PICTURE, new ServiceCallBack<Drawable, RefreshSource>() {
//                            @Override
//                            public void complete(Drawable drawable, RefreshSource source) {
//                                ivPhoto.setImageDrawable(drawable);
//                            }
//
//                            @Override
//                            public void error(String message) {
//                                ivPhoto.setImageResource(R.drawable.image_load_error);
//                            }
//                        });
//                    } else {
//                        try {
//                            InputStream is = getContext().getAssets().open(stickerContent.getUrl().replace("assets://", ""));
//                            ivPhoto.setImageDrawable(new GifDrawable(is));
//                        } catch (Exception e) {
//                            CELog.e(e.getMessage());
//                            ivPhoto.setImageResource(R.drawable.image_load_error);
//                        }
//                    }
                }
            }
        }

        static class FileItemViewHolder extends ItemNoSwipeViewHolder<MessageEntity> {
            CircleImageView civIcon;
            TextView tvFileName;
            TextView tvTypeName;
            TextView tvFileSize;
            TextView tvSign;

            public FileItemViewHolder(View itemView) {
                super(itemView);
                civIcon = itemView.findViewById(R.id.fileIconCIV);
                tvFileName = itemView.findViewById(R.id.fileNameTV);
                tvTypeName = itemView.findViewById(R.id.fileTypeNameTV);
                tvFileSize = itemView.findViewById(R.id.fileSizeTV);
                tvSign = itemView.findViewById(R.id.fileStatusTV);
            }

            @Override
            public void onBind(MessageEntity entity, int section, int position) {
                super.onBind(entity, section, position);
                if (entity.content() instanceof FileContent) {
                    FileContent fileContent = (FileContent) entity.content();
                    String fileTyle = FileHelper.getFileTyle(fileContent.getName());
                    FileType fileType = FileType.of(fileTyle);
                    tvFileName.setText(fileContent.getName());
                    tvSign.setText("");
                    tvTypeName.setText(fileType.getName());
                    civIcon.setBorder(0, fileType.getDrawable());
                    String size;
                    if (!Strings.isNullOrEmpty(fileContent.getUrl()) && fileContent.getUrl().startsWith("http")) {
                        size = formatSizeDisplay(fileContent.getSize());
                        tvFileSize.setText(size);
                    } else if (!Strings.isNullOrEmpty(fileContent.getAndroid_local_path())) {
                        File localFile = new File(fileContent.getAndroid_local_path());
                        if (localFile.exists()) {
                            size = formatSizeDisplay(localFile.length());
                            tvFileSize.setText(size);
                        }
                    }
                }
            }
        }

        class VideoItemViewHolder extends ItemNoSwipeViewHolder<MessageEntity> {
            ConstraintLayout clOccupation;
            RoundImageView rivThumbnail;
            VideoView vvReview;
            ImageView ivPlay;

            public VideoItemViewHolder(View itemView) {
                super(itemView);
                clOccupation = itemView.findViewById(R.id.occupationCL);
                rivThumbnail = itemView.findViewById(R.id.thumbnailRIV);
                vvReview = itemView.findViewById(R.id.reviewVV);
                vvReview.setVisibility(View.GONE);
                ivPlay = itemView.findViewById(R.id.playIV);
            }

            @Override
            public void onBind(MessageEntity entity, int section, int position) {
                super.onBind(entity, section, position);
                if (entity.content() instanceof VideoContent) {
                    VideoContent videoContent = (VideoContent) entity.content();

                    ViewGroup.LayoutParams layoutParams = clOccupation.getLayoutParams();
                    int[] zoom = zoomImage(videoContent.getWidth(), videoContent.getHeight());
                    layoutParams.width = zoom[0];
                    layoutParams.height = zoom[1];
                    ivPlay.setVisibility(View.GONE);
                    rivThumbnail.setImageResource(R.drawable.file_msg_down_bg);

                    String url = !Strings.isNullOrEmpty(videoContent.getUrl()) ? videoContent.getUrl() : videoContent.getAndroid_local_path();
                    PhotoService.loadVideoThumbnail(getContext(), url, R.drawable.file_msg_down_bg, new ServiceCallBack<Bitmap, RefreshSource>() {
                        @Override
                        public void complete(Bitmap bitmap, RefreshSource source) {
                            rivThumbnail.setImageBitmap(bitmap);
                            ivPlay.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void error(String message) {
                            rivThumbnail.setImageResource(R.drawable.file_msg_down_bg);
                            ivPlay.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }

        static class VoiceItemViewHolder extends ItemNoSwipeViewHolder<MessageEntity> {

            TextView msg_voice_right_content;
            TextView msg_voice_left_content;
            ImageView message_voice_dot;

            public VoiceItemViewHolder(View itemView) {
                super(itemView);
                msg_voice_right_content = itemView.findViewById(R.id.msg_voice_right_content);
                msg_voice_left_content = itemView.findViewById(R.id.msg_voice_left_content);
                message_voice_dot = itemView.findViewById(R.id.message_voice_dot);
                message_voice_dot.setVisibility(GONE);
                msg_voice_right_content.setVisibility(GONE);
            }

            @Override
            public void onBind(MessageEntity entity, int section, int position) {
                itemView.setBackgroundResource(R.drawable.file_msg_bg);
                itemView.setPadding(20, 20, 20, 20);
                if (entity.content() instanceof VoiceContent) {
                    VoiceContent voiceContent = (VoiceContent) entity.content();
                    //voiceContent.getDuration();
                    msg_voice_left_content.setText(VoiceHelper.strDuration(voiceContent.getDuration() * 1000));
                }
            }
        }

//        public  String strDuration(double duration) {
//            int ms, s, m, h, d;
//            double dec;
//            double time = duration * 1.0;
//
//            time = (time / 1000.0);
//            dec = time % 1;
//            time = time - dec;
//            ms = (int)(dec * 1000);
//
//            time = (time / 60.0);
//            dec = time % 1;
//            time = time - dec;
//            s = (int)(dec * 60);
//
//            time = (time / 60.0);
//            dec = time % 1;
//            time = time - dec;
//            m = (int)(dec * 60);
//
//            time = (time / 24.0);
//            dec = time % 1;
//            time = time - dec;
//            h = (int)(dec * 24);
//
//            d = (int)time;
//
//            return (String.format("%02d:%02d",  m, s));
////        return (String.format("%d d - %02d:%02d:%02d.%03d", d, h, m, s, ms));
//        }

    }

    public interface OnBroadcastEditorListener<T> {
        // 取消事件
        void onEditorCancel(T t);

        // 刪除事件
        void onEditorDelete(T t);

        // 送出事件
        void onEditorSend(BroadcastMessageBean.BroadcastMessageBeanBuilder broadcastMessageBeanBuilder, T t);

        // 編輯事件
        void onEditorEdit(T t);

        // UI VISIBLE or GONE 事件
        void onEditorVisibilityChange(T t, int visibility);
    }

}
