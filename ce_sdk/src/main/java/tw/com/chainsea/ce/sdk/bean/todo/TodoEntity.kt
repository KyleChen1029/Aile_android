package tw.com.chainsea.ce.sdk.bean.todo

import android.content.ContentValues
import android.database.Cursor
import com.google.common.collect.ComparisonChain
import org.json.JSONObject
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.bean.ProcessStatus
import tw.com.chainsea.ce.sdk.bean.common.EnableType
import tw.com.chainsea.ce.sdk.bean.msg.Tools
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.database.DBContract
import java.math.BigInteger
import java.util.Calendar
import kotlin.math.abs

class TodoEntity private constructor(var id: String,
                                     var title: String,
                                     var status: TodoStatus,
                                     var userId: String,
                                     var createTime: Long,
                                     var updateTime: Long,
                                     var openClock: Boolean,
                                     var remindTime: Long,
                                     var publicType: String,
                                     var roomId: String,
                                     var messageId: String,
                                     var type: Type,
                                     var roomEntity: ChatRoomEntity?,
                                     var messageEntity: MessageEntity?,
                                     var current: Calendar?,
                                     var isOutOfBounds: Boolean,
                                     var processStatus: ProcessStatus?,
                                     var deleteClickCount: Int) : Comparable<TodoEntity>, tw.com.chainsea.ce.sdk.bean.Entity {

    companion object {

        @JvmStatic
        fun formatByCursor(cursor: Cursor): TodoEntity {
            return Builder()
                    .id(Tools.getDbString(cursor, DBContract.TodoEntry._ID))
                    .title(Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_TITLE))
                    .remindTime(Tools.getDbLong(cursor, DBContract.TodoEntry.COLUMN_REMIND_TIME))
                    .openClock(EnableType.valueOf(Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_OPEN_CLOCK)).isStatus)
                    .userId(Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_USER_ID))
                    .createTime(Tools.getDbLong(cursor, DBContract.TodoEntry.COLUMN_CREATE_TIME))
                    .updateTime(Tools.getDbLong(cursor, DBContract.TodoEntry.COLUMN_UPDATE_TIME))
                    .status(TodoStatus.of(Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_TODO_STATUS)))
                    .roomId(Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_ROOM_ID))
                    .messageId(Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_MESSAGE_ID))
                    .publicType(Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_PUBLIC_TYPE))
                    .processStatus(ProcessStatus.parse(Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_PROCESS_STATUS)))
                    .type(Type.MAIN)
                    .build()
        }

        @JvmStatic
        fun formatByCursor(cursor: Cursor, chatRoomEntity: ChatRoomEntity?): TodoEntity {
            val todoEntity = formatByCursor(cursor)
            chatRoomEntity?.let {
                todoEntity.roomEntity = chatRoomEntity
            }
            return todoEntity
        }

        @JvmStatic
        fun formatByCursor(cursor: Cursor, messageEntity: MessageEntity?, chatRoomEntity: ChatRoomEntity?): TodoEntity {
            val todoEntity = formatByCursor(cursor, chatRoomEntity)
            messageEntity?.let {
                todoEntity.messageEntity = messageEntity
            }
            return todoEntity
        }
    }

    fun toCreateJsonObj(): JSONObject {
        return JSONObject().put("id", this.id).put("title", this.title).put("remindTime", this.remindTime).put("openClock", this.openClock).put("roomId", this.roomId).put("messageId", this.messageId).put("status", this.status.status)
    }

    fun toUpdateJsonObj(): JSONObject {
        return JSONObject().put("id", this.id).put("title", this.title).put("remindTime", this.remindTime).put("openClock", this.openClock).put("roomId", this.roomId).put("status", this.status.status)
    }


    fun toCompleteJsonObj(): JSONObject {
        return JSONObject().put("id", this.id).put("status", this.status.status)
    }


    fun getUniqueId(): Int {
        val uuid: String = this.id.replace("-".toRegex(), "")
        val big = BigInteger(uuid, 32)
        val alarmId = big.toInt()
        return abs(alarmId)
    }

    fun toJson(): String {
        return JsonHelper.getInstance().toJson(this)
    }

    fun getWeights(): Double {
        return if (remindTime <= 0) 1.0 else 0.0
    }

    fun getWeights2(): Double {
        return if (remindTime <= 0) createTime.toDouble() else remindTime.toDouble()
    }

    fun getWeights3(): Double {
        return if (TodoStatus.DONE == status) 1.0 else 0.0
    }


    fun getWeights4(): Double {
        return if (remindTime >= System.currentTimeMillis()) updateTime.toDouble() else (if (remindTime <= 0) updateTime else remindTime).toDouble()
    }

    fun getWeights5(): Double {
        return if (remindTime > 0 && remindTime < System.currentTimeMillis()) 0.0 else 1.0
    }

    fun getWeights6(): Long {
        return updateTime
    }

    override fun compareTo(other: TodoEntity): Int {
        return ComparisonChain.start().compare(this.getWeights(), other.getWeights()).compare(this.getWeights2(), other.getWeights2()).result()
    }

    override fun getClassName(): String {
        return this.javaClass.simpleName
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj == null) return false
        if (javaClass != obj.javaClass) return false
        val other = obj as TodoEntity
        if (id == null || other.id == null) {
            return false
        }
        return id == other.id && type == other.type
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + if (id == null) 0 else id.hashCode()
        return result
    }

    data class Builder(var id: String = "",
                       var title: String = "",
                       var status: TodoStatus = TodoStatus.PROGRESS,
                       var userId: String = "",
                       var createTime: Long = 0,
                       var updateTime: Long = 0,
                       var openClock: Boolean = false,
                       var remindTime: Long = 0,
                       var publicType: String = "",
                       var roomId: String = "",
                       var messageId: String = "",
                       var type: Type = Type.MAIN,
                       var roomEntity: ChatRoomEntity? = null,
                       var messageEntity: MessageEntity? = null,
                       var current: Calendar? = null,
                       var isOutOfBounds: Boolean = false,
                       var processStatus: ProcessStatus? = ProcessStatus.UNDEF,
                       var deleteClickCount: Int = 0) {
        fun id(id: String) = apply { this.id = id }
        fun title(title: String) = apply { this.title = title }
        fun status(status: TodoStatus) = apply { this.status = status }
        fun userId(userId: String) = apply { this.userId = userId }
        fun createTime(createTime: Long) = apply { this.createTime = createTime }
        fun updateTime(updateTime: Long) = apply { this.updateTime = updateTime }
        fun openClock(openClock: Boolean) = apply { this.openClock = openClock }
        fun remindTime(remindTime: Long) = apply { this.remindTime = remindTime }
        fun publicType(publicType: String) = apply { this.publicType = publicType }
        fun roomId(roomId: String) = apply { this.roomId = roomId }
        fun type(type: Type) = apply { this.type = type }
        fun roomEntity(roomEntity: ChatRoomEntity) = apply { this.roomEntity = roomEntity }
        fun messageEntity(messageEntity: MessageEntity) = apply { this.messageEntity = messageEntity }
        fun isOutOfBounds(isOutOfBounds: Boolean) = apply { this.isOutOfBounds = isOutOfBounds }
        fun messageId(messageId: String) = apply { this.messageId = messageId }
        fun processStatus(processStatus: ProcessStatus) = apply { this.processStatus = processStatus }
        fun current(current: Calendar) = apply { this.current = current }
        fun deleteClickCount(deleteClickCount: Int) = apply { this.deleteClickCount = deleteClickCount }
        fun build() = TodoEntity(id, title, status, userId, createTime, updateTime, openClock, remindTime, publicType, roomId, messageId, type, roomEntity, messageEntity, current, isOutOfBounds, processStatus, deleteClickCount)
    }
}


