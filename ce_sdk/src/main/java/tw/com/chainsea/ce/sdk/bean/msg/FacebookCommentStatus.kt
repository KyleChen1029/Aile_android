package tw.com.chainsea.ce.sdk.bean.msg

enum class FacebookCommentStatus(value: String) {
    Update("Update"), Create("Create"), Delete("Delete"), UNDEF("");



    companion object {
        @JvmStatic
        fun of(status: String): FacebookCommentStatus {
            if (status == "") {
                return UNDEF
            }
            for (item in FacebookCommentStatus.values()) {
                if (item.name == status) {
                    return item
                }
            }
            return UNDEF
        }
    }
}