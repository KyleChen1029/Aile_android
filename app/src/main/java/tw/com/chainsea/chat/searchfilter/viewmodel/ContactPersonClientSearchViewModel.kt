package tw.com.chainsea.chat.searchfilter.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.collect.Lists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tw.com.aile.sdk.bean.filter.FilterRoomType
import tw.com.aile.sdk.bean.filter.FilterTab
import tw.com.aile.sdk.bean.filter.SearchFilterEntity
import tw.com.chainsea.ce.sdk.SdkLib
import tw.com.chainsea.ce.sdk.bean.CustomerEntity
import tw.com.chainsea.ce.sdk.bean.GroupEntity
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.account.UserType
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.ce.ApiManager
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener
import tw.com.chainsea.ce.sdk.service.UserProfileService
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack
import tw.com.chainsea.ce.sdk.service.type.RefreshSource
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.searchfilter.repository.ContactPersonClientRepository

class ContactPersonClientSearchViewModel(
    private val application: Application,
    private val contactPersonClientRepository: ContactPersonClientRepository
) : ViewModel() {

    val employeeCustomerFilterList = MutableSharedFlow<Triple<String, MutableList<SearchFilterEntity>, Int>>()
    var employees: MutableList<UserProfileEntity> = arrayListOf() //for all employees
    private var employeeCustomerFilteredList: MutableList<SearchFilterEntity> = arrayListOf()// for filtered data
    private var employeeCustomerList: MutableList<SearchFilterEntity> = arrayListOf()// for all data
    private var customers: MutableList<CustomerEntity> = arrayListOf() //for all customers
    val navigateToChatRoom = MutableSharedFlow<Pair<String, Any>>()
    val navigateToCustomerChatRoom = MutableSharedFlow<Triple<String, ChatRoomEntity, CustomerEntity>>()
    // 跟 navigateToCustomerChatRoom 一樣，只是給 java 用
    val toCustomerChatRoom = MutableLiveData<String>()
    val toCustomerChatRoomError = MutableLiveData<String>()

    val navigateToCustomerChatRoomFailure = MutableSharedFlow<String>()
    private val searchKeyWord = MutableStateFlow("")
    private var localAllGroupList: MutableList<GroupEntity> = mutableListOf()
    val sendContactPersonSelectedItem = MutableSharedFlow<Triple<String, Any, Int>>()
    val sendSelectedItem = MutableSharedFlow<Pair<String, MutableList<SearchFilterEntity>>>()
    fun initData(userId: String) = viewModelScope.launch(Dispatchers.IO) {

        contactPersonClientRepository.queryAllGroupRoom().onEach {
            localAllGroupList.clear()
            localAllGroupList.addAll(it)
        }.collect()

        UserProfileService.getEmployeeProfile(
            application,
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
                        showAllData()
                    }
                }
            })
    }

    private fun checkFilterCondition(filter: String, item: Any) : Boolean {
        return when (item) {
            is UserProfileEntity -> {
                item.nickName?.contains(filter, true) == true || item.name?.contains(
                    filter,
                    true
                ) == true || item.alias?.contains(filter, true) == true
            }
            is CustomerEntity -> {
                (item.nickName?.contains(filter, true) == true || item.name?.contains(
                    filter,
                    true
                ) == true || item.customerName?.contains(filter, true) == true)
            }
            is GroupEntity -> {
                //過濾社團聊天室名稱
                item.name.contains(filter, true)
            }
            else -> {
                false
            }
        }
    }
    /**
     * 轉發分享的過濾
     **/
    fun filter(s: String) = viewModelScope.launch(Dispatchers.IO) {
        searchKeyWord.value = s
        val employeesList: MutableList<UserProfileEntity> = mutableListOf()
        val groupList: MutableList<GroupEntity> = mutableListOf()
        if(s.isNotEmpty()) {
            employeeCustomerFilteredList.clear()
            for (entity in employees) {
                if (checkFilterCondition(s, entity))
                    employeesList.add(entity)
            }
            for(entity in localAllGroupList) {
                if(checkFilterCondition(s, entity))
                    groupList.add(entity)
            }
            if(groupList.isNotEmpty()) {
                employeeCustomerFilteredList.add(
                    SearchFilterEntity(
                        application.getString(R.string.text_sectioned_communities_filter, groupList.size),
                        FilterRoomType.GROUP,
                        FilterTab.CONTACT_PERSON,
                        groupList
                    )
                )
            }
            if(employeesList.isNotEmpty()) {
                employeeCustomerFilteredList.add(
                    SearchFilterEntity(
                        application.getString(R.string.text_sectioned_contact_person, employeesList.size),
                        FilterRoomType.EMPLOYEE,
                        FilterTab.CONTACT_PERSON,
                        employeesList
                    )
                )
            }
        }
        employeeCustomerFilterList.emit(Triple(s, if(s.isNotEmpty()) employeeCustomerFilteredList else Lists.newArrayList(), employeesList.size + groupList.size))
    }
    /**
     * 全局搜尋的過濾
     **/
    fun globalFilter(s: String, userId: String) = viewModelScope.launch(Dispatchers.IO) {
        searchKeyWord.value = s
        val employeesList: MutableList<UserProfileEntity> = mutableListOf()
        val customersList: MutableList<CustomerEntity> = mutableListOf()
        val groupList: MutableList<GroupEntity> = mutableListOf()
        if(s.isNotEmpty()) {
            employeeCustomerFilteredList.clear()

            contactPersonClientRepository.queryUsersByName(s, userId).onEach {
                employeesList.addAll(it)
            }.collect()

            contactPersonClientRepository.queryCustomersByName(s).onEach {
                customersList.addAll(
                    it.filter { entity ->
                        entity.serviceNumberIds.contains(TokenPref.getInstance(application).bossServiceNumberId)
                    }
                )
            }.collect()

            contactPersonClientRepository.queryAllGroupRoomByName(s).onEach {
                groupList.addAll(it)
            }.collect()

            if(groupList.isNotEmpty()) {
                employeeCustomerFilteredList.add(
                    SearchFilterEntity(
                        application.getString(R.string.text_sectioned_communities_filter, groupList.size),
                        FilterRoomType.GROUP,
                        FilterTab.CONTACT_PERSON,
                        groupList
                    )
                )
            }
            if(employeesList.isNotEmpty()) {
                employeeCustomerFilteredList.add(
                    SearchFilterEntity(
                        application.getString(R.string.text_sectioned_contact_person, employeesList.size),
                        FilterRoomType.EMPLOYEE,
                        FilterTab.CONTACT_PERSON,
                        employeesList
                    )
                )
            }
            if(customersList.isNotEmpty()) {
                employeeCustomerFilteredList.add(
                    SearchFilterEntity(
                        application.getString(R.string.text_sectioned_customers, customersList.size),
                        FilterRoomType.CUSTOMER,
                        FilterTab.CONTACT_PERSON,
                        customersList
                    )
                )
            }
        }
        employeeCustomerFilterList.emit(Triple(s, if(s.isNotEmpty()) employeeCustomerFilteredList else Lists.newArrayList(), employeesList.size + customersList.size + groupList.size))
    }

    fun onSelectContactPersonCustomerItem(item: Any) = viewModelScope.launch(Dispatchers.IO) {
        when(item) {
            is UserProfileEntity -> navigateToChatRoom.emit(Pair(searchKeyWord.value, item))
            is CustomerEntity -> {
                ApiManager.doServiceRoomItem(
                    SdkLib.getAppContext(),
                    TokenPref.getInstance(SdkLib.getAppContext()).bossServiceNumberId,
                    item.id,
                    1,
                    "",
                    UserType.CONTACT.name.lowercase(),
                    object : ApiListener<ChatRoomEntity> {
                        override fun onSuccess(entity: ChatRoomEntity) {
                            viewModelScope.launch {
                                navigateToCustomerChatRoom.emit(Triple(searchKeyWord.value, entity, item))
                            }
                        }

                        override fun onFailed(errorMessage: String) {
                            viewModelScope.launch {
                                navigateToCustomerChatRoomFailure.emit(errorMessage)
                            }
                        }
                    })
            }
            is GroupEntity -> {
                contactPersonClientRepository.queryGroupRoom(item.id).onEach {
                    it?.let {
                        navigateToChatRoom.emit(Pair(searchKeyWord.value, it))
                    }?: run {
                        navigateToChatRoom.emit(Pair(searchKeyWord.value, item))
                    }
                }.collect()
            }
        }
    }

    fun getServiceCustomerRoomId(serviceNumberId: String, customerId: String) = viewModelScope.launch(Dispatchers.IO) {
        ApiManager.doServiceRoomItem(SdkLib.getAppContext(),
            serviceNumberId,
            customerId,
            1,
            "",
            UserType.CONTACT.name.lowercase(),
            object : ApiListener<ChatRoomEntity> {
                override fun onSuccess(entity: ChatRoomEntity) {
                    viewModelScope.launch {
                       toCustomerChatRoom.postValue(entity.id)
                    }
                }

                override fun onFailed(errorMessage: String) {
                    viewModelScope.launch {
                        toCustomerChatRoomError.postValue(errorMessage)
                    }
                }
            })
    }

    fun onSelectChatRoomItem(item: GroupEntity) = viewModelScope.launch(Dispatchers.IO) {
        navigateToChatRoom.emit(Pair(searchKeyWord.value, item))
    }

    fun showAllData() = viewModelScope.launch(Dispatchers.IO) {
        employeeCustomerFilteredList.clear()
        if(localAllGroupList.isNotEmpty()) {
            employeeCustomerList.add(
                SearchFilterEntity(
                    application.getString(R.string.text_sectioned_communities_filter, localAllGroupList.size),
                    FilterRoomType.GROUP,
                    FilterTab.CONTACT_PERSON,
                    localAllGroupList
                )
            )
        }
        if(employees.isNotEmpty()) {
            employeeCustomerList.add(
                SearchFilterEntity(
                    application.getString(R.string.text_sectioned_contact_person, employees.size),
                    FilterRoomType.EMPLOYEE,
                    FilterTab.CONTACT_PERSON,
                    employees
                )
            )
        }
        employeeCustomerFilterList.emit(Triple("", employeeCustomerList, -1)) // -1表示不顯示統計數字
    }
    fun onSelectedItem(item: Any, position: Int) = viewModelScope.launch(Dispatchers.IO) {
        sendContactPersonSelectedItem.emit(Triple(searchKeyWord.value, item, position))
    }
    fun updateSelectedItemList(item: Any) = viewModelScope.launch(Dispatchers.IO) {
        val filteredList : List<SearchFilterEntity>
        val allList : List<SearchFilterEntity>

        if(item is UserProfileEntity) {
            if(searchKeyWord.value.isNotEmpty()) {
                filteredList = employeeCustomerFilteredList.map { it }
                filteredList.map { info ->
                    if(info.roomType == FilterRoomType.EMPLOYEE && info.data is UserProfileEntity) {
                        info.data.map { t ->
                            if((t as UserProfileEntity).id == item.id)
                                t.isSelected = !item.isSelected
                        }
                    }
                }
                employeeCustomerFilteredList = filteredList.map { it }.toMutableList()
            }

            allList = employeeCustomerList.map { it }
            allList.forEach { info ->
                if(info.roomType == FilterRoomType.EMPLOYEE) {
                    info.data.map { t ->
                        if((t as UserProfileEntity).id == item.id) {
                            t.isSelected = !item.isSelected
                        }
                    }
                }
            }

            employeeCustomerList = allList.map { it }.toMutableList()

            sendSelectedItem.emit(Pair(searchKeyWord.value, if(searchKeyWord.value.isNotEmpty()) employeeCustomerFilteredList else employeeCustomerList))

        } else if(item is GroupEntity) {
            if(searchKeyWord.value.isNotEmpty()) {
                filteredList = employeeCustomerFilteredList.map { it }
                filteredList.forEach { info ->
                    if(info.roomType == FilterRoomType.GROUP) {
                        info.data.map { t ->
                            if((t as GroupEntity).id == item.id) {
                                t.isSelected = !item.isSelected
                            }
                        }
                    }
                }
                employeeCustomerFilteredList = filteredList.map { it }.toMutableList()
            }

            allList = employeeCustomerList.map { it }
            allList.forEach { info ->
                if(info.roomType == FilterRoomType.GROUP) {
                    info.data.map { t ->
                        if((t as GroupEntity).id == item.id) {
                            t.isSelected = !item.isSelected
                        }
                    }
                }
            }

            employeeCustomerList = allList.map { it }.toMutableList()

            sendSelectedItem.emit(Pair(searchKeyWord.value, if(searchKeyWord.value.isNotEmpty()) employeeCustomerFilteredList else employeeCustomerList))

        }
    }

    fun dismissSelectedItem(item: Any) = viewModelScope.launch(Dispatchers.IO) {
        val allList : List<SearchFilterEntity>
        val filteredList : List<SearchFilterEntity>
        when(item) {
            is UserProfileEntity -> {
                if(searchKeyWord.value.isNotEmpty()) {
                    filteredList = employeeCustomerFilteredList.map { it }
                    filteredList.map { info ->
                        if(info.data is UserProfileEntity) {
                            info.data.map { t ->
                                if((t as UserProfileEntity).id == item.id)
                                    t.isSelected = false
                            }
                        }
                    }
                    employeeCustomerFilteredList = filteredList.map { it }.toMutableList()
                }

                allList = employeeCustomerList.map { it }
                allList.forEach { info ->
                    if(info.roomType == FilterRoomType.EMPLOYEE) {
                        info.data.map { t ->
                            if((t as UserProfileEntity).id == item.id) {
                                t.isSelected = false
                            }
                        }
                    }
                }

                employeeCustomerList = allList.map { it }.toMutableList()
            }
            is GroupEntity -> {
                if(searchKeyWord.value.isNotEmpty()) {
                    filteredList = employeeCustomerFilteredList.map { it }
                    filteredList.forEach { info ->
                        if(info.roomType == FilterRoomType.GROUP) {
                            info.data.map { t ->
                                if((t as GroupEntity).id == item.id)
                                    t.isSelected = false
                            }
                        }
                    }
                    employeeCustomerFilteredList = filteredList.map { it }.toMutableList()
                }

                allList = employeeCustomerList.map { it }
                allList.forEach { info ->
                    if(info.roomType == FilterRoomType.GROUP) {
                        info.data.map { t ->
                            if((t as GroupEntity).id == item.id) {
                                t.isSelected = false
                            }
                        }
                    }
                }

                employeeCustomerList = allList.map { it }.toMutableList()
            }
        }
        sendSelectedItem.emit(Pair(searchKeyWord.value, if(searchKeyWord.value.isNotEmpty()) employeeCustomerFilteredList else employeeCustomerList))
    }
}