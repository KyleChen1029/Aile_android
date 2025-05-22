package tw.com.chainsea.custom.view.picker;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.common.collect.Lists;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


import tw.com.chainsea.custom.view.R;
import tw.com.chainsea.custom.view.picker.bean.DateData;

/**
 * current by evan on 2020-07-14
 *
 * @author Evan Wang
 * @date 2020-07-14
 */
public class DateTimePickerLayout extends ConstraintLayout {
    SimpleDateFormat sdf = new SimpleDateFormat("(EEEEE) yyyy年MM月dd日 HH:mm:ss.ZZZZ", Locale.TAIWAN);
    private static final String TAG = DateTimePickerLayout.class.getSimpleName();

    TextView tvCancel;
    TextView tvConfirm;
    WheelPicker dPicker;
    WheelPicker hPicker;
    WheelPicker mPicker;

    // 當前時間
    Calendar currentCal;

    OnDateTimePickerListener onDateTimePickerListener;

    boolean needRespond = true;


    public DateTimePickerLayout(Context context) {
        super(context);
        init(context);
    }

    public DateTimePickerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DateTimePickerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = inflater.inflate(R.layout.layout_date_time_picker, this);
        tvCancel = root.findViewById(R.id.tv_pick_cancel);
        tvConfirm = root.findViewById(R.id.tv_pick_confirm);
        dPicker = root.findViewById(R.id.d_picker);
        hPicker = root.findViewById(R.id.h_picker);
        mPicker = root.findViewById(R.id.m_picker);
//        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"), Locale.TAIWAN);
//        this.currentCal = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), 0);
////        this.currentCal = cal;
//        int h = cal.get(Calendar.HOUR_OF_DAY);
//        int m = cal.get(Calendar.MINUTE);
//        List<DateData> dates = data(cal, 2, "(EEEEE) MM月dd日");
//        dPicker.setData(dates);
//
//        hPicker.setSelectedItemPosition(h, false);
//        mPicker.setSelectedItemPosition(m, false);

