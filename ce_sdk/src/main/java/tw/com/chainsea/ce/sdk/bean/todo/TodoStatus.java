package tw.com.chainsea.ce.sdk.bean.todo;

import androidx.annotation.StringRes;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.util.Set;

import tw.com.chainsea.ce.sdk.R;

/**
 * current by evan on 2020-07-14
 *
 * @author Evan Wang
 * date 2020-07-14
 */

public enum TodoStatus {
    @SerializedName(value = "progress", alternate = {"PROGRESS"}) PROGRESS(0, "progress", R.string.alert_unfinished),
    @SerializedName(value = "done", alternate = {"DONE"}) DONE(1, "done", R.string.alert_complete),
    @SerializedName(value = "deleted", alternate = {"DELETED"}) DELETED(2, "deleted", R.string.alert_delete),
    EMPTY(6, " ", R.string.warning_empry);

    private int index;
    private String status;
    @StringRes
    private int nameRes;

    public static class TodoStatusTypeAdapter {
        @ToJson
        String toJson(TodoStatus type) {
            return type.name();
        }

        @FromJson
        TodoStatus fromJson(String type) {
            return TodoStatus.of(type);
        }
    }

    public static TodoStatus of(String source) {
        if (Strings.isNullOrEmpty(source)) {
            return PROGRESS;
        }

        for (TodoStatus sourceType : values()) {
            if (sourceType.getStatus().toUpperCase().equals(source.toUpperCase())) {
                return sourceType;
            }
        }
        return PROGRESS;
    }

    public static Set<TodoStatus> DONE_or_DELETED = Sets.newHashSet(DONE, DELETED);

    TodoStatus(int index, String status, int nameRes) {
        this.index = index;
        this.status = status;
        this.nameRes = nameRes;
    }

    public int getIndex() {
        return index;
    }

    public String getStatus() {
        return status;
    }

    public int getNameRes() {
        return nameRes;
    }
}
