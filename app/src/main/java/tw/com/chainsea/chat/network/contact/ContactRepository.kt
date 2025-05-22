package tw.com.chainsea.chat.network.contact

import android.app.Application
import com.google.common.collect.Lists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.bean.CustomerEntity
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.account.UserType
import tw.com.chainsea.ce.sdk.bean.label.Label
import tw.com.chainsea.ce.sdk.bean.label.LabelRequest
import tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE
import tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.BOSSSERVICENUMBER_CONTACT_LIST
import tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_CONTACT
import tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_EMPLOYEE
import tw.com.chainsea.ce.sdk.database.DBContract.UserProfileEntry
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.ce.request.AddContactFriendRequest
import tw.com.chainsea.ce.sdk.http.ce.response.AddFriendResponse
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.common.BaseRequest
import tw.com.chainsea.ce.sdk.network.model.request.AddressBookCustomFriendInfoRequest
import tw.com.chainsea.ce.sdk.network.model.request.ServiceNumberRoomItemRequest
import tw.com.chainsea.ce.sdk.network.model.request.UserItemRequest
import tw.com.chainsea.ce.sdk.reference.LabelReference
import tw.com.chainsea.ce.sdk.service.type.RefreshSource
import tw.com.chainsea.chat.config.AiffEmbedLocation

