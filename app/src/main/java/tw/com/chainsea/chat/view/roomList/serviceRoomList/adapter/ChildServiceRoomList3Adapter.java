//package tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter;
//
//import android.view.LayoutInflater;
//import android.view.ViewGroup;
//
//import androidx.annotation.NonNull;
//
//import com.google.common.collect.ComparisonChain;
//
//import java.util.Collections;
//
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
//import tw.com.chainsea.ce.sdk.event.EventMsg;
//import tw.com.chainsea.chat.databinding.ItemSunRoom5Binding;
//import tw.com.chainsea.chat.view.roomList.mainRoomList.adapter.BaseRoomList3Adapter;
//import tw.com.chainsea.chat.view.roomList.type.NodeCategory;
//import tw.com.chainsea.chat.view.roomList.type.SectionedType;
//import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemBaseViewHolder;
//
///**
// * Create by evan on 1/6/21
// *
// * @author Evan Wang
// * @date 1/6/21
// */
//public class ChildServiceRoomList3Adapter extends BaseRoomList3Adapter<ChatRoomEntity> {
//    SectionedType sectionedType;
//
//    public ChildServiceRoomList3Adapter(SectionedType sectionedType, String bindKey) {
//        super();
//        bindKey(bindKey);
//        this.sectionedType = sectionedType;
//    }
//
//    @NonNull
//    @Override
//    public ItemBaseViewHolder<ChatRoomEntity> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        super.setContext(parent.getContext());
//        if (NodeCategory.SERVICE_CHILD_NODE.equals(NodeCategory.of(viewType))) {
//            ItemSunRoom5Binding binding = ItemSunRoom5Binding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
//            return new ChildServiceChatRoomViewHolder(this.sectionedType, binding);
//        } else {
//            throw new RuntimeException();
//        }
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ItemBaseViewHolder<ChatRoomEntity> holder, int position) {
//        ChatRoomEntity entity = this.metadata.get(position);
//        holder.onBind(entity, 0, position);
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        return NodeCategory.SERVICE_CHILD_NODE.getViewType();
////        return super.getItemViewType(position);
//    }
//
//    @Override
//    public int getItemCount() {
//        return this.limitedQuantity;
//    }
//
//
//    @Override
//    public void sort() {
//        Collections.sort(this.metadata, (o1, o2) -> ComparisonChain.start()
//                .compare(o2.getWeights(), o1.getWeights())
//                .compare(o2.getUpdateTime(), o1.getUpdateTime())
//                .result());
//    }
//
//    @Override
//    protected void filter() {
//
//    }
//
//    @Override
//    protected void pullAway() {
//
//    }
//
//    @Override
//    protected void group() {
//
//    }
//
////    @Override
////    public List<ChatRoomEntity> getUnreadEntities() {
////        return Lists.newArrayList();
////    }
//
//
//    @Override
//    public void handleEvent(EventMsg eventMsg) {
//
//    }
//}