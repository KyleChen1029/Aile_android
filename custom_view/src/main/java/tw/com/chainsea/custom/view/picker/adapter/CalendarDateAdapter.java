package tw.com.chainsea.custom.view.picker.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import tw.com.chainsea.custom.view.R;
import tw.com.chainsea.custom.view.picker.adapter.listener.OnCalendarListener;
import tw.com.chainsea.custom.view.picker.bean.DateData;

/**
 * current by evan on 2020-07-06
 *
 * @author Evan Wang
 * @date 2020-07-06
 */
public class CalendarDateAdapter extends RecyclerView.Adapter<CalendarDateAdapter.DateViewHolder> {
    private final List<DateData> dates;
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat simpleDate = new SimpleDateFormat("dd");
    Calendar selectCal;
    Calendar currentCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"), Locale.TAIWAN);
    OnCalendarListener onCalendarListener;

    CalendarDateAdapter(Calendar currentCal, Calendar selectCal, OnCalendarListener onCalendarListener, List<DateData> dates) {
        this.currentCal = currentCal;
        this.selectCal = selectCal;
        this.onCalendarListener = onCalendarListener;
        this.dates = dates;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DateViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_date, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        DateData dateData = this.dates.get(position);
        holder.onBind(dateData);
    }

    @Override
    public int getItemCount() {
        return this.dates.size();
    }

    class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tvDate = itemView.findViewById(R.id.tv_date);
        }

        public void onBind(DateData dateData) {
            if (dateData.isLastMonth() || dateData.isNextMonth()) {
                this.tvDate.setTextColor(Color.LTGRAY);
            } else {
                this.tvDate.setTextColor(Color.BLACK);
            }
            this.tvDate.setText(simpleDate.format(dateData.getCal().getTime()));

            if (currentCal.compareTo(dateData.getCal()) == 0) {
                this.tvDate.setTextColor(Color.RED);
            }

            if (selectCal != null && selectCal.compareTo(dateData.getCal()) == 0) {
                this.tvDate.setTextColor(Color.WHITE);
                this.tvDate.setBackgroundColor(Color.RED);
            }
            this.itemView.setOnClickListener(v -> onCalendarListener.onDateSelect(dateData));
        }
    }
}