        reset();
        listener();
    }


    public Calendar getCurrentCal(){
        return this.currentCal;
    }

    private void listener() {

        tvCancel.setOnClickListener(v -> setVisibility(GONE));
        tvConfirm.setOnClickListener(v -> {
            if (this.onDateTimePickerListener != null && this.needRespond) {
                this.onDateTimePickerListener.onChange(this.currentCal, sdf.format(this.currentCal.getTime()), this.currentCal.getTime().getTime());
            }
            setVisibility(GONE);
        });

        dPicker.setOnItemSelectedListener((picker, data, position) -> {
            if (data instanceof DateData) {
                DateData dateData = (DateData) data;
                currentCal.set(dateData.getCal().get(Calendar.YEAR), dateData.getCal().get(Calendar.MONTH), dateData.getCal().get(Calendar.DATE));
                Log.e(TAG, sdf.format(currentCal.getTime()));
                if (tvCancel.getVisibility() == View.VISIBLE) {

                } else {
                    callback();
                }
            }
        });

        hPicker.setOnItemSelectedListener((picker, data, position) -> {
            if (data instanceof String) {
                String str = data.toString();
                Log.e(TAG, data.toString());
                try {
                    int h = Integer.valueOf(str);
                    currentCal.set(Calendar.HOUR_OF_DAY, h);
                    Log.e(TAG, sdf.format(currentCal.getTime()));

                    if (tvCancel.getVisibility() == View.VISIBLE) {

                    } else {
                        callback();
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });


        mPicker.setOnItemSelectedListener((picker, data, position) -> {
            if (data instanceof String) {
                String str = data.toString();
                Log.e(TAG, data.toString());
                try {
                    int m = Integer.valueOf(str);
                    currentCal.set(Calendar.MINUTE, m);
                    Log.e(TAG, sdf.format(currentCal.getTime()));
                    if (tvCancel.getVisibility() == View.VISIBLE) {

                    } else {
                        callback();
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        this.needRespond = visibility == View.VISIBLE;
    }

    private void callback() {
        if (this.onDateTimePickerListener != null && this.needRespond) {
            this.onDateTimePickerListener.onChange(this.currentCal, sdf.format(this.currentCal.getTime()), this.currentCal.getTime().getTime());
        }
    }


    public void setDateTimeFormat(String pattern) {
        this.sdf.applyPattern(pattern);
    }

    public void setDateTimeFormat(SimpleDateFormat format) {
        this.sdf = format;
    }

    public void setCurrent(Calendar current) {
        Calendar nowCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"), Locale.TAIWAN);
        this.currentCal = new GregorianCalendar(current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH), current.get(Calendar.HOUR_OF_DAY), current.get(Calendar.MINUTE), 0);
        int result = nowCal.compareTo(this.currentCal);
        List<DateData> dates = result < 0 ? data(nowCal, 3, "(EEEEE) MM月dd日") : data(new GregorianCalendar(current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH), current.get(Calendar.HOUR_OF_DAY), current.get(Calendar.MINUTE), 0), 3, "(EEEEE) MM月dd日");
        int h = current.get(Calendar.HOUR_OF_DAY);
        int m = current.get(Calendar.MINUTE);
        dPicker.setData(dates);
        hPicker.setSelectedItemPosition(h, false);
        mPicker.setSelectedItemPosition(m, false);
        DateData select = new DateData().bind("", new GregorianCalendar(currentCal.get(Calendar.YEAR), currentCal.get(Calendar.MONTH), currentCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0), false, false, false);
        int d = result < 0 ? dates.indexOf(select) : 0;
        dPicker.setSelectedItemPosition(d, false);
    }

    public SimpleDateFormat getFormat () {
        return this.sdf;
    }


    public void setControl(boolean show) {
        tvCancel.setVisibility(show ? View.VISIBLE : View.GONE);
        tvConfirm.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void reset() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"), Locale.TAIWAN);
        this.currentCal = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), 0);
        int h = cal.get(Calendar.HOUR_OF_DAY);
        int m = cal.get(Calendar.MINUTE);
        List<DateData> dates = data(cal, 4, "(EEEEE) MM月dd日");

        dPicker.setData(dates);
        hPicker.setSelectedItemPosition(h, false);
        mPicker.setSelectedItemPosition(m, false);
        dPicker.setSelectedItemPosition(0, false);
    }


    public void resetPlus(int plusDay, int plusHour, int plusMinute) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"), Locale.TAIWAN);
        this.currentCal = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) + plusDay, cal.get(Calendar.HOUR_OF_DAY) + plusHour, cal.get(Calendar.MINUTE) + plusMinute, 0);
        int h = currentCal.get(Calendar.HOUR_OF_DAY);
        int m = currentCal.get(Calendar.MINUTE);
        List<DateData> dates = data(currentCal, 4, "(EEEEE) MM月dd日");

        dPicker.setData(dates);
        hPicker.setSelectedItemPosition(h, false);
        mPicker.setSelectedItemPosition(m, false);
        dPicker.setSelectedItemPosition(0, false);
    }

    public void setOnDateTimePickerListener(OnDateTimePickerListener onDateTimePickerListener) {
        this.onDateTimePickerListener = onDateTimePickerListener;
    }

    private synchronized List<DateData> data(Calendar cal, int plusYear, String pattern) {
        Calendar nowCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"), Locale.TAIWAN);

        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.TAIWAN);
        SimpleDateFormat sdf2 = new SimpleDateFormat("(EEEEE) yyyy/MM/dd", Locale.TAIWAN);
        List<DateData> list = Lists.newArrayList();
        for (int y = 0; y <= plusYear - 1; y++) {
            int day = cal.get(Calendar.DAY_OF_YEAR);
            int total = cal.getActualMaximum(Calendar.DAY_OF_YEAR);
            for (int i = day; i <= total; i++) {
                cal.set(Calendar.DAY_OF_YEAR, i);
                Calendar nCal = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
                list.add(new DateData().bind(nCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) ? sdf.format(nCal.getTime()) : sdf2.format(nCal.getTime()), nCal, false, false, false));
            }
            cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1);
            cal.roll(Calendar.DAY_OF_YEAR, 1);//把日期設定為當月第一天
        }


        return list;
    }

    public interface OnDateTimePickerListener {
        void onChange(Calendar current, String dateTime, long millis);
    }
}
