package tw.com.chainsea.ce.sdk.http.ce.request

import java.io.Serializable

data class ActiveServiceConsultArray(
    val consultServiceNumberId: String,
    val consultRoomId: String,
    val serviceNumberAvatarId: String?
): Serializable
