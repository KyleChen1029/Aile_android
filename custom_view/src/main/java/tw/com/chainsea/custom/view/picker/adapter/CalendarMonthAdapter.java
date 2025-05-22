package tw.com.chainsea.custom.view.picker.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import tw.com.chainsea.custom.view.R;
import tw.com.chainsea.custom.view.picker.adapter.listener.OnCalendarListener;
import tw.com.chainsea.custom.view.picker.bean.DateData;
import tw.com.chainsea.custom.view.picker.bean.MonthData;

/**
 * current by evan on 2020-07-06
 * <p>
 * 支持左右滑動切換月份
 * 支援自動增長月曆功能
 * RecyclerView in RecyclerView 結構
 *
 * @author Evan Wang
 * @date 2020-07-06
 */
public class CalendarMonthAdapter extends RecyclerView.Adapter<CalendarMonthAdapter.MonthViewHolder> {
    private final List<MonthData> months = new ArrayList<>();
    Calendar currentCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"), Locale.TAIWAN);
    Calendar selectCal = null;
    GridLayoutManager manager;
    OnCalendarListener onCalendarListener;

    @NonNull
    @Override
    public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        manager = new GridLayoutManager(parent.getContext(), 7);
        this.currentCal.set(Calendar.HOUR_OF_DAY, 0);
        this.currentCal.set(Calendar.MINUTE, 0);
        this.currentCal.set(Calendar.SECOND, 0);
        this.currentCal.set(Calendar.MILLISECOND, 0);
        return new MonthViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_month, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
        MonthData monthData = this.months.get(position);
        holder.onAdapter(monthData.getDates());
    }

    @Override
    public int getItemCount() {
        return this.months.size();
    }


    public CalendarMonthAdapter setCurrent(Calendar currentCal) {
        currentCal.set(Calendar.HOUR_OF_DAY, 0);
        currentCal.set(Calendar.MINUTE, 0);
        currentCal.set(Calendar.SECOND, 0);
        currentCal.set(Calendar.MILLISECOND, 0);
        this.currentCal = currentCal;
        return this;
    }

    public CalendarMonthAdapter setSelect(Calendar selectCal) {
        this.selectCal = selectCal;
        return this;
    }

    public Calendar getSelect() {
        return this.selectCal;
    }

    public CalendarMonthAdapter setData(MonthData data) {
        this.months.add(data);
        return this;
    }

    public CalendarMonthAdapter setData(int index, MonthData data) {
        this.months.add(index, data);
        return this;
    }

    public List<MonthData> getData() {
        return this.months;
    }

    public MonthData first() {
        return this.months.get(0);
    }

    public MonthData last() {
        return this.months.get(this.months.size() - 1);
    }

    public void setOnCalendarListener(OnCalendarListener onCalendarListener) {
        this.onCalendarListener = onCalendarListener;
    }


    public int getCalendarIndex(Calendar cal) {
        Calendar calendar = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1, 0, 0, 0);
        int index = 0;
        for (MonthData m : this.months) {
            if (m.getCal().compareTo(calendar) == 0) {
                index = this.months.indexOf(m);
            }
        }
        return index;
//        handleCurrentPosition(index);
    }

    public void handleCurrentPosition(int position) {
        if (this.onCalendarListener != null) {
            try {
                this.onCalendarListener.doMonthChangeAction(this.months.get(position));
            } catch (IndexOutOfBoundsException ignored) {
            }

            try {
                this.onCalendarListener.doLastAction(this.months.get(position - 1));
            } catch (IndexOutOfBoundsException e) {
                this.onCalendarListener.doLastAction(null);
            }

            try {
                this.onCalendarListener.doNextAction(this.months.get(position + 1));
            } catch (IndexOutOfBoundsException e) {
                this.onCalendarListener.doNextAction(null);
            }
        }
    }


    class MonthViewHolder extends RecyclerView.ViewHolder {
        RecyclerView rvCalendarDate;

        public MonthViewHolder(@NonNull View itemView) {
            super(itemView);
            this.rvCalendarDate = itemView.findViewById(R.id.rv_calendar_date);
            this.rvCalendarDate.setLayoutManager(manager);
        }

        public void onAdapter(List<DateData> dates) {
            this.rvCalendarDate.setAdapter(new CalendarDateAdapter(currentCal, selectCal, onCalendarListener, dates));
        }
    }
}
