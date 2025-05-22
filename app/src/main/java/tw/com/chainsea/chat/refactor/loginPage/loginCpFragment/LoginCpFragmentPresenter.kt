package tw.com.chainsea.chat.refactor.loginPage.loginCpFragment

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.android.common.system.ThreadExecutorHelper
import tw.com.chainsea.android.common.version.VersionHelper
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.cp.CpApiManager
import tw.com.chainsea.ce.sdk.http.cp.base.CpApiListener
import tw.com.chainsea.ce.sdk.http.cp.base.CpNewRequestBase
import tw.com.chainsea.ce.sdk.http.cp.request.CpCheckVersionRequest
import tw.com.chainsea.ce.sdk.http.cp.respone.CheckVersionResponse
import tw.com.chainsea.ce.sdk.http.cp.respone.LoginResponse
import tw.com.chainsea.ce.sdk.lib.ErrCode
import tw.com.chainsea.ce.sdk.socket.cp.CpSocket
import tw.com.chainsea.chat.BuildConfig

class LoginCpFragmentPresenter(
    private val mView: LoginCpFragmentContract.IView
) : LoginCpFragmentContract.IPresenter {
    override fun getVersion(context: Context) {
        if (BuildConfig.FLAVOR != "prod") {
            mView.setChangeServerButton()
            return
        }
        CpCheckVersionRequest(
            context,
            object : CpApiListener<String> {
                override fun onSuccess(responseBody: String) {
                    ThreadExecutorHelper.getIoThreadExecutor().execute {
                        val resp: CheckVersionResponse = JsonHelper.getInstance().from(responseBody, CheckVersionResponse::class.java)

                        var localVersionCode: Int = 0
                        var serverVersionCode: Int = 0
                        try {
                            localVersionCode = VersionHelper.getVersionCode(context).toInt()
                            serverVersionCode = resp.version.toInt()
                            TokenPref.getInstance(context).currentAppVersionFromServer = serverVersionCode
                        } catch (e: Exception) {
                            e.printStackTrace()
                            CELog.e("版本號解析錯誤 " + e.message)
                        }

                        // QA環境的版本會 parse 錯誤 送來的值是(2.x.xx)
                        // 判斷現在版本是否大於server版本, 才可以使用切換server功能
                        if (localVersionCode > serverVersionCode) {
                            mView.setChangeServerButton()
                        }
                    }
                }

                override fun onFailed(
                    errorCode: String?,
                    errorMessage: String?
                ) {
                    mView.onApiFailed(errorCode!!, errorMessage!!)
                }
            }
        ).setMainThreadEnable(true).request(JSONObject())
    }

    override fun changeServer(
        context: Context,
        baseUrl: String,
        cpSocketUrl: String,
        prefText: String
    ) {
        CpNewRequestBase.BASE_URL = baseUrl
        CpSocket.BASE_URL = cpSocketUrl
        CoroutineScope(Dispatchers.IO).launch {
            TokenPref.getInstance(context).currentServer = prefText
        }
    }

    override fun login(
        context: Context,
        countryCode: String,
        mobile: String,
        isRememberMe: Boolean
    ) {
        mView.showProgressDialog()
        CpApiManager.getInstance().login(
            context,
            countryCode,
            mobile,
            object : CpApiListener<String> {
                override fun onSuccess(responseBody: String?) {
                    mView.dismissProgressDialog()
                    ThreadExecutorHelper.getIoThreadExecutor().execute {
                        TokenPref.getInstance(context).isRememberMe = isRememberMe
                        val response: LoginResponse = JsonHelper.getInstance().from(responseBody, LoginResponse::class.java)
                        ThreadExecutorHelper.getMainThreadExecutor().execute {
                            mView.onApiSuccess(response.onceToken)
                        }
                    }
                }

                override fun onFailed(
                    errorCode: String,
                    errorMessage: String
                ) {
                    mView.dismissProgressDialog()
                    Log.d("TAG", errorCode + errorMessage)
                    if (ErrCode.MOBILE_NOT_EXIST.value == errorCode) {
                        mView.goToRegisterPage()
                    } else {
                        mView.onApiFailed(errorCode, errorMessage)
                    }
                }
            }
        )
    }
}
