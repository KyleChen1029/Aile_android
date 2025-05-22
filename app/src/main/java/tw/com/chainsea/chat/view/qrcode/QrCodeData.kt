package tw.com.chainsea.chat.view.qrcode

import android.graphics.Bitmap
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType
import tw.com.chainsea.ce.sdk.bean.servicenumber.BusinessCardInfo
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity

data class QrCodeData(
    val tenantName: String = "",
    var serviceNumber: ServiceNumberEntity? = null,
    var serviceNumberType: ServiceNumberType,
    val duty: String = "",
    var qrCodeLink: String = "",
    var qrCode: Bitmap? = null,
    var businessCardInfo: BusinessCardInfo? = null,
    var isLoading: Boolean = true
)