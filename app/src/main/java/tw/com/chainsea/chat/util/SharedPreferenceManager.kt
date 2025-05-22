package tw.com.chainsea.chat.util

import android.content.Context
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tw.com.chainsea.ce.sdk.bean.AvatarRecord
import tw.com.chainsea.ce.sdk.database.sp.TokenPref

class SharedPreferenceManager(context: Context, userId: String) {
    private val sharedPreferences = context.getSharedPreferences("searchEmployeesRecord_${userId}_${TokenPref.getInstance(context).cpTransTenantId}", Context.MODE_PRIVATE)

    fun addEmployeesAvatarRecord(newRecord: AvatarRecord) = flow {
        val editor = sharedPreferences.edit()
        val json = sharedPreferences.getString("avatarSearchRecord", "")
        val type = object : TypeToken<List<AvatarRecord>>() {}.type

        val list: MutableList<AvatarRecord> = if (json.isNullOrEmpty()) {
            mutableListOf(newRecord)
        } else {
            val temp: MutableList<AvatarRecord> = Gson().fromJson(json, type)
            run {
                temp.remove(newRecord)
                temp.add(newRecord)
                temp
            }
        }

        editor.putString("avatarSearchRecord", Gson().toJson(list))
        emit(editor.commit())
    }
    fun removeEmployeesAvatarRecord(roomId: String) = flow {
        val editor = sharedPreferences.edit()
        val json = sharedPreferences.getString("avatarSearchRecord", "")
        val type = object : TypeToken<List<AvatarRecord>>() {}.type
        val list: MutableList<AvatarRecord> = Gson().fromJson<MutableList<AvatarRecord>?>(json, type).filterNot {
            it.id == roomId
        }.toMutableList()
        editor.putString("avatarSearchRecord", Gson().toJson(list))
        emit(editor.commit())
    }

    fun getEmployeesAvatarRecord(): Flow<List<AvatarRecord>> = flow {
        val json = sharedPreferences.getString("avatarSearchRecord", "")
        val type = object : TypeToken<List<AvatarRecord>>() {}.type
        emit(Gson().fromJson(json, type) ?: emptyList())
    }

    fun saveEmployeesTextRecord(records: List<String>) = flow {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(records)
        editor.putString("employeesText", json)
        emit(editor.commit())
    }

    fun getEmployeesTextRecord(): Flow<List<String>> = flow {
        val gson = Gson()
        val json = sharedPreferences.getString("employeesText", "")
        val type = object : TypeToken<List<String>>() {}.type
        emit(gson.fromJson(json, type) ?: emptyList())
    }
}