package tw.com.chainsea.chat.view.contact

data class ContactListModel(
    val type: ContactViewHolderType,
    val title: String,
    val sort: Int,
    var data: Any,
    var isOpen: Boolean = false
) {
    fun deepCopy(): ContactListModel =
        ContactListModel(
            type = type,
            title = title,
            sort = sort,
            data = data,
            isOpen = isOpen
        )
}

fun List<ContactListModel>.deepCopy(): MutableList<ContactListModel> =
    this
        .toList()
        .filterNotNull()
        .map { it.deepCopy() }
        .toMutableList()

enum class ContactViewHolderType {
    SELF,
    COLLECTS,
    SUBSCRIBE_SERVICE_NUMBER,
    GROUP,
    EMPLOYEE,
    CUSTOMER,
    BLOCK,
    AIFF
}
