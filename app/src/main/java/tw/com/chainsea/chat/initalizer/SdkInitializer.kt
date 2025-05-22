package tw.com.chainsea.chat.initalizer

import android.content.Context
import androidx.startup.Initializer
import tw.com.chainsea.ce.sdk.SdkLib

class SdkInitializer: Initializer<Unit> {
    override fun create(context: Context) {
        SdkLib.init(context)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}