package tw.com.chainsea.ce.sdk.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import tw.com.chainsea.android.common.hash.AESHelper
import java.nio.charset.Charset

class DecryptInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        proceed(request())
    }.let { response ->


        return@let if (response.isSuccessful) {
            val body = response.body!!
            val contentType = body.contentType()
            val charset = contentType?.charset() ?: Charset.defaultCharset()
            val buffer = body.source().apply { request(Long.MAX_VALUE) }.buffer()
            val bodyContent = buffer.clone().readString(charset)
            val decryptBody = bodyContent.let(::decryptBody)
            Log.d("CpApiResponse", "CpApiResponse Url:  ${chain.request().url}")
            try {
                Log.d("CpApiResponse", "CpApiResponse Body: ${JSONObject(decryptBody).toString(4)}")
            } catch (e: Exception) {
                Log.e("CpApiResponse", "CpApiResponse Body: $decryptBody")
            }


            response.newBuilder()
                .body(ResponseBody.create(contentType, decryptBody)).build()
        } else response
    }

    private fun decryptBody(content: String): String {
        return AESHelper.decryptBase64(content)
    }
}