package tw.com.chainsea.ce.sdk.util

import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource

object ConditionUtil {
    fun isNotBossServiceNumberOwnerStop(entity: ChatRoomEntity): Boolean =
            !entity.serviceNumberOwnerStop &&
                    entity.type == ChatRoomType.services &&
                    entity.listClassify.equals(ChatRoomSource.MAIN) &&
                    entity.serviceNumberType.equals(ServiceNumberType.BOSS)
                    && (entity.serviceNumberStatus.equals(ServiceNumberStatus.OFF_LINE) || entity.serviceNumberStatus.equals(ServiceNumberStatus.TIME_OUT) || entity.serviceNumberStatus.equals(ServiceNumberStatus.ON_LINE))
}