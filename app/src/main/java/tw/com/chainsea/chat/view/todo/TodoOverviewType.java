package tw.com.chainsea.chat.view.todo;

import androidx.annotation.StringRes;

import tw.com.chainsea.chat.R;

/**
 * current by evan on 2020-07-16
 *
 * @author Evan Wang
 * date 2020-07-16
 *
 */
public enum TodoOverviewType {
    BUSINESS_EXECUTOR(0, R.string.todo_overview_tab_execute),
    BUSINESS_MANAGER(1, R.string.todo_overview_tab_release),
    BUSINESS_COOPERATE(2, R.string.todo_overview_tab_cooperation),
    SCHEDULE_LIST(3, R.string.todo_overview_tab_remind);

    private final int index;

    @StringRes
    private final int resId;

    TodoOverviewType(int index, int resId) {
        this.index = index;
        this.resId = resId;
    }

    public int getIndex() {
        return this.index;
    }

    public int getResId() {
        return this.resId;
    }
}