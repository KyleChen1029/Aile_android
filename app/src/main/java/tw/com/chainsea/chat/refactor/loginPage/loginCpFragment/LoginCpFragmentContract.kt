package tw.com.chainsea.chat.refactor.loginPage.loginCpFragment

import android.content.Context
import tw.com.chainsea.chat.refactor.IBaseView

interface LoginCpFragmentContract {
    interface IView : IBaseView {
        fun setChangeServerButton()

        fun showProgressDialog()

        fun dismissProgressDialog()

        fun goToRegisterPage()
    }

    interface IPresenter {
        fun getVersion(context: Context)

        fun changeServer(
            context: Context,
            baseUrl: String,
            cpSocketUrl: String,
            prefText: String
        )

        fun login(
            context: Context,
            countryCode: String,
            mobile: String,
            isRememberMe: Boolean
        )
    }
}
