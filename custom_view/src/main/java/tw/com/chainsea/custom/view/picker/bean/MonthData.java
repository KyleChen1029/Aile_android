package tw.com.chainsea.custom.view.picker.bean;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.List;

/**
 * current by evan on 2020-07-06
 *
 * @author Evan Wang
 * @date 2020-07-06
 */
public class MonthData {
    String name;
    int value;
    Calendar cal;
    List<DateData> dates;


    public MonthData bind(String name, Calendar cal, List<DateData> dates) {
        this.name = name;
        this.cal = cal;
        this.dates = dates;
        return this;
    }


    public MonthData bind(String name, int value) {
        this.name = name;
        this.value = value;
        return this;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Calendar getCal() {
        return cal;
    }

    public void setCal(Calendar cal) {
        this.cal = cal;
    }

    public List<DateData> getDates() {
        return dates;
    }

    public void setDates(List<DateData> dates) {
        this.dates = dates;
    }

    public int getValue() {
        return value;
    }

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }
}
