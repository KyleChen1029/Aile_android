package tw.com.chainsea.ce.sdk.network

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.config.AppConfig
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.common.model.DeviceData


/**
 * 給 Ce API 使用的 intercept
 * */
class CeApiRequestInterceptor(private val context: Context) : BaseInterceptor() {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        builder.header("deviceData", JsonHelper.getInstance().toJson(DeviceData()))
        val staticBody = JSONObject()
        val header = JSONObject()
        var sendBody: JSONObject? = null


        val token = if (AppConfig.tokenForNewAPI != "") AppConfig.tokenForNewAPI else TokenPref.getInstance(context).tokenId
        //固定要送的 body
        header.put("tokenId", token)
        header.put("language", AppConfig.LANGUAGE)
        staticBody.put("_header_", header)

        //api 需要的 body
        chain.request().body?.let { body ->
            sendBody = bodyToString(body)?.let { it ->
                if (it.isNotEmpty()) {
                    try {
                        JSONObject(it)
                    } catch (e: JSONException) {
                        null
                    }
                } else {
                    null
                }
            }
        }

        val requestBody = sendBody?.let {
            mergeJsonObject(staticBody, it)
        } ?: run { staticBody }

        Log.d("CeApiRequest", "CeApiRequest Url:  ${chain.request().url}")
        Log.d("CeApiRequest", "CeApiRequest Body: ${requestBody.toString(4)}")

        builder.post(RequestBody.create("application/json".toMediaType(),
            requestBody.toString()))
        return chain.proceed(builder.build())
    }
}