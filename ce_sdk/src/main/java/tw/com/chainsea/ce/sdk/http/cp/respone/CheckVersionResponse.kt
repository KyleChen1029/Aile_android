package tw.com.chainsea.ce.sdk.http.cp.respone

data class CheckVersionResponse(
    var url: String? = "",
    val version: String = "",
    val fileName: String = "",
    val versionName: String = "",
    val isToUpdate: Boolean = false,
    val upgradeKind: String = "",
    val description: String = "",
    val size: Int = 0
)
