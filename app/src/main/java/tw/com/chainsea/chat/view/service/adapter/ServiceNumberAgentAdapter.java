package tw.com.chainsea.chat.view.service.adapter;

import static tw.com.chainsea.ce.sdk.bean.servicenumber.type.ServiceNumberPrivilege.COMMON;
import static tw.com.chainsea.ce.sdk.bean.servicenumber.type.ServiceNumberPrivilege.MANAGER;
import static tw.com.chainsea.ce.sdk.bean.servicenumber.type.ServiceNumberPrivilege.OWNER;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.ServiceNumberPrivilege;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.http.ce.model.Member;
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemServiceNumberAgentBinding;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemSwipeWithActionWidthViewHolder;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperExtension;

/**
 * current by evan on 2020-04-29
 *
 * @author Evan Wang
 * @date 2020-04-29
 */
public class ServiceNumberAgentAdapter extends RecyclerView.Adapter<ServiceNumberAgentAdapter.AgentViewHolder> {

    public ServiceNumberAgentAdapter(String userId, String serviceNumberId) {
        this.userId = userId;
        this.serviceNumberId = serviceNumberId;

    }

    private String userId = "";
    private String serviceNumberId = "";
    private boolean isTenantOwnerOrManager;

    private List<Member> agents = Lists.newLinkedList();
    private ItemTouchHelperExtension itemTouchHelperExtension;
    private OnServiceNumberAgentListener<Member> onServiceNumberAgentListener;

    private OnManagementClick onManagementClick;

