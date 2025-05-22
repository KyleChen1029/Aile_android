package tw.com.chainsea.chat.view.chat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;

import java.lang.ref.WeakReference;
import java.util.List;

import tw.com.chainsea.ce.sdk.bean.Entity;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemAdvisorySmallRoomBinding;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemBaseViewHolder;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemNoSwipeViewHolder;

/**
 * Create by evan on 1/22/21
 *
 * @author Evan Wang
 * @date 1/22/21
 */
public class ChildChatMembersAdapter extends RecyclerView.Adapter<ItemBaseViewHolder<Entity>> {
    private List<UserProfileEntity> entities = Lists.newArrayList();
    private WeakReference<Context> weakReference;

    @NonNull
    @Override
    public ItemBaseViewHolder<Entity> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (weakReference == null) {
            this.weakReference = new WeakReference<Context>(parent.getContext());
        }
        ItemAdvisorySmallRoomBinding binding = ItemAdvisorySmallRoomBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MemberItemView(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemBaseViewHolder<Entity> holder, int position) {
        Entity entity = this.entities.get(position);
        holder.onBind(entity, 0, position);
    }

    @Override
    public int getItemCount() {
        return this.entities.size();
    }

    private Context getContext() {
        return this.weakReference.get();
    }

    public ChildChatMembersAdapter setData(List<UserProfileEntity> list) {
        for (UserProfileEntity profile : list) {
            this.entities.remove(profile);
            this.entities.add(profile);
        }
        return this;
    }

    private void sort() {

    }

    private void filter() {

    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshData() {
        filter();
        sort();
        notifyDataSetChanged();
    }

    class MemberItemView extends ItemNoSwipeViewHolder<Entity> {

        private ItemAdvisorySmallRoomBinding binding;

        public MemberItemView(ItemAdvisorySmallRoomBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.ivTodoIcon.setVisibility(View.GONE);
            binding.tvUnread.setVisibility(View.GONE);
        }

        @Override
        public void onBind(Entity entity, int section, int position) {
            super.onBind(entity, section, position);
            if (entity instanceof UserProfileEntity) {
                UserProfileEntity profile = (UserProfileEntity) entity;
                AvatarService.post(getContext(), profile.getAvatarId(), PicSize.SMALL, binding.civIcon, R.drawable.custom_default_avatar);
            }
        }
    }
}
