package tw.com.chainsea.chat.ui.ife

interface IQrCodeView {
    fun onInvitationCodeComplete(text: String)
    fun onError(error: String)
}