package tw.com.chainsea.chat.initalizer

import android.content.Context
import androidx.startup.Initializer
import tw.com.chainsea.chat.lib.PlayerManager

class PlayerManagerInitializer: Initializer<Unit> {
    override fun create(context: Context) {
        PlayerManager.getInstence().init(context)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}