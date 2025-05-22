package tw.com.chainsea.chat.view.roomAction.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.ServiceNumberPrivilege;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.http.ce.model.Member;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemInvitedFriendBinding;

/**
 * current by evan on 2019-12-16
 */
public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
    private List<Member> userProfiles = Lists.newArrayList();
    private Context ctx;
    private boolean isManager = false;
    private String selfId = "";

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        this.ctx = viewGroup.getContext();
        ItemInvitedFriendBinding binding = ItemInvitedFriendBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        Member profile = this.userProfiles.get(i);
        UserProfileEntity users = DBManager.getInstance().queryUser(profile.getId());
        String name = !Strings.isNullOrEmpty(users.getNickName()) ? users.getNickName() : users.getLoginName();
        holder.binding.memberName.setText(name);
        String avatarId = users.getAvatarId();
        holder.binding.ivAvatar.loadAvatarIcon(avatarId, name, users.getId());


        if (ServiceNumberPrivilege.OWNER.equals(profile.getPrivilege())) {
            holder.binding.crownIcon.setVisibility(View.VISIBLE);
            holder.binding.crownIcon.setImageResource(R.drawable.ic_owner);
        } else if (ServiceNumberPrivilege.MANAGER.equals(profile.getPrivilege())) {
            holder.binding.crownIcon.setVisibility(View.VISIBLE);
            holder.binding.crownIcon.setImageResource(R.drawable.ic_manager);
        } else if (ServiceNumberPrivilege.COMMON.equals(profile.getPrivilege())) {
            holder.binding.crownIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return this.userProfiles.size();
    }


    public MemberAdapter setManager(boolean isManager) {
        this.isManager = isManager;
        return this;
    }

    public MemberAdapter setSelfId(String selfId) {
        this.selfId = selfId;
        return this;
    }

    public MemberAdapter setData(List<Member> list) {
        list.sort((memberEntity, memberEntity1) -> {
            if (Objects.equals(memberEntity.getPrivilege(), ServiceNumberPrivilege.OWNER) ^ Objects.equals(memberEntity1.getPrivilege(), ServiceNumberPrivilege.OWNER)) {
                return Objects.equals(memberEntity.getPrivilege(), ServiceNumberPrivilege.OWNER) ? -1 : 1;
            } else if (Objects.equals(memberEntity.getPrivilege(), ServiceNumberPrivilege.MANAGER) ^
                Objects.equals(memberEntity.getPrivilege(), ServiceNumberPrivilege.MANAGER)) {
                return Objects.equals(memberEntity.getPrivilege(), ServiceNumberPrivilege.MANAGER) ? -1 : 1;
            }
            return 0;
        });
        this.userProfiles = list;
        return this;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ItemInvitedFriendBinding binding;

        public ViewHolder(@NonNull ItemInvitedFriendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.deleteIcon.setVisibility(View.GONE);
        }
    }
}
