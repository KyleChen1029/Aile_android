package tw.com.chainsea.chat.view.contact.adapter

import tw.com.chainsea.chat.view.contact.ContactPersonType

data class RecyclerViewGroupItem(val type: ContactPersonType,
                                    val sort: Int,
                                    val displayedString: String,
                                    var isOpen: Boolean,
                                    var data: Any){
    constructor(type: ContactPersonType, sort: Int, data: Any): this(type, sort, "", false, data)
}