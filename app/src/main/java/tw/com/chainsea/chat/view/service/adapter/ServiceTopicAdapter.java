package tw.com.chainsea.chat.view.service.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.broadcast.TopicEntity;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemInvitedFriendBinding;
import tw.com.chainsea.chat.databinding.ItemTopicBinding;
import tw.com.chainsea.chat.databinding.ItemTopicEditBinding;
import tw.com.chainsea.chat.databinding.ItemTopicMessageBinding;

/**
 * current by evan on 2020-08-14
 *
 * @author Evan Wang
 * @date 2020-08-14
 */
public class ServiceTopicAdapter extends RecyclerView.Adapter<ServiceTopicAdapter.ServiceTopicViewHolder> {
    private Context context;
    private ItemTopicMessageBinding topicMessageBinding;
    private ItemTopicBinding topicBinding;
    private ItemTopicEditBinding topicEditBinding;
    private ItemInvitedFriendBinding invitedFriendBinding;

    private List<TopicEntity> topicEntities = Lists.newArrayList();
    private OnTopicSelectListener onTopicSelectListener;
    private Set<String> selectIds = Sets.newHashSet();
    private Type type;
    private String keyword = "";

    boolean isShowAddCell = true;

    public ServiceTopicAdapter(Type type) {
        this.type = type;

        if (Type.EDIT.equals(this.type)) {
            this.topicEntities.add(TopicEntity.newHardCode());
        }
    }

    public enum Type {
        ROW,
        EDIT,
        GRID,
        MESSAGE
    }

    @NonNull
    @Override
    public ServiceTopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        this.context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding;

        if (Type.MESSAGE.equals(this.type)) {
            binding = DataBindingUtil.inflate(inflater, R.layout.item_topic_message, parent, false);
            return new ServiceTopicMessageViewHolder((ItemTopicMessageBinding) binding);
        }
        if (Type.ROW.equals(this.type)) {
            binding = DataBindingUtil.inflate(inflater, R.layout.item_topic, parent, false);
            return new ServiceTopicRowViewHolder((ItemTopicBinding) binding);
        }
        if (Type.EDIT.equals(this.type)) {
            binding = DataBindingUtil.inflate(inflater, R.layout.item_topic_edit, parent, false);
            return new ServiceTopicEditViewHolder((ItemTopicEditBinding) binding);
        }

