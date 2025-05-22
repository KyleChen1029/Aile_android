package tw.com.chainsea.chat.view.roomList.serviceRoomList

import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity
import tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter.ActionBean

data class ServiceNumberListModel(
    val id: String,
    val type: ServiceNumberListType,
    val serviceNumberEntity: ServiceNumberEntity? = null,
    val serviceNumberIcon: Int? = null,
    val serviceNumberControl: HashMap<Int, ActionBean> = HashMap(),
    var serviceNumberEndServiceChatRoom: MutableList<ChatRoomEntity> = mutableListOf(),
    val serviceNumberServicingChatRoom: MutableList<ChatRoomEntity> = mutableListOf(),
    var isOpen: Boolean = false,
    var openListSize: Int = 0,
    var unReadNum: Int = 0,
) {
    fun deepCopy(): ServiceNumberListModel {
        return ServiceNumberListModel(
            id = id,
            type = type,
            serviceNumberEntity = serviceNumberEntity, // 假設 `ServiceNumberEntity` 是不可變的
            serviceNumberIcon = serviceNumberIcon,
            serviceNumberControl = serviceNumberControl,
            serviceNumberEndServiceChatRoom = serviceNumberEndServiceChatRoom.map { it.clone() }.toMutableList(), // 深拷貝 List
            serviceNumberServicingChatRoom = serviceNumberServicingChatRoom.map { it.clone() }.toMutableList(), // 深拷貝 List
            isOpen = isOpen,
            openListSize = openListSize,
            unReadNum = unReadNum
        )
    }
}

fun List<ServiceNumberListModel>.deepCopy(): List<ServiceNumberListModel> {
    return this.map { it.deepCopy() }
}



/**
 * AIService AI服務
 * MonitorAI 監控AI
 * UnService 剛進件
 * MyService 我服務中
 * Serviced 服務中
 * ServiceNumberGroup 其他服務號群組
 * Other 其他 (時間排序用)
 * */
enum class ServiceNumberListType {
    AIService, MonitorAI, UnService,MyService, Serviced, ServiceNumberGroup, Other
}