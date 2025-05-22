package tw.com.chainsea.chat.network.selfprofile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tw.com.chainsea.ce.sdk.SdkLib
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.UserPref
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.request.SelfProfileRequest
import tw.com.chainsea.ce.sdk.network.services.SelfProfileService
import tw.com.chainsea.ce.sdk.service.type.RefreshSource

open class SelfProfileRepository(private val selfProfileService: SelfProfileService) {

    /**
     * 取得用戶資訊
     * */
    fun getUserProfile(selfId: String, source: RefreshSource = RefreshSource.REMOTE) = flow {
        emit(ApiResult.Loading(true))
        val db = DBManager.getInstance()
        val profile = db.queryUser(selfId)
        val response = selfProfileService.getSelfProfile()
        response.body()?.let {
            db.insertUserAndFriends(it)
            profile?.let {self ->
                self.isMobileVisible = it.isMobileVisible
                self.homePagePics = it.homePagePics
            }
            UserPref.getInstance(SdkLib.getAppContext())
                .setHasBindEmployee(it.isHasBindEmployee)
                .setUserName(it.nickName)
                .setUserAvatarId(it.avatarId)
                .setPersonRoomId(it.personRoomId)
        }

        profile?.let {
            emit(ApiResult.Success(it))
        }

    }.flowOn(Dispatchers.IO).catch { e ->
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }

    /**
     * 設定用戶手機號碼是否可見
     */
    fun updateSelfMobileVisible(request: SelfProfileRequest.UpdateProfile) = flow {
        emit(ApiResult.Loading(true))
        val response = selfProfileService.updateSelfProfile(request)
        response.body()?.let {
            emit(ApiResult.Success(it))
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }
}