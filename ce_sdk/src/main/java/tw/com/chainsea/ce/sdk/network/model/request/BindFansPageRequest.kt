package tw.com.chainsea.ce.sdk.network.model.request

data class BindFansPageRequest(
    val serviceNumberId: String,
    val faceBookFansPages: List<BindThirdPartRequest> = mutableListOf(),
    val instagramFansPages:List<BindThirdPartRequest> = mutableListOf()
)


