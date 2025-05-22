package tw.com.chainsea.custom.view.picker.bean;

import androidx.annotation.ColorInt;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * current by evan on 2020-07-06
 *
 * @author Evan Wang
 * @date 2020-07-06
 */
public class WeekData {
    String name;
    Calendar cal;
    @ColorInt
    int color;
    List<DateData> dates;


    public   WeekData bind(String name, @ColorInt int color) {
        this.name = name;
        this.color = color;
        this.dates = new ArrayList<DateData>();
        return this;
    }

    public  WeekData bind(String name, Calendar cal, List<DateData> dates) {
        this.name = name;
        this.cal = cal;
        this.dates = dates;
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

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public List<DateData> getDates() {
        return dates;
    }

    public void setDates(List<DateData> dates) {
        this.dates = dates;
    }
}
