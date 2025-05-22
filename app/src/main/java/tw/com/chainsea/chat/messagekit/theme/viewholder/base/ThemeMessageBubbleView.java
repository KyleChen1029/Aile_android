package tw.com.chainsea.chat.messagekit.theme.viewholder.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.common.base.Strings;

import java.util.Objects;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.customview.AvatarIcon;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.messagekit.main.viewholder.listener.OnMessageGestureDetector;
import tw.com.chainsea.chat.util.ThemeHelper;
import tw.com.chainsea.custom.view.layout.CoordinateFrameLayout;


public abstract class ThemeMessageBubbleView extends ThemeMessageViewBase {
    public TextView tvTime;
    private CoordinateFrameLayout flContainer;
    public MessageEntity msg;
    private TextView mFrom;
    private LinearLayout mContainerLayout;
    protected TextView mName;
    private ViewGroup mAvLeftl;
    private ImageView mChannel;
    private ChannelType mChannelType;
    private String mUserId;
    private AvatarIcon ivLeftAvatar;
    private AvatarIcon ivRightAvatar;
    protected boolean isGreenTheme = false;


    public abstract void onClick(View v, MessageEntity message);

    public abstract void onDoubleClick(View v, MessageEntity message);

    public abstract void onLongClick(View v, float x, float y, MessageEntity message);

    public ThemeMessageBubbleView(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    protected int getResId() {
        /*if ("normal".equals(session.getKind())) {
            return R.layout.uikit_msg_bubble;
        }
        if ("needuser".equals(session.getKind())) {
            return R.layout.uikit_msg_bubble_need_user;
        }*/
        ChatRoomType type = chatRoomEntity.getType();
        if (type.equals(ChatRoomType.subscribe) || type.equals(ChatRoomType.services)) {
            return R.layout.item_msg_bubble_need_user;
        } else {
            return R.layout.reply_msg_bubble;
        }
        /*return -1;*/
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void inflate() {
        this.isGreenTheme = ThemeHelper.INSTANCE.isGreenTheme();
        mAvLeftl = findView(R.id.avatar_left_l);
        mChannel = findView(R.id.iv_channel);
        mName = findView(R.id.account_name);
        tvTime = findView(R.id.msg_time_tag);
        ImageView ivError = findView(R.id.msg_error);
        flContainer = findView(R.id.msg_container);
        mContainerLayout = findView(R.id.msg_container_layout);
        CheckBox mCheckBox = findView(R.id.check_box);
        mFrom = findView(R.id.from);
        ivLeftAvatar = findView(R.id.iv_left_avatar);
        ivRightAvatar = findView(R.id.iv_right_avatar);

        mCheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (msg != null) {
                msg.setDelete(b);
            }
        });

        ivError.setOnClickListener(v -> onListDilogItemClickListener.retry(msg));
        flContainer.setOnTouchListener(new OnMessageGestureDetector<>(getContext(), true, msg) {

            @Override
            public void onClick(View v, MessageEntity message) {
                ThemeMessageBubbleView.this.onClick(v, message);
            }

            @Override
            public void onDoubleClick(View v, MessageEntity message) {
                ThemeMessageBubbleView.this.onDoubleClick(v, message);
            }

            @Override
            public void onLongClick(View v, float x, float y, MessageEntity message) {
                ThemeMessageBubbleView.this.onLongClick(v, x, y, message);
            }
        });

        View.inflate(getContext(), getContentResId(), flContainer);
        inflateContentView();

    }

    @Override
    public void refresh(MessageEntity item) {
        msg = item;
        mChannelType = msg.getFrom();
        mUserId = TokenPref.getInstance(getContext()).getUserId();
        setAvatarView();
        setContent();
        setAccountName();
        setFrom();
        setChannel();
        try {
            bindContentView();
        } catch (Exception e) {
            CELog.e("Exception message" + msg.getSendTime());
        }
    }

    protected void setChannel() {

        if (mChannelType != null) {
            mChannel.setVisibility(View.VISIBLE);
            if (mChannelType == ChannelType.FB) {
                mChannel.setImageResource(R.drawable.ic_fb);
            } else if (mChannelType == ChannelType.LINE) {
                mChannel.setImageResource(R.drawable.ic_line);
            } else if (mChannelType == ChannelType.QBI) {
                mChannel.setImageResource(R.drawable.qbi_icon);
            } else if (mChannelType == ChannelType.WEICHAT) {
                mChannel.setImageResource(R.drawable.wechat_icon);
            } else if (mChannelType == ChannelType.AILE_WEB_CHAT) {
                mChannel.setImageResource(R.drawable.qbi_icon);
            } else if (mChannelType == ChannelType.IG) {
                mChannel.setImageResource(R.drawable.ic_ig);
            } else if (mChannelType == ChannelType.GOOGLE) {
                mChannel.setImageResource(R.drawable.ic_google_message);
            } else {
                mChannel.setVisibility(View.GONE);
            }
        } else {
            mChannel.setVisibility(View.GONE);
        }
    }

