package tw.com.chainsea.ce.sdk.network.model.response

import com.google.gson.annotations.SerializedName

data class FansPageModel(
    val id: String,
    var name: String,
    val access_token: String,
    val picture: FansPagePicture?,
    @SerializedName("instagram_business_account")
    val instagramFansPageModel: InstagramFansPageModel?,
    var isSelected: Boolean = false
)

data class InstagramFansPageModel(
    val id: String,
    val name: String,
    val username: String?
)


data class FansPagePicture(
    val data: FansPagePictureData
)

data class FansPagePictureData(
    val url: String
)

