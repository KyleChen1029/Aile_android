package tw.com.chainsea.chat.ui.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder;
import com.google.common.base.Strings;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.ServiceNumberPrivilege;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemSwGroupMemberBinding;

public class GroupMemberListAdapter extends BaseQuickAdapter<UserProfileEntity, BaseDataBindingHolder<ItemSwGroupMemberBinding>> implements Filterable {

    private NameFilter filter;
    private List<UserProfileEntity> data;
    private List<UserProfileEntity> filterData;

    public GroupMemberListAdapter(List<UserProfileEntity> entityList) {
        super(R.layout.item_sw_group_member, entityList);
        this.data = entityList;
        filterData = new ArrayList<>(entityList);
        addChildClickViewIds(R.id.cl_del_member);
    }

    @Override
    protected void convert(@NonNull BaseDataBindingHolder<ItemSwGroupMemberBinding> holder, UserProfileEntity userProfileEntity) {
        ItemSwGroupMemberBinding binding = holder.getDataBinding();
        if (binding != null) {
            binding.ivIdentity.setVisibility(View.GONE);
            String name = !Strings.isNullOrEmpty(userProfileEntity.getName()) ? userProfileEntity.getName() : userProfileEntity.getNickName();
            binding.tvMemberName.setText(name);
            if (!Strings.isNullOrEmpty(userProfileEntity.getDepartment())) {
                binding.tvMemberDuty.setText(MessageFormat.format("{0}/{1}", userProfileEntity.getDepartment(), userProfileEntity.getDuty()));
            } else {
                binding.tvMemberDuty.setText("");
            }

//            for (String id : memberIds) {
//                if (userProfileEntity.getId().equals(id)) {
//                    binding.clDelMember.setVisibility(View.GONE);
//                    binding.ivCrown.setVisibility(View.VISIBLE);
//                } else {
//                    binding.clDelMember.setVisibility(View.VISIBLE);
//                }
//            }

            if (userProfileEntity.getPrivilege() == ServiceNumberPrivilege.OWNER) {
                binding.ivIdentity.setImageResource(R.drawable.ic_owner);
                binding.ivIdentity.setVisibility(View.VISIBLE);
            } else if (userProfileEntity.getPrivilege() == ServiceNumberPrivilege.MANAGER) {
                binding.ivIdentity.setImageResource(R.drawable.ic_manager);
                binding.ivIdentity.setVisibility(View.VISIBLE);
            } else {
                binding.ivIdentity.setVisibility(View.GONE);
            }

            binding.civIcon.loadAvatarIcon(userProfileEntity.getAvatarId(), userProfileEntity.getNickName(), userProfileEntity.getId());
        }
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new NameFilter();
        }
        return filter;
    }

    private class NameFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<UserProfileEntity> results = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                results.addAll(filterData);
            } else {
                constraint = toLowerCase(constraint);
                for (UserProfileEntity entity : filterData) {
                    if ((entity.getName() != null && entity.getName().toLowerCase().contains(constraint))
                            || (entity.getNickName() != null && entity.getNickName().toLowerCase().contains(constraint))) {
                        results.add(entity);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = results;
            return filterResults;
        }

        @Override
        @SuppressLint("NotifyDataSetChanged")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            data.clear();
            data.addAll((Collection<? extends UserProfileEntity>) results.values);
            notifyDataSetChanged();
        }
    }

    public static CharSequence toLowerCase(CharSequence chars) {
        StringBuilder builder = new StringBuilder();
        char c;
        for (int i = 0; i < chars.length(); i++) {
            c = chars.charAt(i);
            if (Character.isUpperCase(c))
                c = Character.toLowerCase(c);
            builder.append(c);
        }
        return builder.toString();
    }
}