    private void setFrom() {
        mFrom.setVisibility(View.GONE);
    }

    final protected MessageEntity getMsg() {
        return msg;
    }

    private void setAvatarView() {
        if (isRightMessage()) {
            mAvLeftl.setVisibility(View.GONE);
        } else {
            mAvLeftl.setVisibility(View.VISIBLE);
        }
        AvatarIcon show = isRightMessage() ? ivRightAvatar : ivLeftAvatar;
        if (!isRightMessage() && chatRoomEntity.getType().equals(ChatRoomType.subscribe)) {
//            AvatarService.post(getContext(), this.chatRoomEntity.getServiceNumberAvatarId(), PicSize.SMALL, show, R.drawable.custom_default_avatar);
            show.loadAvatarIcon(chatRoomEntity.getServiceNumberAvatarId(), chatRoomEntity.getName(), chatRoomEntity.getId());
        } else {
            if (ChatRoomType.system.equals(this.chatRoomEntity.getType())) {
                String systemUserId = TokenPref.getInstance(itemView.getContext()).getSystemUserId();
                if (!Strings.isNullOrEmpty(systemUserId)) {
                    if (Objects.equals(systemUserId, msg.getSenderId())) {
                        String systemUserAvatarId = TokenPref.getInstance(itemView.getContext()).getSystemUserAvatarId();
                        String systemUserName = TokenPref.getInstance(itemView.getContext()).getSystemUserName();
                        show.loadAvatarIcon(systemUserAvatarId, systemUserName, systemUserId);
                    }
                }
            } else {
                UserProfileEntity userProfile = DBManager.getInstance().queryUser(msg.getSenderId());
                if (userProfile != null) {
                    show.loadAvatarIcon(userProfile.getAvatarId(), userProfile.getNickName(), userProfile.getId());
                }
            }
        }
        if (!chatRoomEntity.getType().equals(ChatRoomType.subscribe)) {
            show.setOnClickListener(new AvatarClick());
        }
    }

    protected void setAccountName() {
        String senderId = msg.getSenderId();
        if (!TextUtils.isEmpty(senderId)) {
            if (ChatRoomType.system.equals(this.chatRoomEntity.getType())) {
                String systemUserName = TokenPref.getInstance(itemView.getContext()).getSystemUserName();
                mName.setText(systemUserName);
                mName.setVisibility(View.VISIBLE);
            } else {
                UserProfileEntity profileEntity = DBManager.getInstance().queryUser(senderId);
                if (senderId.equals(mUserId)) {
                    mName.setVisibility(View.GONE);
                } else if (profileEntity != null) {
                    mName.setText(profileEntity.getNickName());
                } else {
                    mName.setVisibility(View.VISIBLE);
                    mName.setText(msg.getSenderName());
                }
            }
        }
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    protected abstract boolean showName();

    protected boolean isRightMessage() {
        if ((chatRoomEntity.getType().equals(ChatRoomType.services) || chatRoomEntity.getType().equals(ChatRoomType.subscribe)) && !mUserId.equals(chatRoomEntity.getOwnerId())) {
            return !chatRoomEntity.getOwnerId().equals(msg.getSenderId());
        } else {
            return mUserId.equals(msg.getSenderId());
        }
    }

    protected final void setGravity(View view, int gravity) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.gravity = gravity;
    }

    protected int leftBackground() {
        return R.drawable.bubble_receive;
    }

    protected int rightBackground() {
        if (chatRoomEntity.getType().equals(ChatRoomType.services) && !isSend()) {
            return R.drawable.bubble_send_f;
        } else {
            return R.drawable.bubble_send;
        }
    }

    public boolean isSend() {
        return mUserId.equals(msg.getSenderId());
    }

    private void setContent() {
        LinearLayout bodyContainer = view.findViewById(R.id.msg_body);
        int index = isRightMessage() ? 3 : 0;
        if (bodyContainer.getChildAt(index) != flContainer) {
            bodyContainer.removeView(flContainer);
            bodyContainer.addView(flContainer, index);
        }
        mContainerLayout.setGravity(isRightMessage() ? Gravity.END : Gravity.START);
    }

    abstract protected int getContentResId();

    abstract protected void inflateContentView();

    abstract protected void bindContentView();

    private class AvatarClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (onListDilogItemClickListener != null) {
                onListDilogItemClickListener.onAvatarClick(msg.getSenderId());
            }
        }
    }


}
