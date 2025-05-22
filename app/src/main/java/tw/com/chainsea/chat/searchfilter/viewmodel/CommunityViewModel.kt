package tw.com.chainsea.chat.searchfilter.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import tw.com.chainsea.ce.sdk.bean.CrowdEntity
import tw.com.chainsea.ce.sdk.bean.GroupEntity
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.database.DBManager

class CommunityViewModel : ViewModel() {
    private var localEntities : MutableList<CrowdEntity> = arrayListOf()
    val sendQueryList = MutableSharedFlow<Pair<String, List<Any>>>()
    val sendFilterQueryList = MutableSharedFlow<Pair<String, List<Any>>>()
    private val searchInputText = MutableStateFlow("")
    private var communityRoomFilteredList : MutableList<CrowdEntity> = arrayListOf()
    val sendCommunitySelectedItem = MutableSharedFlow<Triple<String, Any, Int>>()
    val sendEmployeeSelectedList = MutableSharedFlow<List<UserProfileEntity>>()
    val sendChatRoomText = MutableSharedFlow<String>()
    fun getAllCommunityRoom() = viewModelScope.launch(Dispatchers.IO) {
        localEntities = DBManager.getInstance().findAllCrowds()
        localEntities.sortByDescending { it.lastMessageTime }

        sendQueryList.emit(Pair(searchInputText.value, localEntities))
    }

    private fun checkFilterCondition(filter: String, item: CrowdEntity) : Boolean {
        //過濾社團聊天室名稱, 社團成員名稱
        return item.name.contains(filter, true) || (
                item.memberArray.any {
                        it.nickName.contains(filter, true) ||
                                it.alias?.contains(filter, true) == true
                    }
                )
    }

    fun filter(s: String) = viewModelScope.launch {
        searchInputText.value = s
        if(s.isNotEmpty()) {
            communityRoomFilteredList.clear()
            for (entity in localEntities){
                if(checkFilterCondition(s, entity))
                    communityRoomFilteredList.add(entity)
            }
        }

        sendFilterQueryList.emit(Pair(s, if(s.isNotEmpty()) communityRoomFilteredList else localEntities))
    }

    fun onSelectCommunityRoomItem(item: GroupEntity, position: Int) = viewModelScope.launch {
        sendCommunitySelectedItem.emit(Triple(searchInputText.value, item, position))
    }

    fun updateSelectedItemList(item: Any) = viewModelScope.launch {
        val allList : List<CrowdEntity>
        val filteredList : List<CrowdEntity>
        if(item is CrowdEntity) {
            if(searchInputText.value.isNotEmpty()){
                filteredList = communityRoomFilteredList.map { it.clone() }
                filteredList.map { info ->
                    if(info.id == item.id)
                        info.isSelected = !info.isSelected
                }
                communityRoomFilteredList = filteredList.map { it.clone() }.toMutableList()
            }

            allList = localEntities.map { it.clone() }
            allList.map { info ->
                if(info.id == item.id)
                    info.isSelected = !info.isSelected
            }
            localEntities = allList.map { it.clone() }.toMutableList()

            sendQueryList.emit(Pair(searchInputText.value,
                if(searchInputText.value.isNotEmpty()) communityRoomFilteredList else localEntities))
        }
    }

    fun dismissSelectedItem(item: Any) = viewModelScope.launch {
        val allList : List<CrowdEntity>
        val filteredList : List<CrowdEntity>

        if (item is CrowdEntity) {
            if(searchInputText.value.isNotEmpty()){
                filteredList = communityRoomFilteredList.map { it.clone() }
                filteredList.map { info ->
                    if(info.id == item.id)
                        info.isSelected = false
                }
                communityRoomFilteredList = filteredList.map { it.clone() }.toMutableList()
            }

            allList = localEntities.map { it.clone() }
            allList.map { info ->
                if(info.id == item.id)
                    info.isSelected = false
            }
            localEntities = allList.map { it.clone() }.toMutableList()

            sendQueryList.emit(Pair(searchInputText.value,
                if(searchInputText.value.isNotEmpty()) communityRoomFilteredList else localEntities))
        }
    }
}