package tw.com.chainsea.chat.refactor

interface IBaseView {

    fun onApiSuccess(response: Any)

    fun onApiFailed(errorCode: String, errorMessage: String)
}