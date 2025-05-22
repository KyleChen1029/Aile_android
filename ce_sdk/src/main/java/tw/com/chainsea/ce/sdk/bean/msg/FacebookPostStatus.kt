package tw.com.chainsea.ce.sdk.bean.msg

enum class FacebookPostStatus(value: String) {
    Update("Update"), Create("Create"), Delete("Delete"), UNDEF("");

    companion object {
        @JvmStatic
        fun of(status: String): FacebookPostStatus {
            if (status == "") {
                return UNDEF
            }
            for (item in FacebookPostStatus.values()) {
                if (item.name == status) {
                    return item
                }
            }
            return UNDEF
        }
    }
}