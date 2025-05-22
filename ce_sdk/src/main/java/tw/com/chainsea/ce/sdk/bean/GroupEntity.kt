package tw.com.chainsea.ce.sdk.bean

import android.content.ContentValues
import android.database.Cursor
import com.google.gson.reflect.TypeToken
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.bean.common.EnableType
import tw.com.chainsea.ce.sdk.bean.msg.Tools
import tw.com.chainsea.ce.sdk.database.DBContract
import java.io.Serializable

data class GroupEntity(
    var id: String,
    var name: String,
    var lastReadSequence: Int,
    var lastReceivedSequence: Int,
    var isCustomName: Boolean,
    var member_deleted: Boolean,
    var updateTime: Long,
    var type: String,
    var topTime: Long,
    var deleted: Boolean,
    var avatarId: String?,
    var isTop: Boolean,
    var dfrTime: Long,
    var memberIds: List<String>?,
    var isSelected: Boolean = false
): Serializable, Cloneable {

    constructor() : this("", "", 0, 0, false, false, 0, "", 0, false, "", false, 0, listOf(), false)

    public override fun clone(): GroupEntity {
        return super.clone() as GroupEntity
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GroupEntity

        if (id != other.id) return false
        if (name != other.name) return false
        if (lastReadSequence != other.lastReadSequence) return false
        if (lastReceivedSequence != other.lastReceivedSequence) return false
        if (isCustomName != other.isCustomName) return false
        if (member_deleted != other.member_deleted) return false
        if (updateTime != other.updateTime) return false
        if (type != other.type) return false
        if (topTime != other.topTime) return false
        if (deleted != other.deleted) return false
        if (avatarId != other.avatarId) return false
        if (isTop != other.isTop) return false
        if (dfrTime != other.dfrTime) return false
        if (memberIds != other.memberIds) return false
        if (isSelected != other.isSelected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + lastReadSequence
        result = 31 * result + lastReceivedSequence
        result = 31 * result + isCustomName.hashCode()
        result = 31 * result + member_deleted.hashCode()
        result = 31 * result + updateTime.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + topTime.hashCode()
        result = 31 * result + deleted.hashCode()
        result = 31 * result + avatarId.hashCode()
        result = 31 * result + isTop.hashCode()
        result = 31 * result + dfrTime.hashCode()
        result = 31 * result + memberIds.hashCode()
        result = 31 * result + (isSelected?.hashCode() ?: 0)
        return result
    }

    companion object {

        @JvmStatic
        fun getContentValues(entity: GroupEntity): ContentValues {
            val values = ContentValues()
            values.put(DBContract.SyncGroupEntry._ID, entity.id)
            values.put(DBContract.SyncGroupEntry.COLUMN_NAME, entity.name)
            values.put(DBContract.SyncGroupEntry.COLUMN_TYPE, entity.type)
            values.put(DBContract.SyncGroupEntry.COLUMN_AVATAR_URL, entity.avatarId)
            values.put(DBContract.SyncGroupEntry.COLUMN_DFR_TIME, entity.dfrTime)
            values.put(DBContract.SyncGroupEntry.COLUMN_IS_TOP, EnableType.of(entity.isTop).name)
            values.put(DBContract.SyncGroupEntry.COLUMN_TOP_TIME, entity.topTime)
            values.put(DBContract.SyncGroupEntry.COLUMN_IS_CUSTOM_NAME, EnableType.of(entity.isCustomName).name)
            values.put(DBContract.SyncGroupEntry.COLUMN_DELETED, EnableType.of(entity.deleted).name)
            values.put(DBContract.SyncGroupEntry.COLUMN_LAST_READ_SEQUENCE, entity.lastReadSequence)
            values.put(DBContract.SyncGroupEntry.COLUMN_LAST_RECEIVED_SEQUENCE, entity.lastReceivedSequence)
            values.put(
                DBContract.SyncGroupEntry.COLUMN_MEMBER_DELETED,
                EnableType.of(entity.member_deleted).name
            )
            values.put(DBContract.SyncGroupEntry.COLUMN_UPDATE_TIME, entity.updateTime)
            values.put(
                DBContract.SyncGroupEntry.COLUMN_MEMBER_IDS,
                JsonHelper.getInstance().toJson(entity.memberIds)
            )
            return values
        }

        @JvmStatic
        fun getEntity(cursor: Cursor): GroupEntity {
            val entity = GroupEntity()
            entity.id = Tools.getDbString(cursor, DBContract.SyncGroupEntry._ID)
            entity.name = Tools.getDbString(cursor, DBContract.SyncGroupEntry.COLUMN_NAME)
            entity.avatarId = Tools.getDbString(cursor, DBContract.SyncGroupEntry.COLUMN_AVATAR_URL)
            entity.lastReadSequence =
                Tools.getDbInt(cursor, DBContract.SyncGroupEntry.COLUMN_LAST_READ_SEQUENCE)
            entity.lastReceivedSequence =
                Tools.getDbInt(cursor, DBContract.SyncGroupEntry.COLUMN_LAST_RECEIVED_SEQUENCE)
            entity.dfrTime = Tools.getDbLong(cursor, DBContract.SyncGroupEntry.COLUMN_DFR_TIME)
            entity.isTop =
                EnableType.valueOf(Tools.getDbString(cursor, DBContract.SyncGroupEntry.COLUMN_IS_TOP)).isStatus
            entity.topTime = Tools.getDbLong(cursor, DBContract.SyncGroupEntry.COLUMN_TOP_TIME)
            entity.isCustomName = EnableType.valueOf(
                Tools.getDbString(
                    cursor,
                    DBContract.SyncGroupEntry.COLUMN_IS_CUSTOM_NAME
                )
            ).isStatus
            entity.deleted =
                EnableType.valueOf(Tools.getDbString(cursor, DBContract.SyncGroupEntry.COLUMN_DELETED)).isStatus
            entity.member_deleted = EnableType.valueOf(
                Tools.getDbString(
                    cursor,
                    DBContract.SyncGroupEntry.COLUMN_MEMBER_DELETED
                )
            ).isStatus
            entity.updateTime = Tools.getDbLong(cursor, DBContract.SyncGroupEntry.COLUMN_UPDATE_TIME)
            entity.memberIds = JsonHelper.getInstance().from(
                Tools.getDbString(cursor, DBContract.SyncGroupEntry.COLUMN_MEMBER_IDS),
                object : TypeToken<List<String?>?>() {}.type
            )
            entity.type = Tools.getDbString(cursor, DBContract.SyncGroupEntry.COLUMN_TYPE)
            return entity
        }
    }

    fun isAddHardCode(): Boolean {
        return "isAdd" == id && "建立社團" == name //&& "與好友建立社團" == ownerId
    }
}