package tw.com.chainsea.ce.sdk.network.model.response

import android.content.ContentValues
import com.google.common.collect.Lists
import tw.com.chainsea.ce.sdk.bean.common.EnableType
import tw.com.chainsea.ce.sdk.bean.todo.TodoStatus
import tw.com.chainsea.ce.sdk.database.DBContract
import tw.com.chainsea.ce.sdk.network.model.common.Header

data class SyncTodoResponse(
    val _header_: Header,
    val hasNextPage: Boolean? = false,
    val refreshTime: Long,
    val count: Int,
    val items: List<TodoEntity> = Lists.newArrayList()
)


data class TodoEntity(
    val remindTime: Long,
    val createTime: Long,
    val publicType: String,
    val openClock: Boolean,
    val updateTime: Long,
    val id: String,
    val title: String,
    val userId: String,
    val status: TodoStatus,
    val roomId: String?,
    val messageId: String?

) {
    fun getContentValues(): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(DBContract.TodoEntry._ID, id)
        contentValues.put(DBContract.TodoEntry.COLUMN_TITLE, title)
        contentValues.put(DBContract.TodoEntry.COLUMN_USER_ID, userId)
        contentValues.put(DBContract.TodoEntry.COLUMN_TODO_STATUS, status.name)
        contentValues.put(DBContract.TodoEntry.COLUMN_REMIND_TIME, remindTime)
        contentValues.put(DBContract.TodoEntry.COLUMN_CREATE_TIME, createTime)
        contentValues.put(DBContract.TodoEntry.COLUMN_UPDATE_TIME, updateTime)
        contentValues.put(DBContract.TodoEntry.COLUMN_OPEN_CLOCK, EnableType.of(openClock).name)
        contentValues.put(DBContract.TodoEntry.COLUMN_ROOM_ID, roomId)
        contentValues.put(DBContract.TodoEntry.COLUMN_MESSAGE_ID, messageId)
        contentValues.put(DBContract.TodoEntry.COLUMN_PUBLIC_TYPE, publicType)
        return contentValues
    }
}
