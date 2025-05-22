package tw.com.chainsea.chat.initalizer

import android.content.Context
import androidx.startup.Initializer
import tw.com.chainsea.android.common.client.ClientsManager

class ClientsManagerInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        //        // The maximum number of threads in the thread pool
        ClientsManager.initClient()
            .connectTimeout(30000)
            .readTimeout(30000)
            .writeTimeout(30000)
            .maxIdleConnections(30)
            .keepAliveDuration(10L) //                .threadPoolNumber(CPU_COUNT != 0 ? CPU_COUNT - 1 : 2)
            //                .giveLog(BuildConfig.DEBUG ? true : false)
            .giveLog(true)
            .single(true)
            .build()
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}