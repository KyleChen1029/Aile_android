package tw.com.chainsea.chat.searchfilter.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.collect.Lists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import tw.com.aile.sdk.bean.filter.FilterRoomType
import tw.com.aile.sdk.bean.filter.FilterTab
import tw.com.aile.sdk.bean.filter.SearchFilterEntity
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.ce.sdk.bean.CrowdEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.chat.R

class CommunitiesSearchViewModel : ViewModel() {
    var localEntities : MutableList<CrowdEntity> = arrayListOf()
    private val searchKeyWord = MutableStateFlow("")
    private var communitiesFilteredSectionedList : MutableList<SearchFilterEntity> = arrayListOf()
    private var localAllMessagesEntities: MutableList<MessageEntity> = mutableListOf()
    val sendFilterQueryList = MutableSharedFlow<Triple<String, MutableList<SearchFilterEntity>, Int>>()
    val navigateToChatRoom = MutableSharedFlow<Pair<String, Any>>()
    private var allNewsGrouped : Map<String?, List<MessageEntity>> = mapOf()
    val navigateToMessageList =  MutableSharedFlow<Triple<String, List<MessageEntity>, ChatRoomEntity>>()
    val sendInitDataDone = MutableSharedFlow<Unit>()

    fun getAllCommunityRoom(allChatRecord: MutableList<MessageEntity>) = viewModelScope.launch(Dispatchers.IO) {
        localEntities = DBManager.getInstance().findAllCrowds()
        localAllMessagesEntities = allChatRecord
        sendInitDataDone.emit(Unit)
    }

    fun filter(ctx: Context, s: String) = viewModelScope.launch(Dispatchers.IO) {
        searchKeyWord.value = s

        val communityRooms: MutableList<CrowdEntity> = mutableListOf()
        val newsRooms: MutableList<MessageEntity> = mutableListOf()

        if(s.isNotEmpty()) {
            communitiesFilteredSectionedList.clear()
            for (entity in localEntities){
                if(checkFilterCondition(s, entity))
                    communityRooms.add(entity)
            }

            //消息過濾
            for(message in localAllMessagesEntities)
                if(checkFilterCondition(s, message))
                    newsRooms.add(message)

            if(communityRooms.isNotEmpty()) {
                communityRooms.sortByDescending { it.lastMessageTime }
                communitiesFilteredSectionedList.add(
                    SearchFilterEntity(
                        ctx.getString(R.string.text_sectioned_communities_filter, communityRooms.size),
                        FilterRoomType.GROUP,
                        FilterTab.COMMUNITY,
                        communityRooms
                    )
                )
            }

            if(newsRooms.isNotEmpty()) {
                val eachNewsRooms : MutableList<ChatRoomEntity> = mutableListOf()
                allNewsGrouped = newsRooms.groupBy { it.roomId } //先group by roomId和消息
                for((roomId, list) in allNewsGrouped) {
                    val chatRoomEntity = ChatRoomReference.getInstance().findById(roomId)
                    if(ChatRoomType.group == chatRoomEntity.type) { //只顯示社團聊天室
                        if (list.size > 1)
                            chatRoomEntity.searchMessageCount =
                                ctx.getString(R.string.text_sectioned_find_news_count, list.size) //顯示幾則消息
                        else
                            chatRoomEntity.searchMessageCount = list[0].content //直接顯示消息內容
                        eachNewsRooms.add(chatRoomEntity)
                    }
                }

                if(eachNewsRooms.isNotEmpty()) {
                    eachNewsRooms.sortByDescending { it.lastMessage?.sendTime ?: Long.MAX_VALUE }
                    communitiesFilteredSectionedList.add(
                        SearchFilterEntity(
                            ctx.getString(R.string.text_sectioned_search_news, eachNewsRooms.size),
                            FilterRoomType.NEWS,
                            FilterTab.COMMUNITY,
                            eachNewsRooms
                        )
                    )
                }
            }

        }
        sendFilterQueryList.emit(Triple(s, if(s.isNotEmpty()) communitiesFilteredSectionedList else Lists.newArrayList(), communityRooms.size + newsRooms.size))
    }

    private fun checkFilterCondition(filter: String, item: Any) : Boolean {
        return when(item) {
            is MessageEntity -> {
                item.getContent(item.content).contains(filter, true)
            }
            is CrowdEntity -> {
                //過濾社團聊天室名稱, 社團成員名稱
                item.name.contains(filter, true) || (
                    item.memberArray.any {
                        it.nickName.contains(filter, true) ||
                                it.alias?.contains(filter, true) == true
                    })
            }
            else -> { false }
        }
    }

    fun onSelectChatRoomItem(item: Any, isNews: Boolean) = viewModelScope.launch {
        if(isNews) { //點擊消息轉頁
            if(item is ChatRoomEntity) {
                val messageList = allNewsGrouped[item.id]
                messageList?.let {
                    navigateToMessageList.emit(Triple(searchKeyWord.value, it, item))
                }
            }
        } else {
            if(item is CrowdEntity){
                val entity = ChatRoomReference.getInstance().findById(item.id)
                entity?.let {
                    navigateToChatRoom.emit(Pair(searchKeyWord.value, it))
                }?: run {
                    navigateToChatRoom.emit(Pair(searchKeyWord.value, item))
                }
            }
        }
    }
}