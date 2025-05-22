package tw.com.chainsea.chat.view.homepage.bind

import android.app.Application
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookAuthorizationException
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.bean.servicenumber.FacebookFansPages
import tw.com.chainsea.ce.sdk.bean.servicenumber.InstagramFansPages
import tw.com.chainsea.ce.sdk.http.ce.base.BaseViewModel
import tw.com.chainsea.ce.sdk.http.ce.base.TokenRepository
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.response.FansPageModel
import tw.com.chainsea.ce.sdk.network.model.response.InstagramFansPageModel
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.view.service.ServiceNumberAgentsManageRepository

class BindThirdPartViewModel(
    private val application: Application,
    private val tokenRepository: TokenRepository,
    private val bindThirdPartRepository: BindThirdPartRepository,
    private val serviceNumberAgentsManageRepository: ServiceNumberAgentsManageRepository
) : BaseViewModel(application, tokenRepository) {

    private val callbackManager: CallbackManager = CallbackManager.Factory.create()
    private val loginManager: LoginManager = LoginManager.getInstance()

    private var serviceNumberId : String? = null

    // facebook 登入請求權限
    private val facebookPermissions = listOf(
        "public_profile",
        "pages_show_list",
        "pages_messaging",
        "pages_read_engagement",
        "pages_manage_engagement",
        "business_management",
        "pages_manage_metadata"
    )

    // facebook 粉絲專頁請求權限
    private val facebookFansPagePermissions = listOf(
        "name", "access_token", "id", "picture{url}"
    )

    // instagram 登入請求權限
    private val instagramPermissions = listOf(
        "public_profile",
        "pages_show_list",
        "pages_messaging",
        "pages_read_engagement",
        "pages_manage_engagement",
        "instagram_basic",
        "instagram_manage_messages",
        "business_management",
        "pages_manage_metadata"
    )

    // instagram 粉絲專頁請求權限
    private val instagramFansPagePermission = listOf(
        "id",
        "access_token",
        "name",
        "picture{url}",
        "instagram_business_account{id,name,username, profile_picture_url}"
    )

    private val fansPageTokenPermission = listOf("id","access_token", "name", "picture{url}")

    val loginResult = MutableLiveData<ThirdPartEnum>()
    val facebookFansPageModel = MutableLiveData<List<FansPageModel>>()
    val onBindSuccess = MutableLiveData<Triple<ThirdPartEnum, String, Boolean>>()
    val thirdPartList = MutableLiveData<List<ThirdPartListModel>>()


    val boundFacebookFansPage = MutableLiveData<FacebookFansPages>()
    val boundInstagramFansPage = MutableLiveData<InstagramFansPages>()

    val unBindFansPage = MutableLiveData<ThirdPartEnum>()
    val unBindFansPageError = MutableLiveData<String>()
    val showUnBindDialog = MutableLiveData<Triple<ThirdPartEnum, String, String>>()
    private lateinit var loginType: ThirdPartEnum

    init {
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onCancel() {
                CELog.d("Facebook Login Cancel")
            }

            override fun onError(error: FacebookException) {
                CELog.e("Facebook Login Error", error)
            }

            override fun onSuccess(result: LoginResult) {
                loginResult.postValue(loginType)
            }
        })
    }

    /**
     * 取得服務號資料
     * */
    fun getServiceNumber(serviceNumberId: String) = viewModelScope.launch(Dispatchers.IO) {
        serviceNumberAgentsManageRepository.getServiceNumberItem(serviceNumberId).collect {
            when(it) {
                is ApiResult.Success -> {
                    this@BindThirdPartViewModel.serviceNumberId = serviceNumberId
                    it.data.faceBookFansPages?.let {
                        if (it.isNotEmpty()) {
                            boundFacebookFansPage.postValue(it[0])
                        }
                    }
                    it.data.instagramFansPages?.let {
                        if (it.isNotEmpty()) {
                            boundInstagramFansPage.postValue(it[0])
                        }
                    }
                }
                else -> {
                // nothing
                }
            }
        }
    }

    fun getThirdPartList() = viewModelScope.launch(Dispatchers.IO) {
        val defaultThirdPartList = listOf(
            ThirdPartListModel(
                icon = R.drawable.icon_facebook,
                name = application.getString(R.string.bind_facebook),
                type = ThirdPartEnum.Facebook
            ),
            ThirdPartListModel(
                icon = R.drawable.icon_instagram,
                name = application.getString(R.string.bind_instagram),
                type = ThirdPartEnum.Instagram
            )
        )
        thirdPartList.postValue(defaultThirdPartList)
    }


    /**
     * 登入 facebook
     * */
    private fun loginFacebook(activityResultRegistryOwner: ActivityResultRegistryOwner) =
        viewModelScope.launch(Dispatchers.IO) {
            loginManager.logInWithReadPermissions(
                activityResultRegistryOwner,
                callbackManager,
                facebookPermissions
            )
        }

    /**
     * 登入 instagram
     * */
    private fun loginInstagram(activityResultRegistryOwner: ActivityResultRegistryOwner) = viewModelScope.launch(Dispatchers.IO) {
        loginManager.logInWithReadPermissions(
            activityResultRegistryOwner,
            callbackManager,
            instagramPermissions
        )
    }

    /**
     * 取得粉絲專頁資料
     * */
    fun getFansPage(path: String = "me/accounts") = viewModelScope.launch(Dispatchers.IO) {
        val fields = if (loginType == ThirdPartEnum.Facebook) facebookFansPagePermissions else instagramFansPagePermission
        bindThirdPartRepository.getFansPage(path, urlFields =  getFieldsString(fields)).collect {
            when (it) {
                is ApiResult.Success -> {
                    val list = mutableListOf<FansPageModel>()
                    if (loginType == ThirdPartEnum.Instagram) {
                        if (it.data is List<*>) {
                            (it.data as List<FansPageModel>).filter { it.instagramFansPageModel != null }.forEach {
                                getInstagramData(it, it.instagramFansPageModel)?.let {
                                    list.add(it)
                                }
                            }
                        } else {
                            getInstagramData(it.data as FansPageModel, (it.data as FansPageModel).instagramFansPageModel)?.let {
                                list.add(it)
                            }
                        }
                    } else if (loginType == ThirdPartEnum.Facebook) {
                        if (it.data is List<*>) {
                            val facebookFansPageList = (it.data as List<FansPageModel>).filter { it.instagramFansPageModel == null }
                            list.addAll(facebookFansPageList)
                        } else {
                            list.add(it.data as FansPageModel)
                        }
                    }
                    facebookFansPageModel.postValue(list)
                }
                else -> {
                // nothing
                }
            }
        }
    }

    /**
     * 組裝 Instagram 資料
     * */
    private fun getInstagramData(fansPageModel: FansPageModel, instagramFansPageModel: InstagramFansPageModel?): FansPageModel? {
        if (instagramFansPageModel == null) return null
        val name = instagramFansPageModel.username?: instagramFansPageModel.name
        return fansPageModel.copy(name = name)
    }

    /**
     * 取得粉絲專頁永久 Access Token
     * @param fansPageId 粉絲專頁的 Id
     * @param accountName Instagram 要顯示的帳戶名稱
     * */
    fun getFansPageAccessToken(fansPageId: String, accountName: String) = viewModelScope.launch(Dispatchers.IO) {
        val urlFields = getFieldsString(fansPageTokenPermission)
        bindThirdPartRepository.getFansPageAccessToken(fansPageId, urlFields).collect {
            when (it) {
                is ApiResult.Success -> {
                    if (loginType == ThirdPartEnum.Instagram) {
                        it.data.name = accountName
                    }
                    bindFansPage(it.data)
                }

                else -> {
                    // nothing
                }
            }
        }
    }

    /**
     * 綁定粉絲專頁
     * @param fansPageModel 粉絲專頁的資訊
     * */
    private fun bindFansPage(fansPageModel: FansPageModel) =
        viewModelScope.launch(Dispatchers.IO) {
            serviceNumberId?.let {
                bindThirdPartRepository.bindFansPage(loginType, it, fansPageModel).collect {
                    when (it) {
                        is ApiResult.Success -> {
                            onBindSuccess.postValue(Triple(loginType, fansPageModel.name, true))
                        }

                        is ApiResult.Failure -> {
                            CELog.e(it.errorMessage.errorMessage)
                            onBindSuccess.postValue(Triple(loginType, "",false))
                        }

                        else -> {
                            // nothing
                        }
                    }
                }
            }
        }

    /**
     * 解除粉絲專頁綁定
     * @param thirdPartEnum Facebook, Instagram
     * @param fansPageId 粉絲專頁的 Id
     * */
    fun unBindFansPage(thirdPartEnum: ThirdPartEnum, fansPageId: String) = viewModelScope.launch(Dispatchers.IO) {
        serviceNumberId?.let {
            bindThirdPartRepository.unBindFansPage(thirdPartEnum, it, fansPageId).collect {
                when (it) {
                    is ApiResult.Success -> {
                        unBindFansPage.postValue(thirdPartEnum)
                    }

                    is ApiResult.Failure -> {
                        unBindFansPageError.postValue(it.errorMessage.errorMessage)
                        CELog.e(it.errorMessage.errorMessage)
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }
    }

    /**
     * 確認是否有登入過，如果有先登出 (怕使用者更換帳號)
     * @param activityResultRegistryOwner Activity
     * @param thirdPartListModel 粉絲專頁的資訊
     * */
    fun checkIsLogin(activityResultRegistryOwner: ActivityResultRegistryOwner, thirdPartListModel: ThirdPartListModel) {
        loginType = thirdPartListModel.type
        AccessToken.getCurrentAccessToken()?.let {
            LoginManager.getInstance().logOut()
        }

        when {
            thirdPartListModel.fansPageString.isNotEmpty() -> {
                showUnBindDialog.postValue(Triple(thirdPartListModel.type, thirdPartListModel.id, thirdPartListModel.fansPageString))
            }

            else -> {
                login(activityResultRegistryOwner, thirdPartListModel.type)
            }
        }
    }

    /**
     * 登入
     * @param thirdPartEnum Facebook, Instagram
     * */
    private fun login(activityResultRegistryOwner: ActivityResultRegistryOwner, thirdPartEnum: ThirdPartEnum) {
        if (thirdPartEnum == ThirdPartEnum.Facebook) {
            loginFacebook(activityResultRegistryOwner)
        } else {
            // instagram
            loginInstagram(activityResultRegistryOwner)
        }
    }


    private suspend fun getFieldsString(fields: List<String>): String =
        withContext(Dispatchers.IO) {
            val urlFields = StringBuilder()
            urlFields.append("?fields=")
            fields.forEachIndexed { index, s ->
                urlFields.append(s)
                if (index != fields.size -1) {
                    urlFields.append(",")
                }
            }
            return@withContext urlFields.toString()
        }
}