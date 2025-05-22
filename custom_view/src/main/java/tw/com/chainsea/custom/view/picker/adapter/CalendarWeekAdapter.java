package tw.com.chainsea.custom.view.picker.adapter;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tw.com.chainsea.custom.view.R;
import tw.com.chainsea.custom.view.picker.bean.WeekData;

/**
 * current by evan on 2020-07-06
 *
 * @author Evan Wang
 * @date 2020-07-06
 */
public class CalendarWeekAdapter extends RecyclerView.Adapter<CalendarWeekAdapter.WeekViewHolder> {
    private final List<WeekData> dates;

    public CalendarWeekAdapter(List<WeekData> dates) {
        this.dates = dates;
    }

    @NonNull
    @Override
    public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WeekViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_date, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull WeekViewHolder holder, int position) {
        WeekData dateData = this.dates.get(position);
        holder.onBind(dateData);
    }

    @Override
    public int getItemCount() {
        return this.dates.size();
    }

    static class WeekViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;

        public WeekViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tvDate = itemView.findViewById(R.id.tv_date);
        }

        public void onBind(WeekData dateData) {
            this.tvDate.setText(dateData.getName());
            this.tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            this.tvDate.setTextColor(dateData.getColor());
        }
    }
}
