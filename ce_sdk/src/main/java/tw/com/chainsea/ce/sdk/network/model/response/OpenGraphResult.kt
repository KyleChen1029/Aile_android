package tw.com.chainsea.ce.sdk.network.model.response

import android.graphics.Bitmap

data class OpenGraphResult(
    var title: String = "",
    var url: String = "",
    var image: Bitmap? = null,
    var imageUrl: String = ""
)