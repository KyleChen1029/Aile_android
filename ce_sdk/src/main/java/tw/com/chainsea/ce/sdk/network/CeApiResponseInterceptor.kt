package tw.com.chainsea.ce.sdk.network

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import tw.com.chainsea.android.common.json.JsonHelper
import java.nio.charset.Charset


class CeApiResponseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        proceed(request())
    }.let { response ->
        val body = response.body!!
        val contentType = body.contentType()
        val charset = contentType?.charset() ?: Charset.defaultCharset()
        val buffer = body.source().apply { request(Long.MAX_VALUE) }.buffer()
        val bodyContent = buffer.clone().readString(charset)
        Log.d("CeApiResponse", "CeApiResponse Url:  ${chain.request().url}")
        Log.d("CeApiResponse", "CeApiResponse Header: ${response.headers}")
        try {
            Log.d("CeApiResponse", "CeApiResponse Body: ${JSONObject(bodyContent).toString(4)}")
        } catch (e: Exception) {
            Log.e("CeApiResponse", "CeApiResponse Body: $bodyContent")
        }

        return@let response
    }
}