        binding = DataBindingUtil.inflate(inflater, R.layout.item_invited_friend, parent, false);
        return new ServiceTopicGridViewHolder((ItemInvitedFriendBinding) binding);

    }

    @Override
    public void onBindViewHolder(@NonNull ServiceTopicViewHolder holder, int position) {
        holder.onBinding(topicEntities.get(position), position);
    }

    @Override
    public int getItemCount() {
        return this.topicEntities.size();
    }

    public ServiceTopicAdapter setData(List<TopicEntity> topicEntities) {
        this.topicEntities = topicEntities;
        if (Type.EDIT.equals(this.type) && this.isShowAddCell) {
            this.topicEntities.add(TopicEntity.newHardCode());
        }
        return this;
    }

    public ServiceTopicAdapter remove(TopicEntity topicEntity) {
        this.topicEntities.remove(topicEntity);
        return this;
    }


    public ServiceTopicAdapter setShowAddCell(boolean isShowAddCell) {
        this.isShowAddCell = isShowAddCell;
        return this;
    }

    public void restart() {
        this.selectIds.clear();
        this.topicEntities.clear();
        if (this.isShowAddCell) {
            this.topicEntities.add(TopicEntity.newHardCode());
        }
        refresh();
    }


    private void filter() {

    }

    private void sort() {
//        Collections.sort(topicEntities, (o1, o2) -> ComparisonChain.start()
//                .compareFalseFirst(o1.isHardCode(),o2.isHardCode())
//                .compare(o1.getId(), o2.getId())
//                .result());
        Collections.sort(topicEntities, (o1, o2) -> ComparisonChain.start()
            .compare(o1.getWeights(), o2.getWeights())
            .result());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh() {
        filter();
        sort();
        notifyDataSetChanged();
    }

    public ServiceTopicAdapter select(List<TopicEntity> topicEntities, String[] ids) {
        this.selectIds.clear();
        Iterator<String> idIterator = Lists.newArrayList(ids).iterator();
        while (idIterator.hasNext()) {
            String id = idIterator.next();
            Iterator<TopicEntity> topicIterator = Lists.newArrayList(topicEntities).iterator();
            while (topicIterator.hasNext()) {
                TopicEntity topicEntity = topicIterator.next();
                if (id.equals(topicEntity.getId())) {
                    this.selectIds.add(id);
                    if (onTopicSelectListener != null) {
                        onTopicSelectListener.onTopicSelect(topicEntity);
                    }
                }
            }
        }
        return this;

    }

    public String[] getSelectIds() {
        Set<String> idset = Sets.newHashSet();
        for (TopicEntity topicEntity : this.topicEntities) {
            if (!topicEntity.isHardCode()) {
                idset.add(topicEntity.getId());
            }
        }
        String[] ids = idset.toArray(new String[idset.size()]);
        return ids;
    }

    public List<TopicEntity> getSelectTopics() {
        return Lists.newArrayList(this.topicEntities);
    }

    public ServiceTopicAdapter setOnTopicSelectListener(OnTopicSelectListener onTopicSelectListener) {
        this.onTopicSelectListener = onTopicSelectListener;
        return this;
    }

    public ServiceTopicAdapter setSelectIds(Set<String> selectIds) {
        this.selectIds = selectIds;
        return this;
    }

    public ServiceTopicAdapter setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }


    abstract static class ServiceTopicViewHolder extends RecyclerView.ViewHolder {

        public ServiceTopicViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void onBinding(TopicEntity topicEntity, int position);
    }

    class ServiceTopicRowViewHolder extends ServiceTopicViewHolder {

        public ServiceTopicRowViewHolder(@NonNull ItemTopicBinding binding) {
            super(binding.getRoot());
            topicBinding = binding;
        }

        public void onBinding(TopicEntity topicEntity, int position) {
            itemView.setVisibility(!Strings.isNullOrEmpty(keyword) && !topicEntity.getName().contains(keyword) ? View.GONE : View.VISIBLE);
            AvatarService.post(context, topicEntity.getAvatarId(), PicSize.SMALL, topicBinding.civIcon, R.drawable.custom_default_avatar);
            topicBinding.tvTitle.setText(topicEntity.getName());
            topicBinding.tvContent.setText("");
            topicBinding.tvContent.setHint("類別說明");

            itemView.setBackgroundResource(selectIds.contains(topicEntity.getId()) ? R.drawable.selector_item_list_top : R.drawable.selector_item_list);

            itemView.setOnClickListener(v -> {
                if (onTopicSelectListener != null) {
                    onTopicSelectListener.onTopicSelect(topicEntity);
                }
            });
        }
    }

    class ServiceTopicMessageViewHolder extends ServiceTopicViewHolder {

        public ServiceTopicMessageViewHolder(@NonNull ItemTopicMessageBinding binding) {
            super(binding.getRoot());
            topicMessageBinding = binding;
        }

        public void onBinding(TopicEntity topicEntity, int position) {
            AvatarService.post(context, topicEntity.getAvatarId(), PicSize.SMALL, topicMessageBinding.civIcon, R.drawable.custom_default_avatar);

            topicMessageBinding.getRoot().setOnClickListener(v -> {
                if (onTopicSelectListener != null) {
                    onTopicSelectListener.onTopicSelect(topicEntity);
                }
            });
        }
    }

    class ServiceTopicGridViewHolder extends ServiceTopicViewHolder {

        public ServiceTopicGridViewHolder(@NonNull ItemInvitedFriendBinding binding) {
            super(binding.getRoot());
            invitedFriendBinding = binding;
        }

        public void onBinding(TopicEntity topicEntity, int position) {
            AvatarService.post(context, topicEntity.getAvatarId(), PicSize.SMALL, invitedFriendBinding.ivAvatar, R.drawable.custom_default_avatar);
            invitedFriendBinding.memberName.setText(topicEntity.getName());
            invitedFriendBinding.memberName.setVisibility(type.equals(Type.EDIT) ? View.GONE : View.VISIBLE);
            invitedFriendBinding.deleteIcon.setOnClickListener(v -> {
                if (onTopicSelectListener != null) {
                    onTopicSelectListener.onTopicSelect(topicEntity);
                }
            });
            itemView.setOnClickListener(v -> {
                if (onTopicSelectListener != null) {
                    onTopicSelectListener.onTopicSelect(topicEntity);
                }
            });
        }
    }

    class ServiceTopicEditViewHolder extends ServiceTopicViewHolder {

        public ServiceTopicEditViewHolder(@NonNull ItemTopicEditBinding binding) {
            super(binding.getRoot());
            topicEditBinding = binding;
        }

        public void onBinding(TopicEntity topicEntity, int position) {
            if (topicEntity.isHardCode()) {
                itemView.setVisibility(View.VISIBLE);
                topicEditBinding.memberName.setText("");
                topicEditBinding.civIcon.setImageResource(R.drawable.icon_add_people_30dp);
                itemView.setOnClickListener(v -> {
                    if (onTopicSelectListener != null && isShowAddCell) {
                        onTopicSelectListener.onTopicAdd(topicEntity);
                    }
                });
            } else {
                itemView.setVisibility(!Strings.isNullOrEmpty(keyword) && !topicEntity.getName().contains(keyword) ? View.GONE : View.VISIBLE);
                topicEditBinding.memberName.setText(topicEntity.getName());
                AvatarService.post(context, topicEntity.getAvatarId(), PicSize.SMALL, topicEditBinding.civIcon, R.drawable.custom_default_avatar);
                itemView.setOnClickListener(v -> {
                    if (onTopicSelectListener != null && isShowAddCell) {
                        onTopicSelectListener.onTopicSelect(topicEntity);
                    }
                });
            }
        }
    }


    public interface OnTopicSelectListener {
        void onTopicSelect(TopicEntity topicEntity);

        void onTopicAdd(TopicEntity topicEntity);
    }

}
