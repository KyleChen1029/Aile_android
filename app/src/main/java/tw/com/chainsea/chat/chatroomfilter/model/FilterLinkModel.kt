package tw.com.chainsea.chat.chatroomfilter.model

import android.graphics.Bitmap
import tw.com.chainsea.chat.chatroomfilter.BaseFilterModel

data class FilterLinkModel(
    var url: String = "",
    var title: String = "",
    var image: Bitmap? = null,
    var imageUrl: String = "",
) : BaseFilterModel()