//package tw.com.chainsea.chat.view.roomList.mainRoomList;
//
//import android.content.Context;
//import android.text.SpannableStringBuilder;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.databinding.DataBindingUtil;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.common.base.Strings;
//
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Objects;
//
//import tw.com.aile.sdk.bean.message.MessageEntity;
//import tw.com.chainsea.android.common.json.JsonHelper;
//import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
//import tw.com.chainsea.android.common.text.StringHelper;
//import tw.com.chainsea.chat.util.TextViewHelper;
//import tw.com.chainsea.android.common.ui.UiHelper;
//import tw.com.chainsea.ce.sdk.SdkLib;
//import tw.com.chainsea.ce.sdk.bean.CustomerEntity;
//import tw.com.chainsea.ce.sdk.bean.InputLogBean;
//import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
//import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
//import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
//import tw.com.chainsea.ce.sdk.bean.msg.SourceType;
//import tw.com.chainsea.ce.sdk.bean.response.AileTokenApply;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
//import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType;
//import tw.com.chainsea.ce.sdk.database.DBManager;
//import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
//import tw.com.chainsea.ce.sdk.event.EventBusUtils;
//import tw.com.chainsea.ce.sdk.event.EventMsg;
//import tw.com.chainsea.ce.sdk.event.MsgConstant;
//import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
//import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
//import tw.com.chainsea.ce.sdk.http.ce.model.User;
//import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource;
//import tw.com.chainsea.chat.App;
//import tw.com.chainsea.chat.R;
//import tw.com.chainsea.chat.base.Constant;
//import tw.com.chainsea.chat.config.CallStatus;
//import tw.com.chainsea.chat.databinding.ItemRoomRecentBinding;
//import tw.com.chainsea.chat.lib.ToastUtils;
//import tw.com.chainsea.chat.service.ActivityTransitionsControl;
//import tw.com.chainsea.chat.util.TimeUtil;
//import tw.com.chainsea.chat.util.UnreadUtil;
//import tw.com.chainsea.custom.view.alert.AlertView;
//
//public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.ViewHolder> {
//    private Context context;
//    private List<ChatRoomEntity> list = new ArrayList<>();
//    private final String userId;
//    private final AileTokenApply.Resp.User user;
//    private static final int MIN_CLICK_DELAY_TIME = 1000;
//    private long lastClickTime = 0;
//
//    private final HashMap<String, Boolean> getChatMemberChatRoom = new HashMap<>();
//    private GetChatMemberInterface mGetChatMemberInterface;
//
//    public RoomListAdapter(Context context) {
//        this.userId = TokenPref.getInstance(context).getUserId();
//        this.user = TokenPref.getInstance(context).getUserResp();
//    }
//
//    public void setGetChatMemberInterface(GetChatMemberInterface getChatMemberInterface) {
//        mGetChatMemberInterface = getChatMemberInterface;
//    }
//
//    public ChatRoomEntity getCurrentItem(int position) {
//        try {
//            return list.get(position);
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    private void sort() {
//        Collections.sort(list);
////        notifyDataSetChanged();
//        notifyItemRangeChanged(0, list.size());
//    }
//
//    public void setData(List<ChatRoomEntity> list) {
//        this.list = list;
//        sort();
//    }
//
//    public void updateData(List<ChatRoomEntity> list) {
//        for (ChatRoomEntity item : list) {
//            updateData(item);
//        }
//        sort();
//    }
//
//    public void updateData(ChatRoomEntity roomEntity) {
//        int size = list.size();
//        if(size == 0){
//            list.add(roomEntity);
//            notifyItemChanged(0);
//        }else {
//            for (int i = 0; i < size; i++) {
//                if (list.get(i).getId().equals(roomEntity.getId())) {
//                    list.set(i, roomEntity);
//                    notifyItemChanged(i);
//                    break;
//                } else if (i == size - 1) { //比完全部都沒有就新增
//                    list.add(roomEntity);
//                    notifyItemChanged(i + 1);
//                }
//            }
//        }
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        context = parent.getContext();
//        return new ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_room_recent, parent, false));
//    }
//    int total = 0;
//    //參考BaseRoomList3Adapter的GeneralChatRoomViewHolder
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        ChatRoomEntity item = list.get(position);
//        //大頭
//        if (ChatRoomType.person.equals(item.getType())) { //自己的聊天室
//            UserProfileEntity entity = DBManager.getInstance().queryFriend(item.getOwnerId());
//            if (entity != null && entity.getAvatarId() != null) {
//                item.setAvatarId(entity.getAvatarId()); //頭圖要顯示為自己的頭圖
//                item.setName(entity.getNickName()); //名字也要設定
//            }
//        } else if (ChatRoomType.serviceMember.equals(item.getType()) //商務號成員聊天室
//                && ServiceNumberType.BOSS.equals(item.getServiceNumberType())) {
//            item.setAvatarId(user.getAvatarId()); //頭圖要顯示為自己的頭圖
//        }
//        if (ChatRoomType.discuss.equals(item.getType()) && item.getAvatarId() == null) {
//            // 有時候 db 的 chatRoomMember 會消失，之後再找原因，先用此方法代替
//            if (item.getChatRoomMember() == null || item.getChatRoomMember().size() == 0 && mGetChatMemberInterface != null) {
//                if (getChatMemberChatRoom.get(item.getId()) == null) {
//                    mGetChatMemberInterface.getChatMember(item.getId(), item.isCustomName(), position);
//                }
//                getChatMemberChatRoom.put(item.getId(), true);
//            } else {
//                holder.binding.civIcon.getChatRoomMemberIdsAndLoadMultiAvatarIcon(
//                        item.getChatRoomMember());
//            }
//        } else {
//            if(item.getName()!=null)
//                holder.binding.civIcon.loadAvatarIcon(item.getAvatarId(), item.getName());
//        }
//
//        //一般聊天列表除了個人聊天室及臨時成員聊天室以外，都顯示刪除
//        holder.binding.ivDelete.setVisibility(ChatRoomType.person.equals(item.getType())
//                || item.getProvisionalIds().contains(userId) ? View.GONE : View.VISIBLE);
//        //一般聊天列表除了臨時成員聊天室以外，都顯示Mute
//        holder.binding.ivMute.setVisibility(item.getProvisionalIds().contains(userId) ? View.GONE : View.VISIBLE);
//        boolean isTop = item.isTop();
//        boolean isMute = item.isMute();
//        if (isTop) {
//            holder.binding.civSmallIcon.setVisibility(View.VISIBLE);
//        } else {
//            holder.binding.civSmallIcon.setVisibility(View.GONE);
//        }
//        holder.binding.ivTop.setImageResource(isTop ? R.drawable.ic_no_top : R.drawable.ic_top);
//        holder.binding.ivMute.setImageResource(isMute ? R.drawable.amplification : R.drawable.not_remind);
//        holder.binding.ivRemind.setVisibility(!isMute ? View.GONE : View.VISIBLE);
//
//        MessageEntity lastMessage = item.getLastMessage();
//        if (lastMessage != null && !Strings.isNullOrEmpty(lastMessage.getContent())) {
//            if (!Objects.equals(lastMessage.getType(), MessageType.CALL)) {
//                holder.binding.tvContent.setText(getItemContent(lastMessage));
//                holder.binding.ivPhone.setVisibility(View.GONE);
//            } else {
//                holder.binding.ivPhone.setVisibility(View.VISIBLE);
//                try {
//                    JSONObject jsonObject = JsonHelper.getInstance().toJsonObject(lastMessage.getContent());
//                    String status = jsonObject.getString("status");
//                    if (status.equals(CallStatus.CallEnd.name())) {
//                        int callTime = jsonObject.getInt("callTime");
//                        holder.binding.tvContent.setText(App.getContext().getString(R.string.text_phone_call_end, callTime / 60, callTime % 60));
//                    } else if (status.equals(CallStatus.CallCancel.name()))
//                        holder.binding.tvContent.setText(App.getContext().getString(R.string.text_phone_call_cancel));
//                    else if (status.equals(CallStatus.CallBusy.name())) {
//                        holder.binding.ivPhone.setImageResource(!Objects.equals(lastMessage.getSenderId(), userId) ? R.drawable.ic_phone_busy : R.drawable.ic_phone_fill);
//                        holder.binding.tvContent.setText(!Objects.equals(lastMessage.getSenderId(), userId) ? App.getContext().getString(R.string.text_phone_call_busy_myself) : App.getContext().getString(R.string.text_phone_call_busy));
//                    } else
//                        holder.binding.tvContent.setText("");
//                } catch (Exception e) {
//                    holder.binding.tvContent.setText("");
//                }
//            }
//            holder.binding.tvTime.setText(TimeUtil.getTimeShowString(lastMessage.getSendTime(), true));
//        } else {
//            holder.binding.tvContent.setText("");
//            holder.binding.tvTime.setText("");
//        }
//
//        // 商務號擁有者：如果沒有進線服務的未讀訊息，則顯示 N
//        String unReadNumber = UnreadUtil.INSTANCE.getUnreadText(item, userId, item.getUnReadNum());
//        if (Strings.isNullOrEmpty(unReadNumber)) {
//            holder.binding.tvUnread.setVisibility(View.INVISIBLE);
//        } else {
//            holder.binding.tvUnread.setText(lastMessage != null ? lastMessage.getSourceType().equals(SourceType.LOGIN) ? "N" : unReadNumber : unReadNumber);
//            holder.binding.tvUnread.setVisibility(View.VISIBLE);
//        }
//
//        String name = item.getName();
//        if (ChatRoomType.serviceMember.equals(item.getType())
//                && ServiceNumberType.BOSS.equals(item.getServiceNumberType())) { //商務號成員聊天室
//            //顯示格式都是『{商務號擁有者名稱}和秘書群』
//            holder.binding.tvName.setText(TextViewHelper.setLeftImage(App.getContext(), String.format("%s和秘書群", user.getNickName()), R.drawable.ic_service_member_b));
//        } else if ((name == null || name.isEmpty()) && lastMessage != null) { //API有時候會不帶Name
//            holder.binding.tvName.setText(lastMessage.getSenderName());
//        } else if (item.getProvisionalIds().contains(userId) && item.getListClassify() == ChatRoomSource.MAIN) { //臨時成員聊天室
//            holder.binding.tvName.setText(SdkLib.getAppContext().getString(R.string.text_service_number_search_title_format, item.getName(), item.getServiceNumberName())); //顯示格式都是『{商務號擁有者名稱}和秘書群』
//        } else {
//            SpannableStringBuilder builder = new SpannableStringBuilder();
//            builder.append(StringHelper.getString(name, ""));
////         判斷為社團 or 群組才顯示人數
//            if (ChatRoomType.group.equals(item.getType())) {
//                builder.append(item.getMemberIds().size() > 0 ? " (" + item.getMemberIds().size() + ")" : "");
//                holder.binding.tvName.setText(TextViewHelper.setLeftImage(App.getContext(), builder, R.drawable.icon_group_chat_room));
//            } else if (ChatRoomType.discuss.equals(item.getType())) {
//                if (item.getChatRoomMember() != null) {
//                    TextViewHelper.setDiscussTitle(holder.binding.tvName, item.getName(), item.getChatRoomMember().size(), true);
//                }
//            } else if (ChatRoomType.person.equals(item.getType())) {
//                holder.binding.tvName.setText(TextViewHelper.setLeftImage(App.getContext(), builder, R.drawable.icon_self_chat_room_16dp));
//            } else {
//                holder.binding.tvName.setText(builder);
//            }
//        }
//
//        if (ChatRoomType.subscribe.equals(item.getType()) && item.getOwnerId().equals(userId)) {
//            holder.binding.tvName.setText(TextViewHelper.setLeftImage(App.getContext(), name, R.drawable.icon_subscribe_number_pink_15dp));
//        } else if(ChatRoomType.services.equals(item.getType())) { //設定領帶
//            CustomerEntity customerEntity = DBManager.getInstance().queryCustomer(item.getOwnerId());
//            if(customerEntity!=null) {
//                holder.binding.tvName.setCompoundDrawablesRelativeWithIntrinsicBounds(
//                        User.Type.VISITOR.equals(customerEntity.getUserType()) ? R.drawable.ic_visitor_15dp : R.drawable.ic_customer_15dp
//                        , 0, 0, 0);
//                holder.binding.tvName.setCompoundDrawablePadding(8);
//            }
//        }else {
//            holder.binding.tvName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
//        }
//
//        // 有未編輯訊息
//        InputLogBean bean = InputLogBean.from(item.getUnfinishedEdited());
//         if (!Strings.isNullOrEmpty(bean.getText())) {
//             holder.binding.tvContent.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.edit_gray, 0, 0, 0);
//             holder.binding.tvContent.setCompoundDrawablePadding(8);
//             holder.binding.tvContent.setText(bean.getText());
//         } else {
//             holder.binding.tvContent.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
//         }
//         //發送失敗訊息
//        if (item.getFailedMessage() != null) {
//            holder.binding.tvContent.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_mes_failure_14dp, 0, 0, 0);
//            holder.binding.tvContent.setCompoundDrawablePadding(8);
//            holder.binding.tvContent.setText(item.getFailedMessage().getContent(item.getFailedMessage().getContent()));
//        }
//        if (item.isFavourite()) {
//            holder.binding.ivFavourite.setVisibility(View.VISIBLE);
//        } else {
//            holder.binding.ivFavourite.setVisibility(View.INVISIBLE);
//        }
//
//        holder.binding.tvSetupUnreadTag.setText(StringHelper.autoNewLine(context, R.string.alert_notes, item.getUnReadNum() == 0 ? R.string.room_cell_swipe_menu_setup_unread : R.string.room_cell_swipe_menu_setup_read));
//        holder.binding.tvSetupUnreadTag.setBackgroundColor(item.getUnReadNum() == 0 ? 0xFFF5A623 : 0xFF88B1DE);
//    }
//
//    @Override
//    public int getItemCount() {
//        return list.size();
//    }
//
//    private CharSequence getItemContent(MessageEntity lastMessage) {
//        SpannableStringBuilder builder = new SpannableStringBuilder();
//        if (userId.equals(lastMessage.getSenderId())) { //如果是自己就改用我
//            builder.append("我 : ");
//        } else if(SourceType.SYSTEM.equals(lastMessage.getSourceType()) || SourceType.LOGIN.equals(lastMessage.getSourceType())
//                || SourceType.SATISFACTION.equals(lastMessage.getSourceType())){
//            builder.append("");
//        } else {
//            builder.append(lastMessage.getSenderName()).append(" : ");
//        }
//        if (MessageFlag.RETRACT == lastMessage.getFlag()) { //回收訊息
//            builder.append(Constant.RETRACT_MSG);
//        } else {
//            builder.append(lastMessage.getFormatContent());
//        }
//        return builder;
//    }
//
//    public class ViewHolder extends RecyclerView.ViewHolder {
//        ItemRoomRecentBinding binding;
//
//        public ViewHolder(ItemRoomRecentBinding binding) {
//            super(binding.getRoot());
//            this.binding = binding;
//
//            binding.clContentItem.setOnClickListener(clickListener);
//            binding.ivTop.setOnClickListener(clickListener);
//            binding.ivMute.setOnClickListener(clickListener);
//            binding.tvSetupUnreadTag.setOnClickListener(clickListener);
//            binding.ivDelete.setOnClickListener(clickListener);
//        }
//
//        private View.OnClickListener clickListener = view -> {
//            view.setEnabled(false);
//            if (!isFastClick()) {
//                view.setEnabled(true);
//                doClick(view);
//            } else {
//                view.setEnabled(true);
//            }
//        };
//
//        private void doClick(View v) {
//            if (v.equals(binding.clContentItem)) {
//                if(UiHelper.isDoubleClick())
//                    return;
//                doOpenChatRoom();
//            } else if (v.equals(binding.ivTop)) {
//                doSetupTop();
//            } else if (v.equals(binding.ivMute)) {
//                doSetupMute();
//            } else if (v.equals(binding.tvSetupUnreadTag)) {
//                doSetupUnread();
//            } else if (v.equals(binding.ivDelete)) {
//                doDelete();
//            }
//        }
//
//        private void doOpenChatRoom() {
//            if (getAbsoluteAdapterPosition() < 0) return;
//            ChatRoomEntity item = list.get(getAbsoluteAdapterPosition());
//            ActivityTransitionsControl.navigateToChat(context, item.getId(), (intent, s) -> {
//                context.startActivity(intent);
//            });
//        }
//
//        private void doSetupTop() {
//            if (getAbsoluteAdapterPosition() < 0) return;
//            ChatRoomEntity item = list.get(getAbsoluteAdapterPosition());
//            if (item.isTop()) {
//                ApiManager.getInstance().doRoomTopCancel(context, item.getId(), new ApiListener<String>() {
//                    @Override
//                    public void onSuccess(String s) {
//                        DBManager.getInstance().setChatRoomListItemTop(item.getId(), false);
//                        item.setTop(false);
//                        updateData(item);
//                        binding.ivTop.setImageResource(R.drawable.ic_top);
//                        binding.layoutSwip.resetStatus();
//                        sort();
//                    }
//
//                    @Override
//                    public void onFailed(String errorMessage) {
//                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show());
//                    }
//                });
//            } else {
//                ApiManager.getInstance().doRoomTop(context, item.getId(), new ApiListener<String>() {
//                    @Override
//                    public void onSuccess(String s) {
//                        DBManager.getInstance().setChatRoomListItemTop(item.getId(), true);
//                        item.setTop(true);
//                        updateData(item);
//                        binding.ivTop.setImageResource(R.drawable.ic_no_top);
//                        binding.layoutSwip.resetStatus();
//                        sort();
//                    }
//
//                    @Override
//                    public void onFailed(String errorMessage) {
//                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show());
//                    }
//                });
//            }
//        }
//
//        private void doSetupMute() {
//            if (getAbsoluteAdapterPosition() < 0) return;
//            ChatRoomEntity item = list.get(getAbsoluteAdapterPosition());
//            if (item.isMute()) {
//                ApiManager.getInstance().doRoomMuteCancel(context, item.getId(), new ApiListener<String>() {
//                    @Override
//                    public void onSuccess(String s) {
//                        DBManager.getInstance().setChatRoomListItemMute(item.getId(), false);
//                        item.setMute(false);
//                        updateData(item);
//                        binding.ivMute.setImageResource(R.drawable.not_remind);
//                        binding.layoutSwip.resetStatus();
//                    }
//
//                    @Override
//                    public void onFailed(String errorMessage) {
//                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show());
//                    }
//                });
//            } else {
//                ApiManager.getInstance().doRoomMute(context, item.getId(), new ApiListener<String>() {
//                    @Override
//                    public void onSuccess(String s) {
//                        DBManager.getInstance().setChatRoomListItemMute(item.getId(), true);
//                        item.setMute(true);
//                        updateData(item);
//                        binding.ivMute.setImageResource(R.drawable.amplification);
//                        binding.layoutSwip.resetStatus();
//                    }
//
//                    @Override
//                    public void onFailed(String errorMessage) {
//                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show());
//                    }
//                });
//            }
//        }
//
//        private void doSetupUnread() {
//            if (getAbsoluteAdapterPosition() < 0) return;
//            ChatRoomEntity item = list.get(getAbsoluteAdapterPosition());
//            if (item.getUnReadNum() == 0) {
//                DBManager.getInstance().setChatRoomListItemUnreadNum(item.getId(), -1);
//                item.setUnReadNum(-1);
//                list.set(getAbsoluteAdapterPosition(), item);
//                notifyItemChanged(getAbsoluteAdapterPosition());
//                binding.tvSetupUnreadTag.setText(StringHelper.autoNewLine(context, R.string.alert_notes, R.string.room_cell_swipe_menu_setup_read));
//                binding.tvSetupUnreadTag.setBackgroundColor(0xFF88B1DE);
//                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UPDATE_MAIN_BADGE_NUMBER_EVENT));
//                binding.layoutSwip.resetStatus();
//                sort();
//            } else {
//                if (item.getUnReadNum() == -1) {
//                    DBManager.getInstance().setChatRoomListItemUnreadNum(item.getId(), 0);
//                    DBManager.getInstance().setChatRoomListItemInteractionTime(item.getId());
//                    item.setUnReadNum(0);
//                    list.set(getAbsoluteAdapterPosition(), item);
//                    notifyItemChanged(getAbsoluteAdapterPosition());
//                    binding.tvSetupUnreadTag.setText(StringHelper.autoNewLine(context, R.string.alert_notes, R.string.room_cell_swipe_menu_setup_unread));
//                    binding.tvSetupUnreadTag.setBackgroundColor(0xFFF5A623);
//                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UPDATE_MAIN_BADGE_NUMBER_EVENT));
//                    binding.layoutSwip.resetStatus();
//                    sort();
//                } else {
//                    ApiManager.doMessagesRead(context, item.getId(), new ArrayList<>(), new ApiListener<String>() {
//                        @Override
//                        public void onSuccess(String s) {
//                            DBManager.getInstance().setChatRoomListItemUnreadNum(item.getId(), 0);
//                            DBManager.getInstance().setChatRoomListItemInteractionTime(item.getId());
//                            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//                                item.setUnReadNum(0);
//                                list.set(getAbsoluteAdapterPosition(), item);
//                                notifyItemChanged(getAbsoluteAdapterPosition());
//                                binding.tvSetupUnreadTag.setText(StringHelper.autoNewLine(context, R.string.alert_notes, R.string.room_cell_swipe_menu_setup_unread));
//                                binding.tvSetupUnreadTag.setBackgroundColor(0xFFF5A623);
//                                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UPDATE_MAIN_BADGE_NUMBER_EVENT));
//                                binding.layoutSwip.resetStatus();
//                                sort();
//                            });
//                        }
//
//                        @Override
//                        public void onFailed(String errorMessage) {
//                            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> ToastUtils.showToast(context, errorMessage));
//                        }
//                    });
//                }
//            }
//        }
//
//        private void doDelete() {
//            if (getAbsoluteAdapterPosition() < 0) return;
//            new AlertView.Builder()
//                    .setContext(context)
//                    .setStyle(AlertView.Style.Alert)
//                    .setMessage(context.getString(R.string.warning_want_to_delete_this_chat_room))
//                    .setOthers(new String[]{context.getString(R.string.alert_cancel), context.getString(R.string.alert_confirm)})
//                    .setOnItemClickListener((o, position) -> {
//                        if (position == 1) {
//                            ChatRoomEntity item = list.get(getAbsoluteAdapterPosition());
//                            ApiManager.getInstance().doRoomRecentDelete(context, item.getId(), new ApiListener<String>() {
//                                @Override
//                                public void onSuccess(String roomId) {
//                                    DBManager.getInstance().deleteRoomListItem(roomId);
//                                    list.remove(getAbsoluteAdapterPosition());
//                                    binding.layoutSwip.resetStatus();
//                                    //notifyItemRangeChanged(0, list.size());
//                                    notifyDataSetChanged();
//                                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.REFRESH_ROOM_BY_LOCAL));
//                                }
//
//                                @Override
//                                public void onFailed(String errorMessage) {
//                                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> ToastUtils.showToast(context, errorMessage));
//                                }
//                            });
//                        }
//                    })
//                    .build()
//                    .setCancelable(true)
//                    .show();
//        }
//    }
//
//    public boolean isFastClick() {
//        long currentTime = Calendar.getInstance().getTimeInMillis();
//        long interval = currentTime - lastClickTime;
//        if (interval < MIN_CLICK_DELAY_TIME) {
//            return true;
//        }
//        lastClickTime = currentTime;
//        return false;
//    }
//
//    public boolean isIncludeBossServerNumberChatRoom() {
//        if(!this.list.isEmpty()) {
//            for(ChatRoomEntity item: this.list) {
//                if(item.getServiceNumberType() != null &&
//                        item.getServiceNumberType().equals(ServiceNumberType.BOSS) && item.getServiceNumberOwnerId().equals(userId))
//                    return true;
//            }
//        }
//        return false;
//    }
//}
//
//interface GetChatMemberInterface {
//    void getChatMember(String roomId, boolean isCustomName, int position);
//}