package tw.com.chainsea.ce.sdk.network

import android.app.Application
import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import tw.com.chainsea.android.common.hash.AESHelper
import tw.com.chainsea.android.common.hash.HMacHelper
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.config.AppConfig
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.common.model.DeviceData

class EncryptInterceptor(private val application: Application) : BaseInterceptor() {
    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        val builder = chain.request().newBuilder()
        builder.header("deviceData", JsonHelper.getInstance().toJson(DeviceData()))
        val staticBody = JSONObject()
        val header = JSONObject()
        var sendBody: JSONObject? = null

        val token = TokenPref.getInstance(application).cpTokenId


        //固定要送的 body
        header.put("tokenId", token)
        header.put("language", AppConfig.LANGUAGE)
        staticBody.put("_header_", header)

        //api 需要的 body
        chain.request().body?.let { body ->
            sendBody = bodyToString(body)?.let { it ->
                if (it.isNotEmpty()) {
                    JSONObject(it)
                } else {
                    null
                }
            }
        }

        Log.d("CpApiRequest", "CpRequest Url:  ${chain.request().url}")
        Log.d("CpApiRequest", "CpRequest Header: ${header?.toString(4)}")
        Log.d("CpApiRequest", "CpRequest Body: ${sendBody?.toString(4)}")
        val requestBody = sendBody?.let {
            mergeJsonObject(it, staticBody)
        } ?: run {
            staticBody
        }
        val encryptData = AESHelper.encryptBase64(requestBody.toString())


        builder.addHeader("deviceData", JsonHelper.getInstance().toJson(DeviceData()))
        builder.addHeader("x-aile-siguare", HMacHelper.encryptHmac256Base64(encryptData))
        val body = JSONObject()
        body.put("data", encryptData)

        builder.post(RequestBody.create("application/json".toMediaType(), body.toString()))
        proceed(builder.build())
    }

}