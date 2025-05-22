package tw.com.chainsea.chat.view.service.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

import tw.com.chainsea.android.common.text.KeyWordHelper;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemInvitedFriendBinding;
import tw.com.chainsea.chat.databinding.ItemServiceNumberConsultBinding;

/**
 * Create by evan on 1/22/21
 *
 * @author Evan Wang
 * @date 1/22/21
 */

//.serviceNumberId(e.getServiceNumberId())
//.serviceNumberAvatarId(e.getAvatarId())
//.serviceNumberName(e.getName())
public class ServiceNumberConsultListAdapter<T extends ServiceNumberEntity> extends RecyclerView.Adapter<ServiceNumberConsultListAdapter.ConsultViewHolder<T>> {
    private Context context;
    private List<T> matadata = Lists.newArrayList();
    private List<T> entities = Lists.newArrayList();
    private String keyword = "";
    private Type type;

    private OnSelectListener<T> onSelectListener;
    private Set<String> selectedIdSet = Sets.newHashSet();

    public enum Type {
        ROW,
        GRID
    }

    public ServiceNumberConsultListAdapter(Type type) {
        this.type = type;
    }

    @NonNull
    @Override
    public ConsultViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (this.context == null) {
            this.context = parent.getContext();
        }
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == 0) {
            ItemServiceNumberConsultBinding itemServiceNumberConsultBinding = ItemServiceNumberConsultBinding.inflate(layoutInflater, parent, false);
            return new RowConsultViewHolder(itemServiceNumberConsultBinding);
        } else {
            ItemInvitedFriendBinding itemInvitedFriendBinding = ItemInvitedFriendBinding.inflate(layoutInflater, parent, false);
            return new GridConsultViewHolder(itemInvitedFriendBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ConsultViewHolder<T> holder, int position) {
        T t = this.entities.get(position);
        holder.onBind(t, 0, position);
    }


    @Override
    public int getItemViewType(int position) {
        if (Type.ROW.equals(this.type)) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return this.entities.size();
    }


    public ServiceNumberConsultListAdapter<T> setOnSelectListener(OnSelectListener<T> onSelectListener) {
        this.onSelectListener = onSelectListener;
        return this;
    }

    public ServiceNumberConsultListAdapter<T> setData(List<T> list) {
        this.matadata = list;
        return this;
    }

    public ServiceNumberConsultListAdapter<T> removeData(T t) {
        this.matadata.remove(t);
        return this;
    }

    public ServiceNumberConsultListAdapter<T> setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public ServiceNumberConsultListAdapter<T> appendSelectedIds(Set<String> ids) {
        this.selectedIdSet.clear();
        this.selectedIdSet.addAll(ids);
        return this;
    }

    public ServiceNumberConsultListAdapter<T> removeSelected() {
        this.selectedIdSet.clear();
        return this;
    }

    private void sort() {

    }

    private void filter() {
        this.entities.clear();
        if (Strings.isNullOrEmpty(this.keyword)) {
            this.entities.addAll(Lists.newArrayList(this.matadata));
        } else {
            for (T t : this.matadata) {
                if (t.getName().toLowerCase().contains(this.keyword.toLowerCase())) {
                    this.entities.add(t);
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshData() {
        sort();
        filter();
        notifyDataSetChanged();
    }

    private Context getContext() {
        return this.context;
    }


    public static abstract class ConsultViewHolder<T> extends RecyclerView.ViewHolder {
        public ConsultViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void onBind(T t, int section, int position);
    }

    class GridConsultViewHolder extends ConsultViewHolder<T> {

        private ItemInvitedFriendBinding binding;

        public GridConsultViewHolder(ItemInvitedFriendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(T t, int section, int position) {
            AvatarService.post(getContext(), t.getAvatarId(), PicSize.SMALL, binding.ivAvatar, R.drawable.custom_default_avatar);
            binding.memberName.setText(t.getName());
            itemView.setOnClickListener(v -> {
                if (onSelectListener != null) {
                    onSelectListener.onDeselect(t, position);
                }
            });
            binding.deleteIcon.setOnClickListener(v -> {
                if (onSelectListener != null) {
                    onSelectListener.onDeselect(t, position);
                }
            });
        }
    }

    class RowConsultViewHolder extends ConsultViewHolder<T> {

        private ItemServiceNumberConsultBinding binding;

        public RowConsultViewHolder(ItemServiceNumberConsultBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.tvTime.setVisibility(View.GONE);
        }

        @Override
        public void onBind(T t, int section, int position) {
            if (selectedIdSet.contains(t.getRoomId())) {
                itemView.setBackgroundResource(R.drawable.selector_item_list_top);
            } else {
                itemView.setBackgroundResource(R.drawable.selector_item_list);
            }
            AvatarService.post(getContext(), t.getAvatarId(), PicSize.SMALL, binding.civIcon, R.drawable.custom_default_avatar);
            SpannableString sp = KeyWordHelper.highlightKeywords(0xFF4A90E2, t.getName(), keyword);
            binding.tvTitle.setText(sp);
            itemView.setOnClickListener(v -> {
                if (onSelectListener != null) {
                    onSelectListener.onSelect(t, position);
                }
            });
            binding.tvContent.setText(t.getDescription());
//            CELog.e(t.toString());
        }
    }


    public interface OnSelectListener<T> {
        void onSelect(T t, int position);

        void onDeselect(T t, int position);
    }
}
