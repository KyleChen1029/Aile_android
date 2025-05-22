package tw.com.chainsea.ce.sdk.network.model.request

abstract class BaseTokenApplyRequest(open val deviceName: String,
                                     open val deviceType: String,
                                     open val loginMode: String,
                                     open val osType: String,
                                     open val tenantCode: String,
                                     open val uniqueID: String)
