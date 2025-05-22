package tw.com.chainsea.chat.view.homepage.bind

import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.request.BindFansPageRequest
import tw.com.chainsea.ce.sdk.network.model.request.BindThirdPartRequest
import tw.com.chainsea.ce.sdk.network.model.request.UnBindFansPageRequest
import tw.com.chainsea.ce.sdk.network.model.response.ChatRoomMemberResponse
import tw.com.chainsea.ce.sdk.network.model.response.FansPageModel
import tw.com.chainsea.ce.sdk.network.services.BindThirdPartService

class BindThirdPartRepository(private val bindThirdPartService: BindThirdPartService) {

    /**
     * 取得粉絲專頁
     * @param path me/accounts or {粉絲專頁id}/
     * @param urlFields 需要拿取的欄位
     * */
    fun getFansPage(path: String, urlFields: String) = flow {
        emit(ApiResult.Loading(true))
        getDataFromGraphRequest(path, urlFields).collect {
            if (it is ApiResult.Success) {
                val jsonObject = JSONObject(it.data)
                if (jsonObject.optJSONArray("data") != null) {
                    val listType = object : TypeToken<List<FansPageModel>>() {}.type
                    val data = JsonHelper.getInstance().from<List<FansPageModel>>(jsonObject.getJSONArray("data").toString(), listType)
                    emit(ApiResult.Success(data))
                } else {
                    val data = JsonHelper.getInstance().from(jsonObject, FansPageModel::class.java)
                    emit(ApiResult.Success(data))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getFacebookFansPage Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 取得粉絲專頁永久 Token
     * @param fansPageId 粉絲專頁 id
     * @param urlFields 需要拿取的欄位
     * */
    fun getFansPageAccessToken(fansPageId: String, urlFields: String) = flow {
        emit(ApiResult.Loading(true))
        getDataFromGraphRequest("/$fansPageId", urlFields).collect {
            if (it is ApiResult.Success) {
                val facebookFansPageTokenResponse = JsonHelper.getInstance().from(it.data, FansPageModel::class.java)
                emit(ApiResult.Success(facebookFansPageTokenResponse))
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getFansPageAccessToken Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 發送粉絲專頁永久 token 給 service (bind)
     * @param type Facebook, Instagram
     * @param bossServiceNumberId 商務號 id
     * @param token 從 Graph API 取得的粉絲專頁的資料
     * */
    fun bindFansPage(type: ThirdPartEnum, bossServiceNumberId: String, token: FansPageModel) = flow {
        emit(ApiResult.Loading(true))
        val request = if (type == ThirdPartEnum.Facebook) {
            BindFansPageRequest(
                bossServiceNumberId,
                faceBookFansPages = listOf(
                    BindThirdPartRequest(
                        token.id,
                        token.name,
                        token.access_token,
                        token.picture?.data?.url ?: ""
                    )
                )
            )
        } else {
            BindFansPageRequest(
                bossServiceNumberId,
                instagramFansPages = listOf(
                    BindThirdPartRequest(
                        token.id,
                        token.name,
                        token.access_token,
                        token.picture?.data?.url ?: ""
                    )
                )
            )
        }
        val response = if (type == ThirdPartEnum.Facebook) bindThirdPartService.bindFacebook(request) else bindThirdPartService.bindInstagram(request)
        response.body()?.let {
            it._header_?.let { header ->
                if (it.result && header.success!!) {
                    emit(ApiResult.Success(it.result))
                } else {
                    val errorMessage = it.msg.ifEmpty { it.errorMessage }
                    emit(ApiResult.Failure<Boolean>(ApiErrorData(errorMessage!!, "")))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("bindFansPage Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 解除粉絲專頁綁定
     * @param type Facebook, Instagram
     * @param bossServiceNumberId 商務號 id
     * @param fansPageId 粉絲專頁的 Id
     * */
    fun unBindFansPage(type: ThirdPartEnum, bossServiceNumberId: String, fansPageId: String) = flow {
        emit(ApiResult.Loading(true))
        val requestBody = UnBindFansPageRequest(bossServiceNumberId, fansPageId)
        val request = if (type == ThirdPartEnum.Facebook) bindThirdPartService.unBindFacebook(requestBody) else bindThirdPartService.unBindInstagram(requestBody)
        request.body()?.let {
            it._header_?.let { header ->
                if (it.result && header.success!!) {
                    emit(ApiResult.Success(it.result))
                } else {
                    val errorMessage = it.msg.ifEmpty { header.errorMessage }
                    emit(ApiResult.Failure<Boolean>(ApiErrorData(errorMessage!!, "")))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("unBindFansPage Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }


    /**
     * 從 Graph API 取得資料
     * @param path path
     * @param urlFields 需要拿取的欄位
     * */
    private fun getDataFromGraphRequest(path: String, urlFields: String) = flow {
        emit(ApiResult.Loading(true))
        val accessToken = AccessToken.getCurrentAccessToken()
        accessToken?.let {
            GraphRequest(it, "$path$urlFields", null, HttpMethod.GET).executeAndWait().rawResponse?.let {
                emit(ApiResult.Success(it))
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getDataFromGraphRequest Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }
}