fun TodoEntity.getContentValues(): ContentValues {
    val values = getUpdateContentValues()
    values.put(DBContract.TodoEntry._ID, id)
    return values
}


fun TodoEntity.getUpdateContentValues(): ContentValues {
    val values = ContentValues()
    values.put(DBContract.TodoEntry.COLUMN_TITLE, title)
    values.put(DBContract.TodoEntry.COLUMN_USER_ID, userId)
    values.put(DBContract.TodoEntry.COLUMN_TODO_STATUS, status.name)
    values.put(DBContract.TodoEntry.COLUMN_REMIND_TIME, remindTime)
    values.put(DBContract.TodoEntry.COLUMN_CREATE_TIME, createTime)
    values.put(DBContract.TodoEntry.COLUMN_UPDATE_TIME, updateTime)
    values.put(DBContract.TodoEntry.COLUMN_OPEN_CLOCK, EnableType.of(openClock).name)
    values.put(DBContract.TodoEntry.COLUMN_ROOM_ID, roomId)
    values.put(DBContract.TodoEntry.COLUMN_MESSAGE_ID, messageId)
    values.put(DBContract.TodoEntry.COLUMN_PROCESS_STATUS, processStatus?.name)
    values.put(DBContract.TodoEntry.COLUMN_PUBLIC_TYPE, publicType)
    return values
}


enum class Type {
    MAIN, SETTING
}