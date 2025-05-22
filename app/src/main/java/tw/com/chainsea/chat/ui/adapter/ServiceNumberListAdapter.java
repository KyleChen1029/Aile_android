package tw.com.chainsea.chat.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.http.ce.response.TenantServiceNumberListResponse;
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.ItemSwGroupServiceBinding;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.util.IntentUtil;
import tw.com.chainsea.chat.util.NameKit;
import tw.com.chainsea.chat.view.account.homepage.ServicesNumberManagerHomepageActivity;
import tw.com.chainsea.chat.view.homepage.BossServiceNumberHomepageActivity;

public class ServiceNumberListAdapter extends BaseQuickAdapter<TenantServiceNumberListResponse.ItemsDTO, BaseDataBindingHolder<ItemSwGroupServiceBinding>> implements Filterable {

    private NameFilter filter;
    private List<TenantServiceNumberListResponse.ItemsDTO> data;
    private List<TenantServiceNumberListResponse.ItemsDTO> filterData;

    public ServiceNumberListAdapter(List<TenantServiceNumberListResponse.ItemsDTO> entityList) {
        super(R.layout.item_sw_group_service, entityList);
        this.data = entityList;
        filterData = new ArrayList<>(entityList);
        addChildClickViewIds(R.id.cl_to_service);
    }

    @Override
    protected void convert(@NonNull BaseDataBindingHolder<ItemSwGroupServiceBinding> holder, TenantServiceNumberListResponse.ItemsDTO items) {
        ItemSwGroupServiceBinding binding = holder.getDataBinding();
        if (binding != null) {
            binding.tvServiceName.setText(items.getName());

            NameKit nameKit = new NameKit();

            binding.civIcon.setVisibility(View.INVISIBLE);
            binding.tvAvatar.setVisibility(View.VISIBLE);
            binding.tvAvatar.setText(nameKit.getAvatarName(items.getName()));
            binding.ivHome.setOnClickListener(view -> {

                ServiceNumberEntity serviceNumberEntity = ServiceNumberReference.findServiceNumberById(items.getServiceNumberId());
                if (serviceNumberEntity != null) {
                    if (ServiceNumberType.BOSS.getType().equals(serviceNumberEntity.getServiceNumberType())) {
                        Intent intent = new Intent(getContext(), BossServiceNumberHomepageActivity.class);
                        intent.putExtra(BundleKey.BROADCAST_ROOM_ID.key(), serviceNumberEntity.getBroadcastRoomId());
                        intent.putExtra(BundleKey.SERVICE_NUMBER_ID.key(), serviceNumberEntity.getServiceNumberId());
                        IntentUtil.INSTANCE.start(binding.getRoot().getContext(), intent);
                    } else if ((ServiceNumberType.MANAGER.getType().equals(serviceNumberEntity.getServiceNumberType()))) {
                        if (serviceNumberEntity.isManager() || serviceNumberEntity.isOwner()) {
                            Intent intent = new Intent(getContext(), ServicesNumberManagerHomepageActivity.class);
                            intent.putExtra(BundleKey.SERVICE_NUMBER_ID.key(), serviceNumberEntity.getServiceNumberId());
                            IntentUtil.INSTANCE.start(binding.getRoot().getContext(), intent);
                        } else {
                            ActivityTransitionsControl.navigateToServiceNumberManage(getContext(), serviceNumberEntity.getRoomId(), serviceNumberEntity.getServiceNumberId(), (intent, s) -> IntentUtil.INSTANCE.start(binding.getRoot().getContext(), intent));
                        }
                    } else {
                        ActivityTransitionsControl.navigateToServiceNumberManage(getContext(), serviceNumberEntity.getRoomId(), serviceNumberEntity.getServiceNumberId(), (intent, s) -> IntentUtil.INSTANCE.start(binding.getRoot().getContext(), intent));
                    }
                }

            });
            GradientDrawable gradientDrawable = (GradientDrawable) binding.tvAvatar.getBackground();
            gradientDrawable.setColor(Color.parseColor(nameKit.getBackgroundColor(items.getName())));

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
            List<TenantServiceNumberListResponse.ItemsDTO> results = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                results.addAll(filterData);
            } else {
                constraint = toLowerCase(constraint);
                for (TenantServiceNumberListResponse.ItemsDTO entity : filterData) {
                    if ((entity.getName() != null && entity.getName().toLowerCase().contains(constraint))) {
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
            data.addAll((Collection<? extends TenantServiceNumberListResponse.ItemsDTO>) results.values);
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
