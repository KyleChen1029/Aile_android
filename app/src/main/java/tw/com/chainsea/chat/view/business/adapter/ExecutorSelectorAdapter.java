package tw.com.chainsea.chat.view.business.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemExecutorSelectorBinding;

/**
 * current by evan on 2020/5/12
 *
 * @author Evan Wang
 * @date 2020/5/12
 */
public class ExecutorSelectorAdapter extends RecyclerView.Adapter<ExecutorSelectorAdapter.ExecutorSelectorViewHolder> {
    private List<UserProfileEntity> list = Lists.newArrayList();
    private OnExecutorSelectorListener onExecutorSelectorListener;
    private Set<String> selectorSet = Sets.newHashSet();
    private String selfId = "";
    private Context context;

    @NonNull
    @Override
    public ExecutorSelectorViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        this.context = viewGroup.getContext();
        ItemExecutorSelectorBinding binding = ItemExecutorSelectorBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ExecutorSelectorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExecutorSelectorViewHolder holder, int position) {
        UserProfileEntity profile = list.get(position);
        holder.binding.tvSelectName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        if (selfId.equals(profile.getId())) {
            holder.binding.tvSelectName.setText("我");
        } else if (profile.isHardCode()) {
            holder.binding.tvSelectName.setText("其它");
            Drawable drawable = AppCompatResources.getDrawable(context, R.drawable.ic_search_icon_blue);
            holder.binding.tvSelectName.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            holder.binding.tvSelectName.setCompoundDrawablePadding(2);
        } else {
            holder.binding.tvSelectName.setText(profile.getNickName());
        }

        holder.binding.tvSelectName.setSelected(this.selectorSet.contains(profile.getId()));

        holder.itemView.setOnClickListener(v -> {
            if (onExecutorSelectorListener != null) {
                if (selfId.equals(profile.getId())) {
                    onExecutorSelectorListener.onMeClick(profile);
                } else if (profile.isHardCode()) {
                    onExecutorSelectorListener.onOtherClick(profile);
                } else {
                    onExecutorSelectorListener.onItemClick(profile);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public ExecutorSelectorAdapter setSelf(UserProfileEntity self) {
        this.selfId = self.getId();
        this.list.add(0, self);

        return this;
    }

    public ExecutorSelectorAdapter setData(Set<UserProfileEntity> profiles) {
        this.list.addAll(profiles);
        return this;
    }

    public ExecutorSelectorAdapter setOther(UserProfileEntity other) {
        this.list.add(this.list.size(), other);
        return this;
    }

    public ExecutorSelectorAdapter setOnExecutorSelectorListener(OnExecutorSelectorListener onExecutorSelectorListener) {
        this.onExecutorSelectorListener = onExecutorSelectorListener;
        return this;
    }

    public ExecutorSelectorAdapter selectorId(String selectorId) {
        this.selectorSet.clear();
        this.selectorSet.add(selectorId);
        return this;
    }

    static class ExecutorSelectorViewHolder extends RecyclerView.ViewHolder {
        private ItemExecutorSelectorBinding binding;

        public ExecutorSelectorViewHolder(@NonNull ItemExecutorSelectorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    public interface OnExecutorSelectorListener {

        void onMeClick(UserProfileEntity self);

        void onItemClick(UserProfileEntity executor);

        void onOtherClick(UserProfileEntity other);
    }
}
