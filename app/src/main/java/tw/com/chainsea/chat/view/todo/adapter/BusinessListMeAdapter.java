package tw.com.chainsea.chat.view.todo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import tw.com.chainsea.android.common.text.KeyWordHelper;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.business.BusinessCode;
import tw.com.chainsea.ce.sdk.bean.business.BusinessEntity;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemBussinessDetailBinding;
import tw.com.chainsea.chat.view.todo.TodoOverviewType;
import tw.com.chainsea.custom.view.recyclerview.AnimationAdapter;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemBaseViewHolder;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemNoSwipeViewHolder;

/**
 * current by evan on 2020-07-13
 *
 * @author Evan Wang
 * @date 2020-07-13
 */
public class BusinessListMeAdapter extends AnimationAdapter<ItemBaseViewHolder<BusinessEntity>> {
    private TodoOverviewType type;
    private List<BusinessEntity> metadata = Lists.newArrayList();
    private List<BusinessEntity> entities = Lists.newArrayList();

    private OnBusinessListMeItemClockListener<BusinessEntity> onBusinessListMeItemClockListener;

    private Context context;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private String keyword = "";

    @Override
    public int getItemCount() {
        return this.entities.isEmpty() ? 1 : this.entities.size();
    }

    @Override
    public void executeAnimatorEnd(int position) {

    }

