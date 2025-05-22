package tw.com.chainsea.chat.searchfilter.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.collect.Lists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.account.UserType
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.service.UserProfileService
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack
import tw.com.chainsea.ce.sdk.service.type.RefreshSource

class ContactPersonViewModel : ViewModel() {
    val employeeList = MutableSharedFlow<Pair<String, MutableList<UserProfileEntity>>>()
    val employeeFilterList = MutableSharedFlow<Pair<String, List<UserProfileEntity>>>()
    val sendOwnerSelectedItem = MutableSharedFlow<UserProfileEntity>()
    val sendEmployeeSelectedItem = MutableSharedFlow<Triple<String, UserProfileEntity, Int>>()
    val employeesTextRecordList = MutableSharedFlow<String>()
    private val allEmployeeList = MutableStateFlow(listOf<UserProfileEntity>())
    var employees: MutableList<UserProfileEntity> = arrayListOf() //for all employees
    private var employeeFilteredList: MutableList<UserProfileEntity> =
        arrayListOf()// for filtered employees
    val searchInputText = MutableStateFlow("")
    private var ownerId = ""
    fun initPartnerData(context: Context, userId: String, memberIds: MutableList<String>, serviceNumberId: String, discussRoomMember: MutableList<String>) = viewModelScope.launch(Dispatchers.IO) {
        ownerId = userId
        UserProfileService.getEmployeeProfile(
            context,
            RefreshSource.REMOTE,
            object : ServiceCallBack<List<UserProfileEntity>, RefreshSource?> {
                override fun error(message: String?) {}

                override fun complete(t: List<UserProfileEntity>?, e: RefreshSource?) {
                    t?.let {
                        for (entity in t) {
                            if ((!entity.isBlock
                                        && entity.id != userId) && UserType.EMPLOYEE.name == entity.userType.toString()
                                    .uppercase()
                            )
                                employees.add(entity)
                        }
                    }
                    if(serviceNumberId.isNotEmpty()) {
                        val serviceNumber = DBManager.getInstance().queryServiceNumberById(serviceNumberId)
                        val ids: MutableList<String> = Lists.newArrayList()
                        serviceNumber.getMemberItems()?.let { m ->
                            for (id in m) {
                                id.id?.let {
                                    ids.add(it)
                                } ?: run {
                                    id.id?.let { ids.add(it) }
                                }
                            }
                            memberIds.addAll(ids)
                        }
                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        memberIds.addAll(discussRoomMember)
                        if(memberIds.isNotEmpty()) {
                            //排除已存在的臨時成員名單 , 已經在諮詢的成員, 多人聊天室預設成員
                            employees = employees.filterNot { m ->
                                memberIds.contains(m.id)
                            } as MutableList<UserProfileEntity>
                            allEmployeeList.value = employees
                        }else
                            allEmployeeList.value = employees
                        employeeList.emit(Pair(searchInputText.value, allEmployeeList.value.toMutableList()))
                    }
                }
            })
    }

    fun addGroupRoomOwner(ownerId: String) = viewModelScope.launch(Dispatchers.IO) {
        val ownerItem = DBManager.getInstance().queryFriend(ownerId)
        sendOwnerSelectedItem.emit(ownerItem)
    }

    fun addDiscussRoomMember(memberIds: MutableList<String>) = viewModelScope.launch(Dispatchers.IO) {
        memberIds.forEach {
            val member = DBManager.getInstance().queryFriend(it)
            sendOwnerSelectedItem.emit(member)
        }
    }
    fun onSelectEmployeeItem(item: UserProfileEntity, position: Int) = viewModelScope.launch {
        sendEmployeeSelectedItem.emit(Triple(searchInputText.value, item, position))
    }

    fun addEmployeeItem(item: UserProfileEntity) = viewModelScope.launch {
        if (!employees.contains(item))
            employees.add(item)

        allEmployeeList.value = employees //when add/delete refresh all employees info

        if (searchInputText.value.isNotEmpty()) {
            if (checkFilterCondition(searchInputText.value, item)) {
                employeeFilteredList.add(item)
                employeeList.emit(Pair(searchInputText.value, employeeFilteredList))
            }
        } else {
            employeeList.emit(Pair(searchInputText.value, employees))
        }
    }

