package tw.com.chainsea.ce.sdk.network.model.request

class SelfProfileRequest {

    data class UpdateProfile(val userId:String, val mobileVisible: Boolean)
}