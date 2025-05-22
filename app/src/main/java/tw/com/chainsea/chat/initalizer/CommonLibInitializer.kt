package tw.com.chainsea.chat.initalizer

import android.content.Context
import androidx.startup.Initializer
import tw.com.chainsea.android.common.CommonLib

class CommonLibInitializer: Initializer<Unit> {
    override fun create(context: Context) {
        CommonLib.init(context)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}