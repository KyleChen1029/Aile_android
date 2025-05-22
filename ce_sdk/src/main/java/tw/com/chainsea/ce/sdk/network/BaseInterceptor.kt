package tw.com.chainsea.ce.sdk.network

import okhttp3.Interceptor
import okhttp3.RequestBody
import okio.Buffer
import org.json.JSONObject
import java.io.IOException

abstract class BaseInterceptor : Interceptor {

    //將固定要送的 body 和 api 需要的 body 合併
    protected fun mergeJsonObject(staticBody: JSONObject, sendBody: JSONObject): JSONObject {
        val mergeJsonObject = JSONObject()
        for (key in staticBody.keys()) {
            mergeJsonObject.put(key, staticBody.get(key))
        }
        for (key in sendBody.keys()) {
            mergeJsonObject.put(key, sendBody.get(key))
        }

        return mergeJsonObject
    }

    //將 api 需要的 body 分解轉換成 string
    protected fun bodyToString(request: RequestBody?): String? {
        return try {
            val buffer = Buffer()
            if (request != null) request.writeTo(buffer) else return ""
            buffer.readUtf8()
        } catch (e: IOException) {
            ""
        }
    }
}