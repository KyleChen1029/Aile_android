package tw.com.chainsea.ce.sdk.network.model.request

data class BindThirdPartRequest(
    val fansPageId: String,
    val name: String,
    val fansPageAccessToken: String,
    val fansAvatarURL: String?
)