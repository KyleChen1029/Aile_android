package tw.com.chainsea.chat.view.base

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class BottomTab(val title: String,
                     @DrawableRes val src: Int,
                     val type: BottomTabEnum,
                     var isSelected: Boolean = false,
                     var unRead: Int) {
    constructor(title: String, src: Int, type: BottomTabEnum, isSelected: Boolean) : this(title, src,
            type, isSelected, 0)

    constructor(title: String, src: Int, type: BottomTabEnum) : this(title, src, type, false, 0)
    constructor(type: BottomTabEnum) : this("", 0, type, false, 0)
}


enum class BottomTabEnum {
    MAIN, SERVICE, CONTACT, TODO,
}