    @NonNull
    @Override
    public AgentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        ItemServiceNumberAgentBinding binding = ItemServiceNumberAgentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AgentViewHolder(binding, onManagementClick);
    }

    @Override
    public void onBindViewHolder(@NonNull AgentViewHolder holder, int position) {
        Member profile = agents.get(position);
        holder.onBind(profile);
    }

    @Override
    public int getItemCount() {
        return this.agents.size();
    }


    /**
     * @param list
     * @return
     */
    public ServiceNumberAgentAdapter setData(List<Member> list) {
        this.agents.clear();
        this.agents.addAll(list);
        return this;
    }

    public ServiceNumberAgentAdapter setIsTenantOwner(boolean isOwner) {
        this.isTenantOwnerOrManager = isOwner;
        return this;
    }

    /**
     * 通知資料更新
     */
    @SuppressLint("NotifyDataSetChanged")
    public void refreshData() {
        notifyDataSetChanged();
    }

    public void refreshDataWhenRemove(Set<String> ids) {
        int notifyPosition = 0;
        Iterator<Member> iterator = agents.iterator();
        while (iterator.hasNext()) {
            Member profile = iterator.next();
            if (ids.contains(profile.getId())) {
                iterator.remove();
                notifyItemRemoved(notifyPosition);
            }
            notifyPosition++;
        }
    }


    /**
     * 滑塊事件監聽器
     *
     * @param itemTouchHelperExtension
     * @return
     */
    public ServiceNumberAgentAdapter setItemTouchHelperExtension(ItemTouchHelperExtension itemTouchHelperExtension) {
        this.itemTouchHelperExtension = itemTouchHelperExtension;
        return this;
    }


    /**
     * 點擊事件監聽器
     *
     * @param onServiceNumberAgentListener
     * @return
     */
    public ServiceNumberAgentAdapter setOnServiceNumberAgentListener(OnServiceNumberAgentListener<Member> onServiceNumberAgentListener) {
        this.onServiceNumberAgentListener = onServiceNumberAgentListener;
        return this;
    }

    public ServiceNumberAgentAdapter setOnManagementClickListener(OnManagementClick managementClick) {
        this.onManagementClick = managementClick;
        return this;
    }

    public interface OnServiceNumberAgentListener<T> {

        void onAvatarClick(T t);

        void onItemClick(T t);
    }

    public interface OnManagementClick {
        void onTranOwnerClick(UserProfileEntity profile);

        void onDesignateClick(UserProfileEntity profile);

        void onRemoveManagementClick(UserProfileEntity profile);

        void onDeleteClick(UserProfileEntity profile);
    }

    class AgentViewHolder extends ItemSwipeWithActionWidthViewHolder {

        private ItemServiceNumberAgentBinding binding;

        public AgentViewHolder(@NonNull ItemServiceNumberAgentBinding binding, OnManagementClick onManagementClick) {
            super(binding.getRoot());
            this.binding = binding;
            super.setMenuViews(binding.llLeftMenu, binding.llRightMenu);
            super.setContentItemView(binding.clContentItem);
        }


        //設置右邊 menu
        private void setRightMenu(String userId, Member profile) {
            ServiceNumberEntity serviceNumberEntity = ServiceNumberReference.findServiceNumberById(serviceNumberId);
            boolean isInManagerServiceNumber = false;
            if (serviceNumberEntity != null) {
                isInManagerServiceNumber = serviceNumberEntity.getName().contains("管理");
            }

            //如果是團隊管理者以上的權限
            if (isTenantOwnerOrManager && !isInManagerServiceNumber) {
                setTenantOwnerPermissions(userId, profile);
                return;
            }


            AtomicReference<ServiceNumberPrivilege> userPrivilege = new AtomicReference<>(COMMON);
            List<Member> user = agents.stream().filter(userProfile -> userProfile.getId().equals(userId)).collect(Collectors.toList());
            if (!user.isEmpty()) {
                userPrivilege.set(user.get(0).getPrivilege());
            }

            //先判斷進來的人是什麼樣的權限
            switch (userPrivilege.get()) {
                case OWNER:
                    setOwnerPermissions(profile);
                    break;
                case MANAGER:
                    setManagerPermissions(profile);
                    break;
                case COMMON:
                    binding.tvTranOwner.setVisibility(View.GONE);
                    binding.tvDesignate.setVisibility(View.GONE);
                    binding.ivDelete.setVisibility(View.GONE);
                    binding.tvRemoveManagement.setVisibility(View.GONE);
                    break;
            }
        }

        void setTenantOwnerPermissions(String userId, Member profile) {
            switch (profile.getPrivilege()) {
                case OWNER:
                    binding.tvTranOwner.setVisibility(View.GONE);
                    binding.tvDesignate.setVisibility(View.GONE);
                    binding.tvRemoveManagement.setVisibility(View.GONE);
                    binding.ivDelete.setVisibility(View.VISIBLE);
                    break;
                case MANAGER:
                    binding.tvTranOwner.setVisibility(View.VISIBLE);
                    binding.tvDesignate.setVisibility(View.GONE);
                    binding.tvRemoveManagement.setVisibility(View.VISIBLE);
                    binding.ivDelete.setVisibility(View.VISIBLE);
                    break;
                case COMMON:
                    binding.tvTranOwner.setVisibility(View.VISIBLE);
                    binding.tvDesignate.setVisibility(View.VISIBLE);
                    binding.ivDelete.setVisibility(View.VISIBLE);
                    binding.tvRemoveManagement.setVisibility(View.GONE);
            }
        }

        void setOwnerPermissions(Member profile) {
            switch (profile.getPrivilege()) {
                case MANAGER:
                    binding.tvTranOwner.setVisibility(View.VISIBLE);
                    binding.tvDesignate.setVisibility(View.GONE);
                    binding.tvRemoveManagement.setVisibility(View.VISIBLE);
                    binding.ivDelete.setVisibility(View.VISIBLE);
                    break;
                case COMMON:
                    binding.tvTranOwner.setVisibility(View.VISIBLE);
                    binding.tvDesignate.setVisibility(View.VISIBLE);
                    binding.ivDelete.setVisibility(View.VISIBLE);
                    binding.tvRemoveManagement.setVisibility(View.GONE);
                    break;
            }
        }

        void setManagerPermissions(Member profile) {
            if (profile.getId().equals(userId)) return;
            switch (profile.getPrivilege()) {
                case MANAGER:
                    binding.tvTranOwner.setVisibility(View.GONE);
                    binding.tvDesignate.setVisibility(View.GONE);
                    binding.tvRemoveManagement.setVisibility(View.VISIBLE);
                    binding.ivDelete.setVisibility(View.VISIBLE);
                    break;
                case COMMON:
                    binding.tvTranOwner.setVisibility(View.GONE);
                    binding.tvDesignate.setVisibility(View.VISIBLE);
                    binding.ivDelete.setVisibility(View.VISIBLE);
                    binding.tvRemoveManagement.setVisibility(View.GONE);
                    break;
            }
        }

        @SuppressLint("SetTextI18n")
        public void onBind(Member profile) {
            String name = "";
            String avatarId = "";
            UserProfileEntity user = DBManager.getInstance().queryUser(profile.getId());
            if (user != null) {
                name = user.getNickName();
                avatarId = user.getAvatarId();
            }
            binding.tvName.setText(name);
            assert user != null;
            if (!Strings.isNullOrEmpty(user.getDuty()) || !Strings.isNullOrEmpty(user.getDepartment())) {
                binding.tvMood.setText(user.getDuty() + "/" + user.getDepartment());
            } else {
                binding.tvMood.setText("");
            }

            setRightMenu(userId, profile);
            UserProfileEntity userProfileEntity = DBManager.getInstance().queryUser(profile.getId());
            if (profile.getPrivilege().equals(OWNER)) {
                binding.ivIdentity.setImageResource(R.drawable.ic_owner);
            } else if (profile.getPrivilege().equals(MANAGER)) {
                binding.ivIdentity.setImageResource(R.drawable.ic_manager);
            } else if (profile.getPrivilege().equals(COMMON)) {
                binding.ivIdentity.setImageResource(0);
            }

            binding.tvTranOwner.setOnClickListener(view -> {
                itemTouchHelperExtension.closeOpened();
                if (onManagementClick != null) {
                    onManagementClick.onTranOwnerClick(userProfileEntity);
                }
            });
            binding.tvDesignate.setOnClickListener(view -> {
                itemTouchHelperExtension.closeOpened();
                if (onManagementClick != null) {
                    onManagementClick.onDesignateClick(userProfileEntity);
                }
            });
            binding.tvRemoveManagement.setOnClickListener(view -> {
                itemTouchHelperExtension.closeOpened();
                if (onManagementClick != null) {
                    onManagementClick.onRemoveManagementClick(userProfileEntity);
                }
            });
            binding.ivDelete.setOnClickListener(view -> {
                itemTouchHelperExtension.closeOpened();
                if (onManagementClick != null) {
                    onManagementClick.onDeleteClick(userProfileEntity);
                }
            });

            binding.civIcon.loadAvatarIcon(avatarId, name, profile.getId());


            binding.clContentItem.setOnClickListener(v -> {
                itemTouchHelperExtension.closeOpened();
                if (onServiceNumberAgentListener != null) {
                    onServiceNumberAgentListener.onAvatarClick(profile);
                }
            });

            binding.civIcon.setOnClickListener(v -> {
                itemTouchHelperExtension.closeOpened();
                if (onServiceNumberAgentListener != null) {
                    onServiceNumberAgentListener.onItemClick(profile);
                }
            });
        }
    }
}

