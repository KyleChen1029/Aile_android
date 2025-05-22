package tw.com.chainsea.custom.view.picker.bean;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.List;

/**
 * Create by evan on 2/22/21
 *
 * @author Evan Wang
 * @date 2/22/21
 */
public class YearData {
    String name;
    int value;
    Calendar cal;
    List<MonthData> months;

    public YearData bind(String name, Calendar cal, List<MonthData> months) {
        this.name = name;
        this.cal = cal;
        this.months = months;
        return this;
    }


    public YearData bind(String name, int value) {
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

    public List<MonthData> getMonths() {
        return months;
    }

    public void setMonths(List<MonthData> months) {
        this.months = months;
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
