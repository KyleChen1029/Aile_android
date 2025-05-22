package tw.com.chainsea.custom.view.picker.bean;

import androidx.annotation.NonNull;

import java.util.Calendar;

/**
 * current by evan on 2020-07-06
 *
 * @author Evan Wang
 * @date 2020-07-06
 */
public class DateData {
    String name;
    Calendar cal;
    int value;
    boolean lastMonth;
    boolean currentMonth;
    boolean nextMonth;
    boolean select;

    public DateData bind(String name, Calendar cal, boolean lastMonth, boolean currentMonth, boolean nextMonth) {
        this.name = name;
        this.cal = cal;
        this.lastMonth = lastMonth;
        this.currentMonth = currentMonth;
        this.nextMonth = nextMonth;
        return this;
    }

    public DateData bind(String name, int value ) {
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

//    public void setCal(Calendar cal) {
//        this.cal = cal;
//    }

    public boolean isLastMonth() {
        return lastMonth;
    }

//    public void setLastMonth(boolean lastMonth) {
//        this.lastMonth = lastMonth;
//    }
//
//    public boolean isCurrentMonth() {
//        return currentMonth;
//    }
//
//    public void setCurrentMonth(boolean currentMonth) {
//        this.currentMonth = currentMonth;
//    }

    public boolean isNextMonth() {
        return nextMonth;
    }

//    public void setNextMonth(boolean nextMonth) {
//        this.nextMonth = nextMonth;
//    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cal == null) ? 0 : cal.toString().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DateData other = (DateData) obj;
        if (this.cal == null || other.getCal() == null) {
            return false;
        }
        int year = this.cal.get(Calendar.YEAR);
        int day = this.cal.get(Calendar.DAY_OF_YEAR);

        int otherYear = other.getCal().get(Calendar.YEAR);
        int otherDay = other.getCal().get(Calendar.DAY_OF_YEAR);

        return year == otherYear && day == otherDay;

//        if (this.id == null) {
//            if (other.getId() != null)
//                return false;
//        } else if (!this.id.equals(other.getId()))
//            return false;
    }

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }
}
