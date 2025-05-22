//package tw.com.chainsea.chat.view.chatroom.adapter;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.common.base.Strings;
//import com.google.common.collect.Iterables;
//import com.google.common.collect.Lists;
//
//import java.util.Collections;
//import java.util.List;
//
//import tw.com.chainsea.android.common.json.JsonHelper;
//import tw.com.chainsea.android.common.text.StringHelper;
//import tw.com.chainsea.ce.sdk.bean.Entity;
//import tw.com.chainsea.ce.sdk.bean.PicSize;
//import tw.com.chainsea.ce.sdk.bean.ProcessStatus;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
//import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity;
//import tw.com.chainsea.ce.sdk.bean.todo.TodoStatus;
//import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
//import tw.com.chainsea.ce.sdk.database.sp.UserPref;
//import tw.com.chainsea.ce.sdk.event.EventMsg;
//import tw.com.chainsea.ce.sdk.event.MsgConstant;
//import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
//import tw.com.chainsea.ce.sdk.service.AvatarService;
//import tw.com.chainsea.chat.R;
//import tw.com.chainsea.chat.databinding.ItemAdvisorySmallRoomBinding;
//import tw.com.chainsea.chat.style.RoomThemeStyle;
//import tw.com.chainsea.chat.util.Unit;
//import tw.com.chainsea.chat.util.UnreadUtil;
//import tw.com.chainsea.custom.view.image.CircleImageView;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemBaseViewHolder;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemNoSwipeViewHolder;
//
///**
// * Create by evan on 1/15/21
// *
// * @author Evan Wang
// * date 1/15/21
// */
//public class AdvisoryRoomAdapter extends RecyclerView.Adapter<ItemBaseViewHolder<Entity>> {
//    private final List<Entity> entities = Lists.newArrayList();
//
//    private final List<ChatRoomEntity> roomEntities = Lists.newArrayList();
//    private final List<ChatRoomEntity> memberRoomEntities = Lists.newArrayList();
//    private final List<TodoEntity> todoEntities = Lists.newArrayList();
//    private Context context;
//    private String selfId = "";
//    private String roomId = "";
//
//    RoomThemeStyle themeStyle = RoomThemeStyle.UNDEF;
//
//    private OnAdvisoryListener onAdvisoryListener;
//
//    long now = System.currentTimeMillis();
//
//    @NonNull
//    @Override
//    public ItemBaseViewHolder<Entity> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        if (this.context == null) {
//            this.context = parent.getContext();
//            this.selfId = TokenPref.getInstance(this.context).getUserId();
//        }
//        ItemAdvisorySmallRoomBinding binding = ItemAdvisorySmallRoomBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
//        if (viewType == 0) {
//            return new LeftViewHolder(binding);
//        } else {
//            return new SmallRoomViewHolder(binding);
//        }
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        Entity entity = this.entities.get(position);
//        if ("ChatRoomEntity".equals(entity.getClassName())) {
//            return 1;
//        } else {
//            return 0;
//        }
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ItemBaseViewHolder<Entity> holder, int position) {
//        Entity entity = this.entities.get(position);
//        holder.onBind(entity, 0, position);
//    }
//
//    @Override
//    public int getItemCount() {
//        return this.entities.size();
//    }
//
//    private Context getContext() {
//        return this.context;
//    }
//
//    private String getUserId() {
//        return this.selfId;
//    }
//
//    public AdvisoryRoomAdapter setRoomThemeStyle(RoomThemeStyle themeStyle) {
//        this.themeStyle = themeStyle;
//        return this;
//    }
//
//    public AdvisoryRoomAdapter setRoomId(String roomId) {
//        this.roomId = StringHelper.getString(roomId, "").toString();
//        return this;
//    }
//
//    public AdvisoryRoomAdapter setServiceMemberRoom(ChatRoomEntity entity) {
//        if (entity != null) {
//            //this.memberRoomEntities.clear();
//            this.memberRoomEntities.add(entity);
//        }
//        return this;
//    }
//
//    public void clearRoomData() {
//        this.roomEntities.clear();
//        notifyDataSetChanged();
//    }
//    public AdvisoryRoomAdapter removeRoomData(String roomId) {
//        this.roomEntities.remove(ChatRoomEntity.Build().id(roomId).build());
//        return this;
//    }
//
//    public AdvisoryRoomAdapter setRoomData(List<ChatRoomEntity> list) {
////        for (ChatRoomEntity entity : list) {
////            this.roomEntities.remove(entity);
//            this.roomEntities.addAll(list);
////        }
//        return this;
//    }
//
//    public AdvisoryRoomAdapter setTodoData(List<TodoEntity> list) {
//        for (TodoEntity entity : list) {
//            this.todoEntities.remove(entity);
//            this.todoEntities.add(entity);
//        }
//        return this;
//    }
//
//    public AdvisoryRoomAdapter setListener(OnAdvisoryListener onAdvisoryListener) {
//        this.onAdvisoryListener = onAdvisoryListener;
//        return this;
//    }
//
//    public void filter() {
//
//    }
//
//    public void sort() {
//        if (roomEntities != null){
//        Collections.sort(this.roomEntities);
//        }
//    }
//
//    private void assembly() {
//        this.entities.clear();
//        if (!this.memberRoomEntities.isEmpty()) {
//            ChatRoomEntity original = Iterables.getFirst(this.memberRoomEntities, null);
//            this.entities.add(original);
//        }
//
//        if (!this.todoEntities.isEmpty()) {
//            this.now = System.currentTimeMillis();
//            boolean isShow = false;
//            for (TodoEntity t : this.todoEntities) {
//                if (t.getRemindTime() > 0 && t.getRemindTime() < this.now && TodoStatus.PROGRESS.equals(t.getStatus())) {
//                    isShow = true;
//                    break;
//                }
//            }
//
//            if (isShow) {
//                TodoEntity original = Iterables.getFirst(this.todoEntities, null);
//                this.entities.add(original);
//            }
//        }
//
//        if (!this.roomEntities.isEmpty()) {
//            this.entities.addAll(this.roomEntities);
//        }
//    }
//
//    public void refreshData() {
//        this.now = System.currentTimeMillis();
//        filter();
//        sort();
//        assembly();
//        if (this.onAdvisoryListener != null) {
//            this.onAdvisoryListener.onItemCount(this.entities.size());
//        }
//        notifyDataSetChanged();
//    }
//
//    class LeftViewHolder extends ItemNoSwipeViewHolder<Entity> {
//
//        private ItemAdvisorySmallRoomBinding binding;
//
//        public LeftViewHolder(ItemAdvisorySmallRoomBinding binding) {
//            super(binding.getRoot());
//            this.binding = binding;
//            binding.civIcon.setVisibility(View.VISIBLE);
//            binding.ivTodoIcon.setVisibility(View.GONE);
//        }
//
//        @Override
//        public void onBind(Entity entity, int section, int position) {
//            if (entity instanceof TodoEntity) {
//                binding.civIcon.setImageResource(themeStyle.getTodoIconResId());
////                civIcon.setImageResource(R.drawable.res_check_list_circle_def);
////                civIcon.setColorFilter(ContextCompat.getColor(context, themeStyle.getMainColor()));
//                TodoEntity t = (TodoEntity) entity;
//                int count = 0;
//                now = System.currentTimeMillis();
//                for (TodoEntity todo : todoEntities) {
//                    if (todo.getRemindTime() > 0 && todo.getRemindTime() < now && TodoStatus.PROGRESS.equals(todo.getStatus())) {
//                        count++;
//                    }
//                }
//                if (count > 0) {
//                    binding.tvUnread.setVisibility(View.VISIBLE);
//                    binding.tvUnread.setText(String.valueOf(count));
//                } else {
//                    binding.tvUnread.setVisibility(View.GONE);
//                }
//
//                itemView.setOnClickListener(v -> {
//                    if (onAdvisoryListener != null) {
//                        onAdvisoryListener.onTodoSettingClick(todoEntities);
//                    }
//                });
//
//            }
//        }
//    }
//
//    class SmallRoomViewHolder extends ItemNoSwipeViewHolder<Entity> {
//        private ItemAdvisorySmallRoomBinding binding;
//
//        public SmallRoomViewHolder(ItemAdvisorySmallRoomBinding binding) {
//            super(binding.getRoot());
//            this.binding = binding;
//            binding.ivTodoIcon.setVisibility(View.GONE);
//        }
//
//        @Override
//        public void onBind(Entity entity, int section, int position) {
//            if (entity instanceof ChatRoomEntity) {
//                ChatRoomEntity chatRoomEntity = (ChatRoomEntity) entity;
//                if (chatRoomEntity.getUnReadNum() <= 0) {
//                    binding.tvUnread.setVisibility(View.GONE);
//                } else {
//                    binding.tvUnread.setVisibility(View.VISIBLE);
//                }
//
//                if (ChatRoomType.serviceMember.equals(chatRoomEntity.getType())) {
//                    binding.civIcon.setImageResource(themeStyle.getServiceMemberIconResId());
//
//                    itemView.setOnClickListener(v -> {
//                        //服務成員聊天室點擊事件
//                        if (onAdvisoryListener != null) {
//                            onAdvisoryListener.onServiceMemberRoomClick(chatRoomEntity);
//                        }
//                    });
//                } else {
//                    if (ChatRoomType.discuss.equals(chatRoomEntity.getType()) || (ChatRoomType.group.equals(chatRoomEntity.getType()) && Strings.isNullOrEmpty(chatRoomEntity.getAvatarId()))) {
//                        if (chatRoomEntity.getMemberAvatarData() == null || chatRoomEntity.getMemberAvatarData().isEmpty()) {
//                            chatRoomEntity.setMemberAvatarData(UserProfileReference.getMemberAvatarData(null, chatRoomEntity.getId(), getUserId(), 4));
//                        }
//                    }
//
//                    AvatarService.post(getContext(), chatRoomEntity.getServiceNumberAvatarId(), PicSize.SMALL, binding.civIcon, R.drawable.default_avatar);
//
//                    itemView.setOnClickListener(v -> {
//                        if (onAdvisoryListener != null) {
//                            onAdvisoryListener.onPrivateRoomClick(chatRoomEntity);
//                        }
//                    });
//                }
//            }
//        }
//    }
//
//    public interface OnAdvisoryListener {
//
//        void onServiceMemberRoomClick(ChatRoomEntity roomEntity);
//
//        void onTodoSettingClick(List<TodoEntity> entities);
//
//        void onPrivateRoomClick(ChatRoomEntity roomEntity);
//
//        void onItemCount(int count);
//    }
//
//    public void handleEvent(EventMsg eventMsg) {
//        switch (eventMsg.getCode()) {
//            case MsgConstant.SESSION_UPDATE_FILTER:
//            case MsgConstant.REFRESH_ROOM_BY_LOCAL:
//                ChatRoomEntity refreshEntity = JsonHelper.getInstance().from(eventMsg.getString(), ChatRoomEntity.class);
//                int refreshIndex = roomEntities.indexOf(refreshEntity);
//                if (refreshIndex != -1) {
//                    roomEntities.remove(refreshEntity);
//                    roomEntities.add(refreshIndex, refreshEntity);
//                }
//                refreshIndex = memberRoomEntities.indexOf(refreshEntity);
//                if (refreshIndex != -1) {
//                    memberRoomEntities.remove(refreshEntity);
//                    memberRoomEntities.add(refreshIndex, refreshEntity);
//                }
//                refreshData();
//                break;
//            case MsgConstant.UPDATE_TODO_EXPIRED_COUNT_EVENT:
//                refreshData();
//                break;
//            case MsgConstant.UI_NOTICE_TODO_REFRESH:
//                TodoEntity entity = JsonHelper.getInstance().from(eventMsg.getString(), TodoEntity.class);
//                if (TodoStatus.DELETED.equals(entity.getStatus())) {
//                    todoEntities.remove(entity);
//                    refreshData();
//                } else if (!ProcessStatus.UN_SYNC_DELETE.equals(entity.getProcessStatus())) {
//                    String personRoomId = UserPref.getInstance(context).getPersonRoomId();
//                    if (this.roomId.equals(personRoomId) && Strings.isNullOrEmpty(entity.getRoomId())) {
//                        todoEntities.remove(entity);
//                        todoEntities.add(entity);
//                    }
//
//                    if (this.roomId.equals(entity.getRoomId())) {
//                        todoEntities.remove(entity);
//                        todoEntities.add(entity);
//                    }
//                    refreshData();
//                }
//                break;
//
//        }
//    }
//
//
//}
