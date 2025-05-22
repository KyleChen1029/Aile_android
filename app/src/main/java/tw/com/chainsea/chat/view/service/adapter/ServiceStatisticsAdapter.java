package tw.com.chainsea.chat.view.service.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import tw.com.chainsea.ce.sdk.bean.statistics.ServiceNumberStatType;
import tw.com.chainsea.ce.sdk.bean.statistics.StatisticsEntity;
import tw.com.chainsea.chat.databinding.ItemServiceStatisticsBinding;

/**
 * current by evan on 2020-04-17
 *
 * @author Evan Wang
 * @date 2020-04-17
 */
public class ServiceStatisticsAdapter extends RecyclerView.Adapter<ServiceStatisticsAdapter.SummaryViewHolder> {

    List<StatisticsEntity> matadata = Lists.newArrayList();
    List<StatisticsEntity> entities = Lists.newArrayList();

    Set<String> filterSet = ServiceNumberStatType.STAT_ME_BY_MANE;

    OnServiceStatisticsListener<StatisticsEntity> onServiceStatisticsListener;
    Set<String> progressSet = Sets.newHashSet();

    @NonNull
    @Override
    public SummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        ItemServiceStatisticsBinding binding = ItemServiceStatisticsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        SummaryViewHolder viewHolder = new SummaryViewHolder(binding);
        viewHolder.setIsRecyclable(false);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryViewHolder holder, int position) {
        StatisticsEntity stat = entities.get(position);
        holder.onBind(stat);
    }

    @Override
    public int getItemCount() {
        return entities.size();
    }


    public ServiceStatisticsAdapter setData(List<StatisticsEntity> list) {
        for (StatisticsEntity stat : list) {
            if (this.matadata.contains(stat)) {
                this.matadata.remove(stat);
            }
            this.matadata.add(stat);
        }
        return this;
    }

    public ServiceStatisticsAdapter removeAddData() {
        this.matadata.clear();
        return this;
    }

    public StatisticsEntity getData(int position) {
        try {
            return this.entities.get(position);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshData() {
        sort();
        filter();
        notifyDataSetChanged();
    }

    private void sort() {
        Collections.sort(this.matadata);
    }

    private void filter() {
        this.entities.clear();
        for (StatisticsEntity entity : this.matadata) {
            if (this.filterSet.contains(entity.getStatType())) {
                this.entities.add(entity);
            }
        }
    }

    public ServiceStatisticsAdapter switchFilter() {
        if (this.filterSet.contains(ServiceNumberStatType.LAST_DAY.name())) {
            this.filterSet = ServiceNumberStatType.STAT_ME_BY_MANE;
        } else {
            this.filterSet = ServiceNumberStatType.STAT_ALL_BY_MANE;
        }
        return this;
    }

    public ServiceStatisticsAdapter setOnServiceStatisticsListener(OnServiceStatisticsListener<StatisticsEntity> onServiceStatisticsListener) {
        this.onServiceStatisticsListener = onServiceStatisticsListener;
        return this;
    }

    public void handleCurrentPosition(int position) {
        try {
            this.onServiceStatisticsListener.doLastAction(this.entities.get(position - 1));
        } catch (IndexOutOfBoundsException e) {
            this.onServiceStatisticsListener.doLastAction(null);
        }

        try {
            this.onServiceStatisticsListener.doNextAction(this.entities.get(position + 1));
        } catch (IndexOutOfBoundsException e) {
            this.onServiceStatisticsListener.doNextAction(null);
        }
    }

    static class SummaryViewHolder extends RecyclerView.ViewHolder {

        private ItemServiceStatisticsBinding binding;

        public SummaryViewHolder(@NonNull ItemServiceStatisticsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void onBind(StatisticsEntity stat) {
            ServiceNumberStatType type = ServiceNumberStatType.valueOf(stat.getStatType());

            binding.bgaProgress.setMax(0);
            binding.bgaLProgress.setMax(0);
            binding.bgaProgress.setProgress(0);
            binding.bgaLProgress.setProgress(0);

            if (ServiceNumberStatType.UNDEF.equals(type)) {
                binding.tvDay.setText("");
                binding.tvPercentage.setTextColor(type.getTextColor());
                binding.tvPercentage.setText(type.getName());
                binding.tvLeft.setText(type.getCategoryLeft());
                binding.tvRight.setText(type.getCategoryRight());
            } else {
                binding.tvDay.setText(type.getName());
//            int totalRow = stat.getInternalSubscribeCount() + stat.getExternalSubscribeCount();
                binding.bgaProgress.setMax(stat.getTotalRow());
                binding.bgaLProgress.setMax(stat.getTotalRow());
                binding.bgaProgress.setCometColors(type.getReachedColor(), type.getUnReachedColor());

                binding.tvLeft.setText(type.getCategoryLeft());
                binding.tvRight.setText(type.getCategoryRight());
                binding.tvPercentage.setTextColor(type.getTextColor());
                // 執行動畫

//                if (!progressSet.contains(stat.uniqueId())) {
//                    handlerProgress(stat.uniqueId(), stat.getScore(), 0, stat.getTotalRow());
//                } else {
//                    bgaProgress.setProgress(stat.getScore());
//                    bgaLProgress.setProgress(stat.getScore());
//                    tvPercentage.setText(String.format("%s/" + stat.getTotalRow(), stat.getScore()));
//                }
                handlerProgress(stat.uniqueId(), stat.getRowCount(), 0, stat.getTotalRow());
            }
        }


        public void handlerProgress(String code, int rowCount, int progress, int totalRow) {
            itemView.postDelayed(() -> {
                binding.bgaProgress.setProgress(progress);
                binding.bgaLProgress.setProgress(progress);
                binding.tvPercentage.setText(String.format("%s/" + totalRow, progress));
                if (progress < rowCount) {
                    handlerProgress(code, rowCount, progress + 1, totalRow);
                }
            }, rowCount > 0 ? 1000L / rowCount : 0);
        }
    }


    public interface OnServiceStatisticsListener<T> {

        void doLastAction(T t);

        void doNextAction(T t);
    }
}
