package tw.com.chainsea.chat.refactor.loginPage

import android.content.Context

interface LoginCpContract {
    interface IView

    interface IPresenter {
        fun getPermission(context: Context)
    }
}
