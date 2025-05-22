package tw.com.chainsea.chat.view.enlarge.adapter.viewholder.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserType;
import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.util.ThemeHelper;
import tw.com.chainsea.custom.view.image.CircleImageView;

/**
 * current by evan on 2020-04-14
 *
 * @author Evan Wang
 * @date 2020-04-14
 */
public abstract class EnLargeMessageBaseView<C extends IMessageContent> extends RecyclerView.ViewHolder {

    private String userId;
    protected Context context;
    private ChatRoomEntity chatRoomEntity;

    ConstraintLayout clSendInformation;
    public CircleImageView civIcon;
    public TextView tvName;
    protected boolean isGreenTheme = false;

    public EnLargeMessageBaseView(@NonNull View itemView) {
        super(itemView);
        this.isGreenTheme = ThemeHelper.INSTANCE.isGreenTheme();
        clSendInformation = itemView.findViewById(R.id.cl_send_information);
        civIcon = itemView.findViewById(R.id.civ_icon);
        tvName = itemView.findViewById(R.id.tv_name);
        this.context = itemView.getContext();
        this.userId = TokenPref.getInstance(this.context).getUserId();
    }

    public abstract void onBind(MessageEntity entity, C c, int position);

    protected boolean isRightMessage(MessageEntity entity) {
        if (ChatRoomType.SERVICES_or_SUBSCRIBE.contains(this.chatRoomEntity.getType()) && !userId.equals(this.chatRoomEntity.getOwnerId())) {
            return !this.chatRoomEntity.getOwnerId().equals(entity.getSenderId());
        } else {
            return !TextUtils.isEmpty(userId) && userId.equals(entity.getSenderId());
        }
    }

    protected String getUserId() {
        if (this.userId == null) {
            this.userId = TokenPref.getInstance(this.context).getUserId();
        }
        return this.userId;
    }

    public ChatRoomEntity getRoomEntity() {
        return this.chatRoomEntity;
    }

    public EnLargeMessageBaseView setChatRoomEntity(ChatRoomEntity chatRoomEntity) {
        this.chatRoomEntity = chatRoomEntity;
        return this;
    }

    protected List<UserProfileEntity> getMembers() {
        if (this.chatRoomEntity == null || this.chatRoomEntity.getMembers() == null || this.chatRoomEntity.getMembers().isEmpty()) {
            return Lists.newArrayList();
        }
        return this.chatRoomEntity.getMembers();
    }

    protected Map<String, String> getMembersTable() {
        if (this.chatRoomEntity == null || this.chatRoomEntity.getMembers() == null || this.chatRoomEntity.getMembers().isEmpty()) {
            return Maps.newHashMap();
        }
        Map<String, String> data = Maps.newHashMap();
        for (UserProfileEntity a : this.chatRoomEntity.getMembers()) {
            data.put(a.getId(), !Strings.isNullOrEmpty(a.getAlias()) ? a.getAlias() : a.getNickName());
        }
        return data;
    }


    @SuppressLint("RtlHardcoded")
    public EnLargeMessageBaseView setupSendMemberInformation(MessageEntity entity) {
        boolean isRight = isRightMessage(entity);

        int defaultBoundary = UiHelper.dip2px(context, 10);
        int boundary = UiHelper.dip2px(context, 18);
        civIcon.setVisibility(View.VISIBLE);
        tvName.setVisibility(View.VISIBLE);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(clSendInformation);
        constraintSet.connect(civIcon.getId(), isRight ? ConstraintSet.END : ConstraintSet.START, ConstraintSet.PARENT_ID, isRight ? ConstraintSet.END : ConstraintSet.START, boundary);
        constraintSet.connect(civIcon.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, defaultBoundary);
        constraintSet.connect(civIcon.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, defaultBoundary);
        constraintSet.connect(civIcon.getId(), isRight ? ConstraintSet.START : ConstraintSet.END, tvName.getId(), isRight ? ConstraintSet.END : ConstraintSet.START, defaultBoundary);
        constraintSet.connect(tvName.getId(), isRight ? ConstraintSet.START : ConstraintSet.END, ConstraintSet.PARENT_ID, isRight ? ConstraintSet.START : ConstraintSet.END);
        constraintSet.connect(tvName.getId(), isRight ? ConstraintSet.END : ConstraintSet.START, civIcon.getId(), isRight ? ConstraintSet.START : ConstraintSet.END, 0);
        constraintSet.connect(tvName.getId(), ConstraintSet.BOTTOM, civIcon.getId(), ConstraintSet.BOTTOM, 0);
        constraintSet.applyTo(clSendInformation);

        tvName.setGravity(isRight ? Gravity.RIGHT : Gravity.LEFT);
        tvName.setText(entity.getSenderName());


        tvName.setVisibility(isRight ? View.INVISIBLE : View.VISIBLE);
        civIcon.setVisibility(isRight ? View.INVISIBLE : View.VISIBLE);


        if (entity != null) {
            AvatarService.post(context, entity.getAvatarId(), PicSize.SMALL, civIcon, R.drawable.custom_default_avatar);

            if (chatRoomEntity != null) {
                civIcon.setBorder(0, 0);
                for (UserProfileEntity a : chatRoomEntity.getMembers()) {
                    if (a.getId().equals(entity.getSenderId()) && !"ALL".equals(a.getNickName())) {
                        if (!UserType.EMPLOYEE.equals(a.getUserType())) {
                            civIcon.setBorder(2, R.drawable.circle_session_employee_bg);
                            return this;
                        }
                    }
                }
            }
        }
        return this;
    }


}
