package tw.com.chainsea.chat.initalizer

import android.content.Context
import androidx.startup.Initializer
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.android.common.log.LogLevel
import tw.com.chainsea.chat.R

class CELogInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        CELog.init(LogLevel.DEBUG, context.getString(R.string.app_name), context)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}