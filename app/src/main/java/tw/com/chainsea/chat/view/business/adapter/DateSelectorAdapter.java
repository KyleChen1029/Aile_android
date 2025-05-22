package tw.com.chainsea.chat.view.business.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import tw.com.chainsea.android.common.datetime.DateTimeHelper;
import tw.com.chainsea.chat.databinding.ItemDateSelectorBinding;
import tw.com.chainsea.chat.view.business.DateSelector;

/**
 * current by evan on 2020-03-26
 *
 * @author Evan Wang
 * @date 2020-03-26
 */
public class DateSelectorAdapter extends RecyclerView.Adapter<DateSelectorAdapter.DateSelectorViewHolder> {
    private static final String TAG = DateSelectorAdapter.class.getSimpleName();

    private List<DateSelector> list = Lists.newArrayList();

    private OnDateSelectorListener onDateSelectorListener;

    @Override
    public int getItemCount() {
        return this.list.size();
    }

    @NonNull
    @Override
    public DateSelectorViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        ItemDateSelectorBinding binding = ItemDateSelectorBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new DateSelectorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DateSelectorViewHolder holder, int position) {
        DateSelector selector = this.list.get(position);
        holder.binding.tvSelectName.setText(selector.getNameResId());
        holder.itemView.setOnClickListener(v -> {
            if (onDateSelectorListener != null) {
                if (DateSelector.CUSTOMIZE.equals(selector)) {
                    onDateSelectorListener.onCustomize();
                    return;
                }

                Date date = DateTimeHelper.getDateByPlusDay(selector.getInterval(), Locale.TAIWAN);
                String dateName = DateTimeHelper.getDateByPlusDay(selector.getInterval(), Locale.TAIWAN, "yyyy-MM-dd");
                if (selector.isDate()) {
                    onDateSelectorListener.onDateSelect(selector, date.getTime(), dateName);
                } else {
                    onDateSelectorListener.onClear(selector, date.getTime(), "");
                }
                switchTarget(selector);
            }
        });
    }


    private void switchTarget(DateSelector selector) {
//        for (DateSelector s : this.list) {
//            s.setSelected(false);
//        }
//        selector.setSelected(true);
//        notifyDataSetChanged();
    }

    public DateSelectorAdapter setList(List<DateSelector> list) {
        this.list = list;
        return this;
    }

    public DateSelectorAdapter setOnDateSelectorListener(OnDateSelectorListener onDateSelectorListener) {
        this.onDateSelectorListener = onDateSelectorListener;
        return this;
    }

    static class DateSelectorViewHolder extends RecyclerView.ViewHolder {

        private ItemDateSelectorBinding binding;

        public DateSelectorViewHolder(@NonNull ItemDateSelectorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnDateSelectorListener {

        void onDateSelect(DateSelector selector, long timeMillis, String date);

        void onCustomize();

        void onClear(DateSelector selector, long timeMillis, String date);
    }

}
