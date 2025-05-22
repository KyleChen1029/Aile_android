package tw.com.chainsea.chat.view.roomList.mainRoomList.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.google.common.collect.ComparisonChain;

import java.util.Collections;

import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemSunRoom5Binding;
import tw.com.chainsea.chat.view.roomList.type.NodeCategory;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemBaseViewHolder;

/**
 * current by evan on 2020-11-04
 *
 * @author Evan Wang
 * @date 2020-11-04
 */
public class ChildRoomList3Adapter extends BaseRoomList3Adapter<ChatRoomEntity> {
    ChatRoomType type;

    public ChildRoomList3Adapter(ChatRoomType type, String bindKey) {
        super();
        bindKey(bindKey);
        this.type = type;
//        super.setAnimationEnable(false);
    }

    @NonNull
    @Override
    public ItemBaseViewHolder<ChatRoomEntity> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        super.setContext(parent.getContext());
        if (NodeCategory.CHILD_NODE.equals( NodeCategory.of(viewType)) ){
            ItemSunRoom5Binding sunRoom5Binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_sun_room_5, parent, false);
            return new ChildChatRoomViewHolder(sunRoom5Binding);
        }else {
            throw  new RuntimeException();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ItemBaseViewHolder<ChatRoomEntity> holder, int position) {
        ChatRoomEntity entity = this.metadata.get(position);
        holder.onBind(entity, 0, position);
    }

    @Override
    public int getItemViewType(int position) {
        return NodeCategory.CHILD_NODE.getViewType();
    }

    @Override
    public int getItemCount() {
        return this.limitedQuantity;
    }


    @Override
    public void sort() {
        if (ChatRoomType.person.equals(this.type) ) {
            Collections.sort(this.metadata, (o1, o2) -> ComparisonChain.start()
//                    .compareTrueFirst(Strings.isEmptyOrWhitespace(o1.getBusinessExecutorId()), !Strings.isEmptyOrWhitespace(o1.getBusinessExecutorId()))
                    .compare(o2.getWeights(), o1.getWeights())
                    .compare(o2.getUpdateTime(), o1.getUpdateTime())
                    .result());
        }

        if (ChatRoomType.friend.equals(this.type) ) {
            Collections.sort(this.metadata, (o1, o2) -> ComparisonChain.start()
//                    .compareTrueFirst(Strings.isEmptyOrWhitespace(o1.getBusinessExecutorId()), !Strings.isEmptyOrWhitespace(o1.getBusinessExecutorId()))
                    .compare(o2.getWeights(), o1.getWeights())
                    .compare(o2.getUpdateTime(), o1.getUpdateTime())
                    .result());
        }

        if (ChatRoomType.subscribe.equals(this.type) ) {
            Collections.sort(this.metadata);
        }

    }

    @Override
    protected void filter() {

    }

    @Override
    protected void pullAway() {

    }

    @Override
    protected void group() {

    }

//    @Override
//    public List<ChatRoomEntity> getUnreadEntities() {
//        return Lists.newArrayList();
//    }


//    @Override
//    public void executeAnimatorEnd(int position) {
//
//    }


    @Override
    public void handleEvent(EventMsg eventMsg) {

    }
}
