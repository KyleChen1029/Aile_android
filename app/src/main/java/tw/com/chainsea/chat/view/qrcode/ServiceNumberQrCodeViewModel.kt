package tw.com.chainsea.chat.view.qrcode

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.common.EnableType
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.ce.base.BaseViewModel
import tw.com.chainsea.ce.sdk.http.ce.base.TokenRepository
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.network.selfprofile.SelfProfileRepository
import tw.com.chainsea.chat.util.DownloadUtil
import tw.com.chainsea.chat.view.service.ServiceNumberAgentsManageRepository
import tw.com.chainsea.chat.zxing.encoding.EncodingHandler

class ServiceNumberQrCodeViewModel(
    private val application: Application,
    private val tokenRepository: TokenRepository,
    private val selfProfileRepository: SelfProfileRepository,
    private val serviceNumberAgentsManageRepository: ServiceNumberAgentsManageRepository
) : BaseViewModel(application, tokenRepository) {
    private val tenantName: String by lazy { TokenPref.getInstance(application).cpCurrentTenant.tenantName }
    private val bossServiceNumberEntity: ServiceNumberEntity? by lazy { ServiceNumberReference.findSelfBossServiceNumber() }
    private val officialServiceNumberEntity: ServiceNumberEntity? by lazy {
        DBManager.getInstance().queryOfficialServiceNumber()
    }
    private val selfUserId: String by lazy { TokenPref.getInstance(application).userId }
    private val _qrCodeList = mutableListOf<QrCodeData>()
    private var selfUserProfile: UserProfileEntity? = null
    val qrCodeList = MutableLiveData<List<QrCodeData>>()
    val onSelfProfileDataGet = MutableLiveData<UserProfileEntity>()
    val onDownloadBusinessCardSuccess = MutableLiveData<Boolean>()
    val initStartPosition = MutableLiveData<Boolean>()

    /**
     * 取得個人資訊
     * */
    fun getSelfProfileData() =
        viewModelScope.launch {
            selfProfileRepository.getUserProfile(selfUserId).collect {
                when (it) {
                    is ApiResult.Success -> {
                        selfUserProfile = it.data
                        onSelfProfileDataGet.postValue(it.data)
                    }

                    else -> {
                    }
                }
            }
        }

    /**
     * 取得 qr code
     * */
    fun getQrCodeData() =
        viewModelScope.launch {
            selfUserProfile?.let {
                val bossServiceNumber =
                    bossServiceNumberEntity?.let { bossServiceNumberEntity ->
                        bossServiceNumberEntity
                    } ?: run {
                        val emptyServiceNumberEntity = ServiceNumberEntity()
                        emptyServiceNumberEntity.name =
                            application.getString(R.string.text_qrcode_business_title)
                        emptyServiceNumberEntity.serviceNumberType = ServiceNumberType.BOSS.type
                        emptyServiceNumberEntity.enable = EnableType.N
                        emptyServiceNumberEntity
                    }

                val officialServiceNumber =
                    officialServiceNumberEntity?.let { officialServiceNumberEntity ->
                        officialServiceNumberEntity
                    } ?: run {
                        val emptyServiceNumberEntity = ServiceNumberEntity()
                        emptyServiceNumberEntity.name =
                            application.getString(R.string.text_official_service_number)
                        emptyServiceNumberEntity.serviceNumberType = ServiceNumberType.OFFICIAL.type
                        emptyServiceNumberEntity
                    }

                setEmptyQrCodeData(it, bossServiceNumberEntity, ServiceNumberType.BOSS)
                setEmptyQrCodeData(it, officialServiceNumber, ServiceNumberType.OFFICIAL)
                getQrCodeData(bossServiceNumber, officialServiceNumber)
            }
        }

    /**
     * 設置空資料
     * @param userProfileEntity 個人資料
     * @param serviceNumberEntity 服務號資料
     * */
    private fun setEmptyQrCodeData(
        userProfileEntity: UserProfileEntity,
        serviceNumberEntity: ServiceNumberEntity?,
        serviceNumberType: ServiceNumberType
    ) = viewModelScope.launch {
        val qrCodeData =
            QrCodeData(
                tenantName = tenantName,
                serviceNumber = serviceNumberEntity,
                duty = userProfileEntity.duty,
                isLoading = true,
                serviceNumberType = serviceNumberType
            )
        _qrCodeList.add(qrCodeData)
        qrCodeList.postValue(_qrCodeList.toMutableList())
    }

    /**
     *  取得電子名片資料
     *  @param serviceNumberEntity 服務號資料
     * */
    private fun getBusinessCardData(
        serviceNumberEntity: ServiceNumberEntity
    ) = viewModelScope.async(Dispatchers.IO) {
        if (serviceNumberEntity.serviceNumberId != null) {
            serviceNumberAgentsManageRepository
                .getServiceNumberItem(serviceNumberEntity.serviceNumberId)
                .collect { apiResultData ->
                    when (apiResultData) {
                        is ApiResult.Success -> {
                            _qrCodeList.forEach { qrCodeData ->
                                qrCodeData.serviceNumber?.let {
                                    qrCodeData.isLoading = false
                                    if (it.serviceNumberId == serviceNumberEntity.serviceNumberId) {
                                        apiResultData.data.businessCardInfo?.let { businessCarInfo ->
                                            it.businessCardInfo = businessCarInfo
                                        }
                                        apiResultData.data.allChannelURL?.let { allChannelUrl ->
                                            val qrCode =
                                                EncodingHandler.createQRCode(
                                                    allChannelUrl,
                                                    600,
                                                    600,
                                                    null
                                                )
                                            qrCodeData.qrCodeLink = allChannelUrl
                                            qrCodeData.qrCode = qrCode
                                            it.allChannelURL = allChannelUrl
                                        }
                                        return@forEach
                                    }
                                }
                            }
                        }

                        else -> {
                        }
                    }
                }
        } else {
            _qrCodeList.forEach { qrCodeData ->
                qrCodeData.serviceNumber?.let {
                    if (it.serviceNumberType == ServiceNumberType.BOSS.type) {
                        qrCodeData.isLoading = false
                    }
                }
            }
        }
    }

    private fun getOfficialServiceNumber(serviceNumberEntity: ServiceNumberEntity) =
        viewModelScope.async(Dispatchers.IO) {
            if (serviceNumberEntity.serviceNumberId != null) {
                serviceNumberAgentsManageRepository
                    .getServiceNumberItem(serviceNumberEntity.serviceNumberId)
                    .collect { apiResultData ->
                        when (apiResultData) {
                            is ApiResult.Success -> {
                                _qrCodeList.forEach { qrCodeData ->
                                    qrCodeData.serviceNumber?.let {
                                        if (it.serviceNumberId == serviceNumberEntity.serviceNumberId) {
                                            qrCodeData.isLoading = false
                                            apiResultData.data.businessCardInfo?.let { info ->
                                                it.businessCardInfo = info
                                            }
                                            apiResultData.data.allChannelURL?.let { allChannelUrl ->
                                                val qrCode =
                                                    EncodingHandler.createQRCode(
                                                        allChannelUrl,
                                                        600,
                                                        600,
                                                        null
                                                    )

                                                qrCodeData.qrCodeLink = allChannelUrl
                                                qrCodeData.qrCode = qrCode
                                            }
                                            return@forEach
                                        }
                                    }
                                }
                            }

                            else -> {
                            }
                        }
                    }
            } else {
                _qrCodeList.forEach { qrCodeData ->
                    qrCodeData.serviceNumber?.let {
                        if (it.serviceNumberType == ServiceNumberType.OFFICIAL.type) {
                            qrCodeData.isLoading = false
                        }
                    }
                }
            }
        }

    private fun getQrCodeData(
        bossServiceNumber: ServiceNumberEntity,
        officialServiceNumber: ServiceNumberEntity
    ) = viewModelScope.launch(Dispatchers.IO) {
        getBusinessCardData(bossServiceNumber).await()
        getOfficialServiceNumber(officialServiceNumber).await()
        qrCodeList.postValue(_qrCodeList.toMutableList())
        initStartPosition.postValue(true)
    }

    fun downloadBusinessCard(imageUrl: String) =
        viewModelScope.launch(Dispatchers.IO) {
            DownloadUtil.downloadImageFromUrl(imageUrl, {
                onDownloadBusinessCardSuccess.postValue(true)
            }, {
            })
        }
}
