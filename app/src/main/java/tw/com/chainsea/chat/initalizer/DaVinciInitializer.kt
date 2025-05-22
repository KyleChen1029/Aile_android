package tw.com.chainsea.chat.initalizer

import android.content.Context
import androidx.startup.Initializer
import tw.com.chainsea.chat.util.DaVinci

class DaVinciInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        DaVinci.init(context)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
