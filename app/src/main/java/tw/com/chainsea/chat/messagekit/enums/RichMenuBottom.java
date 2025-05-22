package tw.com.chainsea.chat.messagekit.enums;

import androidx.annotation.StringRes;

import tw.com.chainsea.chat.R;


/**
 * EVAN_FLAG 2020-03-03 (1.10.0) Add multi-select forwarding, multi-select copy, screenshot
 *
 * @version 1.10.1
 */
public enum RichMenuBottom {
    //    TRANSPOND(0, R.string.rich_menu_text_transpond, false), //
    MULTI_TRANSPOND(1, R.string.rich_menu_text_transpond, true),
    //    COPY(2, R.string.rich_menu_text_copy, false), // 複製
    MULTI_COPY(3, R.string.rich_menu_text_copy, true),
    REPLY(4, R.string.rich_menu_text_reply, false),
    SHARE(5, R.string.alert_share, false),
    SCREENSHOTS(6, R.string.rich_menu_text_screenshots, true),
    RECOVER(7, R.string.rich_menu_text_recover, false),
    DELETE(8, R.string.rich_menu_text_delete, false),
    TASK(9, R.string.rich_menu_text_work, true), //工作

    QUOTE(10, R.string.rich_menu_text_quote, false), // 引用
    COPY(11, R.string.rich_menu_text_single_copy, false), // 複製 (單個

    TODO(12, R.string.rich_menu_text_todo, false), //記事,
    NEXT(50, R.string.alert_next, false), // 下一步

    /* -------  -------  -------  -------  ------- */
    COMPLETE(99, R.string.alert_complete, false),
    ANONYMOUS(100, R.string.rich_menu_text_anonymous, false), //匿名
    PREVIEW(101, R.string.alert_preview, false),
    CANCEL(102, R.string.alert_cancel, false),
    SAVE(103, R.string.alert_save, false),
    CONFIRM(104, R.string.alert_confirm, false);

    RichMenuBottom(int sortIndex, int strRes, boolean isMulti) {
        this.sortIndex = sortIndex;
        this.strRes = strRes;
        this.isMulti = isMulti;
    }

    int sortIndex;

    @StringRes
    int strRes;

    boolean isMulti;

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }

    public int getStrRes() {
        return strRes;
    }

    public void setStrRes(int strRes) {
        this.strRes = strRes;
    }

    public boolean isMulti() {
        return isMulti;
    }

    public void setMulti(boolean multi) {
        isMulti = multi;
    }

    public RichMenuBottom str(int strRes) {
        this.strRes = strRes;
        return this;
    }

    public RichMenuBottom position(int position) {
        this.sortIndex = position;
        return this;
    }

    public int res() {
        return strRes;
    }
}
