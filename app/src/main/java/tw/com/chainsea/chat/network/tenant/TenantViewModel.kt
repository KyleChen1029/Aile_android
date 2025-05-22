package tw.com.chainsea.chat.network.tenant

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.common.collect.Lists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.database.sp.UserPref
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.ce.sdk.http.ce.base.BaseViewModel
import tw.com.chainsea.ce.sdk.http.ce.base.TokenRepository
import tw.com.chainsea.ce.sdk.http.cp.respone.RelationTenant
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.response.Guarantor
import tw.com.chainsea.ce.sdk.socket.cp.model.TenantDeleteMember

/**
 * 團隊相關
 * 要注意有分 Cp Ce
 * */
open class TenantViewModel(
    private val application: Application,
    private val tenantRepository: TenantRepository,
    private val tokenRepository: TokenRepository
) : BaseViewModel(application, tokenRepository) {
    val tenantList = MutableLiveData<List<RelationTenant>>()

    val guarantorList = MutableLiveData<List<Guarantor>>()

    val isAddGuarantorSuccess = MutableLiveData<Boolean>()

    val agreeToBeGuarantor = MutableLiveData<Boolean>()

    val addGuarantorError = MutableLiveData<Boolean>()

    val onChangeTenant = MutableLiveData<RelationTenant>()

    val toCreateOrJoinTenant = MutableLiveData<Boolean>()

    val onTenantAvatarGot = MutableLiveData<Bitmap?>()

    val onTenantAvatarUpdated = MutableLiveData<String>()

    fun getTenantList(isLogin: Boolean = false) =
        viewModelScope.launch {
            checkCpTokenValid(tenantRepository.getTenantList())?.collect {
                when (it) {
                    is ApiResult.Loading -> loading.postValue(it.isLoading)
                    is ApiResult.Failure -> errorMessage.postValue(it.errorMessage.errorMessage)
                    is ApiResult.Success -> {
                        val currentTenant = TokenPref.getInstance(application).cpCurrentTenant
                        TokenPref.getInstance(application).cpRelationTenantList =
                            it.data.relationTenantArray
                        // 創建/加入團隊權限
                        TokenPref.getInstance(application).createTenantPermission = it.data.createTenant ?: -1
                        TokenPref.getInstance(application).joinTenantPermission = it.data.joinTenant ?: -1
                        val map = TokenPref.getInstance(application).tenantList
                        it.data.relationTenantArray.forEach { tenant ->
                            if (isLogin && tenant.isLastLogin) {
                                TokenPref.getInstance(application).cpCurrentTenant = tenant
                                return@forEach
                            }
                            if (map[tenant.tenantId] == null) {
                                map[tenant.tenantId] = true
                            }
                            if (currentTenant.tenantId == tenant.tenantId) {
                                TokenPref.getInstance(application).cpCurrentTenant = tenant
                                return@forEach
                            }
                        }
                        // 儲存所有團隊 紀錄哪幾個團隊有 preload 過
                        TokenPref.getInstance(application).tenantList = map
                        tenantList.postValue(it.data.relationTenantArray)
                        setAppRoundUnreadCount(it.data.relationTenantArray)
                    }

                    else -> {}
                }
            } ?: run {
                tenantList.postValue(TokenPref.getInstance(application).cpRelationTenantList)
            }
        }

    private fun setAppRoundUnreadCount(data: List<RelationTenant>?) {
        val relationTenantList = mutableListOf<RelationTenant>()
        relationTenantList.addAll(data ?: TokenPref.getInstance(application).cpRelationTenantList)
        var allTenantUnreadCount = 0
        relationTenantList.forEach {
            allTenantUnreadCount += it.unReadNum
        }

        UserPref.getInstance(application).brand = allTenantUnreadCount
        EventBus.getDefault().post(EventMsg<Nothing>(MsgConstant.UPDATE_ALL_BADGE_NUMBER_EVENT))
    }

    // 退出團隊
    fun tenantDeleteMember(tenantDeleteMember: TenantDeleteMember) =
        viewModelScope.launch {
            getTenantList()
            // 如果是自己退出就不動作 android退出團隊流程還需要補
            if (tenantDeleteMember.selfOperated) return@launch
            val currentTenant = TokenPref.getInstance(application).cpCurrentTenant
            if (tenantDeleteMember.tenantCode == currentTenant.tenantCode) {
                val tenantList =
                    TokenPref.getInstance(application).cpRelationTenantList.filter { it.tenantCode != tenantDeleteMember.tenantCode }
                if (tenantList.isNotEmpty()) {
                    val relationTenant = tenantList[0]
                    onChangeTenant.postValue(relationTenant)
                } else {
                    TokenPref
                        .getInstance(application)
                        .clearByKey(TokenPref.PreferencesKey.CP_CURRENT_TENANT)
                    // 要回到加入or創建團隊頁面
                    toCreateOrJoinTenant.postValue(true)
                }
            }
        }

    // 取得擔保人列表
    fun getTenantGuarantorList() =
        viewModelScope.launch {
            checkTokenValid(tenantRepository.getTenantGuarantorList())?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        // 沒有少擔保人
                        if (it.data.hadGuarantorCount == it.data.minGuarantorCount) return@collect

                        // 少擔保人
                        if (it.data.minGuarantorCount > it.data.hadGuarantorCount) {
                            it.data.items?.let { guarantor ->
                                guarantorList.postValue(guarantor)
                            } ?: run {
                                guarantorList.postValue(Lists.newArrayList())
                            }
                        }
                    }

                    else -> {}
                }
            } ?: run {
                return@launch
            }
        }

    /**
     * 掃描別人加擔保人
     * @param 擔保人 userId
     */
    fun tenantGuarantorAdd(userId: String) =
        viewModelScope.launch {
            checkTokenValid(tenantRepository.tenantGuarantorAdd(userId))?.collect {
                when (it) {
                    is ApiResult.Failure -> {
                        addGuarantorError.postValue(false)
                    }

                    is ApiResult.Success -> {
                        isAddGuarantorSuccess.postValue(it.data!!)
                    }

                    else -> {}
                }
            }
        }

    /**
     * 被掃描的同意當擔保人
     * @param 需要擔保人的 userId
     * */
    fun tenantGuarantorJoin(userId: String) =
        viewModelScope.launch {
            checkTokenValid(tenantRepository.tenantGuarantorJoin(userId))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        agreeToBeGuarantor.postValue(true)
                    }

                    is ApiResult.Failure -> {
                        errorMessage.postValue(it.errorMessage.errorMessage)
                    }

                    else -> {}
                }
            }
        }

    /**
     * 被掃描的不同意當擔保人
     * @param 需要擔保人的 userId
     * */
    fun tenantGuarantorReject(userId: String) =
        viewModelScope.launch {
            checkTokenValid(tenantRepository.tenantGuarantorReject(userId))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        agreeToBeGuarantor.postValue(false)
                    }

                    is ApiResult.Failure -> {
                        errorMessage.postValue(it.errorMessage.errorMessage)
                    }

                    else -> {}
                }
            }
        }

    fun updateTenantAvatar(
        filePath: String,
        fileName: String,
        size: Int
    ) = viewModelScope.launch(Dispatchers.IO) {
        val tokenId = TokenPref.getInstance(getApplication()).tokenId
        checkTokenValid(
            tenantRepository.updateTenantAvatar(
                getApplication(),
                filePath,
                fileName,
                size,
                tokenId
            )
        )?.collect {
            when (it) {
                is ApiResult.Success -> {
                    val avatarUrl: String = it.data.optString("avatarUrl")
                    onTenantAvatarUpdated.postValue(avatarUrl)
                }

                else -> {
                    // nothing
                }
            }
        }
    }

    fun getTenantAvatar(avatarId: String?) =
        viewModelScope.launch(Dispatchers.IO) {
            avatarId?.let {
                checkCpTokenValid(tenantRepository.getTenantAvatarUrl(avatarId))?.collect {
                    when (it) {
                        is ApiResult.Success -> {
                            onTenantAvatarGot.postValue(it.data)
                        }

                        is ApiResult.Failure -> {
                            onTenantAvatarGot.postValue(null)
                            CELog.e(it.errorMessage.errorMessage)
                        }

                        else -> {
                            // nothing
                        }
                    }
                }
            } ?: run {
                onTenantAvatarGot.postValue(null)
            }
        }
}
