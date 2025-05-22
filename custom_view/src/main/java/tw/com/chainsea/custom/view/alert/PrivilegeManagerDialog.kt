package tw.com.chainsea.custom.view.alert

import android.content.Context
import tw.com.chainsea.custom.view.R

class PrivilegeManagerDialog(private val context: Context) {

    /**
     * 取得移轉擁有權的 Dialog
     *
     * @param message 需要顯示的訊息
     * @param onConfirm 點擊確認後的 callback
     * */
    fun getTransferOwnerDialog(message: String, onConfirm: () -> Unit): AlertView {
        return getCommonPrivilegeDialog(message, onConfirm = onConfirm)
    }

    /**
     * 取得委派管理權的 Dialog
     *
     * @param message 需要顯示的訊息
     * @param onConfirm 點擊確認後的 callback
     * */
    fun getDesignateManagerDialog(message: String, onConfirm: () -> Unit): AlertView {
        return getCommonPrivilegeDialog(message, onConfirm = onConfirm)
    }

    /**
     * 取得取消管理權的 Dialog
     *
     * @param message 需要顯示的訊息
     * @param onConfirm 點擊確認後的 callback
     * */
    fun getCancelManagerDialog(message: String, onConfirm: () -> Unit): AlertView {
        return getCommonPrivilegeDialog(message, onConfirm = onConfirm)
    }

    /**
     * 取得刪除成員的 Dialog
     *
     * @param message 需要顯示的訊息
     * @param onConfirm 點擊確認後的 callback
     * */
    fun getDeleteMemberDialog(message: String, onConfirm: () -> Unit): AlertView {
        return getCommonPrivilegeDialog(message, true, onConfirm)
    }

    /**
     * 取得 dialog 資訊
     *
     * @param message 需要顯示的訊息
     * @param isDelete 判斷是不是刪除的 dialog 確認的文字需要變色
     * @param onConfirm 點擊確認後的 callback
     * */
    private fun getCommonPrivilegeDialog(message: String, isDelete: Boolean = false, onConfirm: () -> Unit): AlertView {
        val buttonText = if (isDelete) arrayOf(
            context.getString(R.string.text_member_deleted_no)
        ) else arrayOf(
            context.getString(R.string.alert_cancel), context.getString(R.string.alert_confirm)
        )

        val alertView =  AlertView.Builder().setContext(context).setStyle(AlertView.Style.Alert)
            .setMessage(message)
            .setOthers(buttonText)
            .setOnItemClickListener { o: Any?, position: Int ->
                if (position == 1) {
                    onConfirm.invoke()
                }
            }
        if (isDelete) alertView.setDestructive(context.getString(R.string.text_member_deleted_yes))

        return alertView.build()
            .setCancelable(true)
            .setOnDismissListener(null)
    }
}