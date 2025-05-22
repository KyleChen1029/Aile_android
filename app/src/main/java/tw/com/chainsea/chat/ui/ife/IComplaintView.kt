package tw.com.chainsea.chat.ui.ife

interface IComplaintView {
    fun dismissLoadingView()
    fun alertBlockDialog()
    fun showToast(msg: String)
    fun showTipToast(isBlock: Boolean)
    fun alertNavigateToChatRoom(roomId: String)
}