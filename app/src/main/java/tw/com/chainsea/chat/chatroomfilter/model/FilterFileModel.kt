package tw.com.chainsea.chat.chatroomfilter.model

import tw.com.chainsea.chat.chatroomfilter.BaseFilterModel

data class FilterFileModel(
    var fileUrl: String = "",
    var fileName: String= "",
    var fileIcon: Int = -1
): BaseFilterModel()