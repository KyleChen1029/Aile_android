package tw.com.chainsea.ce.sdk.bean

data class BadgeDataModel(
    var unReadNumber: Int = 0, var roomId: String = ""
) {
    constructor() : this(0, "")
}