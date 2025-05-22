package tw.com.chainsea.custom.view.picker.adapter.listener;

import tw.com.chainsea.custom.view.picker.bean.DateData;
import tw.com.chainsea.custom.view.picker.bean.MonthData;

/**
 * current by evan on 2020-07-06
 *
 * @author Evan Wang
 * @date 2020-07-06
 */
public interface OnCalendarListener {
    void onDateSelect(DateData dateData);

    void doMonthChangeAction(MonthData monthData);

    void doLastAction(MonthData monthData);

    void doNextAction(MonthData monthData);
}
