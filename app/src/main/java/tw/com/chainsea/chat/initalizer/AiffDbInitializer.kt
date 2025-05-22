package tw.com.chainsea.chat.initalizer

import android.content.Context
import androidx.startup.Initializer
import tw.com.chainsea.chat.aiff.database.AiffDB

class AiffDbInitializer: Initializer<Unit> {

    override fun create(context: Context) {
        val db = AiffDB.getInstance(context)
        db.openDb()
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}