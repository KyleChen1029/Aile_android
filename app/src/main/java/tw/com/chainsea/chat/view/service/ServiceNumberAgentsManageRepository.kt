package tw.com.chainsea.chat.view.service

import com.google.common.collect.Lists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberChatRoomAgentServicedRequest
import tw.com.chainsea.ce.sdk.bean.servicenumber.StartServiceNUmberConsultAiRequest
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.ServiceNumberPrivilege
import tw.com.chainsea.ce.sdk.http.cp.respone.RelationTenant
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse
import tw.com.chainsea.ce.sdk.network.model.request.GetServiceItemRequest
import tw.com.chainsea.ce.sdk.network.model.request.ServiceManagementRequest
import tw.com.chainsea.ce.sdk.network.model.request.ServiceModifyOwnerRequest
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference
import tw.com.chainsea.ce.sdk.service.ManagementPermissionService
import tw.com.chainsea.chat.util.SortUtil

class ServiceNumberAgentsManageRepository(private val managementService: ManagementPermissionService) {


    /**
     * 判斷是否是團隊擁有者或是管理員
     * */
    fun getIsTenantManager(currentTenant: RelationTenant?, selfUserId: String) = flow {
        currentTenant?.let { tenant ->
            tenant.manageServiceNumberInfo?.let { serviceNumberInfo ->
                getServiceNumberItem(serviceNumberInfo.id).collect {
                    if (it is ApiResult.Success) {
                        var isTenantManager = false
                        it.data.memberItems?.let { memberItems ->
                            memberItems.forEach {
                                if (it.id == selfUserId) {
                                    isTenantManager = it.privilege == ServiceNumberPrivilege.OWNER || it.privilege == ServiceNumberPrivilege.MANAGER
                                    emit(isTenantManager)
                                    return@forEach
                                }
                            }
                        }
                        if (!isTenantManager) {
                            emit(false)
                        }
                    }
                }
            }
        }?: run {
            emit(false)
        }
    }.flowOn(Dispatchers.IO).catch { e ->

    }

    /**
     * 取得 Service Number 資訊
     * 先看本地有沒有 沒有再打 api
     * */
    fun getServiceEntity(serviceNumberId: String) = flow {
        emit(ApiResult.Loading(true))
        getServiceEntityFromRemote(serviceNumberId).collect{
            if (it is ApiResult.Success) {
                val sortedList = SortUtil.sortOwnerManagerByPrivilege(it.data)
                emit(ApiResult.Success(sortedList))
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }


    /**
     * 直接從 api 取得 Service Number 資訊
     * */
    fun getServiceEntityFromRemote(serviceNumberId: String) = flow {
        val response =
            managementService.getServiceNumberItem(GetServiceItemRequest(serviceNumberId))
        response.body()?.let {
            val sortedList = SortUtil.sortOwnerManagerByPrivilege(it.memberItems)
            emit(ApiResult.Success(sortedList))
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getServiceEntityFromRemote Error", e)
    }


    /**
     * 移除服務人員
     * */
    fun removeAgent(serviceNumberId: String, agentId: String) = flow {
        emit(ApiResult.Loading<Boolean>(true))
        val response = managementService.removeMember(
            ServiceManagementRequest(
                serviceNumberId, Lists.newArrayList(agentId)
            )
        )
        response.body()?.let {
            emit(ApiResult.Loading(false))
            it._header_?.let {header ->
                if (header.success!!) {
                    emit(ApiResult.Success(it))
                } else {
                    emit(ApiResult.Failure<CommonResponse<Any>>(ApiErrorData(header.errorMessage!!, header.errorCode!!)))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }


    /**
     * 移除管理權限
     * */
    fun removeManagement(serviceNumberId: String, agentId: String) = flow {
        emit(ApiResult.Loading(true))
        val response = managementService.deleteManager(
            ServiceManagementRequest(
                serviceNumberId, Lists.newArrayList(agentId)
            )
        )
        response.body()?.let {
            emit(ApiResult.Loading(false))
            it._header_?.let {header ->
                if (header.success!!) {
                    emit(ApiResult.Success(it))
                } else {
                    emit(ApiResult.Failure<CommonResponse<Any>>(ApiErrorData(header.errorMessage!!, header.errorCode!!)))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }

    /**
     * 增加管理者
     * */
    fun addManagement(serviceNumberId: String, agentId: String) = flow {
        emit(ApiResult.Loading(true))
        val response = managementService.addManager(
            ServiceManagementRequest(
                serviceNumberId, Lists.newArrayList(agentId)
            )
        )
        response.body()?.let {
            emit(ApiResult.Loading(false))
            it._header_?.let {header ->
                if (header.success!!) {
                    emit(ApiResult.Success(it))
                } else {
                    emit(ApiResult.Failure<CommonResponse<Any>>(ApiErrorData(header.errorMessage!!, header.errorCode!!)))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }


    /**
     * 轉移擁有者權限
     * */
    fun modifyOwner(serviceNumberId: String, agentId: String) = flow {
        emit(ApiResult.Loading(true))
        val response = managementService.modifyOwner(
            ServiceModifyOwnerRequest(serviceNumberId, agentId)
        )
        response.body()?.let {
            emit(ApiResult.Loading(false))
            it._header_?.let {header ->
                if (header.success!!) {
                    emit(ApiResult.Success(it))
                } else {
                    emit(ApiResult.Failure<CommonResponse<Any>>(ApiErrorData(header.errorMessage!!, header.errorCode!!)))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }
    /**
     * 取得諮詢服務號列表
     * */
    fun getServiceNumberConsultationList() = flow {
        emit(ApiResult.Loading(true))
        val response = managementService.getServiceNumberConsultationList()
        response.body()?.let {
            it.items?.let { list ->
                emit(ApiResult.Success(list))
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }

    /**
     * 取得服務號資訊
     * */
    fun getServiceNumberItem(serviceNumberId: String) = flow {
        emit(ApiResult.Loading(true))
        val response = managementService.getServiceNumberItem(GetServiceItemRequest(serviceNumberId))
        response.body()?.let {
            emit(ApiResult.Success(it))
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }
    /**
     * 取得是否有服務人員服務中
     * */
    fun getServiceNumberChatRoomAgentServicedInfo(roomId: String) = flow {
        emit(ApiResult.Loading(true))
        val response = managementService.getServiceNumberChatRoomAgentServicedInfo(
            ServiceNumberChatRoomAgentServicedRequest(roomId)
        )
        response.body()?.let {
            emit(ApiResult.Success(it))
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }

    fun doStartServiceNUmberConsultAi(srcRoomId: String) = flow {
        emit(ApiResult.Loading(true))
        val response = managementService.doStartServiceNUmberConsultAi(
            StartServiceNUmberConsultAiRequest(srcRoomId)
        )
        response.body()?.let {
            it.consultId?.let { consultId ->
                emit(ApiResult.Success(consultId))
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }
}