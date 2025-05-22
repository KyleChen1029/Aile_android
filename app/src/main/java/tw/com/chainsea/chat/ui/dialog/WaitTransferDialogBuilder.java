package tw.com.chainsea.chat.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.common.base.Strings;

import java.util.List;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.bean.InputLogBean;
import tw.com.chainsea.ce.sdk.bean.InputLogType;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.content.AtContent;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.base.Constant;
import tw.com.chainsea.chat.databinding.BottomSheetTransferListBinding;
import tw.com.chainsea.chat.databinding.ItemBottomSheetTransferBinding;
import tw.com.chainsea.chat.lib.AtMatcherHelper;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.util.ContentUtil;
import tw.com.chainsea.chat.util.IntentUtil;
import tw.com.chainsea.chat.util.TextViewHelper;
import tw.com.chainsea.chat.view.roomList.serviceRoomList.ServiceRoomList3Fragment;

public class WaitTransferDialogBuilder {
    private final Context context;
    private List<ChatRoomEntity> list;

    public WaitTransferDialogBuilder(Context context, List<ChatRoomEntity> list) {
        this.context = context;
        this.list = list;
    }

    public WaitTransferDialogBuilder(Context context) {
        this.context = context;
    }

    public void setList(List<ChatRoomEntity> list) {
        this.list = list;
    }

    public Dialog create() {
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.ios_dialog);
        BottomSheetTransferListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottom_sheet_transfer_list, null, false);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        binding.btnClose.setOnClickListener(v -> dialog.dismiss());
        binding.recycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        binding.recycler.setAdapter(new Adapter(list));
        binding.recycler.measure(View.MeasureSpec.makeMeasureSpec(binding.recycler.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.UNSPECIFIED);
        int height = (int) (binding.recycler.getMeasuredHeight() + UiHelper.dp2px(context, 40));
        int maxHeight = (int) (UiHelper.getDisplayHeight(context) * 0.8);
        params.height = height < maxHeight ? height : maxHeight;
        binding.getRoot().setLayoutParams(params);
        dialog.setContentView(binding.getRoot());
        return dialog;
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        private final List<ChatRoomEntity> list;

        Adapter(List<ChatRoomEntity> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemBottomSheetTransferBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.item_bottom_sheet_transfer, parent, false);
            return new Adapter.ViewHolder(binding);
        }

        @Override
        @SuppressLint("SetTextI18n")
        public void onBindViewHolder(@NonNull Adapter.ViewHolder holder, int position) {
            ChatRoomEntity data = list.get(position);
            holder.binding.root.setVisibility(View.VISIBLE);
//            holder.binding.clContentCell.setBackgroundResource(ServiceNumberStatus.TIME_OUT.equals(data.getServiceNumberStatus()) ? R.drawable.selector_item_list_timeout : R.drawable.selector_item_list);
            holder.binding.ivMute.setImageResource(data.isMute() ? R.drawable.amplification : R.drawable.not_remind);
            holder.binding.tvName.setText(data.getName() + "@" + data.getServiceNumberName());

            holder.binding.civIcon.loadAvatarIcon(data.getAvatarId(), data.getName(), data.getId());
            holder.binding.tvReason.setText(data.getTransferReason());

            String agentId = data.getServiceNumberAgentId();
            if (!Strings.isNullOrEmpty(agentId)) {
                UserProfileEntity user = UserProfileReference.findById(null, agentId);
                assert user != null;
                if (!Strings.isNullOrEmpty(user.getNickName())) {
                    holder.binding.tvServiceAgent.setText(user.getNickName());
                }
                holder.binding.civAgentIcon.loadAvatarIcon(user.getAvatarId(), user.getNickName(), user.getId());
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final ItemBottomSheetTransferBinding binding;

            ViewHolder(ItemBottomSheetTransferBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
                this.binding.clContentItem.setOnClickListener(v -> {
                    ChatRoomEntity entity = list.get(getAdapterPosition());
                    if (ChatRoomReference.getInstance().hasLocalData(entity.getId())) {
                        ActivityTransitionsControl.navigateToChat(context, entity, ServiceRoomList3Fragment.class.getSimpleName(), (intent, s) -> {
                            if (context instanceof Activity) IntentUtil.INSTANCE.start(context, intent);
                        });
                    }
                });
            }
        }

        private CharSequence getItemContent(Context context, ChatRoomEntity t) {
            if (t == null) {
                return new SpannableString("");
            }
            MessageEntity failedMessage = t.getFailedMessage();
            // sort index == 0
            if (failedMessage != null) {
                SpannableStringBuilder builder = new SpannableStringBuilder("");
                if (failedMessage.getType() == MessageType.AT) {
                    builder = AtMatcherHelper.matcherAtUsers("@", ((AtContent) failedMessage.content()).getMentionContents(), t.getMembersTable());
                } else {
                    builder.append(failedMessage.content().simpleContent());
                }
                return TextViewHelper.setLeftImage(context, builder, R.drawable.ic_mes_failure_14dp);
            }

            // sort index == 1
            InputLogBean bean = InputLogBean.from(t.getUnfinishedEdited());
            if (!Strings.isNullOrEmpty(bean.getText())) {
                return AtMatcherHelper.setLeftImageAndHighLightAt(context, bean.getText(), R.drawable.ic_edit_gray_14dp, t.getMembersLinkedList(), InputLogType.AT.equals(bean.getType()) ? (ChatRoomType.subscribe.equals(t.getType()) ? 0xFF8F8E94 : 0xFF4A90E2) : 0xFF8F8E94);
            }

            // sort index == 2
            String senderId = DBManager.getInstance().querySenderIdFromLastMessage(t.getId());
            String selfId = TokenPref.getInstance(context).getUserId();

            String sendName = ChatRoomType.subscribe.equals(t.getType()) ? selfId.equals(senderId) ? "我" : t.getName() : "我";
            if (!Strings.isNullOrEmpty(senderId) && !TokenPref.getInstance(context).getUserId().equals(senderId)) {
                UserProfileEntity profile = DBManager.getInstance().queryFriend(senderId);
                if (profile != null) {
                    sendName = ChatRoomType.subscribe.equals(t.getType()) ? t.getName() : !Strings.isNullOrEmpty(profile.getAlias()) ? profile.getAlias() : profile.getNickName();
                }
            }
            int flag = DBManager.getInstance().queryFlagFromLastMessage(t.getId());
            if (MessageFlag.RETRACT.equals(MessageFlag.of(flag))) {
                return new SpannableString(sendName + ": " + Constant.RETRACT_MSG);
            } else {
                String type = DBManager.getInstance().queryTypeFromLastMessage(t.getId());
                String content = DBManager.getInstance().queryContentFromLastMessage(t.getId());
                SpannableStringBuilder builder = new SpannableStringBuilder("");
                if (MessageType.of(type) == MessageType.AT) {
                    AtContent atContent = (AtContent) ContentUtil.INSTANCE.content(MessageType.of(type), content);
                    builder = AtMatcherHelper.matcherAtUsers("@", atContent.getMentionContents(), t.getMembersTable());
                } else {
                    builder.append(ContentUtil.INSTANCE.content(MessageType.of(type), content).simpleContent());
                }
                builder.insert(0, sendName + ": ");
                return builder;
            }
        }
    }
}
