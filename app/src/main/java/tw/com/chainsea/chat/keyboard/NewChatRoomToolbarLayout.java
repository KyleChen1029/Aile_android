package tw.com.chainsea.chat.keyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.google.common.base.Strings;

import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.keyboard.listener.OnNewChatRoomToolbarListener;
import tw.com.chainsea.chat.style.RoomThemeStyle;

/**
 * current by evan on 2020-07-09
 *
 * @author Evan Wang
 * @date 2020-07-09
 */
public class NewChatRoomToolbarLayout extends LinearLayout implements View.OnClickListener {
    public NewChatRoomToolbarView tView;
    OnNewChatRoomToolbarListener<ChatRoomEntity> onNewChatRoomToolbarListener;

    // bind data
    ChatRoomEntity entity;

    public NewChatRoomToolbarLayout(Context context) {
        super(context);
        init(context);
    }

    public NewChatRoomToolbarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NewChatRoomToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.setOrientation(LinearLayout.VERTICAL);
        View root = inflater.inflate(R.layout.new_chat_room_toolbar, this);
        tView = NewChatRoomToolbarView.bindView(root);
        tView.setListener(this);
    }

    public void setOnNewChatRoomToolbarListener(OnNewChatRoomToolbarListener<ChatRoomEntity> onNewChatRoomToolbarListener) {
        this.onNewChatRoomToolbarListener = onNewChatRoomToolbarListener;
    }

    /**
     * 綁定聊天室資料
     */
    public void bind(ChatRoomEntity entity) {
        this.entity = entity;
    }

    /**
     * 刷新 Toolbar 狀態
     * <p>room name (title)</p>
     * <p>room business name (s title)</p>
     * <p>room icon (service)</p>
     * <p>room member size ( discuss & group )</p>
     */
    @SuppressLint("SetTextI18n")
    public void refresh() {
        // 聊天室名稱
        String title = entity.getTitle(getContext());

        tView.tvTitle.setText(title);

        // 是訂閱號 服務號 顯示 icon
        if (ChatRoomType.SERVICES_or_SUBSCRIBE.contains(entity.getType())) {
            tView.tvTitle.setText(entity.getServiceNumberName());
            tView.civTitleIcon.setVisibility(VISIBLE);
            AvatarService.post(getContext(), entity.getServiceNumberAvatarId(), PicSize.SMALL, tView.civTitleIcon, R.drawable.custom_default_avatar);
        } else {
            tView.civTitleIcon.setVisibility(GONE);
        }

        // 有商機名稱顯示 小型title 廣播聊天室
        String subTitle = entity.getSubTitle(getContext());
        if (!Strings.isNullOrEmpty(subTitle)) {
            tView.tvSTitle.setVisibility(View.GONE);
            tView.tvSTitle.setText(subTitle);
        } else {
            tView.tvSTitle.setVisibility(View.GONE);
        }

        // 社團與多人顯示 人員數量
        if (ChatRoomType.GROUP_or_DISCUSS.contains(entity.getType())) {
            tView.tvMemberSize.setVisibility(VISIBLE);
            tView.tvMemberSize.setText("(" + entity.getMemberIds().size() + ")");
        } else {
            tView.tvMemberSize.setVisibility(GONE);
        }


        if (ChatRoomType.broadcast.equals(entity.getType())) {
            tView.civTitleIcon.setVisibility(GONE);
            tView.tvMemberSize.setVisibility(GONE);

        }

        RoomThemeStyle themeStyle = RoomThemeStyle.UNDEF;
        if (!Strings.isNullOrEmpty(entity.getBusinessId()) && !ChatRoomType.SERVICES_or_SUBSCRIBE.contains(entity.getType())) {
            themeStyle = RoomThemeStyle.BUSINESS;
        } else {
            themeStyle = RoomThemeStyle.of(entity.getType().name());
        }

        themeStyle(themeStyle);
        setFunctionStatus();
    }


    /**
     * 依照聊天室特性調整該聊天室功能按鈕
     */
    private void setFunctionStatus() {
        tView.ivBusiness.setVisibility(GONE);
        tView.ivPen.setVisibility(GONE);
        tView.ivChannel.setVisibility(GONE);
        tView.ivSearch.setVisibility(GONE);
        tView.ivInvite.setVisibility(GONE);
        tView.ivCall.setVisibility(GONE);
        tView.ivDropDown.setVisibility(GONE);
        tView.ivDropDown.setImageResource(R.drawable.arrow_down);

        switch (entity.getType()) {
            case self:
                break;
            case friend:
            case group:
                tView.ivInvite.setVisibility(VISIBLE);
                tView.ivDropDown.setVisibility(VISIBLE);
                break;
            case discuss:
                tView.ivInvite.setVisibility(VISIBLE);
                tView.ivDropDown.setVisibility(VISIBLE);
                tView.ivPen.setVisibility(entity.isCustomName() ? GONE : VISIBLE);
                tView.ivPen.setOnClickListener(entity.isCustomName() ? null : this);
                break;
            case subscribe:
                tView.ivDropDown.setImageResource(R.drawable.ic_vipcn_detail);
                tView.ivSearch.setVisibility(VISIBLE);
                tView.ivDropDown.setVisibility(VISIBLE);
                break;
            case services:
                tView.ivSearch.setVisibility(VISIBLE);
                if (entity.getLastMessage() != null) {
                    ChannelType channelType = entity.getLastMessage().getFrom();
                    setChannel(channelType);
                }
                break;
            case business:
                tView.ivBusiness.setVisibility(GONE);
                tView.ivPen.setVisibility(GONE);
                tView.ivChannel.setVisibility(GONE);
                tView.ivSearch.setVisibility(GONE);
                tView.ivInvite.setVisibility(GONE);
                tView.ivCall.setVisibility(GONE);
                tView.ivDropDown.setVisibility(GONE);
                break;
            case broadcast:
                tView.ivBusiness.setVisibility(View.INVISIBLE);
                break;
        }
    }


    public void setBackAction(View.OnClickListener listener) {
        tView.ivBack.setOnClickListener(listener);
    }

    /**
     * 設置渠道圖標與點擊事件
     */
    public void setChannel(ChannelType channelType) {
        if (!ChatRoomType.services.equals(entity.getType())) {
            tView.ivChannel.setOnClickListener(null);
            return;
        }

        tView.ivChannel.setVisibility(VISIBLE);
        tView.ivChannel.setOnClickListener(this);
        switch (channelType) {
            case FB:
                tView.ivChannel.setImageResource(R.drawable.ic_fb);
                break;
            case QBI:
            case AILE_WEB_CHAT:
                tView.ivChannel.setImageResource(R.drawable.qbi_icon);
                break;
            case LINE:
                tView.ivChannel.setImageResource(R.drawable.ic_line);
                break;
            case WEICHAT:
                tView.ivChannel.setImageResource(R.drawable.wechat_icon);
                break;
            case CE:
                tView.ivChannel.setImageResource(R.drawable.ce_icon);
                break;
            case IG:
                tView.ivChannel.setImageResource(R.drawable.ic_ig);
                break;
            case GOOGLE:
                tView.ivChannel.setImageResource(R.drawable.ic_google_message);
                break;
            case UNDEF:
            default:
                tView.ivChannel.setOnClickListener(null);
                tView.ivChannel.setVisibility(GONE);
                break;
        }
    }


    /**
     * Toolbar 樣式設定
     *
     * @param themeStyle
     */
    private void themeStyle(RoomThemeStyle themeStyle) {
        tView.root.setBackgroundColor(themeStyle.getMainColor());
    }

    public void setTitle(String title) {
        entity.getTitle(getContext());
    }


    public void setSmallTitle(String smallTitle) {

    }


    public void setStyle() {

    }


    @Override
    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        if (onNewChatRoomToolbarListener == null) {
            return;
        }

        switch (v.getId()) {
            case R.id.iv_back:
                onNewChatRoomToolbarListener.onBackClick(entity, v);
                break;
            case R.id.nkl_input:
            case R.id.ll_title_box:
            case R.id.tv_title:
            case R.id.tv_s_title:
            case R.id.civ_title_icon:
                onNewChatRoomToolbarListener.onTitleBoxClick(entity, v);
                break;
            case R.id.iv_pen:
                onNewChatRoomToolbarListener.onPenClick(entity, v);
                break;
            case R.id.iv_channel:
                onNewChatRoomToolbarListener.onChannelClick(entity, v);
                break;
            case R.id.iv_search:
                onNewChatRoomToolbarListener.onSearchClick(entity, v);
                break;
            case R.id.iv_invite:
                onNewChatRoomToolbarListener.onInviteClick(entity, v);
                break;
            case R.id.iv_call:
                onNewChatRoomToolbarListener.onCallClick(entity, v);
                break;
            case R.id.iv_drop_down:
                onNewChatRoomToolbarListener.onDropDownClick(entity, v);
                break;
        }
    }
}