    @NonNull
    @Override
    public ItemBaseViewHolder<BusinessEntity> onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        this.context = parent.getContext();
        if (type == 1) {
            ItemBussinessDetailBinding binding = ItemBussinessDetailBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new BusinessItemViewHolder(binding);
        } else {
            return new NoDataItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ItemBaseViewHolder<BusinessEntity> holder, int position) {
        if (!this.entities.isEmpty()) {
            BusinessEntity entity = this.entities.get(position);
            holder.onBind(entity, 0, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (this.entities.isEmpty()) {
            return 0;
        } else {
            return 1;
        }
    }

    public int getDataCount() {
        return this.entities.size();
    }


    public int getExpiredCount(long now) {
        int count = 0;
        for (BusinessEntity b : this.entities) {
            if (b.getEndTimestamp() > 0 && now > b.getEndTimestamp()) {
                count++;
            }
//            else if (!Strings.isNullOrEmpty(b.getEndTime())) {
//                boolean parseSuccess = false;
//                try {
//                    long endTime = DateTimeHelper.parseToMillis(b.getEndTime(), "yyyy-MM-dd");
//                    if (now > endTime) {
//                        count++;
//                    }
//                    parseSuccess = true;
//                } catch (Exception e) {
//
//                }
//                if (!parseSuccess) {
//                    try {
//                        long endTime = DateTimeHelper.parseToMillis(b.getEndTime(), "yyyy/MM/dd");
//                        if (now > endTime) {
//                            count++;
//                        }
//                    } catch (Exception e) {
//                    }
//                }
//            }
        }
        return count;
    }


    public BusinessListMeAdapter setType(TodoOverviewType type) {
        this.type = type;
        return this;
    }

    public BusinessListMeAdapter setData(List<BusinessEntity> metadata) {
        Iterator<BusinessEntity> iterator = this.metadata.iterator();
        while (iterator.hasNext()) {
            if (metadata.contains(iterator.next())) {
                iterator.remove();
            }
        }
        this.metadata.addAll(metadata);
        return this;
    }

    public BusinessListMeAdapter setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public BusinessListMeAdapter setOnBusinessListMeItemClockListener(OnBusinessListMeItemClockListener<BusinessEntity> onBusinessListMeItemClockListener) {
        this.onBusinessListMeItemClockListener = onBusinessListMeItemClockListener;
        return this;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshData() {
        filter();
        sort();
        notifyDataSetChanged();
    }

    public void filter() {
        String userId = TokenPref.getInstance(context).getUserId();
        List<BusinessEntity> list = Lists.newArrayList();
        Iterator<BusinessEntity> iterator = this.metadata.iterator();
        while (iterator.hasNext()) {
            BusinessEntity content = iterator.next();
            String name = content.getName();
            String executorName = content.getExecutorName();
            String managerName = content.getManagerName();
            switch (type) {
                case BUSINESS_EXECUTOR:
                    if (userId.equals(content.getExecutorId())) {
                        if (Strings.isNullOrEmpty(this.keyword)) {
                            list.add(content);
                        } else {
                            if (name.toLowerCase().contains(this.keyword.toLowerCase()) || managerName.toLowerCase().contains(this.keyword.toLowerCase())) {
                                list.add(content);
                            }
                        }
                    }
                    break;
                case BUSINESS_MANAGER:
                    if (userId.equals(content.getManagerId())) {
                        if (Strings.isNullOrEmpty(this.keyword)) {
                            list.add(content);
                        } else {
                            if (name.toLowerCase().contains(this.keyword.toLowerCase()) || executorName.toLowerCase().contains(this.keyword.toLowerCase())) {
                                list.add(content);
                            }
                        }
                    }
                    break;
                case BUSINESS_COOPERATE:
                    if (!userId.equals(content.getExecutorId()) && !userId.equals(content.getManagerId())) {
                        if (Strings.isNullOrEmpty(this.keyword)) {
                            list.add(content);
                        } else {
                            if (name.toLowerCase().contains(this.keyword.toLowerCase()) || executorName.toLowerCase().contains(this.keyword.toLowerCase()) || managerName.toLowerCase().contains(this.keyword.toLowerCase())) {
                                list.add(content);
                            }
                        }
                    }
                    break;
                case SCHEDULE_LIST:
            }
        }

        this.entities.clear();
        this.entities.addAll(list);
    }

    private void sort() {
        Collections.sort(this.entities);
    }

    class NoDataItemViewHolder extends ItemNoSwipeViewHolder<BusinessEntity> {

        public NoDataItemViewHolder(View itemView) {
            super(itemView);
            TextView tv = itemView.findViewById(android.R.id.text1);
            tv.setText("查詢無結果");
        }
    }

    class BusinessItemViewHolder extends ItemNoSwipeViewHolder<BusinessEntity> {

        private ItemBussinessDetailBinding binding;

        public BusinessItemViewHolder(ItemBussinessDetailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(BusinessEntity entity, int section, int position) {
            if (entity.getCode() == null) {
                entity.setCode(BusinessCode.TASK);
            }

            AvatarService.post(itemView.getContext(), entity.getExecutorAvatarId(), PicSize.SMALL, binding.civIcon, R.drawable.custom_default_avatar);

            binding.tvBusinessName.setText(KeyWordHelper.matcherSearchTitle(0xFF4A90E2, entity.getName(), keyword));
//            tvTime.setText(simpleDateFormat.format(entity.getEndTimestamp()));

//            if (entity.getWeights() < 0) {
//                tvTime.setTextColor(Color.RED);
//            } else {
//                tvTime.setTextColor(Color.BLACK);
//            }
            binding.tvTime.setText(entity.getEndTimestamp() > 0 ? simpleDateFormat.format(entity.getEndTimestamp()) : !Strings.isNullOrEmpty(entity.getEndTime()) ? entity.getEndTime() : "");
            binding.tvCode.setText(entity.getCode().getName());
            binding.tvBusinessContent.setText(Strings.isNullOrEmpty(entity.getPrimaryName()) ? "" : entity.getPrimaryName());
            binding.tvDebugSort.setVisibility(View.GONE);

            switch (type) {
                case BUSINESS_EXECUTOR:
                    binding.tvManagerName.setText(entity.getManagerName());
                    binding.tvExecutorName.setText(KeyWordHelper.matcherSearchTitle(0xFF4A90E2, entity.getExecutorName(), keyword));
                    break;
                case BUSINESS_MANAGER:
                    binding.tvManagerName.setText(KeyWordHelper.matcherSearchTitle(0xFF4A90E2, entity.getManagerName(), keyword));
                    binding.tvExecutorName.setText(entity.getExecutorName());
                    break;
                case BUSINESS_COOPERATE:
                    binding.tvManagerName.setText(KeyWordHelper.matcherSearchTitle(0xFF4A90E2, entity.getManagerName(), keyword));
                    binding.tvExecutorName.setText(KeyWordHelper.matcherSearchTitle(0xFF4A90E2, entity.getExecutorName(), keyword));
                    break;
            }

            itemView.setOnClickListener(v -> {
                if (onBusinessListMeItemClockListener != null) {
                    onBusinessListMeItemClockListener.onItemSelect(type, entity, position);
                }
                refreshData();
            });
        }
    }

    public interface OnBusinessListMeItemClockListener<T> {
        void onItemSelect(TodoOverviewType type, T t, int position);
    }

}
