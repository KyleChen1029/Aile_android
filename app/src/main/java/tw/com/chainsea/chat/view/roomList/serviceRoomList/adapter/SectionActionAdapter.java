//package tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter;
//
//import androidx.annotation.NonNull;
//import androidx.databinding.DataBindingUtil;
//import androidx.recyclerview.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.google.common.base.Strings;
//import com.google.common.collect.ComparisonChain;
//import com.google.common.collect.Lists;
//
//import java.util.Collections;
//import java.util.List;
//
//import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
//import tw.com.chainsea.chat.R;
//import tw.com.chainsea.chat.databinding.ItemSectionActionBinding;
//import tw.com.chainsea.chat.util.Unit;
//import tw.com.chainsea.chat.util.UnreadUtil;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemNoSwipeViewHolder;
//
///**
// * current by evan on 12/3/20
// *
// * @author Evan Wang
// * date 12/3/20
// */
//// item_section_action
//public class SectionActionAdapter extends RecyclerView.Adapter<ItemNoSwipeViewHolder> {
//    private ItemSectionActionBinding binding;
//    List<ActionBean> actions = Lists.newArrayList();
//    private ServiceNumberEntity entity;
//    private OnActionListener<ActionBean, ServiceNumberEntity> onActionListener;
//    private int unreadNumber;
//
//    @NonNull
//    @Override
//    public ItemNoSwipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_section_action, parent, false);
//        return new ActionViewHolder(binding);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ItemNoSwipeViewHolder holder, int position) {
//        ActionBean bean = this.actions.get(position);
//        holder.onBind(bean, 0, position);
//    }
//
//    @Override
//    public int getItemCount() {
//        return this.actions.size();
//    }
//
//
//    public SectionActionAdapter bind(ServiceNumberEntity entity) {
//        this.entity = entity;
//        return this;
//    }
//
//    public SectionActionAdapter setOnActionListener(OnActionListener<ActionBean, ServiceNumberEntity> onActionListener) {
//        this.onActionListener = onActionListener;
//        return this;
//    }
//
//    public SectionActionAdapter setData(ActionBean... beans) {
//        this.actions = Lists.newArrayList(beans);
//        return this;
//    }
//
//    public SectionActionAdapter setData(ActionBean bean) {
//        this.actions.remove(bean);
//        this.actions.add(bean);
//        return this;
//    }
//
//    public SectionActionAdapter setData(List<ActionBean> actions) {
//        this.actions = actions;
//        return this;
//    }
//
//    public SectionActionAdapter setUnreadNumber(int unreadNumber) {
//        this.unreadNumber = unreadNumber;
//        return this;
//    }
//
//    private void sort() {
//        Collections.sort(this.actions, (o1, o2) -> ComparisonChain.start()
//                .compare(o1.getIndex(), o2.getIndex())
//                .result());
//    }
//
//    public void refresh() {
//        sort();
//        notifyDataSetChanged();
//    }
//
//    class ActionViewHolder extends ItemNoSwipeViewHolder<ActionBean> {
//        private ItemSectionActionBinding itemSectionActionBinding;
//
//        public ActionViewHolder(ItemSectionActionBinding binding) {
//            super(binding.getRoot());
//            itemSectionActionBinding = binding;
//        }
//
//        @Override
//        public void onBind(ActionBean bean, int section, int position) {
//            itemSectionActionBinding.tvUnread.setVisibility(View.GONE);
//            itemSectionActionBinding.ivActionIcon.setImageResource(bean.getResId());
//            itemSectionActionBinding.tvActionName.setText(bean.getNameResId());
//
//            if (ActionBean.CHAT.equals(bean)) {
//                String unReadNumber = UnreadUtil.INSTANCE.getUnreadText(unreadNumber);
//                if (Strings.isNullOrEmpty(unReadNumber)) {
//                    itemSectionActionBinding.tvUnread.setVisibility(View.GONE);
//                } else {
//                    itemSectionActionBinding.tvUnread.setText(unReadNumber);
//                    itemSectionActionBinding.tvUnread.setVisibility(View.VISIBLE);
//                }
//            }else {
//                itemSectionActionBinding.tvUnread.setVisibility(View.GONE);
//            }
//            itemView.setOnClickListener(v -> {
//                if (onActionListener != null) {
//                    switch (bean) {
//                        case BROADCAST:
//                            onActionListener.onBroadcastClick(bean, entity);
//                            break;
//                        case WAIT_TRANSFER:
//                            onActionListener.onWaitTransFerClick(bean, entity);
//                            break;
//                        case WELCOME_MESSAGE:
//                            onActionListener.onWelcomeMessageClick(bean, entity);
//                            break;
//                        case MEMBERS:
//                            onActionListener.onMembersClick(bean, entity);
//                            break;
//                        case CHAT:
//                            onActionListener.onChatClick(bean, entity);
//                            break;
//                        case HOME:
//                            onActionListener.onHomePageClick(bean, entity);
//                            break;
//                    }
//                }
//            });
//        }
//    }
//
//    public interface OnActionListener<E extends Enum, T> {
//        void onChatClick(E e, T t); //
//        void onWaitTransFerClick(E e, T t);
//        void onBroadcastClick(E e, T t); //
//        void onWelcomeMessageClick(E e, T t);
//        void onMembersClick(E e, T t);
//        void onHomePageClick(E e, T t);
//    }
//}
