package tw.com.chainsea.chat.view.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import tw.com.chainsea.ce.sdk.bean.tenant.EnvironmentInfo;
import tw.com.chainsea.ce.sdk.database.sp.TenantPref;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.chat.databinding.ItemTenantSelectorBinding;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemBaseViewHolder;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemNoSwipeViewHolder;

/**
 * current by evan on 12/17/20
 *
 * @author Evan Wang
 * date 12/17/20
 * item_tenant_selector
 * bg_item_tenant_selector
 */
public class TenantSelectorAdapter extends RecyclerView.Adapter<ItemBaseViewHolder<EnvironmentInfo>> {
    private final WeakReference<Context> weakReference;
    private OnTenantSelectorListener<EnvironmentInfo> onTenantSelectorListener;
    private final List<EnvironmentInfo> environmentInfos = Lists.newArrayList();
    private boolean deleteActionable = false;

    public TenantSelectorAdapter(Context context) {
        this.weakReference = new WeakReference<Context>(context);
    }

    @NonNull
    @Override
    public ItemBaseViewHolder<EnvironmentInfo> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTenantSelectorBinding binding = ItemTenantSelectorBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TenantSelectorViewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemBaseViewHolder<EnvironmentInfo> holder, int position) {
        EnvironmentInfo info = environmentInfos.get(position);
        holder.onBind(info, 0, position);
    }

    @Override
    public int getItemCount() {
        return environmentInfos.size();
    }

    public TenantSelectorAdapter setOnTenantSelectorListener(OnTenantSelectorListener<EnvironmentInfo> onTenantSelectorListener) {
        this.onTenantSelectorListener = onTenantSelectorListener;
        return this;
    }


    public TenantSelectorAdapter setDeleteActionable(boolean deleteActionable) {
        this.deleteActionable = deleteActionable;
        return this;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh(boolean isDeveloper) {
        if (this.weakReference.get() != null) {
            List<EnvironmentInfo> list = !isDeveloper ? TokenPref.getInstance(this.weakReference.get()).getEnvironmentList() : TokenPref.getInstance(weakReference.get()).getDeveloperEnvironmentList();
            sort(list);
            environmentInfos.clear();
            environmentInfos.addAll(list);
            notifyDataSetChanged();
        }
    }

    private void sort(List<EnvironmentInfo> list) {
        Collections.sort(list);
    }

    class TenantSelectorViewViewHolder extends ItemNoSwipeViewHolder<EnvironmentInfo> {

        private ItemTenantSelectorBinding binding;

        public TenantSelectorViewViewHolder(ItemTenantSelectorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        @SuppressLint("NotifyDataSetChanged")
        public void onBind(EnvironmentInfo info, int section, int position) {
            super.onBind(info, section, position);
            if (!binding.tvTenantName.getText().equals(info.getName())) {
                binding.tvTenantName.setText(info.getName());
            }

            binding.ivClose.setVisibility(View.GONE);
            binding.ivClose.setOnClickListener(null);

            itemView.setOnClickListener(v -> {
                if (onTenantSelectorListener != null) {
                    onTenantSelectorListener.onSelected(info);
                }
            });

            if (deleteActionable) {
                binding.ivClose.setVisibility(View.VISIBLE);
                binding.ivClose.setOnClickListener(v -> {
                    TenantPref.getClosed(weakReference.get(), info.getId()).delete();
                    TokenPref.getInstance(weakReference.get()).removeRecordEnvironment(info.getId());
                    TenantPref.clearSharedPreferences(weakReference.get(), info.getId());
                    environmentInfos.remove(info);
                    notifyDataSetChanged();
                    if (onTenantSelectorListener != null && environmentInfos.isEmpty()) {
                        onTenantSelectorListener.onClose();

                    }
                });
            }
        }
    }

    public interface OnTenantSelectorListener<T> {
        void onSelected(T t);

        void onClose();
    }
}