class ContactRepository(
    private val contactPersonService: ContactPersonService
) {
    /**
     * 取得我的收藏 (我的最愛)
     * */
    fun getCollectionData(
        labelRequest: LabelRequest,
        source: RefreshSource
    ) = flow {
        emit(ApiResult.Loading(true))
        val db = DBManager.getInstance()

        if (source == RefreshSource.REMOTE) {
            val response = contactPersonService.getCollection(labelRequest)
            response.body()?.let {
                if (it.isReadOnly) LabelReference.save(null, it)
            }
        }

        val collects: MutableList<UserProfileEntity> = Lists.newArrayList()
        val labelList = db.queryAllLabels()
        // 照原本的邏輯
        labelList.forEach { label ->
            if (label.isReadOnly) {
                label.users.forEach { user ->
                    if (user.labels == null) {
                        user.labels = Lists.newArrayList()
                    }
                    user.labels.add(label)
                }
                collects.addAll(label.users)
            }
        }

        emit(ApiResult.Success(collects))
    }.flowOn(Dispatchers.IO).catch { e ->
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 取得我的收藏標籤
     * */
    fun getLabels(
        baseRequest: BaseRequest,
        source: RefreshSource
    ) = flow {
        emit(ApiResult.Loading(true))
        val db = DBManager.getInstance()
        if (source == RefreshSource.REMOTE) {
            val response = contactPersonService.getLabels(baseRequest)
            response.body()?.let {
                it.items?.let { labels ->
                    labels.forEach { label ->
                        val status = LabelReference.save(null, label)
                        if (status) {
                            emit(ApiResult.SaveStatus(true))
                        }
                    }
                }
                DBManager.getInstance().updateOrInsertApiInfoField(REFRESH_TIME_SOURCE.SYNC_LABEL, it.refreshTime)
                // 取得下一頁資料
                it.hasNextPage?.let { has ->
                    emit(ApiResult.NextPage<List<Label>>(has))
                } ?: run {
                    emit(ApiResult.NextPage<List<Label>>(false))
                }
            }
        }
        val labels = db.queryAllLabels().filter { !it.isReadOnly }
        emit(ApiResult.Success(labels))
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getLabels Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 取得訂閱服務號列表
     * */
    fun getSubscribeServiceNumberList(source: RefreshSource = RefreshSource.REMOTE) =
        flow {
            emit(ApiResult.Loading(true))
            val db = DBManager.getInstance()
            val serviceNumList = db.querySubscribeServiceNumber()
            if (source == RefreshSource.REMOTE) {
                val response = contactPersonService.getSubscribeServiceNumberList()
                response.body()?.let {
                    it.items?.let { list ->
                        list.forEach {
                            val status = db.insertServiceNum(it)
                            emit(ApiResult.SaveStatus<Boolean>(status))
                        }
                        // 刪除已經禁用或不存在的 service number
                        val subtractList = serviceNumList - list
                        if (subtractList.isNotEmpty()) {
                            subtractList.forEach { serviceNum ->
                                db.deleteBannedServiceNum(serviceNum.serviceNumberId)
                            }
                        }
                    }
                    // 取得下一頁資料
                    it.hasNextPage?.let { has ->
                        emit(ApiResult.NextPage<List<Label>>(has))
                    } ?: run {
                        emit(ApiResult.NextPage<List<Label>>(false))
                    }
                }
            }
            emit(ApiResult.Success(db.querySubscribeServiceNumber().filter { !it.serviceOpenType.contains("C") }))
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("getSubscribeServiceNumberList Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    /**
     * 取得夥伴列表
     * 這邊有分 friend 跟 employee 要注意
     * friend 有 roomId ; employee 沒有
     * */
    fun getEmployeeList(
        refreshTime: Long,
        userId: String,
        source: RefreshSource
    ) = flow {
        emit(ApiResult.Loading(true))
        val db = DBManager.getInstance()
        val employeeList: List<UserProfileEntity>
        if (source == RefreshSource.REMOTE) {
            val response =
                contactPersonService.getEmployeeList(BaseRequest(refreshTime = refreshTime))
            response.body()?.let { body ->

                db.updateOrInsertApiInfoField(
                    SYNC_EMPLOYEE,
                    body.refreshTime
                )

                body.hasNextPage?.let {
                    emit(ApiResult.NextPage<List<UserProfileEntity>>(it))
                }

                body.items?.let { list ->
                    list.forEach { data ->
                        val status = db.insertFriends(data)
                        if (status) {
                            emit(ApiResult.SaveStatus(true))
                        }
                    }
                }
            }
        }

        employeeList =
            db.queryEmployeeList().filter { userProfileEntity ->
                UserType.EMPLOYEE == userProfileEntity.userType && userProfileEntity.id != userId
            } as ArrayList<UserProfileEntity>
        // 過濾 type 不是 employee 和 自己
        emit(ApiResult.Success(employeeList))
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getEmployeeList Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 取得客戶列表
     * */
    fun getServiceNumberContactList(
        source: RefreshSource,
        selfBossServiceNumberId: String,
        baseRequest: BaseRequest
    ) = flow {
        emit(ApiResult.Loading(true))
        val db = DBManager.getInstance()
        if (source == RefreshSource.REMOTE) {
            val response = contactPersonService.getServiceNumberContactList(baseRequest)
            response.body()?.let { body ->

                db.updateOrInsertApiInfoField(
                    BOSSSERVICENUMBER_CONTACT_LIST,
                    body.refreshTime
                )

                body.hasNextPage?.let {
                    emit(ApiResult.NextPage<List<CustomerEntity>>(it))
                }

                body.items?.let { list ->
                    list.forEach { db.insertCustomer(it) }
                }
            }
        }
        // 過濾訪客
        val customers =
            db.queryCustomers().filter {
                it.userType != "visitor" && it.serviceNumberIds.contains(selfBossServiceNumberId)
            }
        emit(ApiResult.Success(customers))
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getServiceNumberContactList Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 加聯絡人好友
     * */
    fun addContactFriend(addContactFriendRequest: AddContactFriendRequest) =
        flow {
            emit(ApiResult.Loading(true))
            val response = contactPersonService.addContactFriend(addContactFriendRequest)
            response.body()?.let { body ->
                if (body._header_?.success!!) {
                    emit(ApiResult.Success(body))
                } else {
                    emit(
                        ApiResult.Failure<AddFriendResponse>(
                            ApiErrorData(
                                body._header_?.errorMessage!!,
                                body._header_?.errorCode!!
                            )
                        )
                    )
                }
            }
            emit(ApiResult.Loading<AddFriendResponse>(false))
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("addContactFriend Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    /**
     * 加完好友跟 Server 同步並存在資料庫
     * */
    fun syncFriends(source: RefreshSource) =
        flow {
            emit(ApiResult.Loading(true))
            val db = DBManager.getInstance()
            if (source == RefreshSource.REMOTE) {
                val response = contactPersonService.syncFriends()
                response.body()?.let { body ->
                    if (body._header_!!.success!!) {
                        body.items?.let { list ->
                            list.forEach { data ->
                                db.insertFriends(data)
                            }
                        }
                    }
                }
            }
            val queryFriends = db.queryFriends()
            emit(ApiResult.Success(queryFriends))
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("syncFriends Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    /**
     * 取得 aiff List
     * 並過濾出能在聯絡人頁面出現的 aiff
     * */
    fun getAiffList() =
        flow {
            emit(ApiResult.Loading(true))
            val response = contactPersonService.getAiffList()
            response.body()?.let { body ->
                if (body._header_.success!!) {
                    val filterData =
                        body.aiffInfo.filter { info ->
                            info.embedLocation == AiffEmbedLocation.ApplicationList.toString()
                        }
                    emit(ApiResult.Success(filterData))
                }
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("getAiffList Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    fun getCustomerRoom(customerId: String) =
        flow {
            emit(ApiResult.Loading(true))
            val db = DBManager.getInstance()
            val roomEntity = db.queryCustomBossServiceRoom(customerId)
            emit(ApiResult.Success(roomEntity))
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("getCustomerRoom Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    fun getServiceNumberRoom(
        customerId: String,
        application: Application
    ) = flow {
        emit(ApiResult.Loading(true))
        val bossServiceNumberId = TokenPref.getInstance(application.applicationContext).bossServiceNumberId
        val response = contactPersonService.getServiceRoomItem(ServiceNumberRoomItemRequest(bossServiceNumberId, customerId, 1))
        response.body()?.let {
            emit(ApiResult.Success(it))
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getServiceNumberRoom Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * sync/contact 取得所有客戶列表
     * */
    fun syncAllContactList() =
        flow {
            emit(ApiResult.Loading(true))
            val response =
                contactPersonService.getContactList(
                    BaseRequest(DBManager.getInstance().getLastRefreshTime(SYNC_CONTACT))
                )
            response.body()?.let { body ->
                val db = DBManager.getInstance()

                db.updateOrInsertApiInfoField(
                    SYNC_CONTACT,
                    body.refreshTime
                )

                body.hasNextPage?.let {
                    emit(ApiResult.NextPage<List<CustomerEntity>>(it))
                } ?: run {
                    emit(ApiResult.NextPage<List<CustomerEntity>>(false))
                }

                body.items?.let { list ->
                    list.forEach {
                        val status = db.insertCustomer(it)
                        if (status) {
                            emit(ApiResult.SaveStatus(true))
                        }
                    }
                }
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("syncAllContactList Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    fun syncServiceNumber(refreshTime: Long) =
        flow {
            emit(ApiResult.Loading(true))
            val response = contactPersonService.syncServiceNumber(BaseRequest(refreshTime = refreshTime))
            response.body()?.let {
                it.items?.let { list ->
                    list.forEach {
                        val status = DBManager.getInstance().insertServiceNum(it)
                        emit(ApiResult.SaveStatus<Boolean>(status))
                    }
                }
                DBManager.getInstance().updateOrInsertApiInfoField(REFRESH_TIME_SOURCE.SYNC_SERVICENUMBER, it.refreshTime)
                // 取得下一頁資料
                emit(ApiResult.NextPage<List<Label>>(it.isHasNextPage))
                emit(ApiResult.Success(it))
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("syncServiceNumber Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    fun getUserItem(userId: String) =
        flow {
            emit(ApiResult.Loading(true))
            val response = contactPersonService.getUserItem(UserItemRequest(userId))
            response.body()?.let {
                DBManager.getInstance().updateUserField(userId, UserProfileEntry.COLUMN_ROOM_ID, it.personRoomId)
                emit(ApiResult.Success(it))
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("getUserItem Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    fun modifyFriendInfo(
        userId: String,
        name: String
    ) = flow {
        emit(ApiResult.Loading(true))
        val response = contactPersonService.modifyFriendInfo(AddressBookCustomFriendInfoRequest(userId, name))
        response.body()?.let {
            it._header_?.let { header ->
                emit(ApiResult.Success(header.status == "0000"))
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("modifyFriendInfo Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }
}
