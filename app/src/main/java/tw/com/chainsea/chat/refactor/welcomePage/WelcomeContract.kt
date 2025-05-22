package tw.com.chainsea.chat.refactor.welcomePage

import android.content.Context

interface WelcomeContract {
    interface IView {
        fun goToLoginPage()

        fun goToHomePage(
            isBindAile: Boolean,
            bindUrl: String?,
            isCollectInfo: Boolean
        )

        fun goToHomePage()

        fun goToHomePageWithError(errorMessage: String)
    }

    interface IPresenter {
        fun setServerUrl(context: Context)

        fun autoLogin(context: Context)
    }
}
