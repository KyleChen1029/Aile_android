package tw.com.chainsea.chat.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;

import java.util.List;

import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.GroupPrivilegeEnum;
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.ServiceNumberPrivilege;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemChatMemberViewBinding;
import tw.com.chainsea.chat.databinding.ItemPopupHomePageBinding;

/**
 * Created by sunhui on 2017/5/14.
 */

public class ChatRoomMembersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<UserProfileEntity> datas = Lists.newArrayList();
    private final List<UserProfileEntity> entities = Lists.newArrayList();
    private OnItemClickListener onItemClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    private boolean isEdit;
    private final List<View> allItems = Lists.newArrayList();
    private Context ctx;
    private boolean deletable;
    private static final int ITEM_TYPE = 0;
    private static final int ITEM_HOME_PAGE = 1;
    public ChatRoomMembersAdapter(boolean deletable) {
        this.deletable = deletable;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.ctx = parent.getContext();
        if (viewType == ITEM_HOME_PAGE) {
            ItemPopupHomePageBinding itemPopupHomePageBinding = ItemPopupHomePageBinding.inflate(
                    LayoutInflater.from(this.ctx), parent, false);
            return new HomePageViewHolder(itemPopupHomePageBinding);
        }
        ItemChatMemberViewBinding binding = ItemChatMemberViewBinding.inflate(
                LayoutInflater.from(this.ctx), parent, false);
        return new MemberViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        UserProfileEntity account = this.entities.get(position);
        if (getItemViewType(position) == ITEM_HOME_PAGE) {
            // 主頁
            HomePageViewHolder homePageViewHolder = (HomePageViewHolder)holder;
            homePageViewHolder.viewBinding.ivHomePage.setImageResource(R.drawable.ic_home_page);
            homePageViewHolder.viewBinding.tvHomePage.setText(homePageViewHolder.viewBinding.getRoot().getContext().getString(R.string.text_home_page));
            homePageViewHolder.viewBinding.getRoot().setOnClickListener(v -> {
                if (this.onItemClickListener != null) {
                    this.onItemClickListener.onItemClick(v, account, position);
                }
            });
        } else {
            MemberViewHolder memberViewHolder = (MemberViewHolder) holder;
            memberViewHolder.viewBinding.memberName.setText(account.getNickName());
            //顯示左上擁有者圖案
            if (account.getGroupPrivilege() == GroupPrivilegeEnum.Owner || account.isOwner()
                    || account.getPrivilege() == ServiceNumberPrivilege.OWNER) {
                memberViewHolder.viewBinding.ivOwner.setVisibility(View.VISIBLE);
            } else {
                memberViewHolder.viewBinding.ivOwner.setVisibility(View.GONE);
            }

            //顯示左上管理者圖案
            if (account.getGroupPrivilege() == GroupPrivilegeEnum.Manager
                    || account.getPrivilege() == ServiceNumberPrivilege.MANAGER) {
                memberViewHolder.viewBinding.ivManager.setVisibility(View.VISIBLE);
            } else {
                memberViewHolder.viewBinding.ivManager.setVisibility(View.GONE);
            }

            if (deletable) {
                memberViewHolder.viewBinding.deleteIcon.setVisibility(View.VISIBLE);
            } else {
                memberViewHolder.viewBinding.deleteIcon.setVisibility(View.GONE);
            }

            memberViewHolder.viewBinding.ivAvatar.loadAvatarIcon(account.getAvatarId(), account.getNickName(), account.getId());

            memberViewHolder.viewBinding.getRoot().setOnClickListener(view -> {
                if (this.onItemClickListener != null) {
                    this.onItemClickListener.onItemClick(view, account, position);
                }
            });

            memberViewHolder.viewBinding.deleteIcon.setOnClickListener(view -> {
                if (this.onDeleteClickListener != null) {
                    this.onDeleteClickListener.onDeleteClick(view, account, position);
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return this.entities.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (entities.get(position).getId() == null) {
            return ITEM_HOME_PAGE;
        }
        return ITEM_TYPE;
    }

    private void sort() {

    }

    private void filter() {
        this.entities.clear();
        for (UserProfileEntity profile : this.datas) {
            if (!profile.isHardCode()) {
                this.entities.add(profile);
            }
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    public void refreshData() {
        filter();
        sort();
        notifyDataSetChanged();
    }

    public ChatRoomMembersAdapter setData(List<UserProfileEntity> datas) {
        this.datas.clear();
        this.datas.addAll(datas);
        return this;
    }

    public ChatRoomMembersAdapter setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }

//    public ChatRoomMembersAdapter setDeleteClickListenner(OnDeleteClickListener onDeleteClickListener) {
//        this.onDeleteClickListener = onDeleteClickListener;
//        return this;
//    }

//    public ChatRoomMembersAdapter setOnAddListener(OnAddListener addListener) {
//        return this;
//    }

    public interface OnItemClickListener {
        void onItemClick(View v, UserProfileEntity account, int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(View v, UserProfileEntity account, int position);
    }

//    public interface OnAddListener {
//        void onAdd();
//    }

    /**
     * 设置编辑状态
     *
     * @param isEdit 是否为编辑状态
     */
    public void setEdit(boolean isEdit) {
        this.isEdit = isEdit;
    }

//    public void removeItem(int position) {
//        allItems.remove(position);
//    }

    /**
     * 关闭所有 item
     */
//    public void closeAll() {
//        Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.hide_anim);
//        for (View allItem : allItems) {
//            allItem.startAnimation(animation);
//            allItem.setVisibility(View.INVISIBLE);
//        }
//    }

    /**
     * 将所有 item 向左展开
     */
//    public void openLeftAll() {
//        Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.show_anim);
//        for (View view : allItems) {
//            view.startAnimation(animation);
//            view.setVisibility(View.VISIBLE);
//        }
//    }

    /**
     * 获取编辑状态
     *
     * @return 是否为编辑状态
     */
    public boolean isEdit() {
        return isEdit;
    }

//    public void setDelable(boolean deletable) {
//        this.deletable = deletable;
//    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        ItemChatMemberViewBinding viewBinding;
        public MemberViewHolder(ItemChatMemberViewBinding binding) {
            super(binding.getRoot());
            viewBinding = binding;
        }
    }

    static class HomePageViewHolder extends RecyclerView.ViewHolder {
        ItemPopupHomePageBinding viewBinding;

        public HomePageViewHolder(ItemPopupHomePageBinding binding) {
            super(binding.getRoot());
            this.viewBinding = binding;
        }
    }
}