    private fun checkFilterCondition(filter: String, item: UserProfileEntity): Boolean {
        return item.nickName.contains(filter, true)
    }

    fun filter(s: String) = viewModelScope.launch {
        searchInputText.value = s
        if(s.isNotEmpty()) {
            employeeFilteredList.clear()
            for (entity in employees) {
                if (checkFilterCondition(s, entity))
                    employeeFilteredList.add(entity)
            }
        }
        employeeFilterList.emit(Pair(s, if(s.isNotEmpty()) employeeFilteredList else employees))
    }

    fun updateSelectedItemList(item: Any) = viewModelScope.launch {
        val allList : List<UserProfileEntity>
        val filteredList : List<UserProfileEntity>

        if(item is UserProfileEntity) {
            if(searchInputText.value.isNotEmpty()) {
                filteredList = employeeFilteredList.map { it.clone() }
                filteredList.map { info ->
                    if(info.id == item.id)
                        info.isSelected = !info.isSelected
                }
                employeeFilteredList = filteredList.map { it.clone() }.toMutableList()
            }

            allList = employees.map { it.clone() }
            allList.map { info ->
                if(info.id == item.id)
                    info.isSelected = !info.isSelected
            }

            employees = allList.map { it.clone() }.toMutableList()

            employeeList.emit(Pair(searchInputText.value,
                if(searchInputText.value.isNotEmpty()) employeeFilteredList else employees))

        }else if(item is ChatRoomEntity) {
            if(searchInputText.value.isNotEmpty()) {
                filteredList = employeeFilteredList.map { it.clone() }
                filteredList.map { info ->
                    if(info.id == item.memberIds.filterNot { it == ownerId }.firstOrNull())
                        info.isSelected = !info.isSelected
                }
                employeeFilteredList = filteredList.map { it.clone() }.toMutableList()
            }

            allList = employees.map { it.clone() }
            allList.map { info ->
                if(info.id == item.memberIds.filterNot { it == ownerId }.firstOrNull())
                    info.isSelected = !info.isSelected
            }

            employees = allList.map { it.clone() }.toMutableList()

            employeeList.emit(Pair(searchInputText.value,
                if(searchInputText.value.isNotEmpty()) employeeFilteredList else employees))
        }
    }

    fun dismissSelectedItem(item: Any) = viewModelScope.launch {
        val allList : List<UserProfileEntity>
        var filteredList : List<UserProfileEntity> = listOf()

        if(item is UserProfileEntity) {
            if(searchInputText.value.isNotEmpty()) {
                filteredList = employeeFilteredList.map { it.clone() }
                filteredList.map { info ->
                    if(info.id == item.id)
                        info.isSelected = false
                }
            }

            allList = employees.map { it.clone() }
            allList.map { info ->
                if(info.id == item.id)
                    info.isSelected = false
            }

            if(searchInputText.value.isNotEmpty())
                employeeFilteredList = filteredList.map { it.clone() }.toMutableList()
            employees = allList.map { it.clone() }.toMutableList()

            employeeList.emit(Pair(searchInputText.value,
                if(searchInputText.value.isNotEmpty()) employeeFilteredList else employees))

        } else if(item is ChatRoomEntity) {
            if(searchInputText.value.isNotEmpty()) {
                filteredList = employeeFilteredList.map { it.clone() }
                filteredList.map { info ->
                    if(info.id == item.memberIds.filterNot { it == ownerId }.firstOrNull())
                        info.isSelected = false
                }
            }

            allList = employees.map { it.clone() }
            allList.map { info ->
                if(info.id == item.memberIds.filterNot { it == ownerId }.firstOrNull())
                    info.isSelected = false
            }

            if(searchInputText.value.isNotEmpty())
                employeeFilteredList = filteredList.map { it.clone() }.toMutableList()
            employees = allList.map { it.clone() }.toMutableList()

            employeeList.emit(Pair(searchInputText.value,
                if(searchInputText.value.isNotEmpty()) employeeFilteredList else employees))
        }
    }
}
