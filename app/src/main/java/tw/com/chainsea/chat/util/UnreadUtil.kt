package tw.com.chainsea.chat.util

import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.util.ConditionUtil

/**
 * 未讀數顯示工具
 *
 * 商務號擁有者 && 沒有未讀數 && ServiceNumberStatus == ON_LINE => 顯示 N
 * 商務號擁有者 && 有讀數 => 顯示未讀數 不顯示 N
 * 未讀數非0 && 未讀數 < 99 ==> 顯示未讀數
 * 未讀數非0 && 未讀數 > 99 ==> 顯示99+
 * 以上所有列表適用，除了側邊欄團隊未讀要顯示完整數字
 */

object UnreadUtil {
    fun getUnreadText(count: Int): String = getUnreadText(count, false, false)

    fun getUnreadText(
        count: Int,
        isAtMe: Boolean = false
    ): String = getUnreadText(count, false, isAtMe)

    /**
     * @param count 未讀數量
     * @param isCanOver 是否可以顯示超過 100 => true: 104 ; false: 99+
     * @param isAtMe 是否有 at 自己
     * */
    fun getUnreadText(
        count: Int,
        isCanOver: Boolean,
        isAtMe: Boolean = false
    ): String {
        if (isCanOver) return count.toString()
        val stringBuilder = StringBuilder()
        if (isAtMe) stringBuilder.append("@")
        if (count >= 99) {
            stringBuilder.append("99+")
        } else if (count == -1) {
            stringBuilder.append("1")
        } else if (count <= 0) {
            return ""
        } else {
            stringBuilder.append(count)
        }
        return stringBuilder.toString()
    }

    // 商務號擁有者 && 沒有未讀數 && ServiceNumberStatus == ON_LINE => 顯示 N
    fun getUnreadText(
        item: ChatRoomEntity,
        userId: String,
        count: Int,
        isAtMe: Boolean = false
    ): String {
        if (userId == item.serviceNumberOwnerId) {
            if (count <= 0) {
                if (ConditionUtil.isNotBossServiceNumberOwnerStop(item)) {
                    return "N"
                }
            }
        }
        return getUnreadText(count, isAtMe)
    }
}
