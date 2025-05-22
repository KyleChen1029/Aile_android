package tw.com.chainsea.chat.view.business;

import androidx.annotation.StringRes;

import com.google.common.collect.Lists;

import java.util.List;
import tw.com.chainsea.chat.R;

/**
 * current by evan on 2020-03-26
 *
 *
 * @author Evan Wang
 * date 2020-03-26
 */
public enum DateSelector {
    PLUS_8_SEC(0, 8, R.string.date_selector_text_plus_8_sec, true, true),
    PLUS_10_MINUTE(0, 10, R.string.date_selector_text_plus_10_minute, true, true),
    PLUS_30_MINUTE(1, 30, R.string.date_selector_text_plus_30_minute, true, true),
    PLUS_1_HOUR(2, 1, R.string.date_selector_text_plus_1_hour, true, true),
    TODAY(3, 1, R.string.date_selector_text_plus_1_day, true, true),
    TOMORROW(4, 2, R.string.date_selector_text_plus_2_day, true, false),
    AFTER_TOMORROW(5, 3, R.string.date_selector_text_plus_3_day, true, false),
    ONE_WEEK(6, 7, R.string.date_selector_text_1_week, true, false),
    TWO_WEEK(7, 14, R.string.date_selector_text_2_week, true, false),
    ONE_MONTH(8, 1, R.string.date_selector_text_1_month, true, false),
//    CLEAR(6, 0, "清除", false, false);
    CUSTOMIZE(99 , 0, R.string.alert_custom,false,false );

    DateSelector(int index, int interval, int nameResId, boolean isDate, boolean isSelected) {
        this.index = index;
        this.interval = interval;
        this.nameResId = nameResId;
        this.isDate = isDate;
        this.isSelected = isSelected;
    }

    private int index;
    private int interval;
    @StringRes
    private int nameResId;
    private boolean isDate;
    private boolean isSelected;

    public static List<DateSelector> TODO_ITEM_SETTING_DATE_SELECTOR = Lists.newArrayList(PLUS_10_MINUTE, PLUS_1_HOUR, TODAY, ONE_WEEK, ONE_MONTH, CUSTOMIZE);
//    public static List<DateSelector> TODO_ITEM_SETTING_DATE_SELECTOR_DEBUG = Lists.newArrayList(PLUS_8_SEC, PLUS_10_MINUTE, PLUS_1_HOUR, TODAY, ONE_WEEK, ONE_MONTH, CUSTOMIZE);

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getNameResId() {
        return nameResId;
    }

    public void setNameResId(int nameResId) {
        this.nameResId = nameResId;
    }

    public boolean isDate() {
        return isDate;
    }

    public void setDate(boolean date) {
        isDate = date;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}