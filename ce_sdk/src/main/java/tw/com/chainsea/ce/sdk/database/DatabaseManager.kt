package tw.com.chainsea.ce.sdk.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.google.common.base.Strings
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Multimap
import com.google.common.collect.Sets
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.aile.sdk.bean.message.MessageEntity.Companion.formatByCursor
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.SdkLib
import tw.com.chainsea.ce.sdk.bean.BadgeDataModel
import tw.com.chainsea.ce.sdk.bean.CrowdEntity
import tw.com.chainsea.ce.sdk.bean.CustomerEntity
import tw.com.chainsea.ce.sdk.bean.GroupEntity
import tw.com.chainsea.ce.sdk.bean.ProcessStatus
import tw.com.chainsea.ce.sdk.bean.SearchBean
import tw.com.chainsea.ce.sdk.bean.ServiceNum
import tw.com.chainsea.ce.sdk.bean.account.AccountType
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.account.UserType
import tw.com.chainsea.ce.sdk.bean.broadcast.TopicEntity
import tw.com.chainsea.ce.sdk.bean.business.BusinessEntity
import tw.com.chainsea.ce.sdk.bean.common.EnableType
import tw.com.chainsea.ce.sdk.bean.label.Label
import tw.com.chainsea.ce.sdk.bean.msg.BroadcastFlag
import tw.com.chainsea.ce.sdk.bean.msg.FacebookCommentStatus
import tw.com.chainsea.ce.sdk.bean.msg.FacebookPostStatus
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.msg.SourceType
import tw.com.chainsea.ce.sdk.bean.msg.Tools
import tw.com.chainsea.ce.sdk.bean.msg.content.BusinessContent
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity
import tw.com.chainsea.ce.sdk.bean.statistics.StatisticsEntity
import tw.com.chainsea.ce.sdk.bean.sticker.EmoticonType
import tw.com.chainsea.ce.sdk.bean.sticker.StickerItemEntity
import tw.com.chainsea.ce.sdk.bean.sticker.StickerPackageEntity
import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity
import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity.Companion.formatByCursor
import tw.com.chainsea.ce.sdk.bean.todo.TodoStatus
import tw.com.chainsea.ce.sdk.bean.todo.getContentValues
import tw.com.chainsea.ce.sdk.bean.todo.getUpdateContentValues
import tw.com.chainsea.ce.sdk.database.DBContract.AccountRoomRel
import tw.com.chainsea.ce.sdk.database.DBContract.ChatRoomEntry
import tw.com.chainsea.ce.sdk.database.DBContract.MessageEntry
import tw.com.chainsea.ce.sdk.database.DBContract.UserProfileEntry
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.ce.model.Member
import tw.com.chainsea.ce.sdk.http.ce.model.ServiceNumber
import tw.com.chainsea.ce.sdk.http.ce.model.User
import tw.com.chainsea.ce.sdk.network.model.response.ChatRoomMemberResponse
import tw.com.chainsea.ce.sdk.network.model.response.ChatRoomMemberResponse.Companion.getContentValue
import tw.com.chainsea.ce.sdk.network.model.response.SyncRoomNormalResponse
import tw.com.chainsea.ce.sdk.reference.QueryServiceNumberChatRoom.buildFormalQuery
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DatabaseManager private constructor(
    private val appContext: Context
) {
    private var currentDatabaseName: String = ""
    private val databaseHelpers = ConcurrentHashMap<String, SQLiteOpenHelper>()
    private var currentDatabase: SQLiteDatabase? = null
    private val writeLock = ReentrantReadWriteLock()

    enum class Sort {
        ASC,
        DESC
    }

    enum class TopicRelType {
        MESSAGE
    }

    companion object {
        private const val DATABASE_VERSION = 84

        @Volatile
        private var instance: DatabaseManager? = null
        private val lock = Any()

        fun getInstance(context: Context = SdkLib.getAppContext()): DatabaseManager =
            instance ?: synchronized(lock) {
                instance ?: DatabaseManager(context.applicationContext).also { instance = it }
            }
    }

    fun switchDatabase(databaseName: String) {
        if (currentDatabaseName != databaseName) {
            writeLock.writeLock().lock()
            try {
                closeCurrentDatabase()
                currentDatabaseName = databaseName
            } finally {
                writeLock.writeLock().unlock()
            }
        }
    }

    @Synchronized
    fun openDatabase(): SQLiteDatabase {
        if (currentDatabase == null || !currentDatabase!!.isOpen) {
            if (currentDatabaseName.isEmpty()) currentDatabaseName = TokenPref.getInstance(appContext).userId
            val helper =
                databaseHelpers.getOrPut(currentDatabaseName) {
                    object : SQLiteOpenHelper(appContext, currentDatabaseName, null, DATABASE_VERSION) {
                        override fun onCreate(db: SQLiteDatabase) {
                            createDbTable(db)
                        }

                        override fun onUpgrade(
                            db: SQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int
                        ) {
                            upgradeDbVersion(db, oldVersion, newVersion)
                        }
                    }
                }
            currentDatabase = helper.writableDatabase
        }
        return currentDatabase!!
    }

    @Synchronized
    fun closeCurrentDatabase() {
        currentDatabase?.close()
        currentDatabase = null
    }

    fun closeAllDatabases() {
        databaseHelpers.values.forEach { it.close() }
        databaseHelpers.clear()
        currentDatabase = null
    }

    private suspend fun <T> withDatabase(block: (SQLiteDatabase) -> T): T =
        withContext(Dispatchers.IO) {
            val db = openDatabase()
            try {
                block(db)
            } finally {
            }
        }

    /**
     * for Java callback
     */
    fun queryFriend(
        userId: String?,
        callback: (UserProfileEntity?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            try {
                userId?.takeIf { it.isNotEmpty() }?.let { id ->
                    val selection = "${UserProfileEntry.COLUMN_ID} = ?"
                    val selectionArgs = arrayOf(id)
                    db
                        .query(
                            UserProfileEntry.TABLE_NAME,
                            null,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            null
                        )?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                callback.invoke(
                                    UserProfileEntity.getEntity(cursor)
                                )
                            } else {
                                callback.invoke(null)
                            }
                        } ?: callback.invoke(null)
                }
            } catch (e: Exception) {
                Log.e("DatabaseManager", "queryFriend error = ${e.message}")
            }
        }
    }

    suspend fun queryFriend(userId: String?): UserProfileEntity? =
        withContext(Dispatchers.IO) {
            withDatabase { db ->
                try {
                    userId?.takeIf { it.isNotEmpty() }?.let { id ->
                        val selection = "${UserProfileEntry.COLUMN_ID} = ?"
                        val selectionArgs = arrayOf(id)
                        db
                            .query(
                                UserProfileEntry.TABLE_NAME,
                                null,
                                selection,
                                selectionArgs,
                                null,
                                null,
                                null
                            )?.use { cursor ->
                                if (cursor.moveToFirst()) {
                                    UserProfileEntity.getEntity(cursor)
                                } else {
                                    null
                                }
                            }
                    }
                } catch (e: Exception) {
                    Log.e("DatabaseManager", "queryFriend error = ${e.message}")
                    null
                }
            }
        }

    fun queryFriendById(userId: String?): CompletableFuture<UserProfileEntity?> = GlobalScope.future { queryFriend(userId) }

    // for Kotlin flow
    fun queryUser(userId: String?) =
        flow {
            try {
                userId?.takeIf { it.isNotEmpty() }?.let { id ->
                    val selection = "${UserProfileEntry.COLUMN_ID} = ?"
                    val selectionArgs = arrayOf(id)
                    openDatabase()
                        .query(
                            UserProfileEntry.TABLE_NAME,
                            null,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            null
                        )?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                emit(UserProfileEntity.getEntity(cursor))
                            } else {
                                emit(null)
                            }
                        } ?: emit(null)
                } ?: emit(null)
            } catch (e: Exception) {
                Log.e("DatabaseManager", "queryUser error = ${e.message}")
                emit(null)
            }
        }.flowOn(Dispatchers.IO)
            .catch {
                Log.e("DatabaseManager", "queryUser error1 = ${it.message}")
                emit(null)
            }

    suspend fun queryUserInfo(userId: String?): UserProfileEntity? =
        withContext(Dispatchers.IO) {
            withDatabase { db ->
                try {
                    userId?.takeIf { it.isNotEmpty() }?.let { id ->
                        val selection = "${UserProfileEntry.COLUMN_ID} = ?"
                        val selectionArgs = arrayOf(id)
                        db
                            .query(
                                UserProfileEntry.TABLE_NAME,
                                null,
                                selection,
                                selectionArgs,
                                null,
                                null,
                                null
                            )?.use { cursor ->
                                if (cursor.moveToFirst()) {
                                    UserProfileEntity.getEntity(cursor)
                                } else {
                                    null
                                }
                            } ?: run { null }
                    }
                } catch (e: Exception) {
                    Log.e("DatabaseManager", "queryUserInfo error = ${e.message}")
                    null
                }
            }
        }

    @Synchronized
    fun insertCustomers(
        entities: List<CustomerEntity>,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            try {
                db.beginTransaction()
                for (entity in entities) {
                    val contentValues = CustomerEntity.getContentValues(entity)
                    db.replace(
                        DBContract.BossServiceNumberContactEntry.TABLE_NAME,
                        null,
                        contentValues
                    )
                }
                callback.invoke(true)
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                Log.e("DatabaseManager", "insertCustomers error = ${e.message}")
                callback.invoke(false)
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun insertCustomers(entities: List<CustomerEntity>) =
        flow {
            val db = openDatabase()
            try {
                db.beginTransaction()
                for (entity in entities) {
                    val contentValues = CustomerEntity.getContentValues(entity)
                    db.replace(
                        DBContract.BossServiceNumberContactEntry.TABLE_NAME,
                        null,
                        contentValues
                    )
                }
                emit(true)
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                Log.e("DatabaseManager", "insertCustomers error = ${e.message}")
                emit(false)
            } finally {
                db.endTransaction()
            }
        }.flowOn(Dispatchers.IO)

    fun queryCustomers(callback: (List<CustomerEntity>) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withDatabase { db ->
                    val selection = DBContract.BossServiceNumberContactEntry.STATUS + " = 'Enable' "
                    db
                        .query(
                            DBContract.BossServiceNumberContactEntry.TABLE_NAME,
                            null,
                            selection,
                            null,
                            null,
                            null,
                            null
                        )?.use { cursor ->
                            val entities: MutableList<CustomerEntity> = Lists.newArrayList()
                            while (cursor.moveToNext()) {
                                entities.add(
                                    CustomerEntity(
                                        Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.ID),
                                        Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.NAME),
                                        Tools.getDbString(
                                            cursor,
                                            DBContract.BossServiceNumberContactEntry.NICKNAME
                                        ),
                                        Tools.getDbString(
                                            cursor,
                                            DBContract.BossServiceNumberContactEntry.CUSTOMER_NAME
                                        ),
                                        Tools.getDbString(
                                            cursor,
                                            DBContract.BossServiceNumberContactEntry.CUSTOMER_DESCRIPTION
                                        ),
                                        Tools.getDbString(
                                            cursor,
                                            DBContract.BossServiceNumberContactEntry.CUSTOMER_BUSINESS_CARD_URL
                                        ),
                                        Tools.getDbString(
                                            cursor,
                                            DBContract.BossServiceNumberContactEntry.AVATAR_ID
                                        ),
                                        Tools.getDbInt(
                                            cursor,
                                            DBContract.BossServiceNumberContactEntry.IS_ADDRESS_BOOK
                                        ) != 0,
                                        Tools.getDbInt(
                                            cursor,
                                            DBContract.BossServiceNumberContactEntry.IS_MOBILE
                                        ) != 0,
                                        Tools.getDbString(
                                            cursor,
                                            DBContract.BossServiceNumberContactEntry.IS_MOBILE
                                        ),
                                        Tools.getDbString(
                                            cursor,
                                            DBContract.BossServiceNumberContactEntry.USER_TYPE
                                        ),
                                        Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.ROOM_ID),
                                        Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.OPEN_ID),
                                        Tools.getDbString(
                                            cursor,
                                            DBContract.BossServiceNumberContactEntry.SCOPE_INFOS
                                        ),
                                        Tools.getDbString(
                                            cursor,
                                            DBContract.BossServiceNumberContactEntry.SERVICE_NUMBER_IDS
                                        ),
                                        Tools.getDbInt(
                                            cursor,
                                            DBContract.BossServiceNumberContactEntry.MOBILE_VISIBLE
                                        ) != 0,
                                        Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.STATUS)
                                    )
                                )
                            }
                            callback.invoke(entities)
                        } ?: callback.invoke(Lists.newArrayList())
                }
            } catch (e: Exception) {
                Log.e("DatabaseManager", "queryCustomers error = ${e.message}")
                callback.invoke(Lists.newArrayList())
            }
        }

    fun queryCustomers() =
        flow {
            try {
                val db = openDatabase()
                val selection = DBContract.BossServiceNumberContactEntry.STATUS + " = 'Enable' "
                db
                    .query(
                        DBContract.BossServiceNumberContactEntry.TABLE_NAME,
                        null,
                        selection,
                        null,
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        val entities: MutableList<CustomerEntity> = Lists.newArrayList()
                        while (cursor.moveToNext()) {
                            entities.add(
                                CustomerEntity(
                                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.ID),
                                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.NAME),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.NICKNAME
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.CUSTOMER_NAME
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.CUSTOMER_DESCRIPTION
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.CUSTOMER_BUSINESS_CARD_URL
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.AVATAR_ID
                                    ),
                                    Tools.getDbInt(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.IS_ADDRESS_BOOK
                                    ) != 0,
                                    Tools.getDbInt(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.IS_MOBILE
                                    ) != 0,
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.IS_MOBILE
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.USER_TYPE
                                    ),
                                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.ROOM_ID),
                                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.OPEN_ID),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.SCOPE_INFOS
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.SERVICE_NUMBER_IDS
                                    ),
                                    Tools.getDbInt(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.MOBILE_VISIBLE
                                    ) != 0,
                                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.STATUS)
                                )
                            )
                        }
                        emit(entities)
                    } ?: emit(Lists.newArrayList())
            } catch (e: Exception) {
                Log.e("DatabaseManager", "queryCustomers error = ${e.message}")
                emit(Lists.newArrayList())
            }
        }.flowOn(Dispatchers.IO)

    fun queryCustomersByName(keyWord: String) =
        flow {
            try {
                val selection = (
                    DBContract.BossServiceNumberContactEntry.STATUS + " = 'Enable'" +
                        "AND " + DBContract.BossServiceNumberContactEntry.USER_TYPE + " = 'contact'" +
                        " AND (" + DBContract.BossServiceNumberContactEntry.NAME + " LIKE '%" + keyWord + "%'" +
                        " OR " + DBContract.BossServiceNumberContactEntry.NICKNAME + " LIKE '%" + keyWord + "%'" +
                        " OR " + DBContract.BossServiceNumberContactEntry.CUSTOMER_NAME + " LIKE '%" + keyWord + "%')"
                )
                openDatabase()
                    .query(
                        DBContract.BossServiceNumberContactEntry.TABLE_NAME,
                        null,
                        selection,
                        null,
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        val entities: MutableList<CustomerEntity> = Lists.newArrayList()
                        while (cursor.moveToNext()) {
                            entities.add(
                                CustomerEntity(
                                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.ID),
                                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.NAME),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.NICKNAME
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.CUSTOMER_NAME
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.CUSTOMER_DESCRIPTION
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.CUSTOMER_BUSINESS_CARD_URL
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.AVATAR_ID
                                    ),
                                    Tools.getDbInt(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.IS_ADDRESS_BOOK
                                    ) != 0,
                                    Tools.getDbInt(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.IS_MOBILE
                                    ) != 0,
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.IS_MOBILE
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.USER_TYPE
                                    ),
                                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.ROOM_ID),
                                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.OPEN_ID),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.SCOPE_INFOS
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.SERVICE_NUMBER_IDS
                                    ),
                                    Tools.getDbInt(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.MOBILE_VISIBLE
                                    ) != 0,
                                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.STATUS)
                                )
                            )
                        }
                        emit(entities)
                    } ?: emit(emptyList<CustomerEntity>())
            } catch (e: Exception) {
                Log.e("DatabaseManager", "queryCustomersByName error = ${e.message}")
                emit(emptyList<CustomerEntity>())
            }
        }.flowOn(Dispatchers.IO)

    fun queryUserIdByName(
        name: String,
        callback: (String) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            withDatabase { db ->
                val selection = "${UserProfileEntry.COLUMN_NICKNAME} = ?"
                val selectionArgs = arrayOf(name)
                db
                    .query(
                        UserProfileEntry.TABLE_NAME,
                        arrayOf(UserProfileEntry.COLUMN_ID),
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            callback.invoke(Tools.getDbString(cursor, 0))
                        } else {
                            callback.invoke("")
                        }
                    } ?: callback.invoke("")
            }
        } catch (e: Exception) {
            Log.e("DatabaseManager", "queryUserIdByName error = ${e.message}")
            callback.invoke("")
        }
    }

    fun queryCustomer(userId: String) =
        flow {
            try {
                val selection = "${DBContract.BossServiceNumberContactEntry.ID} = ?"
                val selectionArgs = arrayOf(userId)
                openDatabase()
                    .query(
                        DBContract.BossServiceNumberContactEntry.TABLE_NAME,
                        null,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        var customerEntity: CustomerEntity? = null
                        if (cursor.moveToFirst()) {
                            customerEntity =
                                CustomerEntity(
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.ID
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.NAME
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.NICKNAME
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.CUSTOMER_NAME
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.CUSTOMER_DESCRIPTION
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.CUSTOMER_BUSINESS_CARD_URL
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.AVATAR_ID
                                    ),
                                    Tools.getDbInt(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.IS_ADDRESS_BOOK
                                    ) != 0,
                                    Tools.getDbInt(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.IS_MOBILE
                                    ) != 0,
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.IS_MOBILE
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.USER_TYPE
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.ROOM_ID
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.OPEN_ID
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.SCOPE_INFOS
                                    ),
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.SERVICE_NUMBER_IDS
                                    ),
                                    Tools.getDbInt(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.MOBILE_VISIBLE
                                    ) != 0,
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BossServiceNumberContactEntry.STATUS
                                    )
                                )
                        }
                        emit(customerEntity)
                    }
            } catch (e: Exception) {
                Log.e("DatabaseManager", "queryCustomer error = ${e.message}")
                emit(null)
            }
        }.flowOn(Dispatchers.IO).catch {
            Log.e("DatabaseManager", "queryCustomer error1 = ${it.message}")
            emit(null)
        }

    fun queryUsersWithSelfTableSize(selfId: String) =
        flow {
            try {
                val projection = arrayOf("COUNT(*)")
                val selection = "${UserProfileEntry.COLUMN_STATUS} = ? AND ${UserProfileEntry.COLUMN_ID} != ?"
                val selectionArgs = arrayOf("Enable", selfId)

                openDatabase()
                    .query(
                        UserProfileEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null
                    )?.use { c ->
                        if (c.moveToFirst()) {
                            emit(c.getInt(0))
                        } else {
                            emit(0)
                        }
                    } ?: emit(0)
            } catch (e: Exception) {
                Log.e("DatabaseManager", "queryUsersWithSelfTableSize error = ${e.message}")
                emit(null)
            }
        }.flowOn(Dispatchers.IO).catch {
            Log.e("DatabaseManager", "queryUsersWithSelfTableSize1 error = ${it.message}")
            emit(0)
        }

    @Synchronized
    fun insertFriend(
        account: UserProfileEntity,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val friendValues = UserProfileEntity.getFriendValues(account)
                val isUpdated =
                    db.replace(
                        UserProfileEntry.TABLE_NAME,
                        null,
                        friendValues
                    )
                callback.invoke(isUpdated > 0)
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                Log.e("DatabaseManager", "insertFriend error = ${e.message}")
                callback.invoke(false)
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun insertFriend(account: UserProfileEntity) =
        flow {
            val db = openDatabase()
            db.beginTransaction()
            try {
                val friendValues = UserProfileEntity.getFriendValues(account)
                val isUpdated =
                    db.replace(
                        UserProfileEntry.TABLE_NAME,
                        null,
                        friendValues
                    )
                emit(isUpdated > 0)
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                Log.e("DatabaseManager", "insertFriend error = ${e.message}")
                emit(false)
            } finally {
                db.endTransaction()
            }
        }.flowOn(Dispatchers.IO)

    @Synchronized
    fun insertFriends(entities: List<UserProfileEntity>) =
        flow {
            val db = openDatabase()
            try {
                db.beginTransaction()
                for (entity in entities) {
                    val contentValues = UserProfileEntity.getFriendValues(entity)
                    db.replace(
                        UserProfileEntry.TABLE_NAME,
                        null,
                        contentValues
                    )
                }
                db.setTransactionSuccessful()
                emit(true)
            } catch (e: Exception) {
                Log.e("DatabaseManager", "insertFriends error = ${e.message}")
                emit(false)
            } finally {
                db.endTransaction()
            }
        }.flowOn(Dispatchers.IO)

    @Synchronized
    fun updateFriendField(
        id: String,
        key: String,
        values: String,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val contentValues = ContentValues()
                contentValues.put(key, values)
                val isUpdated =
                    db.update(
                        UserProfileEntry.TABLE_NAME,
                        contentValues,
                        UserProfileEntry.COLUMN_ID + " = ?",
                        arrayOf(id)
                    )
                callback.invoke(isUpdated > 0)
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                Log.e("DatabaseManager", "updateFriendField error = ${e.message}")
                callback.invoke(false)
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun setFriendBlock(
        id: String,
        values: Boolean
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val contentValues = ContentValues()
                contentValues.put(UserProfileEntry.COLUMN_BLOCK, if (values) 1 else 0)
                db.update(
                    UserProfileEntry.TABLE_NAME,
                    contentValues,
                    UserProfileEntry.COLUMN_ID + " = ?",
                    arrayOf(id)
                )
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                Log.e("DatabaseManager", "setFriendBlock error = ${e.message}")
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateUserField(
        id: String,
        key: String,
        values: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val contentValues = ContentValues()
                contentValues.put(key, values)
                db.update(
                    DBContract.USER_INFO.TABLE_NAME,
                    contentValues,
                    DBContract.USER_INFO._ID + " = ?",
                    arrayOf(id)
                )
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                Log.e("DatabaseManager", "updateUserField error = ${e.message}")
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateOrInsertApiInfoField(
        key: String,
        values: Long?
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val initialValues = ContentValues()
                initialValues.put(DBContract.API_INFO.COLUMN_SOURCE, key)
                initialValues.put(DBContract.API_INFO.COLUMN_LAST_REFRESH_TIME, values)
                initialValues.put(
                    DBContract.API_INFO.COLUMN_USER_ID,
                    TokenPref.getInstance(appContext).userId
                )
                db.replace(
                    DBContract.API_INFO.TABLE_NAME,
                    null,
                    initialValues
                )
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                Log.e("DatabaseManager", "updateOrInsertApiInfoField error = ${e.message}")
            } finally {
                db.endTransaction()
            }
        }
    }

    @SuppressLint("Range")
    fun getLastRefreshTime(
        source: String,
        callback: (Long) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            try {
                db
                    .query(
                        DBContract.API_INFO.TABLE_NAME,
                        arrayOf(DBContract.API_INFO.COLUMN_LAST_REFRESH_TIME),
                        DBContract.API_INFO.COLUMN_USER_ID + " =? AND " + DBContract.API_INFO.COLUMN_SOURCE + "=?",
                        arrayOf(TokenPref.getInstance(appContext).userId, source),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            callback.invoke(cursor.getLong(cursor.getColumnIndex(DBContract.API_INFO.COLUMN_LAST_REFRESH_TIME)))
                        } else {
                            callback.invoke(0L)
                        }
                    } ?: callback.invoke(0L)
            } catch (e: Exception) {
                Log.e("DatabaseManager", "getLastRefreshTime error = ${e.message}")
            }
        }
    }

    @SuppressLint("Range")
    fun getLastRefreshTime(source: String) =
        flow {
            try {
                openDatabase()
                    .query(
                        DBContract.API_INFO.TABLE_NAME,
                        arrayOf(DBContract.API_INFO.COLUMN_LAST_REFRESH_TIME),
                        DBContract.API_INFO.COLUMN_USER_ID + " =? AND " + DBContract.API_INFO.COLUMN_SOURCE + "=?",
                        arrayOf(TokenPref.getInstance(appContext).userId, source),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val time = cursor.getLong(cursor.getColumnIndex(DBContract.API_INFO.COLUMN_LAST_REFRESH_TIME))
                            emit(time)
                        } else {
                            emit(0L)
                        }
                    } ?: emit(0L)
            } catch (e: Exception) {
                Log.e("DatabaseManager", "getLastRefreshTime1 error = ${e.message}")
                emit(0L)
            }
        }.flowOn(Dispatchers.IO)

    @Synchronized
    fun clearTableData() =
        flow {
            val db = openDatabase()
            db.beginTransaction()
            try {
                val isDeleted = db.delete(DBContract.API_INFO.TABLE_NAME, null, null) > 0
                db.setTransactionSuccessful()
                emit(isDeleted)
            } catch (e: Exception) {
                Log.e("DatabaseManager", "clearTableData error = ${e.message}")
                emit(false)
            } finally {
                db.endTransaction()
            }
        }.flowOn(Dispatchers.IO)

    @Synchronized
    fun deleteBannedServiceNum(serviceNumberId: String) =
        flow {
            val db = openDatabase()
            db.beginTransaction()
            try {
                val messageSelection =
                    DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + " = ?"
                val selectionArgs = arrayOf<String>(serviceNumberId)
                val isDeleted =
                    db.delete(
                        DBContract.ServiceNumEntry.TABLE_NAME,
                        messageSelection,
                        selectionArgs
                    )
                db.setTransactionSuccessful()
                emit(isDeleted > 0)
            } catch (e: Exception) {
                Log.e("DatabaseManager", "clearTableData error = ${e.message}")
                emit(false)
            } finally {
                db.endTransaction()
            }
        }.flowOn(Dispatchers.IO)

    @Synchronized
    fun insertGroup(
        crowdEntity: CrowdEntity,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            try {
                val kind = crowdEntity.kind
                val name = crowdEntity.name
                val ownerId = crowdEntity.ownerId
                val avatarUrl = crowdEntity.avatarUrl
                val isCustomName = crowdEntity.isCustomName
                val values = ContentValues()
                if (kind != null) {
                    values.put(DBContract.GroupEntry.COLUMN_KIND, kind)
                }
                if (name != null) {
                    values.put(DBContract.GroupEntry.COLUMN_NAME, name)
                }
                if (ownerId != null) {
                    values.put(DBContract.GroupEntry.COLUMN_OWNER_ID, ownerId)
                }
                if (isCustomName) {
                    values.put(DBContract.GroupEntry.COLUMN_CUSTOM_NAME, 1)
                } else {
                    values.put(DBContract.GroupEntry.COLUMN_CUSTOM_NAME, 0)
                }
                if (avatarUrl != null) {
                    values.put(DBContract.GroupEntry.COLUMN_AVATAR_URL, avatarUrl)
                } else {
                    values.put(DBContract.GroupEntry.COLUMN_AVATAR_URL, crowdEntity.avatarId)
                }
                val isReplaced =
                    db.replace(
                        DBContract.GroupEntry.TABLE_NAME,
                        null,
                        values
                    )
                val users = crowdEntity.memberArray
                if (users != null && users.isNotEmpty()) {
                    crowdEntity.memberArray.forEach {
                        saveByAccountIdAndRoomId(it.id, crowdEntity.id)
                    }
                    val memberSelection = UserProfileEntry.COLUMN_ID + " = ?"
                    for (user in users) {
                        val memberArgs = arrayOf(user.id)
                        db
                            .query(
                                UserProfileEntry.TABLE_NAME,
                                null,
                                memberSelection,
                                memberArgs,
                                null,
                                null,
                                null
                            )?.use { cursor ->
                                db.beginTransaction()
                                try {
                                    if (cursor.count > 0) {
                                        db.update(
                                            UserProfileEntry.TABLE_NAME,
                                            UserProfileEntity.getFriendValues(user),
                                            memberSelection,
                                            memberArgs
                                        )
                                    } else {
                                        val accountValues = UserProfileEntity.getFriendValues(user)
                                        accountValues.put(UserProfileEntry.COLUMN_ID, user.id)
                                        db.insert(UserProfileEntry.TABLE_NAME, null, accountValues)
                                    }
                                    db.setTransactionSuccessful()
                                } catch (_: Exception) {
                                    callback.invoke(false)
                                } finally {
                                    db.endTransaction()
                                }
                            } ?: callback.invoke(false)
                    }
                }
                callback.invoke(isReplaced > 0)
            } catch (e: Exception) {
                Log.e("DatabaseManager", "insertGroup error = ${e.message}")
            }
        }
    }

    @Synchronized
    fun updateGroupField(
        roomId: String,
        key: String,
        value: String,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put(key, value)
                val whereClause = DBContract.GroupEntry._ID + " = ?"
                val whereArgs = arrayOf(roomId)
                val isUpdated = db.update(DBContract.GroupEntry.TABLE_NAME, values, whereClause, whereArgs)
                callback.invoke(isUpdated > 0)
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                callback.invoke(false)
                Log.e("DatabaseManager", "updateGroupField error = ${e.message}")
            } finally {
                db.endTransaction()
            }
        }
    }

    // 编辑标签页面，删除标签成员
    @Synchronized
    fun delAccountLabel(
        labelId: String,
        accountId: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val whereClause = DBContract.FriendsLabelRel.COLUMN_ID + " = ?"
                val whereArgs = arrayOf(accountId + labelId)
                db.delete(DBContract.FriendsLabelRel.TABLE_NAME, whereClause, whereArgs)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun delAccountLabels(labelId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val whereClause = DBContract.FriendsLabelRel.COLUMN_LABEL_ID + " = ?"
                    val whereArgs = arrayOf(labelId)
                    db.delete(DBContract.FriendsLabelRel.TABLE_NAME, whereClause, whereArgs)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    @Synchronized
    fun insertServiceNum(
        serviceNum: ServiceNum,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val selfId = TokenPref.getInstance(appContext).userId
                val values = ContentValues()
                values.put(DBContract.ServiceNumEntry.COLUMN_ROOM_ID, serviceNum.roomId)
                values.put(DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID, serviceNum.serviceNumberId)
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE,
                    serviceNum.serviceNumberType
                )
                values.put(DBContract.ServiceNumEntry.COLUMN_DESCRIPTION, serviceNum.description)
                values.put(DBContract.ServiceNumEntry.COLUMN_ROBOT_ID, serviceNum.robotId)
                values.put(DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE, serviceNum.isSubscribe.toString())
                values.put(DBContract.ServiceNumEntry.COLUMN_NAME, serviceNum.name)
                values.put(DBContract.ServiceNumEntry.COLUMN_AVATAR_URL, serviceNum.serviceNumberAvatarId)
                values.put(DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID, serviceNum.broadcastRoomId)
                values.put(DBContract.ServiceNumEntry.COLUMN_OWNER_ID, serviceNum.ownerId)
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_FIRST_WELCOME_MESSAGE,
                    serviceNum.serviceWelcomeMessage
                )
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_EACH_WELCOME_MESSAGE,
                    serviceNum.everyContactMessage
                )
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_INTERVAL_WELCOME_MESSAGE,
                    serviceNum.serviceIdleMessage
                )
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_IS_OWNER,
                    if (serviceNum.ownerId != null && serviceNum.ownerId == selfId) "true" else "false"
                )
                values.put(DBContract.ServiceNumEntry.COLUMN_SERVICE_IDEL_TIME, serviceNum.serviceIdleTime)
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_SERVICE_TIMEOUT_TIME,
                    serviceNum.serviceTimeoutTime
                )
                values.put(DBContract.ServiceNumEntry.COLUMN_STATUS, serviceNum.getStatus())
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_SERVICE_OPEN_TYPE,
                    JsonHelper
                        .getInstance()
                        .toJson(
                            serviceNum.serviceOpenType,
                            object : TypeToken<List<String?>?>() {
                            }.type
                        )
                )
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_SERVICE_MEMBER_ROOM_ID,
                    serviceNum.serviceMemberRoomId
                )
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_ROBOT_SERVICE_FLAG,
                    serviceNum.isRobotServiceFlag.toString()
                )
                values.put(DBContract.ServiceNumEntry.COLUMN_ROBOT_NAME, serviceNum.robotName)
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_MEMBER_ITEMS,
                    JsonHelper
                        .getInstance()
                        .toJson(
                            serviceNum.getMemberItems(),
                            object : TypeToken<List<Member?>?>() {
                            }.type
                        )
                )

                for (member in serviceNum.getMemberItems()) {
                    if (selfId == member.id) {
                        if (ServiceNumber.PrivilegeType.OWNER == member.privilege.name) {
                            values.put(DBContract.ServiceNumEntry.COLUMN_IS_OWNER, true.toString())
                        } else if (ServiceNumber.PrivilegeType.MANAGER == member.privilege.name) {
                            values.put(DBContract.ServiceNumEntry.COLUMN_IS_MANAGER, true.toString())
                        } else if (ServiceNumber.PrivilegeType.COMMON == member.privilege.name) {
                            values.put(DBContract.ServiceNumEntry.COLUMN_IS_COMMON, true.toString())
                        }
                    }
                }

                val id =
                    db
                        .update(
                            DBContract.ServiceNumEntry.TABLE_NAME,
                            values,
                            DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + " = ?",
                            arrayOf(serviceNum.serviceNumberId)
                        ).toLong()
                if (id == 0L) {
                    db.insertWithOnConflict(
                        DBContract.ServiceNumEntry.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                    )
                }
                callback.invoke(id > 0)
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                callback.invoke(false)
                Log.e("DatabaseManager", "insertServiceNum error = ${e.message}")
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun insertServiceNum(serviceNum: ServiceNum) =
        flow {
            val db = openDatabase()
            db.beginTransaction()
            try {
                val selfId = TokenPref.getInstance(appContext).userId
                val values = ContentValues()
                values.put(DBContract.ServiceNumEntry.COLUMN_ROOM_ID, serviceNum.roomId)
                values.put(DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID, serviceNum.serviceNumberId)
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE,
                    serviceNum.serviceNumberType
                )
                values.put(DBContract.ServiceNumEntry.COLUMN_DESCRIPTION, serviceNum.description)
                values.put(DBContract.ServiceNumEntry.COLUMN_ROBOT_ID, serviceNum.robotId)
                values.put(DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE, serviceNum.isSubscribe.toString())
                values.put(DBContract.ServiceNumEntry.COLUMN_NAME, serviceNum.name)
                values.put(DBContract.ServiceNumEntry.COLUMN_AVATAR_URL, serviceNum.serviceNumberAvatarId)
                values.put(DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID, serviceNum.broadcastRoomId)
                values.put(DBContract.ServiceNumEntry.COLUMN_OWNER_ID, serviceNum.ownerId)
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_FIRST_WELCOME_MESSAGE,
                    serviceNum.serviceWelcomeMessage
                )
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_EACH_WELCOME_MESSAGE,
                    serviceNum.everyContactMessage
                )
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_INTERVAL_WELCOME_MESSAGE,
                    serviceNum.serviceIdleMessage
                )
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_IS_OWNER,
                    if (serviceNum.ownerId != null && serviceNum.ownerId == selfId) "true" else "false"
                )
                values.put(DBContract.ServiceNumEntry.COLUMN_SERVICE_IDEL_TIME, serviceNum.serviceIdleTime)
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_SERVICE_TIMEOUT_TIME,
                    serviceNum.serviceTimeoutTime
                )
                values.put(DBContract.ServiceNumEntry.COLUMN_STATUS, serviceNum.getStatus())
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_SERVICE_OPEN_TYPE,
                    JsonHelper
                        .getInstance()
                        .toJson(
                            serviceNum.serviceOpenType,
                            object : TypeToken<List<String?>?>() {
                            }.type
                        )
                )
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_SERVICE_MEMBER_ROOM_ID,
                    serviceNum.serviceMemberRoomId
                )
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_ROBOT_SERVICE_FLAG,
                    serviceNum.isRobotServiceFlag.toString()
                )
                values.put(DBContract.ServiceNumEntry.COLUMN_ROBOT_NAME, serviceNum.robotName)
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_MEMBER_ITEMS,
                    JsonHelper
                        .getInstance()
                        .toJson(
                            serviceNum.getMemberItems(),
                            object : TypeToken<List<Member?>?>() {
                            }.type
                        )
                )

                for (member in serviceNum.getMemberItems()) {
                    if (selfId == member.id) {
                        if (ServiceNumber.PrivilegeType.OWNER == member.privilege.name) {
                            values.put(DBContract.ServiceNumEntry.COLUMN_IS_OWNER, true.toString())
                        } else if (ServiceNumber.PrivilegeType.MANAGER == member.privilege.name) {
                            values.put(DBContract.ServiceNumEntry.COLUMN_IS_MANAGER, true.toString())
                        } else if (ServiceNumber.PrivilegeType.COMMON == member.privilege.name) {
                            values.put(DBContract.ServiceNumEntry.COLUMN_IS_COMMON, true.toString())
                        }
                    }
                }

                val id =
                    db
                        .update(
                            DBContract.ServiceNumEntry.TABLE_NAME,
                            values,
                            DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + " = ?",
                            arrayOf(serviceNum.serviceNumberId)
                        ).toLong()
                if (id == 0L) {
                    db.insertWithOnConflict(
                        DBContract.ServiceNumEntry.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                    )
                }
                emit(id > 0)
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                emit(false)
                Log.e("DatabaseManager", "insertServiceNum error = ${e.message}")
            } finally {
                db.endTransaction()
            }
        }.flowOn(Dispatchers.IO)

    fun queryAllServiceNumberId() =
        flow {
            try {
                val serviceNumIdList: MutableList<String> = Lists.newArrayList()
                val selection = DBContract.ServiceNumEntry.COLUMN_STATUS + " =? "
                openDatabase()
                    .query(
                        DBContract.ServiceNumEntry.TABLE_NAME,
                        arrayOf(DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID),
                        selection,
                        arrayOf("Enable"),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        while (cursor.moveToNext()) {
                            val serviceNumberId = Tools.getDbString(cursor, 0)
                            serviceNumIdList.add(serviceNumberId)
                        }
                        emit(serviceNumIdList)
                    } ?: emit(mutableListOf())
            } catch (e: Exception) {
                Log.e("DatabaseManager", "queryAllServiceNumberId error = ${e.message}")
                emit(mutableListOf())
            }
        }.flowOn(Dispatchers.IO).catch {
            Log.e("DatabaseManager", "queryAllServiceNumberId1 error = ${it.message}")
            emit(mutableListOf())
        }

    fun queryFriendIsBlock(
        memberId: String,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            try {
                val selection = UserProfileEntry.COLUMN_ID + " = ?"
                val selectionArgs = arrayOf(memberId)
                val columns = arrayOf(UserProfileEntry.COLUMN_BLOCK)
                db
                    .query(
                        UserProfileEntry.TABLE_NAME,
                        columns,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val isBlock = Tools.getDbInt(cursor, UserProfileEntry.COLUMN_BLOCK)
                            callback.invoke(isBlock == 1)
                        } else {
                            callback.invoke(false)
                        }
                    } ?: callback.invoke(false)
            } catch (e: Exception) {
                Log.e("DatabaseManager", "queryFriendIsBlock error = ${e.message}")
                callback.invoke(false)
            }
        }
    }

    fun queryFriendByOpenId(openId: String) =
        flow {
            try {
                val selection = UserProfileEntry.COLUMN_OPEN_ID + " = ?"
                val selectionArgs = arrayOf(openId)
                openDatabase()
                    .query(
                        UserProfileEntry.TABLE_NAME,
                        null,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val accountCE = UserProfileEntity.getEntity(cursor)
                            emit(accountCE)
                        } else {
                            emit(null)
                        }
                    } ?: emit(null)
            } catch (e: Exception) {
                Log.e("DatabaseManager", "queryFriendByOpenId error = ${e.message}")
                emit(null)
            }
        }.flowOn(Dispatchers.IO).catch {
            Log.e("DatabaseManager", "queryFriendByOpenId1 error = ${it.message}")
            emit(null)
        }

    fun queryGroup(
        id: String,
        callback: (CrowdEntity?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            try {
                val selection = DBContract.GroupEntry._ID + " = ?"
                val selectionArgs = arrayOf(id)
                db
                    .query(
                        DBContract.GroupEntry.TABLE_NAME,
                        null,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val crowdEntity = CrowdEntity()
                            crowdEntity.isCustomName = "true" ==
                                Tools.getDbString(
                                    cursor,
                                    DBContract.GroupEntry.COLUMN_CUSTOM_NAME
                                )
                            crowdEntity.kind = Tools.getDbString(cursor, DBContract.GroupEntry.COLUMN_KIND)
                            crowdEntity.name = Tools.getDbString(cursor, DBContract.GroupEntry.COLUMN_NAME)
                            crowdEntity.ownerId =
                                Tools.getDbString(cursor, DBContract.GroupEntry.COLUMN_OWNER_ID)
                            crowdEntity.avatarUrl =
                                Tools.getDbString(cursor, DBContract.GroupEntry.COLUMN_AVATAR_URL)
                            crowdEntity.id = id

                            callback.invoke(crowdEntity)
                        } else {
                            callback.invoke(null)
                        }
                    } ?: callback.invoke(null)
            } catch (e: Exception) {
                Log.e("DatabaseManager", "queryGroup error = ${e.message}")
                callback.invoke(null)
            }
        }
    }

    /**
     * get all group
     */
    fun findAllCrowds(callback: (MutableList<CrowdEntity>) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                openDatabase()
                    .query(
                        DBContract.GroupEntry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        val crowdEntities: MutableList<CrowdEntity> = mutableListOf()
                        while (cursor.moveToNext()) {
                            val id = Tools.getDbString(cursor, DBContract.GroupEntry._ID)
                            val users = mutableListOf<UserProfileEntity>() // ChatMemberCacheService.getChatMembers(id)
                            crowdEntities.add(
                                CrowdEntity
                                    .Build()
                                    .id(Tools.getDbString(cursor, DBContract.GroupEntry._ID))
                                    .users(users)
                                    .isCustomName(
                                        "true" ==
                                            Tools.getDbString(
                                                cursor,
                                                DBContract.GroupEntry.COLUMN_CUSTOM_NAME
                                            )
                                    ).kind(Tools.getDbString(cursor, DBContract.GroupEntry.COLUMN_KIND))
                                    .name(Tools.getDbString(cursor, DBContract.GroupEntry.COLUMN_NAME))
                                    .ownerId(
                                        Tools.getDbString(
                                            cursor,
                                            DBContract.GroupEntry.COLUMN_OWNER_ID
                                        )
                                    ).avatarUrl(
                                        Tools.getDbString(
                                            cursor,
                                            DBContract.GroupEntry.COLUMN_AVATAR_URL
                                        )
                                    ).build()
                            )
                            callback.invoke(crowdEntities)
                        }
                    } ?: callback.invoke(mutableListOf())
            } catch (e: Exception) {
                Log.e("DatabaseManager", "findAllCrowds error = ${e.message}")
                callback.invoke(mutableListOf())
            }
        }

    suspend fun saveGroupInfo(entity: GroupEntity): Boolean =
        withContext(Dispatchers.IO) {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val contentValue = GroupEntity.getContentValues(entity)
                    val isSave =
                        db.replace(
                            DBContract.SyncGroupEntry.TABLE_NAME,
                            null,
                            contentValue
                        )
                    db.setTransactionSuccessful()
                    isSave > 0
                } catch (e: Exception) {
                    Log.e("DatabaseManager", "saveGroupInfo error = ${e.message}")
                    false
                } finally {
                    db.endTransaction()
                }
            }
        }

    fun queryGroupInfo(id: String) =
        flow {
            try {
                openDatabase()
                    .query(
                        DBContract.SyncGroupEntry.TABLE_NAME,
                        null,
                        DBContract.SyncGroupEntry._ID + "= ? AND " +
                            DBContract.SyncGroupEntry.COLUMN_DELETED + " =? " +
                            "AND " + DBContract.SyncGroupEntry.COLUMN_MEMBER_DELETED + " =?",
                        arrayOf(id, "N", "N"),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val entity = GroupEntity.getEntity(cursor)
                            emit(entity)
                        } else {
                            emit(null)
                        }
                    } ?: emit(null)
            } catch (e: Exception) {
                Log.e("DatabaseManager", "queryGroupInfo error = ${e.message}")
                emit(null)
            }
        }.flowOn(Dispatchers.IO).catch { emit(null) }

    /**
     * 取得所有Group
     */
    fun findAllGroups() =
        flow {
            try {
                openDatabase()
                    .query(
                        DBContract.SyncGroupEntry.TABLE_NAME,
                        null,
                        DBContract.SyncGroupEntry.COLUMN_DELETED + " =?" +
                            " AND " + DBContract.SyncGroupEntry.COLUMN_MEMBER_DELETED + " =?",
                        arrayOf("N", "N"),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        val groupEntities: MutableList<GroupEntity> = mutableListOf()
                        while (cursor.moveToNext()) {
                            val entity = GroupEntity.getEntity(cursor)
                            groupEntities.add(entity)
                        }
                        emit(groupEntities)
                    } ?: emit(mutableListOf())
            } catch (e: Exception) {
                Log.e("DatabaseManager", "findAllGroups error = ${e.message}")
                emit(mutableListOf())
            }
        }.flowOn(Dispatchers.IO).catch { emit(mutableListOf()) }

    fun findAllGroupsByName(keyWord: String) =
        flow {
            try {
                openDatabase()
                    .query(
                        DBContract.SyncGroupEntry.TABLE_NAME,
                        null,
                        DBContract.SyncGroupEntry.COLUMN_DELETED + " =?" +
                            " AND " + DBContract.SyncGroupEntry.COLUMN_MEMBER_DELETED + " =?" +
                            " AND " + DBContract.SyncGroupEntry.COLUMN_NAME + " LIKE ?",
                        arrayOf("N", "N", "%$keyWord%"),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        val groupEntities: MutableList<GroupEntity> = mutableListOf()
                        while (cursor.moveToNext()) {
                            val entity = GroupEntity.getEntity(cursor)
                            groupEntities.add(entity)
                        }
                        emit(groupEntities)
                    } ?: emit(mutableListOf())
            } catch (e: Exception) {
                Log.e("DatabaseManager", "findAllGroupsByName error = ${e.message}")
                emit(mutableListOf())
            }
        }.flowOn(Dispatchers.IO).catch { emit(mutableListOf()) }

    suspend fun queryAllLabels(): List<Label> =
        withDatabase { db ->
            val labels: MutableList<Label> = mutableListOf()
            try {
                db
                    .query(
                        DBContract.LabelEntry.TABLE_NAME,
                        null,
                        "${DBContract.LabelEntry.COLUMN_DELETED} =?",
                        arrayOf("false"),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        while (cursor.moveToNext()) {
                            val label =
                                Label(
                                    Tools.getDbString(cursor, DBContract.LabelEntry._ID),
                                    Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_NAME),
                                    JsonHelper.getInstance().from(
                                        Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_USER_IDS),
                                        object : TypeToken<List<String>>() {}.type
                                    ),
                                    Tools.getDbLong(cursor, DBContract.LabelEntry.COLUMN_CREATE_TIME),
                                    Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_OWNER_ID),
                                    "true" == Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_READ_ONLY),
                                    "true" == Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_DELETED)
                                )
                            db
                                .query(
                                    DBContract.FriendsLabelRel.TABLE_NAME + " AS l INNER JOIN " + UserProfileEntry.TABLE_NAME + " AS f ON f.id = l.friend_id",
                                    arrayOf("f.*"),
                                    "l.label_id = ? AND f." + UserProfileEntry.COLUMN_BLOCK + " !=? " +
                                        " AND f." + UserProfileEntry.COLUMN_STATUS + " !=?",
                                    arrayOf(label.id, "1", User.Status.DISABLE),
                                    null,
                                    null,
                                    null
                                ).use { userCursor ->
                                    val friends: MutableList<UserProfileEntity> = mutableListOf()
                                    while (userCursor.moveToNext()) {
                                        val friend = UserProfileEntity.getEntity(userCursor)
                                        friends.add(friend)
                                    }
                                    label.users = friends
                                    labels.add(label)
                                }
                        }
                    }
                labels
            } catch (e: Exception) {
                Log.e("DatabaseManager", "queryAllLabels error = ${e.message}")
                mutableListOf()
            }
        }

    fun queryAllLabels(callback: (MutableList<Label>) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.query(DBContract.LabelEntry.TABLE_NAME, null, "${DBContract.LabelEntry.COLUMN_DELETED} =?", arrayOf("false"), null, null, null)?.use { cursor ->
                    val labels: MutableList<Label> = mutableListOf()
                    while (cursor.moveToNext()) {
                        val label =
                            Label(
                                Tools.getDbString(cursor, DBContract.LabelEntry._ID),
                                Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_NAME),
                                JsonHelper.getInstance().from(
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.LabelEntry.COLUMN_USER_IDS
                                    ),
                                    object : TypeToken<List<String>>() {
                                    }.type
                                ),
                                Tools.getDbLong(cursor, DBContract.LabelEntry.COLUMN_CREATE_TIME),
                                Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_OWNER_ID),
                                "true" ==
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.LabelEntry.COLUMN_READ_ONLY
                                    ),
                                "true" ==
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.LabelEntry.COLUMN_DELETED
                                    )
                            )
                        db
                            .query(
                                DBContract.FriendsLabelRel.TABLE_NAME + " AS l INNER JOIN " + UserProfileEntry.TABLE_NAME + " AS f ON f.id = l.friend_id",
                                arrayOf("f.*"),
                                "l.label_id = ? AND f." + UserProfileEntry.COLUMN_BLOCK + " !=? " +
                                    " AND f." + UserProfileEntry.COLUMN_STATUS + " !=?",
                                arrayOf(label.id, "1", User.Status.DISABLE),
                                null,
                                null,
                                null
                            ).use { userCursor ->
                                val friends: MutableList<UserProfileEntity> = mutableListOf()
                                while (userCursor.moveToNext()) {
                                    val friend = UserProfileEntity.getEntity(userCursor)
                                    friends.add(friend)
                                }
                                label.users = friends
                                labels.add(label)
                            }
                    }
                    callback.invoke(labels)
                } ?: callback.invoke(Lists.newArrayList())
            }
        }

    fun queryOfficialServiceNumber() =
        flow {
            openDatabase()
                .query(
                    DBContract.ServiceNumEntry.TABLE_NAME,
                    null,
                    DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE + "=?",
                    arrayOf(ServiceNumberType.OFFICIAL.type),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        emit(ServiceNumberEntity.formatByCursor(cursor).build())
                    } else {
                        emit(null)
                    }
                } ?: emit(null)
        }.flowOn(Dispatchers.IO).catch { emit(null) }

    fun queryServiceNumberById(serviceNumberId: String) =
        flow {
            try {
                openDatabase()
                    .query(
                        DBContract.ServiceNumEntry.TABLE_NAME,
                        null,
                        DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + "=?",
                        arrayOf(serviceNumberId),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val serviceNum = ServiceNum()
                            serviceNum.description =
                                Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_DESCRIPTION)
                            serviceNum.isSubscribe =
                                Tools
                                    .getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE)
                                    .toBoolean()
                            serviceNum.name =
                                Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_NAME)
                            serviceNum.roomId =
                                Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_ROOM_ID)
                            serviceNum.serviceNumberAvatarId =
                                Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_AVATAR_URL)
                            serviceNum.serviceNumberId =
                                Tools.getDbString(
                                    cursor,
                                    DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID
                                )
                            serviceNum.memberItems =
                                JsonHelper.getInstance().from(
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.ServiceNumEntry.COLUMN_MEMBER_ITEMS
                                    ),
                                    object : TypeToken<List<Member?>?>() {}.type
                                )
                            emit(serviceNum)
                        } else {
                            emit(null)
                        }
                    }
            } catch (e: Exception) {
                Log.e("queryServiceNumberById", e.toString())
                emit(null)
            }
        }.flowOn(Dispatchers.IO).catch { emit(null) }

    fun queryServiceNumberByConsultRoomId(consultRoomId: String) =
        flow {
            openDatabase()
                .query(
                    DBContract.ServiceNumEntry.TABLE_NAME,
                    null,
                    DBContract.ServiceNumEntry.COLUMN_ROOM_ID + "=?",
                    arrayOf(consultRoomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val serviceNum = ServiceNum()
                        serviceNum.description =
                            Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_DESCRIPTION)
                        serviceNum.isSubscribe =
                            Tools
                                .getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE)
                                .toBoolean()
                        serviceNum.name =
                            Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_NAME)
                        serviceNum.roomId =
                            Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_ROOM_ID)
                        serviceNum.serviceNumberAvatarId =
                            Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_AVATAR_URL)
                        serviceNum.serviceNumberId =
                            Tools.getDbString(
                                cursor,
                                DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID
                            )
                        serviceNum.memberItems =
                            JsonHelper.getInstance().from(
                                Tools.getDbString(
                                    cursor,
                                    DBContract.ServiceNumEntry.COLUMN_MEMBER_ITEMS
                                ),
                                object : TypeToken<List<Member?>?>() {}.type
                            )
                        emit(serviceNum)
                    } else {
                        emit(null)
                    }
                } ?: emit(null)
        }.flowOn(Dispatchers.IO).catch { emit(null) }

    fun querySubscribeServiceNumbersByName(keyWord: String) =
        flow {
            val serviceNumList: MutableList<ServiceNum> = mutableListOf()
            openDatabase()
                .query(
                    DBContract.ServiceNumEntry.TABLE_NAME,
                    null,
                    DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE + " = ? AND " + DBContract.ServiceNumEntry.COLUMN_STATUS + " = ? AND " + DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE + " !=? AND " + DBContract.ServiceNumEntry.COLUMN_NAME + " LIKE ?",
                    arrayOf("true", "Enable", ServiceNumberType.BOSS.getType(), "%" + keyWord + "%"),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val serviceNum =
                            ServiceNum().apply {
                                description = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_DESCRIPTION)
                                isSubscribe = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE).toBoolean()
                                name = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_NAME)
                                roomId = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_ROOM_ID)
                                serviceNumberAvatarId = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_AVATAR_URL)
                                serviceNumberId = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID)
                                serviceOpenType = JsonHelper.getInstance().from(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_MEMBER_ITEMS), object : TypeToken<List<String>>() {}.type)
                            }
                        serviceNumList.add(serviceNum)
                    }
                }
            emit(serviceNumList.filterNot { it.serviceOpenType.contains("C") })
        }.flowOn(Dispatchers.IO).catch { emit(mutableListOf()) }

    fun querySubscribeServiceNumber() =
        flow {
            val serviceNumList: MutableList<ServiceNum> = mutableListOf()
            openDatabase()
                .query(
                    DBContract.ServiceNumEntry.TABLE_NAME,
                    null,
                    DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE + " = ? AND " + DBContract.ServiceNumEntry.COLUMN_STATUS + " = ? AND " + DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE + " !=?",
                    arrayOf("true", "Enable", ServiceNumberType.BOSS.type),
                    null,
                    null,
                    DBContract.ServiceNumEntry.COLUMN_UPDATE_TIME + " DESC"
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val serviceNum = ServiceNum()
                        serviceNum.description =
                            Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_DESCRIPTION)
                        serviceNum.isSubscribe =
                            Tools
                                .getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE)
                                .toBoolean()
                        serviceNum.name =
                            Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_NAME)
                        serviceNum.roomId =
                            Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_ROOM_ID)
                        serviceNum.serviceNumberAvatarId =
                            Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_AVATAR_URL)
                        serviceNum.serviceNumberId =
                            Tools.getDbString(
                                cursor,
                                DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID
                            )
                        val openType =
                            Tools.getDbString(
                                cursor,
                                DBContract.ServiceNumEntry.COLUMN_SERVICE_OPEN_TYPE
                            )
                        serviceNum.serviceOpenType =
                            JsonHelper
                                .getInstance()
                                .from(openType, object : TypeToken<List<String?>?>() {}.type)
                        serviceNum.updateTime =
                            Tools.getDbLong(cursor, DBContract.ServiceNumEntry.COLUMN_UPDATE_TIME)
                        serviceNumList.add(serviceNum)
                    }
                }
            emit(serviceNumList.filterNot { it.serviceOpenType.contains("C") })
        }.flowOn(Dispatchers.IO).catch { emit(mutableListOf()) }

    @Synchronized
    fun updateMessageStatus(
        messageId: String?,
        status: MessageStatus,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val whereClause = MessageEntry._ID + " = ?"
                val whereArgs = arrayOf(messageId)
                val values = ContentValues()
                values.put(MessageEntry._ID, messageId)
                values.put(MessageEntry.COLUMN_STATUS, status.value)
                val isUpdated = db.update(MessageEntry.TABLE_NAME, values, whereClause, whereArgs)
                callback.invoke(isUpdated > 0)
                db.setTransactionSuccessful()
            } catch (_: Exception) {
                callback.invoke(false)
            } finally {
                db.endTransaction()
            }
        }
    }

    suspend fun updateServiceStatus(
        broadcastRoomId: String,
        enable: Boolean
    ) = withContext(
        Dispatchers.IO
    ) {
        withDatabase { db ->
            val values = ContentValues()
            values.put(DBContract.ServiceNumEntry.COLUMN_STATUS, if (enable) "Enable" else "Disable")
            db.update(
                DBContract.ServiceNumEntry.TABLE_NAME,
                values,
                DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID + " = ?",
                arrayOf(broadcastRoomId)
            )
        }
    }

    suspend fun updateUserAvatarId(
        userId: String,
        avatarId: String
    ) = withContext(Dispatchers.IO) {
        withDatabase { db ->
            val values = ContentValues()
            values.put(UserProfileEntry.COLUMN_AVATAR_URL, avatarId)
            db.update(
                UserProfileEntry.TABLE_NAME,
                values,
                UserProfileEntry.COLUMN_ID + " = ?",
                arrayOf(userId)
            )
        }
    }

    suspend fun updateFriendRoomAvatarId(
        roomId: String,
        avatarId: String?
    ) = withContext(
        Dispatchers.IO
    ) {
        withDatabase { db ->
            val values = ContentValues()
            values.put(ChatRoomEntry.COLUMN_AVATAR_ID, avatarId)
            db.update(
                ChatRoomEntry.TABLE_NAME,
                values,
                ChatRoomEntry._ID + " = ?",
                arrayOf(roomId)
            )
        }
    }

    suspend fun setChatRoomListItemUnreadNum(
        id: String,
        num: Int
    ) = withContext(Dispatchers.IO) {
        withDatabase { db ->
            val values = ContentValues()
            values.put(ChatRoomEntry.COLUMN_UNREAD_NUMBER, num)
            db.update(
                ChatRoomEntry.TABLE_NAME,
                values,
                ChatRoomEntry._ID + " = ?",
                arrayOf(id)
            )
        }
    }

    suspend fun setChatRoomListItemInteractionTime(id: String) =
        withContext(Dispatchers.IO) {
            withDatabase { db ->
                val values = ContentValues()
                values.put(ChatRoomEntry.COLUMN_UPDATE_TIME, System.currentTimeMillis())
                db.update(
                    ChatRoomEntry.TABLE_NAME,
                    values,
                    ChatRoomEntry._ID + " = ?",
                    arrayOf(id)
                )
            }
        }

    suspend fun deleteRoomListItem(id: String): Boolean =
        withContext(Dispatchers.IO) {
            var isDeleted = false
            withDatabase { db ->
                val values = ContentValues()
                values.put(ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED, "Y")
                val updatedRows =
                    db.update(
                        ChatRoomEntry.TABLE_NAME,
                        values,
                        "${ChatRoomEntry._ID} = ?",
                        arrayOf(id)
                    )
                isDeleted = updatedRows > 0
            }
            isDeleted
        }

    suspend fun setRoomNotDeleted(roomId: String): Boolean =
        withContext(Dispatchers.IO) {
            var isUpdated = false
            withDatabase { db ->
                val values = ContentValues()
                values.put(ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED, "N")
                val updatedRows =
                    db.update(
                        ChatRoomEntry.TABLE_NAME,
                        values,
                        ChatRoomEntry._ID + " = ?",
                        arrayOf(roomId)
                    )
                isUpdated = updatedRows > 0
            }
            isUpdated
        }

    /***
     * 服務號列表要判斷自己是否是該服務號成員，才能取得該服務號聊天室或成員聊天室的未讀數
     * SELECT chat.unread
     * FROM chat
     * JOIN service ON chat.service_id = service.id
     * WHERE service.owner = true OR service.manager = true OR service.common = true;
     *
     * 一般聊天列表僅抓取未讀數
     */
    suspend fun getChatRoomListUnReadSum(source: String): Int =
        withContext(Dispatchers.IO) {
            var sum = 0
            withDatabase { db ->
                val columns =
                    if (source == "SERVICE") {
                        "SUM(abs(" + ChatRoomEntry.TABLE_NAME + "." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + "))"
                    } else {
                        "SUM(abs(" + ChatRoomEntry.COLUMN_UNREAD_NUMBER + "))"
                    }
                val table =
                    if (source == "SERVICE") {
                        ChatRoomEntry.TABLE_NAME + " JOIN " + DBContract.ServiceNumEntry.TABLE_NAME + " ON " + ChatRoomEntry.TABLE_NAME + "." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " = " + DBContract.ServiceNumEntry.TABLE_NAME + "." + DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID
                    } else {
                        ChatRoomEntry.TABLE_NAME
                    }
                val where =
                    if (source == "SERVICE") {
                        ChatRoomEntry.TABLE_NAME + "." + ChatRoomEntry.COLUMN_LIST_CLASSIFY + " =?" +
                            " AND (" + DBContract.ServiceNumEntry.TABLE_NAME + "." + DBContract.ServiceNumEntry.COLUMN_IS_OWNER + " = 'true'" +
                            " OR " + DBContract.ServiceNumEntry.TABLE_NAME + "." + DBContract.ServiceNumEntry.COLUMN_IS_MANAGER + " = 'true'" +
                            " OR " + DBContract.ServiceNumEntry.TABLE_NAME + "." + DBContract.ServiceNumEntry.COLUMN_IS_COMMON + " = 'true')"
                    } else {
                        ChatRoomEntry.COLUMN_LIST_CLASSIFY + " =? AND " + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = 'N'"
                    }
                db
                    .query(
                        table,
                        arrayOf(columns),
                        where,
                        arrayOf(source),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            sum = cursor.getInt(0)
                        }
                    } ?: run { sum = 0 }
            }
            sum
        }

    suspend fun updateReceivedNum(
        messageId: String,
        receivedNum: Int
    ): Boolean =
        withContext(Dispatchers.IO) {
            var isUpdated = false
            withDatabase { db ->
                val values = ContentValues()
                values.put(MessageEntry.COLUMN_RECEIVED_NUM, receivedNum)
                val whereClause = MessageEntry._ID + "= ?"
                val id =
                    db.update(MessageEntry.TABLE_NAME, values, whereClause, arrayOf(messageId))
                isUpdated = id > 0
            }
            isUpdated
        }

    suspend fun updateReadNum(
        messageId: String,
        unReadNum: Int
    ): Boolean =
        withContext(Dispatchers.IO) {
            var isUpdated = false
            withDatabase { db ->
                val values = ContentValues()
                values.put(MessageEntry.COLUMN_READED_NUM, unReadNum)
                val whereClause = MessageEntry._ID + "= ?"
                val id =
                    db.update(MessageEntry.TABLE_NAME, values, whereClause, arrayOf(messageId))
                isUpdated = id > 0
            }
            isUpdated
        }

    @Synchronized
    fun updateSendTime(
        messageId: String,
        sendTime: Long
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put(MessageEntry.COLUMN_SEND_TIME, sendTime)
                val whereClause = MessageEntry._ID + "= ?"
                val whereArgs = arrayOf(messageId)
                db.update(MessageEntry.TABLE_NAME, values, whereClause, whereArgs)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateSendNum(
        messageId: String,
        sendNum: Int
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put(MessageEntry.COLUMN_SEND_NUM, sendNum)
                val whereClause = MessageEntry._ID + "= ?"
                val whereArgs = arrayOf(messageId)
                db.update(MessageEntry.TABLE_NAME, values, whereClause, whereArgs)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun deleteGroup(groupId: String) =
        synchronized(this) {
            CoroutineScope(Dispatchers.IO).launch {
                withDatabase { db ->
                    db.beginTransaction()
                    try {
                        val messageSelection = DBContract.GroupEntry._ID + " = ?"
                        val selectionArgs = arrayOf(groupId)
                        db.delete(DBContract.GroupEntry.TABLE_NAME, messageSelection, selectionArgs)

                        val whereClause = AccountRoomRel.COLUMN_ROOM_ID + " = ?"
                        db.delete(AccountRoomRel.TABLE_NAME, whereClause, selectionArgs)

                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                }
            }
        }

    @Synchronized
    fun deleteServiceNum(roomId: String) =
        synchronized(this) {
            CoroutineScope(Dispatchers.IO).launch {
                withDatabase { db ->
                    db.beginTransaction()
                    try {
                        val messageSelection = DBContract.ServiceNumEntry.COLUMN_ROOM_ID + " = ?"
                        val selectionArgs = arrayOf(roomId)
                        db.delete(DBContract.ServiceNumEntry.TABLE_NAME, messageSelection, selectionArgs)
                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                }
            }
        }

    @Synchronized
    fun deleteServiceNumber(
        serviceNumberId: String,
        callback: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val messageSelection = DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + " =? "
                    val selectionArgs = arrayOf(serviceNumberId)
                    val isDeleted =
                        db.delete(
                            DBContract.ServiceNumEntry.TABLE_NAME,
                            messageSelection,
                            selectionArgs
                        ) > 0
                    callback.invoke(isDeleted)
                    db.setTransactionSuccessful()
                } catch (_: Exception) {
                    callback.invoke(false)
                } finally {
                    db.endTransaction()
                }
            }
        }
    }

    fun dimQueryLastReadMsg() =
        flow {
            val messages: MutableList<MessageEntity> = mutableListOf()
            val selection = MessageEntry.COLUMN_READED_NUM + " > ?"
            val selectionArgs = arrayOf("0")
            val order = MessageEntry.COLUMN_SEND_TIME + " DESC"
            openDatabase()
                .query(MessageEntry.TABLE_NAME, null, selection, selectionArgs, null, null, order, "1")
                ?.use { cursor ->
                    val index = MessageEntry.getIndex(cursor)
                    while (cursor.moveToNext()) {
                        val msg = formatByCursor(index, cursor)
                        messages.add(msg)
                    }
                    emit(messages)
                } ?: emit(mutableListOf())
        }.flowOn(Dispatchers.IO).catch { emit(mutableListOf()) }

    fun queryMessagesByMsgStatus(roomId: String) =
        flow {
            val messages: MutableList<MessageEntity> = Lists.newArrayList()
            val args = arrayOf(roomId, "0", "0")
            openDatabase()
                .query(
                    MessageEntry.TABLE_NAME,
                    null,
                    MessageEntry.COLUMN_ROOM_ID + "=?" +
                        " AND " + MessageEntry.COLUMN_RECEIVED_NUM + ">?" +
                        " AND " + MessageEntry.COLUMN_READED_NUM + "=?",
                    args,
                    null,
                    null,
                    MessageEntry.COLUMN_SEND_TIME + " ASC"
                )?.use { cursor ->
                    val index = MessageEntry.getIndex(cursor)
                    while (cursor.moveToNext()) {
                        val msg = formatByCursor(index, cursor)
                        messages.add(msg)
                    }
                    emit(messages)
                } ?: emit(mutableListOf())
        }.flowOn(Dispatchers.IO).catch { emit(mutableListOf()) }

    fun queryLastReadMsgId(
        roomId: String,
        userId: String
    ) = flow {
        val args = arrayOf(roomId, userId, "0")
        openDatabase()
            .query(
                MessageEntry.TABLE_NAME,
                null,
                MessageEntry.COLUMN_ROOM_ID + "=?" +
                    " AND " + MessageEntry.COLUMN_SENDER_ID + "=?" +
                    " AND " + MessageEntry.COLUMN_READED_NUM + ">?",
                args,
                null,
                null,
                MessageEntry.COLUMN_SEND_TIME + " DESC"
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val messageId = Tools.getDbString(cursor, MessageEntry._ID)
                    emit(messageId)
                } else {
                    emit("")
                }
            } ?: emit("")
    }.flowOn(Dispatchers.IO).catch { emit("") }

    fun insertUserAndFriends(user: UserProfileEntity) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                // 插入或更新用戶
                insertOrUpdateUserInfo(db, user)
                // 插入或更新好友
                insertOrUpdateFriendInfo(db, user)
            }
        }

    @Synchronized
    private fun insertOrUpdateUserInfo(
        db: SQLiteDatabase,
        user: UserProfileEntity
    ) {
        val userValues =
            ContentValues().apply {
                user.id?.let { put(DBContract.USER_INFO._ID, it) }
                user.loginName?.let { put(DBContract.USER_INFO.COLUMN_LOGIN_NAME, it) }
                user.gender?.let { put(DBContract.USER_INFO.COLUMN_GENDER, it.value) }
                user.birthday?.let { put(DBContract.USER_INFO.COLUMN_BIRTHDAY, it) }
                user.otherPhone?.takeIf { it.isNotEmpty() }?.let {
                    put(DBContract.USER_INFO.COLUMN_OTHER_PHONE, it)
                }
                user.mobile.takeIf { it != 0L }?.let {
                    put(DBContract.USER_INFO.COLUMN_MOBILE, it)
                }
                user.mood?.let { put(DBContract.USER_INFO.COLUMN_MOOD, it) }
                user.email?.let { put(DBContract.USER_INFO.COLUMN_EMAIL, it) }
                user.openId?.let { put(DBContract.USER_INFO.COLUMN_OPEN_ID, it) }
            }

        val selection = "${DBContract.USER_INFO._ID} = ?"
        val selectionArgs = arrayOf(user.id)

        db
            .query(
                DBContract.USER_INFO.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
            )?.use { cursor ->
                db.beginTransaction()
                try {
                    if (cursor.count <= 0) {
                        db.insert(DBContract.USER_INFO.TABLE_NAME, null, userValues)
                    } else {
                        db.update(DBContract.USER_INFO.TABLE_NAME, userValues, selection, selectionArgs)
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
    }

    @Synchronized
    private fun insertOrUpdateFriendInfo(
        db: SQLiteDatabase,
        user: UserProfileEntity
    ) {
        val friendValues =
            ContentValues().apply {
                user.avatarId?.let { put(UserProfileEntry.COLUMN_AVATAR_URL, it) }
                user.nickName?.let { put(UserProfileEntry.COLUMN_NICKNAME, it) }
                user.name?.let { put(UserProfileEntry.COLUMN_NAME, it) }
                user.customerName?.let { put(UserProfileEntry.COLUMN_CUSTOMER_NAME, it) }
                user.customerDescription?.let {
                    put(UserProfileEntry.COLUMN_CUSTOMER_DESCRIPTION, it)
                }
                user.customerBusinessCardUrl?.let {
                    put(UserProfileEntry.COLUMN_CUSTOMER_BUSINESS_CARD_URL, it)
                }
                user.mood?.let { put(UserProfileEntry.COLUMN_SIGNATURE, it) }
                user.alias?.let { put(UserProfileEntry.COLUMN_ALIAS, it) }
                user.roomId?.let { put(UserProfileEntry.COLUMN_ROOM_ID, it) }
                user.openId?.let { put(UserProfileEntry.COLUMN_OPEN_ID, it) }
                put(UserProfileEntry.COLUMN_BLOCK, if (user.isBlock) 1 else 0)
                put(UserProfileEntry.COLUMN_COLLECTION, user.isCollection.toString())
                user.type?.let { put(UserProfileEntry.COLUMN_RELATION, it.value) }
                user.userType?.let { put(UserProfileEntry.COLUMN_USER_TYPE, it.userType) }
                user.department?.let { put(UserProfileEntry.COLUMN_DEPARTMENT, it) }
                user.duty?.let { put(UserProfileEntry.COLUMN_DUTY, it) }
            }

        val selection = "${UserProfileEntry.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(user.id)

        db
            .query(
                UserProfileEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
            )?.use { cursor ->
                db.beginTransaction()
                try {
                    if (cursor.count <= 0) {
                        friendValues.put(UserProfileEntry.COLUMN_ID, user.id)
                        db.insert(UserProfileEntry.TABLE_NAME, null, friendValues)
                    } else {
                        db.update(UserProfileEntry.TABLE_NAME, friendValues, selection, selectionArgs)
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
    }

    fun queryMembersFromUser(memberIds: List<String>) =
        flow {
            if (memberIds.isEmpty()) return@flow
            openDatabase()
                .query(
                    UserProfileEntry.TABLE_NAME,
                    null,
                    UserProfileEntry.COLUMN_ID + " IN (" + generatePlaceholdersForIn(memberIds.size) + ")",
                    memberIds.toTypedArray(),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val users: MutableList<UserProfileEntity> = Lists.newArrayList()
                    while (cursor.moveToNext()) {
                        val isBlock = Tools.getDbInt(cursor, UserProfileEntry.COLUMN_BLOCK)
                        if (1 != isBlock &&
                            User.Status.DISABLE !=
                            Tools.getDbString(
                                cursor,
                                UserProfileEntry.COLUMN_STATUS
                            )
                        ) {
                            users.add(UserProfileEntity.getEntity(cursor))
                        }
                    }
                    emit(users)
                } ?: emit(emptyList())
        }.flowOn(Dispatchers.IO).catch { emit(emptyList()) }

    fun queryFriends(callback: (List<UserProfileEntity>) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            openDatabase()
                .query(
                    UserProfileEntry.TABLE_NAME,
                    null,
                    UserProfileEntry.COLUMN_STATUS + " !=?" +
                        " AND " + UserProfileEntry.COLUMN_RELATION + " =?" +
                        " AND " + UserProfileEntry.COLUMN_BLOCK + " !=?",
                    arrayOf(User.Status.DISABLE, AccountType.FRIEND.value.toString(), "1"),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val friends: MutableList<UserProfileEntity> = Lists.newArrayList()
                    while (cursor.moveToNext()) {
                        friends.add(UserProfileEntity.getEntity(cursor))

                        for (f in friends) {
                            f.labels = getFriendLabels(f.id)
                        }
                    }
                    callback.invoke(friends)
                } ?: callback.invoke(Lists.newArrayList())
        }

    fun queryFriends() =
        flow {
            openDatabase()
                .query(
                    UserProfileEntry.TABLE_NAME,
                    null,
                    UserProfileEntry.COLUMN_STATUS + " !=?" +
                        " AND " + UserProfileEntry.COLUMN_RELATION + " =?" +
                        " AND " + UserProfileEntry.COLUMN_BLOCK + " !=?",
                    arrayOf(User.Status.DISABLE, AccountType.FRIEND.value.toString(), "1"),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val friends: MutableList<UserProfileEntity> = Lists.newArrayList()
                    while (cursor.moveToNext()) {
                        friends.add(UserProfileEntity.getEntity(cursor))
                        for (f in friends) {
                            f.labels = getFriendLabels(f.id)
                        }
                    }
                    emit(friends)
                } ?: emit(emptyList())
        }.flowOn(Dispatchers.IO).catch { emit(emptyList()) }

    private suspend fun getFriendLabels(id: String) =
        withDatabase { db ->
            db
                .query(
                    DBContract.FriendsLabelRel.TABLE_NAME + " AS f INNER JOIN " + DBContract.LabelEntry.TABLE_NAME +
                        " AS u ON u._id = f.label_id",
                    arrayOf("u._id", "u.name"),
                    "f.friend_id =?",
                    arrayOf(id),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val labels: MutableList<Label> = Lists.newArrayList()
                    while (cursor.moveToNext()) {
                        val labelId = Tools.getDbString(cursor, DBContract.LabelEntry._ID)
                        val name = Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_NAME)
                        val label = Label()
                        label.id = labelId
                        label.name = name
                        labels.add(label)
                    }
                    labels
                } ?: run { emptyList() }
        }

    fun queryAllContactsByName(
        keyWord: String,
        userId: String
    ) = flow {
        openDatabase()
            .query(
                UserProfileEntry.TABLE_NAME,
                null,
                UserProfileEntry.COLUMN_ID + " !=?" +
                    " AND (" + UserProfileEntry.COLUMN_NICKNAME + " LIKE ?" +
                    " OR " + UserProfileEntry.COLUMN_ALIAS + " LIKE ?" +
                    " OR " + UserProfileEntry.COLUMN_NAME + " LIKE ?)" +
                    " AND " + UserProfileEntry.COLUMN_BLOCK + " != ?" +
                    " AND " + UserProfileEntry.COLUMN_STATUS + " != ?",
                arrayOf(userId, "%$keyWord%", "%$keyWord%", "%$keyWord%", "1", User.Status.DISABLE),
                null,
                null,
                null
            )?.use { cursor ->
                val users: MutableList<UserProfileEntity> = Lists.newArrayList()
                while (cursor.moveToNext()) {
                    users.add(UserProfileEntity.getEntity(cursor))
                }
                emit(users)
            } ?: emit(emptyList())
    }.flowOn(Dispatchers.IO).catch { emit(mutableListOf<UserProfileEntity>()) }

    fun queryBlockFriends(callback: (List<UserProfileEntity>) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            openDatabase()
                .query(
                    UserProfileEntry.TABLE_NAME,
                    null,
                    UserProfileEntry.COLUMN_BLOCK + " = ?" +
                        " AND " + UserProfileEntry.COLUMN_STATUS + " != ?" +
                        " AND " + UserProfileEntry.COLUMN_RELATION + " = ?",
                    arrayOf("1", User.Status.DISABLE, AccountType.FRIEND.value.toString()),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val friends: MutableList<UserProfileEntity> = mutableListOf()
                    while (cursor.moveToNext()) {
                        friends.add(UserProfileEntity.getEntity(cursor))
                    }
                    for (f in friends) {
                        f.labels = getFriendLabels(f.id)
                    }
                } ?: callback.invoke(Lists.newArrayList())
        }

    fun queryEmployeeList(callback: (List<UserProfileEntity>) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db
                    .query(
                        UserProfileEntry.TABLE_NAME,
                        null,
                        UserProfileEntry.COLUMN_STATUS + " !=?" +
                            " AND " + UserProfileEntry.COLUMN_BLOCK + " !=?",
                        arrayOf(User.Status.DISABLE, "1"),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        val users: MutableList<UserProfileEntity> = mutableListOf()
                        while (cursor.moveToNext()) {
                            users.add(UserProfileEntity.getEntity(cursor))
                        }
                        callback.invoke(users)
                    } ?: callback.invoke(Lists.newArrayList())
            }
        }

    suspend fun queryEmployeeList() =
        withDatabase { db ->
            db
                .query(
                    UserProfileEntry.TABLE_NAME,
                    null,
                    UserProfileEntry.COLUMN_STATUS + " !=?" +
                        " AND " + UserProfileEntry.COLUMN_BLOCK + " !=?",
                    arrayOf(User.Status.DISABLE, "1"),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val users: MutableList<UserProfileEntity> = mutableListOf()
                    while (cursor.moveToNext()) {
                        users.add(UserProfileEntity.getEntity(cursor))
                    }
                    users
                } ?: run { emptyList() }
        }

    @Synchronized
    fun insertSearchHistory(
        content: String,
        time: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            val values = ContentValues()
            values.put(DBContract.SEARCH_HISTORY.COLUMN_TIME, time)
            values.put(DBContract.SEARCH_HISTORY.COLUMN_CONTENT, content)
            db
                .query(DBContract.SEARCH_HISTORY.TABLE_NAME, null, null, null, null, null, null)
                ?.use { cursor ->
                    db.beginTransaction()
                    try {
                        while (cursor.moveToNext()) {
                            val mSearchBean =
                                SearchBean(
                                    Tools.getDbInt(cursor, DBContract.SEARCH_HISTORY.COLUMN_ID),
                                    Tools.getDbString(cursor, DBContract.SEARCH_HISTORY.COLUMN_CONTENT),
                                    Tools.getDbString(cursor, DBContract.SEARCH_HISTORY.COLUMN_TIME)
                                )
                            if (content == mSearchBean.content) {
                                val whereClause = DBContract.SEARCH_HISTORY.COLUMN_ID + " = ?"
                                val whereArgs = arrayOf(mSearchBean.id.toString())
                                db.delete(DBContract.SEARCH_HISTORY.TABLE_NAME, whereClause, whereArgs)
                            }
                        }

                        db.insert(DBContract.SEARCH_HISTORY.TABLE_NAME, null, values)

                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                }
        }
    }

    fun querySearchHistory(callback: (List<SearchBean>) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                val order = DBContract.SEARCH_HISTORY.COLUMN_TIME + " DESC"
                db
                    .query(
                        DBContract.SEARCH_HISTORY.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        order,
                        "10"
                    )?.use { cursor ->
                        val mSearchHistory: MutableList<SearchBean> = mutableListOf()
                        while (cursor.moveToNext()) {
                            mSearchHistory.add(
                                SearchBean(
                                    Tools.getDbInt(cursor, DBContract.SEARCH_HISTORY.COLUMN_ID),
                                    Tools.getDbString(cursor, DBContract.SEARCH_HISTORY.COLUMN_CONTENT),
                                    Tools.getDbString(cursor, DBContract.SEARCH_HISTORY.COLUMN_TIME)
                                )
                            )
                        }
                        callback.invoke(mSearchHistory)
                    } ?: callback.invoke(Lists.newArrayList())
            }
        }

    fun queryCustomBossServiceId(
        customerId: String,
        callback: (String) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db
                .query(
                    ChatRoomEntry.TABLE_NAME,
                    null,
                    ChatRoomEntry.COLUMN_OWNER_ID + "=? AND " + ChatRoomEntry.COLUMN_TYPE + "=? AND " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE + " =?",
                    arrayOf(customerId, "services", "BOSS"),
                    null,
                    null,
                    null
                )?.use {
                    if (it.moveToFirst()) {
                        val roomId = Tools.getDbString(it, ChatRoomEntry._ID)
                        callback.invoke(roomId)
                    } else {
                        callback.invoke("")
                    }
                } ?: callback.invoke("")
        }
    }

    fun querySourceTypeFromLastMessage(roomId: String) =
        flow {
            openDatabase()
                .query(
                    DBContract.LastMessageEntry.TABLE_NAME,
                    arrayOf(DBContract.LastMessageEntry.COLUMN_SOURCE_TYPE),
                    DBContract.LastMessageEntry.COLUMN_ROOM_ID + " =?",
                    arrayOf(roomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val sourceType =
                            Tools.getDbString(
                                cursor,
                                DBContract.LastMessageEntry.COLUMN_SOURCE_TYPE
                            )
                        emit(sourceType)
                    } else {
                        emit("")
                    }
                } ?: emit("")
        }.flowOn(Dispatchers.IO).catch { emit("") }

    fun queryTypeFromLastMessage(
        roomId: String,
        callback: (String) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db
                .query(
                    DBContract.LastMessageEntry.TABLE_NAME,
                    arrayOf(DBContract.LastMessageEntry.COLUMN_TYPE),
                    DBContract.LastMessageEntry.COLUMN_ROOM_ID + " = ?",
                    arrayOf(roomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val type = Tools.getDbString(cursor, DBContract.LastMessageEntry.COLUMN_TYPE)
                        callback.invoke(type)
                    } else {
                        callback.invoke("")
                    }
                } ?: callback.invoke("")
        }
    }

    fun querySenderIdFromLastMessage(roomId: String) =
        flow {
            openDatabase()
                .query(
                    DBContract.LastMessageEntry.TABLE_NAME,
                    arrayOf(DBContract.LastMessageEntry.COLUMN_SENDER_ID),
                    DBContract.LastMessageEntry.COLUMN_ROOM_ID + " = ?",
                    arrayOf(roomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val senderId = Tools.getDbString(cursor, DBContract.LastMessageEntry.COLUMN_SENDER_ID)
                        emit(senderId)
                    } else {
                        emit("")
                    }
                } ?: emit("")
        }.flowOn(Dispatchers.IO).catch { emit("") }

    fun queryContentFromLastMessage(roomId: String) =
        flow {
            openDatabase()
                .query(
                    DBContract.LastMessageEntry.TABLE_NAME,
                    arrayOf(DBContract.LastMessageEntry.COLUMN_CONTENT),
                    DBContract.LastMessageEntry.COLUMN_ROOM_ID + " = ?",
                    arrayOf(roomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val content = Tools.getDbString(cursor, DBContract.LastMessageEntry.COLUMN_CONTENT)
                        emit(content)
                    } else {
                        emit("")
                    }
                } ?: emit("")
        }.flowOn(Dispatchers.IO).catch { emit("") }

    fun querySendTimeFromLastMessage(roomId: String) =
        flow {
            openDatabase()
                .query(
                    DBContract.LastMessageEntry.TABLE_NAME,
                    arrayOf(DBContract.LastMessageEntry.COLUMN_SEND_TIME),
                    DBContract.LastMessageEntry.COLUMN_ROOM_ID + " = ?",
                    arrayOf(roomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val sendTime = Tools.getDbLong(cursor, DBContract.LastMessageEntry.COLUMN_SEND_TIME)
                        emit(sendTime)
                    } else {
                        emit(0L)
                    }
                } ?: emit(0L)
        }.flowOn(Dispatchers.IO).catch { emit(0L) }

    fun queryFlagFromLastMessage(roomId: String) =
        flow {
            openDatabase()
                .query(
                    DBContract.LastMessageEntry.TABLE_NAME,
                    arrayOf(DBContract.LastMessageEntry.COLUMN_FLAG),
                    DBContract.LastMessageEntry.COLUMN_ROOM_ID + " = ?",
                    arrayOf(roomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val flag = Tools.getDbInt(cursor, DBContract.LastMessageEntry.COLUMN_FLAG)
                        emit(flag)
                    } else {
                        emit(-99)
                    }
                } ?: emit(-99)
        }.flowOn(Dispatchers.IO).catch { emit(-99) }

    fun querySenderNameFromLastMessage(roomId: String) =
        flow {
            openDatabase()
                .query(
                    DBContract.LastMessageEntry.TABLE_NAME,
                    arrayOf(DBContract.LastMessageEntry.COLUMN_SENDER_NAME),
                    DBContract.LastMessageEntry.COLUMN_ROOM_ID + " = ?",
                    arrayOf(roomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val senderName = Tools.getDbString(cursor, DBContract.LastMessageEntry.COLUMN_SENDER_NAME)
                        emit(senderName)
                    } else {
                        emit("")
                    }
                } ?: emit("")
        }.flowOn(Dispatchers.IO).catch { emit("") }

    @Synchronized
    fun clearChatRoomData() =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    db.execSQL("DELETE FROM " + ChatRoomEntry.TABLE_NAME)
                    db.execSQL("DELETE FROM " + MessageEntry.TABLE_NAME)
                    db.execSQL("DELETE FROM " + AccountRoomRel.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.TodoEntry.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.API_INFO.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.BroadcastTopicEntry.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.BossServiceNumberContactEntry.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.GroupEntry.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.BusinessEntry.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.StatisticsEntry.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.StickerPackageEntry.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.StickerItemEntry.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.EntityTopicRel.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.FriendsLabelRel.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.SyncGroupEntry.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.GroupMemberEntry.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.LabelEntry.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.RecommendEntry.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.SearchRecordEntry.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.SEARCH_HISTORY.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.SearchLabelHistoryEntry.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.ServiceNumEntry.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.ServiceNumberAgentRel.TABLE_NAME)
                    db.execSQL("DELETE FROM " + DBContract.USER_INFO.TABLE_NAME)
                    db.execSQL("DELETE FROM " + UserProfileEntry.TABLE_NAME)
                    db.setTransactionSuccessful()
                } catch (ignored: Exception) {
                } finally {
                    try {
                        db.endTransaction()
                    } catch (ignored: Exception) {
                    }
                }
            }
        }

    @Synchronized
    fun saveByAccountIdAndRoomId(
        accountId: String,
        roomId: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put(AccountRoomRel.COLUMN_ID, roomId + accountId)
                values.put(AccountRoomRel.COLUMN_ACCOUNT_ID, accountId)
                values.put(AccountRoomRel.COLUMN_ROOM_ID, roomId)
                db.replace(
                    AccountRoomRel.TABLE_NAME,
                    null,
                    values
                )
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun batchSaveByRoomIdsAndAccountIds(multimap: Multimap<String, String>) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val startTime = System.currentTimeMillis()
                    // 先刪除現有數據
                    val roomIdsInClause =
                        buildString {
                            append(AccountRoomRel.COLUMN_ROOM_ID)
                            append(" IN (")
                            append(multimap.keySet().joinToString(",") { "'$it'" })
                            append(")")
                        }

                    db.delete(AccountRoomRel.TABLE_NAME, roomIdsInClause, null)
                    // 插入新數據
                    multimap.entries().forEach { (roomId, accountId) ->
                        val contentValues = ContentValues()
                        contentValues.put(AccountRoomRel.COLUMN_ID, roomId)
                        contentValues.put(AccountRoomRel.COLUMN_ACCOUNT_ID, roomId + accountId)
                        contentValues.put(AccountRoomRel.COLUMN_ROOM_ID, roomId)
                        db.replace(AccountRoomRel.TABLE_NAME, null, contentValues)
                    }
                    db.setTransactionSuccessful()
                    val executionTime = (System.currentTimeMillis() - startTime) / 1000.0
                    Log.d(
                        "DatabaseManager",
                        "batchSaveByRoomIdsAndAccountIds " +
                            "roomId count->${multimap.keySet().size}, " +
                            "count->${multimap.values().size}, " +
                            "useTime->$executionTime /s"
                    )
                } finally {
                    db.endTransaction()
                }
            }
        }

    @Synchronized
    fun batchSaveByAccountIdsAndRoomId(
        roomId: String,
        accountIds: List<String>
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val startTime = System.currentTimeMillis()
                accountIds.forEachIndexed { index, accountId ->
                    if (index == 0) {
                        // 根據當前用戶id和圈子id刪除圈子成員
                        val whereClause = "${AccountRoomRel.COLUMN_ROOM_ID} = ?"
                        val whereArgs = arrayOf(roomId)
                        db.delete(AccountRoomRel.TABLE_NAME, whereClause, whereArgs)
                    }

                    val contentValues = ContentValues()
                    contentValues.put(AccountRoomRel.COLUMN_ID, roomId)
                    contentValues.put(AccountRoomRel.COLUMN_ACCOUNT_ID, roomId + accountId)
                    contentValues.put(AccountRoomRel.COLUMN_ROOM_ID, roomId)
                    db.replace(AccountRoomRel.TABLE_NAME, null, contentValues)
                }
                db.setTransactionSuccessful()
                val executionTime = (System.currentTimeMillis() - startTime) / 1000.0
                Log.d(
                    "DatabaseManager",
                    "batchSaveByAccountIdsAndRoomId " +
                        "roomId-->$roomId, " +
                        "count-->${accountIds.size}, " +
                        "useTime-->$executionTime /s"
                )
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun deleteRelByRoomId(roomId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val whereClause = AccountRoomRel.COLUMN_ROOM_ID + " = ?"
                    val whereArgs = arrayOf(roomId)
                    db.delete(AccountRoomRel.TABLE_NAME, whereClause, whereArgs)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    @Synchronized
    fun deleteRelByRoomIdAndAccountId(
        roomId: String,
        accountId: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val whereClause = (
                    AccountRoomRel.COLUMN_ACCOUNT_ID + " = ?" +
                        " AND " + AccountRoomRel.COLUMN_ROOM_ID + " = ?"
                )
                val whereArgs = arrayOf<String>(accountId, roomId)
                db.delete(AccountRoomRel.TABLE_NAME, whereClause, whereArgs)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    // findMemberIdsByRoomId for java
    fun findMemberIdsByRoomId(
        roomId: String,
        callback: (List<String>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            val memberIds: MutableList<String> = mutableListOf()
            db
                .query(
                    true,
                    AccountRoomRel.TABLE_NAME,
                    null,
                    AccountRoomRel.COLUMN_ROOM_ID + " =?",
                    arrayOf(roomId),
                    null,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val index = AccountRoomRel.getIndex(cursor)
                    while (cursor.moveToNext()) {
                        index[AccountRoomRel.COLUMN_ACCOUNT_ID]?.let {
                            memberIds.add(cursor.getString(it))
                        }
                    }
                    callback.invoke(memberIds)
                } ?: callback.invoke(Lists.newArrayList())
        }
    }

    // findMemberIdsByRoomId for kotlin flow
    fun findMemberIdsByRoomId(roomId: String?) =
        flow {
            val memberIds: MutableList<String> = mutableListOf()
            openDatabase()
                .query(
                    true,
                    AccountRoomRel.TABLE_NAME,
                    null,
                    AccountRoomRel.COLUMN_ROOM_ID + " =?",
                    arrayOf(roomId),
                    null,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val index = AccountRoomRel.getIndex(cursor)
                    while (cursor.moveToNext()) {
                        index[AccountRoomRel.COLUMN_ACCOUNT_ID]?.let {
                            memberIds.add(cursor.getString(it))
                        }
                    }
                    emit(memberIds)
                } ?: emit(mutableListOf())
        }.flowOn(Dispatchers.IO).catch { emit(mutableListOf()) }

    fun getExpiredCount(callback: (Int) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                val now = System.currentTimeMillis()
                db
                    .query(
                        DBContract.BusinessEntry.TABLE_NAME,
                        arrayOf("_id"),
                        DBContract.BusinessEntry.COLUMN_END_TIMESTAMP + ">0" +
                            " AND " + DBContract.BusinessEntry.COLUMN_END_TIMESTAMP + "<?",
                        arrayOf(now.toString()),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val count = cursor.count
                            callback.invoke(count)
                        } else {
                            callback.invoke(0)
                        }
                    } ?: callback.invoke(0)
            }
        }

    fun findBusinessItem(
        businessId: String,
        callback: (BusinessEntity?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        openDatabase()
            .query(
                DBContract.BusinessEntry.TABLE_NAME,
                null,
                DBContract.BusinessEntry._ID + " =?",
                arrayOf(businessId),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = Tools.getDbString(cursor, DBContract.BusinessEntry._ID)
                    val max = maxInteractionTime(id).single()
                    val entity = BusinessEntity.formatByCursor(cursor, max)
                    callback.invoke(entity)
                } else {
                    callback.invoke(null)
                }
            } ?: callback.invoke(null)
    }

    private fun maxInteractionTime(businessId: String) =
        flow {
            openDatabase()
                .query(
                    ChatRoomEntry.TABLE_NAME + " cr " +
                        " INNER JOIN " + MessageEntry.TABLE_NAME + " cm" +
                        " ON cr." + ChatRoomEntry._ID + "=cm." + MessageEntry.COLUMN_ROOM_ID,
                    arrayOf("MAX(cr." + ChatRoomEntry.COLUMN_UPDATE_TIME + ", cm." + MessageEntry.COLUMN_SEND_TIME + ") AS update_time"),
                    "cr." + ChatRoomEntry.COLUMN_BUSINESS_ID + "=?",
                    arrayOf(businessId),
                    null,
                    null,
                    "update_time DESC",
                    "1"
                )?.use { cursor ->
                    var max = -1L
                    if (cursor.moveToNext()) {
                        val updateTime = Tools.getDbLong(cursor, "update_time")
                        max = updateTime
                    }
                    emit(max)
                } ?: emit(-1L)
        }.flowOn(Dispatchers.IO).catch { emit(-1L) }

    fun findExecutorIdMappingByIds(
        businessIds: Set<String>,
        callback: (Map<String, String>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            val roomIdsIn =
                buildString {
                    append(DBContract.BusinessEntry._ID)
                    append(" IN (")
                    append(businessIds.joinToString(",") { "'$it'" })
                    append(")")
                }
            db
                .query(
                    DBContract.BusinessEntry.TABLE_NAME,
                    null,
                    roomIdsIn,
                    null,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val data: MutableMap<String, String> = Maps.newHashMap()
                    while (cursor.moveToNext()) {
                        val id = Tools.getDbString(cursor, DBContract.BusinessEntry._ID)
                        val executorId =
                            Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_EXECUTOR_ID)
                        data[id] = executorId
                    }
                    callback.invoke(data)
                } ?: callback.invoke(Maps.newHashMap())
        }
    }

    fun findExecutorAvatarAndNameById(
        executorId: String,
        callback: (BusinessEntity?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db
                .query(
                    DBContract.BusinessEntry.TABLE_NAME,
                    arrayOf(DBContract.BusinessEntry.COLUMN_EXECUTOR_AVATAR_ID, DBContract.BusinessEntry.COLUMN_EXECUTOR_NAME),
                    DBContract.BusinessEntry.COLUMN_EXECUTOR_ID + " =?",
                    arrayOf(executorId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val entity =
                            BusinessEntity
                                .Build()
                                .executorId(executorId)
                                .executorAvatarId(
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BusinessEntry.COLUMN_EXECUTOR_AVATAR_ID
                                    )
                                ).executorName(
                                    Tools.getDbString(
                                        cursor,
                                        DBContract.BusinessEntry.COLUMN_EXECUTOR_NAME
                                    )
                                ).build()
                        callback.invoke(entity)
                    } else {
                        callback.invoke(null)
                    }
                } ?: callback.invoke(null)
        }
    }

    fun findAllBusinessEntities(callback: (List<BusinessEntity>) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            val entities: MutableList<BusinessEntity> = mutableListOf()
            openDatabase()
                .query(
                    DBContract.BusinessEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val id = Tools.getDbString(cursor, DBContract.BusinessEntry._ID)
                        val max = maxInteractionTime(id).single()
                        entities.add(BusinessEntity.formatByCursor(cursor, max))
                    }
                    callback.invoke(entities)
                } ?: callback.invoke(entities)
        }

    @Synchronized
    fun updateEndTimestampById(
        id: String,
        endTimestamp: Long
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val contentValues = ContentValues()
                contentValues.put(DBContract.BusinessEntry.COLUMN_END_TIMESTAMP, endTimestamp)
                val whereClause = DBContract.BusinessEntry._ID + " = ?"
                val whereArgs = arrayOf(id)
                db
                    .update(
                        DBContract.BusinessEntry.TABLE_NAME,
                        contentValues,
                        whereClause,
                        whereArgs
                    ).toLong()
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    fun findLabelById(
        id: String,
        callback: (Label?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db
                .query(
                    DBContract.LabelEntry.TABLE_NAME,
                    null,
                    DBContract.LabelEntry._ID + " = ?",
                    arrayOf(id),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val label =
                            Label(
                                Tools.getDbString(cursor, DBContract.LabelEntry._ID),
                                Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_NAME),
                                JsonHelper.getInstance().from(
                                    Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_USER_IDS),
                                    object : TypeToken<List<String?>?>() {}.type
                                ),
                                Tools.getDbLong(cursor, DBContract.LabelEntry.COLUMN_CREATE_TIME),
                                Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_OWNER_ID),
                                "true" == Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_READ_ONLY),
                                "true" == Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_DELETED)
                            )
                        callback.invoke(label)
                    } else {
                        callback.invoke(null)
                    }
                } ?: callback.invoke(null)
        }
    }

    @Synchronized
    fun saveLabel(label: Label) =
        flow {
            val db = openDatabase()
            db.beginTransaction()
            try {
                val values =
                    ContentValues().apply {
                        put(DBContract.LabelEntry._ID, label.id)
                        put(DBContract.LabelEntry.COLUMN_NAME, label.name)
                        put(
                            DBContract.LabelEntry.COLUMN_USER_IDS,
                            JsonHelper.getInstance().toJson(label.userIds)
                        )
                        put(DBContract.LabelEntry.COLUMN_CREATE_TIME, label.createTime)
                        put(DBContract.LabelEntry.COLUMN_OWNER_ID, label.ownerId)
                        put(
                            DBContract.LabelEntry.COLUMN_READ_ONLY,
                            if (label.isReadOnly) "true" else "false"
                        )
                        put(
                            DBContract.LabelEntry.COLUMN_DELETED,
                            if (label.isDeleted) "true" else "false"
                        )
                    }

                val isSaved = db.replace(DBContract.LabelEntry.TABLE_NAME, null, values) > 0L
                db.setTransactionSuccessful()
                emit(isSaved)
            } catch (e: Exception) {
                emit(false)
            } finally {
                db.endTransaction()
            }
        }.flowOn(Dispatchers.IO)

    @Synchronized
    fun saveLabelWithUserIds(
        label: Label,
        userIds: List<String>
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put(DBContract.LabelEntry._ID, label.id)
                values.put(DBContract.LabelEntry.COLUMN_NAME, label.name)
                values.put(
                    DBContract.LabelEntry.COLUMN_USER_IDS,
                    JsonHelper.getInstance().toJson(label.userIds)
                )
                values.put(DBContract.LabelEntry.COLUMN_CREATE_TIME, label.createTime)
                values.put(DBContract.LabelEntry.COLUMN_OWNER_ID, label.ownerId)
                values.put(
                    DBContract.LabelEntry.COLUMN_READ_ONLY,
                    if (label.isReadOnly) "true" else "false"
                )
                values.put(
                    DBContract.LabelEntry.COLUMN_DELETED,
                    if (label.isDeleted) "true" else "false"
                )
                db.replace(DBContract.LabelEntry.TABLE_NAME, null, values) > 0L
                saveFriendsLabelViaUserIds(label.id, userIds)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun saveFriendsLabelViaUserIds(
        labelId: String,
        userIds: List<String>
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                for (userId in userIds) {
                    val values = ContentValues()
                    values.put(DBContract.FriendsLabelRel.COLUMN_ID, userId + labelId)
                    values.put(DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID, userId)
                    values.put(DBContract.FriendsLabelRel.COLUMN_LABEL_ID, labelId)
                    db.replace(DBContract.FriendsLabelRel.TABLE_NAME, null, values)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    private fun findFavouriteLabels() =
        flow {
            openDatabase()
                .query(
                    DBContract.FriendsLabelRel.TABLE_NAME + " AS f " +
                        " INNER JOIN " + DBContract.LabelEntry.TABLE_NAME + " AS l " +
                        " ON l." + DBContract.LabelEntry._ID + "= f." + DBContract.FriendsLabelRel.COLUMN_LABEL_ID,
                    arrayOf("f.*"),
                    "l." + DBContract.LabelEntry.COLUMN_READ_ONLY + "=? ",
                    arrayOf("true"),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val favouriteIds: MutableList<String> = mutableListOf()
                    while (cursor.moveToNext()) {
                        favouriteIds.add(
                            Tools.getDbString(
                                cursor,
                                DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID
                            )
                        )
                    }
                    emit(favouriteIds)
                } ?: emit(mutableListOf())
        }.flowOn(Dispatchers.IO).catch { emit(mutableListOf()) }

    fun findAllChatRoomSource(
        source: ChatRoomSource,
        userId: String,
        callback: (List<ChatRoomEntity>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        val dateTime = System.currentTimeMillis()
        val entities: MutableList<ChatRoomEntity> = Lists.newArrayList()
        openDatabase()
            .query(
                ChatRoomEntry.TABLE_NAME,
                null,
                ChatRoomEntry.COLUMN_LIST_CLASSIFY + "=? AND " + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " =?",
                arrayOf(source.name, "N"),
                null,
                null,
                null
            )?.use { cursor ->
                val assemblyList = assemblyDetails(cursor, userId, true, true, false, false).single()
                entities.addAll(assemblyList)
            }
        callback.invoke(entities)
        Log.d(
            "Kyle111",
            String.format(
                "findAllChatRoomSource count->%s, use time->%s/秒  ",
                entities.size,
                (System.currentTimeMillis() - dateTime) / 1000.0
            )
        )
    }

    private fun assemblyDetails(
        cursor: Cursor,
        userId: String,
        needLastMessage: Boolean,
        needFailedMessage: Boolean,
        needMembersProfile: Boolean,
        isJoin: Boolean
    ) = flow {
        val entities: MutableList<ChatRoomEntity> = Lists.newArrayList()
        val index = ChatRoomEntry.getIndex()
        while (cursor.moveToNext()) {
            val roomId = Tools.getDbString(cursor, ChatRoomEntry._ID)
            val memberIds: List<String> = findMemberIdsByRoomId(roomId).single()
            var entity: ChatRoomEntity
            val lastMessage: MessageEntity? = null
            var failedMessage: MessageEntity? = null
            if (needFailedMessage) {
                failedMessage =
                    findMessageByRoomIdAndStatusAndLimitOne(
                        roomId,
                        MessageStatus.getFailedErrorStatus(),
                        Sort.DESC
                    ).single()
            }

            var members: List<UserProfileEntity?>? = Lists.newArrayList()
            if (needMembersProfile) {
                members = mutableListOf<UserProfileEntity>() // ChatMemberCacheService.getChatMembers(roomId)
            }
            entity =
                ChatRoomEntity
                    .formatByCursor(
                        index,
                        cursor,
                        isJoin,
                        memberIds,
                        members,
                        lastMessage,
                        failedMessage
                    ).build()

            val lastMessageStr = entity.lastMessageStr
            if ("{}" != lastMessageStr) {
                entity.lastMessage =
                    JsonHelper.getInstance().from(
                        entity.lastMessageStr,
                        MessageEntity::class.java
                    )
            }

//            if (entity.lastMessage != null && MessageType.AT == entity.lastMessage.type) {
//                entity.members = ChatMemberCacheService.getChatMembers(entity.id)
//            }

            if (ChatRoomType.discuss == entity.type ||
                (
                    ChatRoomType.group == entity.type &&
                        Strings.isNullOrEmpty(
                            entity.avatarId
                        )
                )
            ) {
                getMemberAvatarData(entity.id, userId, 4) { data ->
                    entity.memberAvatarData = data
                }
            }

            // EVAN_FLAG 2020-04-21 (1.10.0) My collection is compared and judged, if it is a friend,
            //  and there is no object content, and whether the user is included after the intersection member
            if (ChatRoomType.friend == entity.type && Strings.isNullOrEmpty(entity.businessId)) {
                val favouriteUserIds = findFavouriteLabels().single()
                favouriteUserIds.toMutableList().retainAll(memberIds)
                entity.isFavourite = favouriteUserIds.isNotEmpty()
            }
            if (ChatRoomType.services == entity.type) {
                val consultsUnreadNumber: Int = getConsultsUnreadNumber(entity.id).single()
                if (consultsUnreadNumber > 0) {
                    entity.consultSrcUnreadNumber = consultsUnreadNumber
                }
            }
            entities.add(entity)
        }
        emit(entities)
    }.flowOn(Dispatchers.IO).catch { emit(mutableListOf()) }

    fun findMessageByRoomIdAndStatusAndLimitOne(
        roomId: String?,
        status: Array<Int?>,
        sort: Sort
    ) = flow {
        val statusIn =
            buildString {
                append(MessageEntry.COLUMN_STATUS)
                append(" IN (")
                append(status.joinToString(",") { "'$it'" })
                append(")")
            }
        openDatabase()
            .query(
                MessageEntry.TABLE_NAME,
                null,
                MessageEntry.COLUMN_ROOM_ID + "=?" +
                    (if (status.isNotEmpty()) " AND $statusIn" else "") +
                    " AND " + MessageEntry.COLUMN_FLAG + " !=?",
                arrayOf(roomId, MessageFlag.DELETED.flag.toString()),
                null,
                null,
                MessageEntry.COLUMN_SEND_TIME + " " + sort.name,
                "1"
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = MessageEntry.getIndex(cursor)
                    val message = formatByCursor(index, cursor)
                    emit(message)
                } else {
                    emit(null)
                }
            }
    }.flowOn(Dispatchers.IO).catch { emit(null) }

    private fun getConsultsUnreadNumber(roomId: String) =
        flow {
            openDatabase()
                .query(
                    ChatRoomEntry.TABLE_NAME + " AS r ",
                    arrayOf("SUM(CASE WHEN r." + ChatRoomEntry.COLUMN_TYPE + "='SUBSCRIBE' " + " THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS consults_unread_number "),
                    "( r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + " > 0 " + " OR r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + " = -1 )" +
                        " AND r." + ChatRoomEntry.COLUMN_CONSULT_ROOM_ID + "= ?",
                    arrayOf(roomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val consultsUnreadNumber = Tools.getDbInt(cursor, "consults_unread_number")
                        emit(consultsUnreadNumber)
                    } else {
                        emit(-1)
                    }
                } ?: emit(-1)
        }.flowOn(Dispatchers.IO).catch { emit(-1) }

    /**
     * findRoomByChatRoomSource 使用案例：
     * 當內部服務號成員進線內部服務號並觸發AI服務時，
     * 其他成員接手服務後，應該解除AI服務狀態，從ROBOT SERVICE 轉至 ONLINE
     * 但進線者因為 list_classify = MAIN 以致於撈資料失誤，
     * 故改採以下方法取得正確的資料
     */
    fun findRoomByChatRoomSource(
        source: ChatRoomSource,
        userId: String
    ) = flow {
        val entities: MutableList<ChatRoomEntity> = Lists.newArrayList()
        openDatabase()
            .query(
                ChatRoomEntry.TABLE_NAME,
                null,
                "(" + ChatRoomEntry.COLUMN_LIST_CLASSIFY + "=? OR (" + ChatRoomEntry.COLUMN_LIST_CLASSIFY + " = ?" +
                    " AND " + ChatRoomEntry.COLUMN_TYPE + " = 'subscribe'))" +
                    " AND " + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = 'N'",
                arrayOf(source.name, ChatRoomSource.MAIN.toString()),
                null,
                null,
                null
            )?.use { cursor ->
                val assemblyList = assemblyDetails(cursor, userId, true, true, false, false).single()
                entities.addAll(assemblyList)
                emit(entities)
            } ?: emit(emptyList())
    }.flowOn(Dispatchers.IO).catch { emit(Lists.newArrayList()) }

    fun findAllByChatRoomsAndExcludeType(
        userId: String,
        type: ChatRoomType,
        page: Int,
        limit: Int,
        needLastMessage: Boolean,
        needFailedMessage: Boolean,
        needMembersProfile: Boolean,
        callback: (List<ChatRoomEntity>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        val dateTime = System.currentTimeMillis()
        val entities: MutableList<ChatRoomEntity> = Lists.newArrayList()
        openDatabase()
            .query(
                ChatRoomEntry.TABLE_NAME,
                null,
                ChatRoomEntry.COLUMN_TYPE + " !=?",
                arrayOf(type.name, (page * (limit - 1)).toString(), limit.toString()),
                null,
                null,
                ChatRoomEntry.COLUMN_UPDATE_TIME + " DESC",
                "?, ?"
            )?.use { cursor ->
                val assemblyList =
                    assemblyDetails(
                        cursor,
                        userId,
                        needLastMessage,
                        needFailedMessage,
                        needMembersProfile,
                        true
                    ).single()
                entities.addAll(assemblyList)
                callback.invoke(entities)
                CELog.d(
                    String.format(
                        "room find all limit by %s, page->%s, count->%s, use time->%s/秒  ",
                        "All",
                        "all page",
                        entities.size,
                        (System.currentTimeMillis() - dateTime) / 1000.0
                    )
                )
            }
    }

    fun findAllChatRoomsByType(
        userId: String,
        types: List<ChatRoomType>,
        needLastMessage: Boolean,
        needFailedMessage: Boolean,
        needMembersProfile: Boolean
    ) = flow {
        val dateTime = System.currentTimeMillis()
        val entities: MutableList<ChatRoomEntity> = Lists.newArrayList()
        openDatabase()
            .query(
                ChatRoomEntry.TABLE_NAME,
                null,
                "UPPER(" + ChatRoomEntry.COLUMN_TYPE + ") IN (" + generatePlaceholdersForIn(types.size) + ") AND " + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = 'N'",
                toUpperCaseOfChatRoomType(types),
                null,
                null,
                null
            )?.use { cursor ->
                val assemblyList =
                    assemblyDetails(
                        cursor,
                        userId,
                        needLastMessage,
                        needFailedMessage,
                        needMembersProfile,
                        true
                    ).single()
                entities.addAll(assemblyList)
                emit(entities)
            } ?: emit(emptyList())

        CELog.d(
            String.format(
                "room find all limit by %s, page->%s, count->%s, use time->%s/秒  ",
                "All",
                "all page",
                entities.size,
                (System.currentTimeMillis() - dateTime) / 1000.0
            )
        )
    }.flowOn(Dispatchers.IO).catch { emit(Lists.newArrayList()) }

    fun findAllChatRoomsByTypeAndName(
        userId: String,
        types: List<ChatRoomType>,
        keyWord: String,
        needLastMessage: Boolean,
        needFailedMessage: Boolean,
        needMembersProfile: Boolean
    ) = flow {
        val dateTime = System.currentTimeMillis()
        val entities: MutableList<ChatRoomEntity> = Lists.newArrayList()
        openDatabase()
            .query(
                ChatRoomEntry.TABLE_NAME,
                null,
                "UPPER(" + ChatRoomEntry.COLUMN_TYPE + ") IN (" + generatePlaceholdersForIn(types.size) +
                    ") AND " + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = 'N'" +
                    " AND (" + ChatRoomEntry.COLUMN_TITLE + " LIKE ?" +
                    " OR " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_NAME + " LIKE ?)",
                toUpperCaseOfChatRoomType(types).plus("%$keyWord%").plus("%$keyWord%"),
                null,
                null,
                null
            )?.use { cursor ->
                val assemblyList = assemblyDetails(cursor, userId, needLastMessage, needFailedMessage, needMembersProfile, true).single()
                entities.addAll(assemblyList)
                Log.d(
                    "findAllChatRoomsByTypeAndName",
                    String.format(
                        "room find all limit by %s, page->%s, count->%s, use time->%s/秒  ",
                        "All",
                        "all page",
                        entities.size,
                        (System.currentTimeMillis() - dateTime) / 1000.0
                    )
                )
                emit(entities)
            } ?: emit(Lists.newArrayList<ChatRoomEntity>())
    }.flowOn(Dispatchers.IO).catch { emit(Lists.newArrayList()) }

    fun findAllChatRoomsByKeyword(
        userId: String,
        keyword: String
    ) = flow {
        val dateTime = System.currentTimeMillis()
        val result: MutableList<ChatRoomEntity> = Lists.newArrayList()
        // 建立索引加速查詢
        openDatabase().execSQL(
            "CREATE INDEX IF NOT EXISTS idx_members_roomid " +
                "ON " + DBContract.ChatRoomMemberIdsEntry.TABLE_NAME + "(" + DBContract.ChatRoomMemberIdsEntry.COLUMN_ROOM_ID + ")"
        )
        openDatabase().execSQL(
            "CREATE INDEX IF NOT EXISTS idx_members_memberid " +
                "ON " + DBContract.ChatRoomMemberIdsEntry.TABLE_NAME + "(" + DBContract.ChatRoomMemberIdsEntry.COLUMN_MEMBER_ID + ")"
        )
        openDatabase().execSQL(
            "CREATE INDEX IF NOT EXISTS idx_users_nickName " +
                "ON " + UserProfileEntry.TABLE_NAME + "(" + UserProfileEntry.COLUMN_NICKNAME + ")"
        )
        val selectionArgs =
            arrayOf(
                "%$keyword%",
                "%$keyword%",
                "%$keyword%"
            )
        openDatabase()
            .query(
                true,
                ChatRoomEntry.TABLE_NAME + " cr " +
                    "INNER JOIN " + DBContract.ChatRoomMemberIdsEntry.TABLE_NAME + " m ON cr." + ChatRoomEntry._ID + " = m." + DBContract.ChatRoomMemberIdsEntry.COLUMN_ROOM_ID +
                    " INNER JOIN " + UserProfileEntry.TABLE_NAME + " u ON m." + DBContract.ChatRoomMemberIdsEntry.COLUMN_MEMBER_ID + " = u." + UserProfileEntry.COLUMN_ID,
                arrayOf("cr.*"),
                "((u." + UserProfileEntry.COLUMN_NICKNAME + " LIKE ? " +
                    "OR u." + UserProfileEntry.COLUMN_ALIAS + " LIKE ?) " +
                    "AND cr." + ChatRoomEntry.COLUMN_TYPE + " IN ('" +
                    ChatRoomType.group.name + "', '" +
                    ChatRoomType.discuss.name + "', '" +
                    ChatRoomType.person.name + "')) " +
                    "OR (cr." + ChatRoomEntry.COLUMN_TITLE + " LIKE ? " +
                    "AND cr." + ChatRoomEntry.COLUMN_TYPE + " IN ('" +
                    ChatRoomType.friend.name + "', '" +
                    ChatRoomType.system.name + "', '" +
                    ChatRoomType.group.name + "')) " +
                    "AND cr." + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = 'N' ",
                selectionArgs,
                null,
                null,
                null,
                null
            )?.use { cursor ->
                val assemblyList = assemblyDetails(cursor, userId, true, false, true, true).single()
                result.addAll(assemblyList)
                emit(result)
                Log.d(
                    "Kyle116",
                    String.format(
                        "findAllChatRoomsByKeyword count->%s, use time->%s/秒  ",
                        result.size,
                        (System.currentTimeMillis() - dateTime) / 1000.0
                    )
                )
            } ?: emit(Lists.newArrayList<ChatRoomEntity>())
    }.flowOn(Dispatchers.IO).catch { emit(Lists.newArrayList()) }

    fun findAllChatRoomsByBossServiceNumberId(
        userId: String,
        serviceNumberId: String
    ) = flow {
        val dateTime = System.currentTimeMillis()
        val entities: MutableList<ChatRoomEntity> = mutableListOf()
        openDatabase()
            .query(
                ChatRoomEntry.TABLE_NAME,
                null,
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " =?",
                arrayOf(serviceNumberId),
                null,
                null,
                null
            )?.use { cursor ->
                val assemblyList = assemblyDetails(cursor, userId, true, false, true, true).single()
                entities.addAll(assemblyList)
                Log.d(
                    "findAllChatRoomsByBossServiceNumberId",
                    String.format(
                        "room find all limit by %s, page->%s, count->%s, use time->%s/秒  ",
                        "All",
                        "all page",
                        entities.size,
                        (System.currentTimeMillis() - dateTime) / 1000.0
                    )
                )
                emit(entities)
            } ?: emit(emptyList<ChatRoomEntity>())
    }.flowOn(Dispatchers.IO).catch { emit(Lists.newArrayList()) }

    fun findAllChatRoomsByType(
        userId: String,
        type: ChatRoomType,
        needFindIsCustomName: Boolean
    ) = flow {
        val dateTime = System.currentTimeMillis()
        val entities: MutableList<ChatRoomEntity> = Lists.newArrayList()
        openDatabase()
            .query(
                ChatRoomEntry.TABLE_NAME,
                null,
                "UPPER(" + ChatRoomEntry.COLUMN_TYPE + ") IN (UPPER(?))" +
                    if (!needFindIsCustomName) {
                        " AND " + ChatRoomEntry.COLUMN_IS_CUSTOM_NAME + " = 'N'"
                    } else {
                        ""
                    },
                arrayOf(type.name.uppercase(Locale.getDefault())),
                null,
                null,
                null
            )?.use { cursor ->
                val assemblyList = assemblyDetails(cursor, userId, false, false, false, true).single()
                entities.addAll(assemblyList)
                emit(entities)
                Log.d(
                    "findAllChatRoomsByType",
                    String.format(
                        "room find all limit by %s, page->%s, count->%s, use time->%s/秒  ",
                        "All",
                        "all page",
                        entities.size,
                        (System.currentTimeMillis() - dateTime) / 1000.0
                    )
                )
            } ?: emit(emptyList<ChatRoomEntity>())
    }.flowOn(Dispatchers.IO).catch { emit(Lists.newArrayList()) }

    fun findByIdsAndChatRoomSource2(
        source: ChatRoomSource,
        userId: String,
        roomIds: Set<String>,
        needLastMessage: Boolean,
        needFailedMessage: Boolean,
        needMembersProfile: Boolean,
        callback: (List<ChatRoomEntity>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        val dateTime = System.currentTimeMillis()
        val entities: MutableList<ChatRoomEntity> = Lists.newArrayList()
        val roomIdsIn =
            buildString {
                append(ChatRoomEntry._ID)
                append(" IN (")
                append(roomIds.joinToString(",") { "'$it'" })
                append(")")
            }
        openDatabase()
            .query(
                ChatRoomEntry.TABLE_NAME,
                null,
                ChatRoomEntry.COLUMN_LIST_CLASSIFY + "=?" +
                    " AND " + roomIdsIn + " AND " + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " != 'N'",
                arrayOf(source.name),
                null,
                null,
                null
            )?.use { cursor ->
                val assemblyList = assemblyDetails(cursor, userId, needLastMessage, needFailedMessage, needMembersProfile, false).single()
                entities.addAll(assemblyList)
                callback.invoke(entities)
                Log.d(
                    "findByIdsAndChatRoomSource2",
                    String.format(
                        "room find all by ids and %s , count->%s, use time->%s/second  ",
                        source.name,
                        entities.size,
                        (System.currentTimeMillis() - dateTime) / 1000.0
                    )
                )
            } ?: callback.invoke(Lists.newArrayList())
    }

    fun findByIds(
        userId: String,
        roomIds: List<String>,
        needLastMessage: Boolean,
        needFailedMessage: Boolean,
        needMembersProfile: Boolean,
        callback: (List<ChatRoomEntity>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        val entities: MutableList<ChatRoomEntity> = Lists.newArrayList()
        val roomIdsIn =
            buildString {
                append(ChatRoomEntry._ID)
                append(" IN (")
                append(roomIds.joinToString(",") { "'$it'" })
                append(")")
            }
        val sql =
            "SELECT * FROM " + ChatRoomEntry.TABLE_NAME +
                " WHERE " + roomIdsIn
        openDatabase()
            .query(
                ChatRoomEntry.TABLE_NAME,
                null,
                roomIdsIn.toString(),
                null,
                null,
                null,
                null
            )?.use { cursor ->
                val assemblyList = assemblyDetails(cursor, userId, needLastMessage, needFailedMessage, needMembersProfile, false).single()
                entities.addAll(assemblyList)
                callback.invoke(entities)
            } ?: callback.invoke(Lists.newArrayList())
    }

    @Synchronized
    fun deleteChatRoomAndMessageById(
        roomId: String,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val sessionSelection = ChatRoomEntry._ID + " = ?"
                val messageSelection = MessageEntry.COLUMN_ROOM_ID + " = ?"
                val selectionArgs = arrayOf(roomId)
                val isDeletedChatRoom = db.delete(ChatRoomEntry.TABLE_NAME, sessionSelection, selectionArgs)
                val isDeletedMessage = db.delete(MessageEntry.TABLE_NAME, messageSelection, selectionArgs)
                callback.invoke(isDeletedChatRoom > 0 && isDeletedMessage > 0)
                db.setTransactionSuccessful()
            } catch (_: Exception) {
                callback.invoke(false)
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun deleteAllServiceNumberData() =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val deleteSql = ChatRoomEntry.COLUMN_LIST_CLASSIFY + " =? "
                    val selectionArgs = arrayOf(ChatRoomSource.SERVICE.name)
                    db.delete(ChatRoomEntry.TABLE_NAME, deleteSql, selectionArgs)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    fun getChatRoomMember(
        roomId: String,
        callback: (List<ChatRoomMemberResponse>?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            withDatabase { db ->
                db
                    .query(
                        ChatRoomEntry.TABLE_NAME,
                        arrayOf(ChatRoomEntry.COLUMN_CHAT_ROOM_MEMBER),
                        ChatRoomEntry._ID + "=?",
                        arrayOf(roomId),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val data = Tools.getDbString(cursor, 0)
                            val typeToken = object : TypeToken<List<ChatRoomMemberResponse>>() {}.type
                            val list = JsonHelper.getInstance().from<List<ChatRoomMemberResponse>>(data, typeToken)
                            callback.invoke(list)
                        } else {
                            callback.invoke(emptyList())
                        }
                    }
            }
        } catch (e: Exception) {
            callback.invoke(null)
            // Log or handle the exception
        }
    }

    suspend fun queryOnlineServiceRoomByTime(
        offset: Int,
        limit: Int
    ): MutableList<ChatRoomEntity> =
        withContext(Dispatchers.IO) {
            withDatabase { db ->
                val dateTime = System.currentTimeMillis()
                val result: MutableList<ChatRoomEntity> = Lists.newArrayList()
                // 建立索引加速查詢
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS idx_lastmessage_roomid_sendtime " +
                        "ON " + DBContract.LastMessageEntry.TABLE_NAME + "(" + DBContract.LastMessageEntry.COLUMN_ROOM_ID + ", " + DBContract.LastMessageEntry.COLUMN_SEND_TIME + " DESC)"
                )

//            val sql =
//                ("SELECT c.*, COALESCE(l." + DBContract.LastMessageEntry.COLUMN_SEND_TIME + ", 0) FROM " +
//                        ChatRoomEntry.TABLE_NAME + " c " +
//                        "INNER JOIN (" +
//                        "SELECT roomId, MAX(sendTime) as sendTime " +
//                        "FROM " + DBContract.LastMessageEntry.TABLE_NAME +
//                        " GROUP BY roomId " +
//                        ") l ON c." + ChatRoomEntry._ID + " = l." + DBContract.LastMessageEntry.COLUMN_ROOM_ID +
//                        " WHERE c." + ChatRoomEntry.COLUMN_LIST_CLASSIFY + " = 'SERVICE' " +
//                        "AND c." + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = 'N' " +
//                        "AND (c." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS + " != '" + ServiceNumberStatus.ON_LINE.name + "' " +
//                        "AND  c." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID + " = '')" +
//                        "ORDER BY COALESCE(l." + DBContract.LastMessageEntry.COLUMN_SEND_TIME + ", 0) DESC " +
//                        "LIMIT " + limit + " OFFSET " + offset)
                db
                    .query(
                        ChatRoomEntry.TABLE_NAME + " c " +
                            "INNER JOIN (" +
                            "SELECT roomId, MAX(sendTime) as sendTime " +
                            "FROM " + DBContract.LastMessageEntry.TABLE_NAME +
                            " GROUP BY roomId " +
                            ") l ON c." + ChatRoomEntry._ID + " = l." + DBContract.LastMessageEntry.COLUMN_ROOM_ID,
                        arrayOf("c.*", "COALESCE(l." + DBContract.LastMessageEntry.COLUMN_SEND_TIME + ", 0)"),
                        "c." + ChatRoomEntry.COLUMN_LIST_CLASSIFY + " = 'SERVICE' " +
                            "AND c." + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = 'N' " +
                            "AND (c." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS + " != ? " +
                            "AND  c." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID + " = '')",
                        arrayOf(ServiceNumberStatus.ON_LINE.name, limit.toString(), offset.toString()),
                        null,
                        null,
                        "COALESCE(l." + DBContract.LastMessageEntry.COLUMN_SEND_TIME + ", 0) DESC ",
                        "?, ?"
                    )?.use { cursor ->
                        while (cursor.moveToNext()) {
                            val index = ChatRoomEntry.getIndex(cursor)
                            val entity = ChatRoomEntity.formatByCursor(index, cursor, false).build()
                            Log.d(
                                "Kyle117",
                                "queryOnlineServiceRoomByTime name=" + entity.name + ", status=" + entity.serviceNumberStatus.name + ", type = " + entity.listClassify
                            )
                            result.add(entity)
                        }
                        Log.d(
                            "Kyle117",
                            String.format(
                                "queryOnlineServiceRoomByTime offset=%s, limit=%s, count->%s, use time->%s/秒  ",
                                offset,
                                limit,
                                result.size,
                                (System.currentTimeMillis() - dateTime) / 1000.0
                            )
                        )
                        result
                    } ?: mutableListOf()
            }
        }

    /**
     * 取得除了服務號分組以外的聊天室
     * @return List<ChatRoomEntity>
     */
    fun queryOnlineServiceRoom(selfUserId: String) =
        flow {
            val db = openDatabase()
            val dateTime = System.currentTimeMillis()
            val result: MutableList<ChatRoomEntity> = mutableListOf()

            // 建立索引加速查詢
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_roomid " +
                    "ON " + ChatRoomEntry.TABLE_NAME + "(" + ChatRoomEntry._ID + ")"
            )
            val query = buildFormalQuery(selfUserId)
            db
                .query(
                    ChatRoomEntry.TABLE_NAME,
                    null,
                    query,
                    null,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val index = ChatRoomEntry.getIndex(cursor)
                        val entity = ChatRoomEntity.formatByCursor(index, cursor, false).build()
                        Log.d(
                            "Kyle117",
                            "queryOnlineServiceRoom name=" + entity.name + ", status=" + entity.serviceNumberStatus.name + ", type = " + entity.listClassify
                        )
                        result.add(entity)
                    }
                    Log.d(
                        "Kyle117",
                        String.format(
                            "queryOnlineServiceRoom count->%s, use time->%s/秒  ",
                            result.size,
                            (System.currentTimeMillis() - dateTime) / 1000.0
                        )
                    )
                    emit(result)
                } ?: emit(mutableListOf())
        }.flowOn(Dispatchers.IO).catch { emit(mutableListOf()) }

    /**
     * Open DataBase, query "chat_room" table
     * @param offset 從第幾筆開始抓
     * @param limit 限制幾筆
     * @return List<ChatRoomEntity>
     */
    fun queryServiceRoomByServiceNumberId(
        serviceNumberId: String?,
        offset: Int,
        limit: Int
    ) = flow {
        serviceNumberId?.let {
            val db = openDatabase()
            val dateTime = System.currentTimeMillis()
            val result: MutableList<ChatRoomEntity> = mutableListOf()
            // 建立索引加速查詢
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_lastmessage_roomid_sendtime " +
                    "ON " + DBContract.LastMessageEntry.TABLE_NAME + "(" + DBContract.LastMessageEntry.COLUMN_ROOM_ID + ", " + DBContract.LastMessageEntry.COLUMN_SEND_TIME + " DESC)"
            )

//            val sql =
//                ("SELECT c.*, COALESCE(l." + DBContract.LastMessageEntry.COLUMN_SEND_TIME + ", 0) FROM " +
//                        ChatRoomEntry.TABLE_NAME + " c " +
//                        "INNER JOIN (" +
//                        "SELECT roomId, MAX(sendTime) as sendTime " +
//                        "FROM " + DBContract.LastMessageEntry.TABLE_NAME +
//                        " GROUP BY roomId" +
//                        ") l ON c." + ChatRoomEntry._ID + " = l." + DBContract.LastMessageEntry.COLUMN_ROOM_ID +
//                        " WHERE c." + ChatRoomEntry.COLUMN_LIST_CLASSIFY + " = 'SERVICE' " +
//                        "AND c." + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = 'N' " +
//                        "AND " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " = '" + serviceNumberId + "' " +
//                        "AND " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS + " != '" + ServiceNumberStatus.ON_LINE.status + "' " +
//                        "ORDER BY COALESCE(l." + DBContract.LastMessageEntry.COLUMN_SEND_TIME + ", 0) DESC " +
//                        "LIMIT " + limit + " OFFSET " + offset)
            db
                .query(
                    ChatRoomEntry.TABLE_NAME + " c " +
                        "INNER JOIN (" +
                        "SELECT roomId, MAX(sendTime) as sendTime " +
                        "FROM " + DBContract.LastMessageEntry.TABLE_NAME +
                        " GROUP BY roomId" +
                        ") l ON c." + ChatRoomEntry._ID + " = l." + DBContract.LastMessageEntry.COLUMN_ROOM_ID,
                    arrayOf("c.*", "COALESCE(l." + DBContract.LastMessageEntry.COLUMN_SEND_TIME + ", 0)"),
                    "c." + ChatRoomEntry.COLUMN_LIST_CLASSIFY + " = 'SERVICE' " +
                        "AND c." + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = 'N' " +
                        "AND " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " = ? " +
                        "AND " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS + " != ?",
                    arrayOf(serviceNumberId, ServiceNumberStatus.ON_LINE.status, limit.toString(), offset.toString()),
                    null,
                    null,
                    "COALESCE(l." + DBContract.LastMessageEntry.COLUMN_SEND_TIME + ", 0) DESC ",
                    "?, ?"
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val index = ChatRoomEntry.getIndex(cursor)
                        val entity = ChatRoomEntity.formatByCursor(index, cursor, false).build()
                        result.add(entity)
                    }
                    Log.d(
                        "Kyle117",
                        String.format(
                            "queryServiceRoomByServiceNumberId id -> %s, offset-> %s, count->%s, use time->%s/秒  ",
                            serviceNumberId,
                            offset,
                            result.size,
                            (System.currentTimeMillis() - dateTime) / 1000.0
                        )
                    )
                    emit(result)
                } ?: emit(mutableListOf())
        } ?: emit(mutableListOf())
    }.flowOn(Dispatchers.IO).catch { emit(mutableListOf()) }

    /**
     * Open DataBase, query "chat_room" table, get ChatRoomEntities
     * @param offset 從第幾筆開始抓
     * @param limit 限制幾筆
     * @return
     */
    fun findMainChatRoomData(
        offset: Int,
        limit: Int
    ) = flow {
        val dateTime = System.currentTimeMillis()
        val result: MutableList<ChatRoomEntity> = mutableListOf()
        // 建立索引加速查詢
        openDatabase().execSQL(
            "CREATE INDEX IF NOT EXISTS idx_lastmessage_roomid_sendtime " +
                "ON " + DBContract.LastMessageEntry.TABLE_NAME + "(" + DBContract.LastMessageEntry.COLUMN_ROOM_ID + ", " + DBContract.LastMessageEntry.COLUMN_SEND_TIME + " DESC)"
        )
        openDatabase()
            .query(
                ChatRoomEntry.TABLE_NAME + " c " +
                    "LEFT JOIN (" +
                    "SELECT roomId, MAX(sendTime) as sendTime " +
                    "FROM " + DBContract.LastMessageEntry.TABLE_NAME +
                    " GROUP BY roomId" +
                    ") l ON c." + ChatRoomEntry._ID + " = l." + DBContract.LastMessageEntry.COLUMN_ROOM_ID,
                arrayOf("c.*", "COALESCE(l." + DBContract.LastMessageEntry.COLUMN_SEND_TIME + ", 0)"),
                (
                    "c." + ChatRoomEntry.COLUMN_LIST_CLASSIFY + " = ? " +
                        "AND c." + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = ? " +
                        "AND " + ChatRoomEntry.COLUMN_TYPE + " != ? " +
                        "AND (COALESCE(c." + ChatRoomEntry.COLUMN_DFR_TIME + ", 0) <= 0 OR COALESCE(c." + ChatRoomEntry.COLUMN_DFR_TIME + ", 0) < l." + DBContract.LastMessageEntry.COLUMN_SEND_TIME + ") "
                ),
                arrayOf("MAIN", "N", "broadcast"),
                null,
                null,
                (
                    ChatRoomEntry.COLUMN_SORT_WEIGHTS + " DESC, " +
                        "COALESCE(c." + ChatRoomEntry.COLUMN_UPDATE_TIME + ",0) DESC," +
                        "COALESCE(l." + DBContract.LastMessageEntry.COLUMN_SEND_TIME + ", 0) DESC"
                ),
                "$limit OFFSET $offset"
            )?.use { cursor ->
                Log.d("Kyle116", "count = ${cursor.count}")
                while (cursor.moveToNext()) {
                    val index = ChatRoomEntry.getIndex(cursor)
                    val entity = ChatRoomEntity.formatByCursor(index, cursor, false).build()
                    result.add(entity)
                }
                Log.d(
                    "Kyle116",
                    String.format(
                        "findAllChatRoomSource offset-> %s, count->%s, use time->%s/秒  ",
                        offset,
                        result.size,
                        (System.currentTimeMillis() - dateTime) / 1000.0
                    )
                )
                emit(result)
            } ?: emit(mutableListOf())
    }.flowOn(Dispatchers.IO).catch { emit(mutableListOf()) }

    fun queryChatRoomMember(roomId: String) =
        flow {
            openDatabase()
                .query(
                    ChatRoomEntry.TABLE_NAME,
                    arrayOf(ChatRoomEntry.COLUMN_CHAT_ROOM_MEMBER),
                    ChatRoomEntry._ID + " =?",
                    arrayOf(roomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val data = Tools.getDbString(cursor, 0)
                        val typeToken =
                            object : TypeToken<List<ChatRoomMemberResponse?>?>() {}.type
                        emit(JsonHelper.getInstance().from(data, typeToken))
                    } else {
                        emit(mutableListOf<ChatRoomMemberResponse>())
                    }
                } ?: emit(mutableListOf())
        }.flowOn(Dispatchers.IO).catch { emit(mutableListOf()) }

    fun findByRoomIdAndServiceNumberId(
        roomId: String?,
        serviceNumberId: String,
        callback: (ChatRoomEntity?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        roomId?.let {
            openDatabase()
                .query(
                    ChatRoomEntry.TABLE_NAME,
                    null,
                    ChatRoomEntry._ID + "=? AND " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + "=?",
                    arrayOf(roomId, serviceNumberId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val memberIds = findMemberIdsByRoomId(roomId).single()
                        val members = mutableListOf<UserProfileEntity>() // ChatMemberCacheService.getChatMembers(roomId)
                        val lastMessage =
                            findMessageByRoomIdAndStatusAndLimitOne(
                                roomId,
                                MessageStatus.getValidStatus(),
                                Sort.DESC
                            ).single()
                        val failedMessage =
                            findMessageByRoomIdAndStatusAndLimitOne(
                                roomId,
                                MessageStatus.getFailedErrorStatus(),
                                Sort.DESC
                            ).single()
                        val index = ChatRoomEntry.getIndex(cursor)
                        val entity =
                            ChatRoomEntity
                                .formatByCursor(
                                    index,
                                    cursor,
                                    false,
                                    memberIds,
                                    members,
                                    lastMessage,
                                    failedMessage
                                ).build()
                        callback.invoke(entity)
                    } else {
                        callback.invoke(null)
                    }
                } ?: callback.invoke(null)
        } ?: callback.invoke(null)
    }

    fun findSelfRoomBySelfId(
        selfId: String,
        callback: (ChatRoomEntity?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        openDatabase()
            .query(
                ChatRoomEntry.TABLE_NAME,
                null,
                ChatRoomEntry.COLUMN_OWNER_ID + "=? AND " + ChatRoomEntry.COLUMN_TYPE + " =?",
                arrayOf(selfId, ChatRoomType.person.name),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = ChatRoomEntry.getIndex(cursor)
                    val entity = assemblyDetail(index, cursor, selfId, true, true, true, true, true, false).single()
                    callback.invoke(entity)
                } else {
                    callback.invoke(null)
                }
            } ?: callback.invoke(null)
    }

    // Find roomEntity by java
    fun findChatRoomById(
        userId: String,
        roomId: String?,
        needLastMessage: Boolean,
        needFailedMessage: Boolean,
        needMembersProfile: Boolean,
        needCheckFavourite: Boolean,
        needCheckUnreadAtMessage: Boolean,
        callback: (ChatRoomEntity?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        roomId?.let {
            openDatabase()
                .query(
                    ChatRoomEntry.TABLE_NAME,
                    null,
                    ChatRoomEntry._ID + "=?",
                    arrayOf(roomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = ChatRoomEntry.getIndex()
                        val entity =
                            assemblyDetail(
                                index,
                                cursor,
                                userId,
                                needLastMessage,
                                needFailedMessage,
                                needMembersProfile,
                                needCheckFavourite,
                                needCheckUnreadAtMessage,
                                false
                            ).single()
                        callback.invoke(entity)
                    } else {
                        callback.invoke(null)
                    }
                } ?: callback.invoke(null)
        } ?: callback.invoke(null)
    }

    // Find roomEntity by Kotlin
    fun findChatRoomById(
        userId: String,
        roomId: String?,
        needLastMessage: Boolean,
        needFailedMessage: Boolean,
        needMembersProfile: Boolean,
        needCheckFavourite: Boolean,
        needCheckUnreadAtMessage: Boolean
    ) = flow {
        roomId?.let {
            openDatabase()
                .query(
                    ChatRoomEntry.TABLE_NAME,
                    null,
                    ChatRoomEntry._ID + "=?",
                    arrayOf(roomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = ChatRoomEntry.getIndex()
                        val entity =
                            assemblyDetail(
                                index,
                                cursor,
                                userId,
                                needLastMessage,
                                needFailedMessage,
                                needMembersProfile,
                                needCheckFavourite,
                                needCheckUnreadAtMessage,
                                false
                            ).single()
                        emit(entity)
                    } else {
                        emit(null)
                    }
                } ?: emit(null)
        }
    }.flowOn(Dispatchers.IO).catch { emit(null) }

    /**
     * assembly Chat Room Entity Other Details
     */
    private fun assemblyDetail(
        index: Map<String, Int>,
        cursor: Cursor,
        userId: String,
        needLastMessage: Boolean,
        needFailedMessage: Boolean,
        needMembersProfile: Boolean,
        needCheckFavourite: Boolean,
        needCheckUnreadAtMessage: Boolean,
        isJoin: Boolean
    ) = flow {
        val favouriteUserIds = findFavouriteLabels().single()
        val roomId = Tools.getDbString(cursor, ChatRoomEntry._ID)
        val memberIds = findMemberIdsByRoomId(roomId).single()
        val entity: ChatRoomEntity
        var lastMessage: MessageEntity? = null
        if (needLastMessage) {
            lastMessage =
                findMessageByRoomIdAndStatusAndLimitOne(
                    roomId,
                    MessageStatus.getValidStatus(),
                    Sort.DESC
                ).single()
        }
        var failedMessage: MessageEntity? = null
        if (needFailedMessage) {
            failedMessage =
                findMessageByRoomIdAndStatusAndLimitOne(
                    roomId,
                    MessageStatus.getFailedErrorStatus(),
                    Sort.DESC
                ).single()
        }
        var members: List<UserProfileEntity?>? = Lists.newArrayList()
        if (needMembersProfile) {
            members = mutableListOf<UserProfileEntity>() // ChatMemberCacheService.getChatMembers(roomId)
        }
        entity =
            ChatRoomEntity
                .formatByCursor(
                    index,
                    cursor,
                    isJoin,
                    memberIds,
                    members,
                    lastMessage,
                    failedMessage
                ).build()
        // EVAN_FLAG 2020-04-21 (1.10.0) My collection is compared and judged, if it is a friend,
        //  and there is no object content, and whether the user is included after the intersection member
        if (needCheckFavourite) {
            favouriteUserIds.toMutableList().retainAll(memberIds)
            entity.isFavourite =
                ChatRoomType.friend == entity.type &&
                Strings.isNullOrEmpty(entity.businessId) &&
                favouriteUserIds.isNotEmpty()
        }

        // EVAN_FLAG 2020-04-21 (1.10.0) If there is an unread message, check locally whether there is an unread message of Atme or All.
        //  ＊＊＊＊＊＊ Need to match Jocket Message.New & Judge during the supplementary message process ＊＊＊＊＊＊
        if (entity.unReadNum > 0 && needCheckUnreadAtMessage) {
            findUnreadAtMessagesByRoomId(userId, roomId) { hasUnReadAtMe ->
                var hasAtMe = false
                if (!hasUnReadAtMe && entity.lastMessage != null) {
                    hasAtMe = MessageType.AT == entity.lastMessage.type &&
                        (
                            entity.lastMessage.content.contains(userId) ||
                                entity.lastMessage.content.contains("\"objectType\": \"All\"")
                        )
                }
                entity.isAtMe = hasUnReadAtMe || hasAtMe
            }
        }

        if (ChatRoomType.services == entity.type) {
            val consultsUnreadNumber = getConsultsUnreadNumber(entity.id).single()
            if (consultsUnreadNumber > 0) {
                entity.consultSrcUnreadNumber = consultsUnreadNumber
            }
        }
        emit(entity)
    }.flowOn(Dispatchers.IO)

    /**
     * 查詢有無AT我的未讀訊息
     */
    fun findUnreadAtMessagesByRoomId(
        userId: String,
        roomId: String,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            val dateTime = System.currentTimeMillis()
            var hasUnReadAtMe = false
//            val sql = "SELECT " + MessageEntry._ID + " FROM " + MessageEntry.TABLE_NAME +
//                    " WHERE " + MessageEntry.COLUMN_ROOM_ID + "='" + roomId + "'" +
//                    " AND " + MessageEntry.COLUMN_FLAG + " IN(0,1)" +
//                    " AND " + MessageEntry.COLUMN_TYPE + " ='" + MessageType.AT.value + "'" +
//                    " AND (" + MessageEntry.COLUMN_CONTENT + " LIKE " + "'%" + userId + "%'" + " COLLATE NOCASE ESCAPE '/'" +
//                    " OR " + MessageEntry.COLUMN_CONTENT + " LIKE " + "'%" + "\"objectType\":\"All\"" + "%'" + " COLLATE NOCASE ESCAPE '/'" +
//                    " OR " + MessageEntry.COLUMN_CONTENT + " LIKE " + "'%" + "\"objectType\": \"All\"" + "%'" + " COLLATE NOCASE ESCAPE '/')"
            db
                .query(
                    MessageEntry.TABLE_NAME,
                    arrayOf(MessageEntry._ID),
                    MessageEntry.COLUMN_ROOM_ID + "=?" +
                        " AND " + MessageEntry.COLUMN_FLAG + " IN(0,1)" +
                        " AND " + MessageEntry.COLUMN_TYPE + " = ?" +
                        " AND (" + MessageEntry.COLUMN_CONTENT + " LIKE ? COLLATE NOCASE ESCAPE '/'" +
                        " OR " + MessageEntry.COLUMN_CONTENT + " LIKE " + "'%" + "\"objectType\":\"All\"" + "%'" + " COLLATE NOCASE ESCAPE '/'" +
                        " OR " + MessageEntry.COLUMN_CONTENT + " LIKE " + "'%" + "\"objectType\": \"All\"" + "%'" + " COLLATE NOCASE ESCAPE '/')",
                    arrayOf(roomId, MessageType.AT.value, "%$userId%"),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.count > 0) {
                        hasUnReadAtMe = true
                    }
                    callback.invoke(hasUnReadAtMe)
                    Log.w(
                        "findUnreadAtMessagesByRoomId",
                        String.format(
                            "room find has At Me  by %s, isAtMe->%s, use time->%s/秒  ",
                            roomId,
                            hasUnReadAtMe,
                            (System.currentTimeMillis() - dateTime) / 1000.0
                        )
                    )
                } ?: callback.invoke(hasUnReadAtMe)
        }
    }

    fun findServiceMemberRoomIdById(serviceNumberId: String) =
        flow {
            openDatabase()
                .query(
                    DBContract.ServiceNumEntry.TABLE_NAME,
                    arrayOf(DBContract.ServiceNumEntry.COLUMN_SERVICE_MEMBER_ROOM_ID),
                    DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + "=?" +
                        " AND " + DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID + "!=''",
                    arrayOf(serviceNumberId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    var entity: ChatRoomEntity? = null
                    if (cursor.moveToFirst()) {
                        val serviceMemberRoomId =
                            Tools.getDbString(
                                cursor,
                                DBContract.ServiceNumEntry.COLUMN_SERVICE_MEMBER_ROOM_ID
                            )
                        val hasData = hasLocalData(serviceMemberRoomId).single()
                        if (hasData) {
                            entity = findChatRoomById("", serviceMemberRoomId, false, false, false, false, false).single()
                        } else {
                            entity =
                                ChatRoomEntity
                                    .Build()
                                    .id(serviceMemberRoomId)
                                    .type(ChatRoomType.serviceMember)
                                    .build()
                        }
                        emit(entity)
                    } else {
                        emit(null)
                    }
                }
        }.flowOn(Dispatchers.IO).catch { emit(null) }

    /**
     * checking If There Is Information
     */
    fun hasLocalData(
        roomId: String?,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        roomId?.let {
            withDatabase { db ->
                db
                    .query(
                        ChatRoomEntry.TABLE_NAME,
                        null,
                        ChatRoomEntry._ID + "=?",
                        arrayOf<String?>(roomId),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        val count = cursor.count
                        callback.invoke(count > 0)
                    } ?: callback.invoke(false)
            }
        } ?: callback.invoke(false)
    }

    fun hasLocalData(roomId: String?) =
        flow {
            if (Strings.isNullOrEmpty(roomId)) return@flow
            openDatabase()
                .query(
                    ChatRoomEntry.TABLE_NAME,
                    null,
                    ChatRoomEntry._ID + "=?",
                    arrayOf<String?>(roomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val count = cursor.count
                    emit(count > 0)
                } ?: emit(false)
        }.flowOn(Dispatchers.IO).catch { emit(false) }

    // findRoomById for java
    fun findRoomById(
        roomId: String?,
        callback: (ChatRoomEntity?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        roomId?.let {
            openDatabase()
                .query(
                    ChatRoomEntry.TABLE_NAME,
                    null,
                    ChatRoomEntry._ID + "=?",
                    arrayOf(roomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val memberIds: List<String> = findMemberIdsByRoomId(roomId).single()
                        val members = mutableListOf<UserProfileEntity>() // ChatMemberCacheService.getChatMembers(roomId)
                        val lastMessage =
                            findMessageByRoomIdAndStatusAndLimitOne(
                                roomId,
                                MessageStatus.getValidStatus(),
                                Sort.DESC
                            ).single()
                        val failedMessage =
                            findMessageByRoomIdAndStatusAndLimitOne(
                                roomId,
                                MessageStatus.getFailedErrorStatus(),
                                Sort.DESC
                            ).single()
                        val index = ChatRoomEntry.getIndex(cursor)
                        val entity =
                            ChatRoomEntity
                                .formatByCursor(
                                    index,
                                    cursor,
                                    false,
                                    memberIds,
                                    members,
                                    lastMessage,
                                    failedMessage
                                ).build()
                        callback.invoke(entity)
                    } else {
                        callback.invoke(null)
                    }
                } ?: callback.invoke(null)
        } ?: callback.invoke(null)
    }

    // findRoomById for Kotlin flow
    fun findRoomById(roomId: String?) =
        flow {
            if (Strings.isNullOrEmpty(roomId)) return@flow
            openDatabase()
                .query(
                    ChatRoomEntry.TABLE_NAME,
                    null,
                    ChatRoomEntry._ID + "=?",
                    arrayOf(roomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val memberIds: List<String> = findMemberIdsByRoomId(roomId).single()
                        val members = mutableListOf<UserProfileEntity>() // ChatMemberCacheService.getChatMembers(roomId)
                        val lastMessage =
                            findMessageByRoomIdAndStatusAndLimitOne(
                                roomId,
                                MessageStatus.getValidStatus(),
                                Sort.DESC
                            ).single()
                        val failedMessage =
                            findMessageByRoomIdAndStatusAndLimitOne(
                                roomId,
                                MessageStatus.getFailedErrorStatus(),
                                Sort.DESC
                            ).single()
                        val index = ChatRoomEntry.getIndex(cursor)
                        val entity =
                            ChatRoomEntity
                                .formatByCursor(
                                    index,
                                    cursor,
                                    false,
                                    memberIds,
                                    members,
                                    lastMessage,
                                    failedMessage
                                ).build()
                        emit(entity)
                    } else {
                        emit(null)
                    }
                }
        }.flowOn(Dispatchers.IO).catch { emit(null) }

    fun findUserIdByOpenId(openId: String) =
        flow {
            openDatabase()
                .query(
                    UserProfileEntry.TABLE_NAME,
                    arrayOf(UserProfileEntry.COLUMN_ID),
                    UserProfileEntry.COLUMN_OPEN_ID + " =?",
                    arrayOf(openId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val ownerId: String = Tools.getDbString(cursor, UserProfileEntry.COLUMN_ID)
                        emit(ownerId)
                    } else {
                        emit("")
                    }
                } ?: emit("")
        }.flowOn(Dispatchers.IO).catch { emit("") }

    fun findRoomIdByUserIdAndServiceNumberId(
        userId: String,
        serviceNumberId: String
    ) = flow {
        openDatabase()
            .query(
                ChatRoomEntry.TABLE_NAME,
                arrayOf(ChatRoomEntry._ID),
                ChatRoomEntry.COLUMN_OWNER_ID + " = ? AND " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " = ?",
                arrayOf(userId, serviceNumberId),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val roomId: String = Tools.getDbString(cursor, ChatRoomEntry._ID)
                    emit(roomId)
                } else {
                    emit("")
                }
            } ?: emit("")
    }.flowOn(Dispatchers.IO).catch { emit("") }

    @Synchronized
    fun save(syncRoomNormalResponse: SyncRoomNormalResponse) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    if (syncRoomNormalResponse.unReadNum == 0) {
                        if (getUnReadNumberById(db, syncRoomNormalResponse.id) == -1) {
                            syncRoomNormalResponse.unReadNum = -1
                        }
                    }

                    for (id in syncRoomNormalResponse.memberIds) {
                        val contentValue = ChatRoomEntity.getMemberId(syncRoomNormalResponse.id, id)
                        db.replace(DBContract.ChatRoomMemberIdsEntry.TABLE_NAME, null, contentValue)
                    }

                    if (syncRoomNormalResponse.lastMessage != null) {
                        val lastMessageValues =
                            ChatRoomEntity.getLastMessageContentValues(syncRoomNormalResponse.lastMessage)
                        db.replace(DBContract.LastMessageEntry.TABLE_NAME, null, lastMessageValues)
                    }

                    val values = syncRoomNormalResponse.getContentValues()
                    db.replace(ChatRoomEntry.TABLE_NAME, null, values)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    private fun getUnReadNumberById(
        db: SQLiteDatabase,
        roomId: String
    ): Int {
        db
            .query(
                ChatRoomEntry.TABLE_NAME,
                arrayOf(ChatRoomEntry.COLUMN_UNREAD_NUMBER),
                ChatRoomEntry._ID + "=?",
                arrayOf(roomId),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val unreadNumber = cursor.getInt(0)
                    return unreadNumber
                } else {
                    return 0
                }
            } ?: return 0
    }

    @Synchronized
    fun updateIsAtMeFlag(
        roomId: String,
        isAtMe: Boolean
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put(ChatRoomEntry.COLUMN_IS_AT_ME, if (isAtMe) 1 else 0)
                val whereClause = ChatRoomEntry._ID + "= ?"
                val whereArgs = arrayOf(roomId)
                db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateIsAtMeFlagByFlow(
        roomId: String,
        isAtMe: Boolean
    ) = flow {
        val db = openDatabase()
        db.beginTransaction()
        try {
            val values = ContentValues()
            values.put(ChatRoomEntry.COLUMN_IS_AT_ME, if (isAtMe) 1 else 0)
            val whereClause = ChatRoomEntry._ID + "= ?"
            val whereArgs = arrayOf(roomId)
            val isUpdated = db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs)
            emit(isUpdated)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            emit(false)
        } finally {
            db.endTransaction()
        }
    }.flowOn(Dispatchers.IO)

    fun getIsAtMe(
        roomId: String,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            withDatabase { db ->
                db
                    .query(
                        ChatRoomEntry.TABLE_NAME,
                        arrayOf(ChatRoomEntry.COLUMN_IS_AT_ME),
                        ChatRoomEntry._ID + " =?",
                        arrayOf(roomId),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val isAtMe = Tools.getDbInt(cursor, ChatRoomEntry.COLUMN_IS_AT_ME)
                            callback.invoke(isAtMe == 1)
                        } else {
                            callback.invoke(false)
                        }
                    } ?: callback.invoke(false)
            }
        } catch (e: Exception) {
            callback.invoke(false)
        }
    }

    @Synchronized
    fun deleteChatRoomLastMsg(roomId: String) =
        flow {
            val db = openDatabase()
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put(ChatRoomEntry.COLUMN_LAST_MESSAGE_ENTITY, "{}")
                val whereClause = ChatRoomEntry._ID + "= ?"
                val whereArgs = arrayOf(roomId)
                val isUpdate =
                    db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs) > 0

                val whereClause1 = DBContract.LastMessageEntry.COLUMN_ROOM_ID + "= ?"
                val whereArgs1 = arrayOf(roomId)
                val isDelete =
                    db.delete(
                        DBContract.LastMessageEntry.TABLE_NAME,
                        whereClause1,
                        whereArgs1
                    ) > 0
                db.setTransactionSuccessful()
                emit(isUpdate && isDelete)
            } catch (ignored: Exception) {
                emit(false)
            } finally {
                db.endTransaction()
            }
        }.flowOn(Dispatchers.IO)

    suspend fun updateChatRoomTitle(
        roomId: String,
        title: String?
    ): Boolean =
        withContext(Dispatchers.IO) {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val values = ContentValues()
                    values.put(ChatRoomEntry.COLUMN_TITLE, title)
                    val whereClause = ChatRoomEntry._ID + "= ?"
                    val whereArgs = arrayOf(roomId)
                    val isUpdated =
                        db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs) > 0
                    db.setTransactionSuccessful()
                    isUpdated
                } catch (e: Exception) {
                    false
                } finally {
                    db.endTransaction()
                }
            }
        }

    @Synchronized
    fun updateChatRoomMember(
        roomId: String,
        chatRoomMemberResponses: List<ChatRoomMemberResponse>
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val json = JsonHelper.getInstance().toJson(chatRoomMemberResponses)
                val values = ContentValues()
                values.put(ChatRoomEntry.COLUMN_CHAT_ROOM_MEMBER, json)
                val whereClause = ChatRoomEntry._ID + "= ?"
                val whereArgs = arrayOf(roomId)
                db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs)
                for (chatMember in chatRoomMemberResponses) {
                    val contentValue =
                        getContentValue(
                            roomId,
                            chatMember
                        )
                    db.replace(DBContract.ChatMemberEntry.TABLE_NAME, null, contentValue)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateConsultRoomIdById(
        roomId: String,
        consultRoomId: String?
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put(ChatRoomEntry.COLUMN_CONSULT_ROOM_ID, consultRoomId)
                val whereClause = ChatRoomEntry._ID + "= ?"
                val whereArgs = arrayOf(roomId)
                db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs) > 0
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateBusinessExecutorIdByBusinessId(
        businessId: String,
        businessExecutorId: String?
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val whereClause = ChatRoomEntry.COLUMN_BUSINESS_ID + " = ?"
                val whereArgs = arrayOf(businessId)
                val values = ContentValues()
                values.put(ChatRoomEntry.COLUMN_BUSINESS_EXECUTOR_ID, businessExecutorId)
                db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateRoomAvatarByServiceNumberId(
        serviceNumberId: String,
        avatarId: String?
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val whereClause =
                    ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " = ? AND " + ChatRoomEntry.COLUMN_TYPE + " = ?"
                val whereArgs = arrayOf(serviceNumberId, ChatRoomType.serviceMember.name)
                val values = ContentValues()
                values.put(ChatRoomEntry.COLUMN_AVATAR_ID, avatarId)
                db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    /**
     * update Chat Room Business Info
     */
    @Synchronized
    fun updateBusinessContent(
        roomId: String?,
        businessContent: BusinessContent,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        roomId?.let {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val whereClause = ChatRoomEntry._ID + " = ?"
                    val whereArgs = arrayOf(roomId)
                    val values = ContentValues()
                    values.put(ChatRoomEntry._ID, roomId)
                    values.put(
                        ChatRoomEntry.COLUMN_BUSINESS_ID,
                        if (Strings.isNullOrEmpty(businessContent.id)) "" else businessContent.id
                    )
                    values.put(
                        ChatRoomEntry.COLUMN_BUSINESS_NAME,
                        if (Strings.isNullOrEmpty(businessContent.name)) "" else businessContent.name
                    )
                    values.put(
                        ChatRoomEntry.COLUMN_BUSINESS_CODE,
                        if (Strings.isNullOrEmpty(businessContent.code.code)) "" else businessContent.code.code
                    )
                    val isUpdated = db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs)
                    db.setTransactionSuccessful()
                    callback.invoke(isUpdated > 0)
                } catch (_: Exception) {
                    callback.invoke(false)
                } finally {
                    db.endTransaction()
                }
            }
        } ?: callback.invoke(false)
    }

    /**
     * update isMute
     */
    @Synchronized
    fun updateMuteById(
        roomId: String,
        isMute: Boolean,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val whereClause = ChatRoomEntry._ID + " = ?"
                val whereArgs = arrayOf(roomId)

                val values = ContentValues()
                values.put(ChatRoomEntry._ID, roomId)
                values.put(ChatRoomEntry.COLUMN_IS_MUTE, if (isMute) "Y" else "N")

                val isUpdated = db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs)
                callback.invoke(isUpdated > 0)
                db.setTransactionSuccessful()
            } catch (_: Exception) {
                callback.invoke(false)
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateTopAndTopTimeById(
        roomId: String,
        isTop: Boolean,
        topTime: Long,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val whereClause = ChatRoomEntry._ID + " = ?"
                val whereArgs = arrayOf(roomId)

                val values = ContentValues()
                values.put(ChatRoomEntry._ID, roomId)
                values.put(ChatRoomEntry.COLUMN_IS_TOP, if (isTop) "Y" else "N")
                values.put(
                    ChatRoomEntry.COLUMN_TOP_TIME,
                    if (isTop) {
                        if (topTime == 0L) {
                            System.currentTimeMillis()
                        } else {
                            topTime
                        }
                    } else {
                        0L
                    }
                )

                val isUpdated = db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs)
                callback.invoke(isUpdated > 0)
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                callback.invoke(false)
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateServiceNumberOwnerStopById(
        id: String,
        status: Boolean
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put(ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_STOP, status)
                val whereClause = ChatRoomEntry._ID + "= ?"
                val whereArgs = arrayOf(id)
                db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    /**
     * Update service number Agent to user serviced status
     */
    @Synchronized
    fun updateServiceNumberStatusById(
        id: String,
        status: ServiceNumberStatus
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put(ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS, status.status)
                val whereClause = ChatRoomEntry._ID + "= ?"
                val whereArgs = arrayOf(id)
                db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateCustomerRoomName(
        serviceNumberId: String,
        title: String?,
        ownerId: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val contentValues = ContentValues()
                contentValues.put(ChatRoomEntry.COLUMN_TITLE, title)
                val whereClause =
                    ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + "= ? AND " + ChatRoomEntry.COLUMN_OWNER_ID + "=?"
                db.update(ChatRoomEntry.TABLE_NAME, contentValues, whereClause, arrayOf<String>(serviceNumberId, ownerId))
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    fun findRoomIdByServiceNumberId(serviceNumberId: String) =
        flow {
            val roomIds: MutableList<String> = Lists.newArrayList()
            openDatabase()
                .query(
                    ChatRoomEntry.TABLE_NAME,
                    arrayOf(ChatRoomEntry._ID),
                    ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + "=?",
                    arrayOf(serviceNumberId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        roomIds.add(cursor.getString(0))
                    }
                    emit(roomIds)
                } ?: emit(mutableListOf())
        }.flowOn(Dispatchers.IO).catch { emit(Lists.newArrayList()) }

    /**
     * Update this chat room Pickup Agent Id
     */
    @Synchronized
    fun updateServiceNumberAgentIdById(
        id: String,
        serviceNumberAgentId: String?
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put(ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID, serviceNumberAgentId)
                val whereClause = ChatRoomEntry._ID + "= ?"
                val whereArgs = arrayOf(id)
                db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    fun getUnfinishedEdited(
        roomId: String,
        callback: (String) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db
                .query(
                    ChatRoomEntry.TABLE_NAME,
                    arrayOf(ChatRoomEntry.COLUMN_UNFINISHED_EDITED),
                    ChatRoomEntry._ID + "=?",
                    arrayOf(roomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val unfinishedEdited = Tools.getDbString(cursor, ChatRoomEntry.COLUMN_UNFINISHED_EDITED)
                        callback.invoke(unfinishedEdited)
                    } else {
                        callback.invoke("")
                    }
                } ?: callback.invoke("")
        }
    }

    @Synchronized
    fun updateLastMessage(
        roomId: String,
        message: MessageEntity
    ) = flow {
        val db = openDatabase()
        db.beginTransaction()
        try {
            val contentValues = ContentValues()
            contentValues.put(ChatRoomEntry.COLUMN_LAST_MESSAGE_ENTITY, message.toJson())
            val whereClause = ChatRoomEntry._ID + "= ?"
            val whereArgs = arrayOf(roomId)
            val isUpdatedLastMsg = db.update(ChatRoomEntry.TABLE_NAME, contentValues, whereClause, whereArgs) > 0

            val lastMessageContentValues = ChatRoomEntity.getLastMessageContentValues(message)
            val whereClause1 = DBContract.LastMessageEntry.COLUMN_ROOM_ID + "= ?"
            val whereArgs1 = arrayOf(roomId)
            val isUpdatedRoomInfo =
                db.update(
                    DBContract.LastMessageEntry.TABLE_NAME,
                    lastMessageContentValues,
                    whereClause1,
                    whereArgs1
                ) > 0
            db.setTransactionSuccessful()
            emit(isUpdatedLastMsg && isUpdatedRoomInfo)
        } catch (e: Exception) {
            emit(false)
        } finally {
            db.endTransaction()
        }
    }.flowOn(Dispatchers.IO)

    @Synchronized
    fun updateUnfinishedEditedAndTimeById(
        id: String,
        text: String?
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put(ChatRoomEntry.COLUMN_UNFINISHED_EDITED, text)
                val whereClause = ChatRoomEntry._ID + "= ?"
                val whereArgs = arrayOf(id)
                db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateOwnerIdById(
        id: String,
        ownerId: String?
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put(ChatRoomEntry.COLUMN_OWNER_ID, ownerId)
                val whereClause = ChatRoomEntry._ID + "= ?"
                val whereArgs = arrayOf(id)
                db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateUnreadNumberById(
        roomId: String,
        unreadNumber: Int
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put(ChatRoomEntry.COLUMN_UNREAD_NUMBER, unreadNumber)
                val whereClause = ChatRoomEntry._ID + "= ?"
                val whereArgs = arrayOf(roomId)
                db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateLogoUtlById(
        id: String,
        logoUrl: String?
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put(ChatRoomEntry.COLUMN_AVATAR_ID, logoUrl)
                val whereClause = ChatRoomEntry._ID + "= ?"
                val whereArgs = arrayOf(id)
                db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateTitleById(
        id: String,
        title: String?
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put(ChatRoomEntry.COLUMN_TITLE, title)
                val whereClause = ChatRoomEntry._ID + "= ?"
                val whereArgs = arrayOf(id)
                db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateChatRoomNameById(
        id: String,
        name: String?
    ) = flow {
        val db = openDatabase()
        db.beginTransaction()
        try {
            val values = ContentValues()
            values.put(ChatRoomEntry.COLUMN_TITLE, name)
            val whereClause = ChatRoomEntry._ID + "= ?"
            val whereArgs = arrayOf(id)
            val isUpdated = db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs) > 0
            db.setTransactionSuccessful()
            emit(isUpdated)
        } catch (e: Exception) {
            emit(false)
        } finally {
            db.endTransaction()
        }
    }.flowOn(Dispatchers.IO)

    @Synchronized
    fun updateUnread(roomId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val values = ContentValues()
                    values.put(ChatRoomEntry.COLUMN_UNREAD_NUMBER, 0)
                    val whereClause = ChatRoomEntry._ID + "= ?"
                    val whereArgs = arrayOf(roomId)
                    db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    /**
     * update Chat Room Interaction Time
     */
    fun updateInteractionTimeById(id: String?) =
        CoroutineScope(Dispatchers.IO).launch {
            id?.let {
                withDatabase { db ->
                    db.beginTransaction()
                    try {
                        val values = ContentValues()
                        values.put(ChatRoomEntry.COLUMN_UPDATE_TIME, System.currentTimeMillis())
                        val whereClause = ChatRoomEntry._ID + "= ?"
                        val whereArgs = arrayOf(id)
                        db.update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs)
                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                }
            }
        }

    fun findFriendRoomByRelAccountIdAndContainTypes(
        userId: String,
        accountId: String,
        types: Set<ChatRoomType?>,
        needLastMessage: Boolean,
        needFailedMessage: Boolean,
        needMembersProfile: Boolean,
        callback: (MutableList<ChatRoomEntity>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        val entities: MutableList<ChatRoomEntity> = Lists.newArrayList()
        val selection =
            buildString {
                append(AccountRoomRel.COLUMN_ROOM_ID)
                append(" IN (")
                append(types.joinToString(",") { "'$it'" })
                append(")")
            }
        openDatabase()
            .query(
                ChatRoomEntry.TABLE_NAME + " AS r " +
                    " INNER JOIN " + AccountRoomRel.TABLE_NAME + " AS a ON a.room_id = r._id ",
                arrayOf("r.*"),
                "a.account_id = ? AND $selection",
                arrayOf(accountId),
                null,
                null,
                null
            )?.use { cursor ->
                val assemblyList =
                    assemblyDetails(
                        cursor,
                        userId,
                        needLastMessage,
                        needFailedMessage,
                        needMembersProfile,
                        false
                    ).single()
                entities.addAll(assemblyList)
                callback.invoke(entities)
            } ?: callback.invoke(Lists.newArrayList())
    }

    fun findAllBusinessRoomByBusinessId(
        userId: String,
        businessId: String,
        callback: (List<ChatRoomEntity>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        val entities: MutableList<ChatRoomEntity> = Lists.newArrayList()
        openDatabase()
            .query(
                ChatRoomEntry.TABLE_NAME,
                null,
                ChatRoomEntry.COLUMN_BUSINESS_ID + "=?",
                arrayOf(businessId),
                null,
                null,
                null
            )?.use { cursor ->
                val assemblyList = assemblyDetails(cursor, userId, true, true, true, false).single()
                entities.addAll(assemblyList)
                callback.invoke(entities)
            } ?: callback.invoke(Lists.newArrayList())
    }

    fun findServiceBusinessByServiceNumberIdAndOwnerIdAndNotRoomId(
        serviceNumberId: String,
        ownerId: String,
        roomId: String,
        type: ChatRoomType,
        callback: (List<ChatRoomEntity>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        val entities: MutableList<ChatRoomEntity> = Lists.newArrayList()
        openDatabase()
            .query(
                ChatRoomEntry.TABLE_NAME,
                null,
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " =? " +
                    " AND " + ChatRoomEntry.COLUMN_OWNER_ID + " =? " +
                    " AND " + ChatRoomEntry.COLUMN_BUSINESS_ID + " !=? " +
                    " AND " + ChatRoomEntry._ID + " !=? " +
                    " AND " + ChatRoomEntry.COLUMN_TYPE + " =? ",
                arrayOf(serviceNumberId, ownerId, "", roomId, type.name),
                null,
                null,
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = Tools.getDbString(cursor, ChatRoomEntry._ID)
                    val memberIds: List<String> = findMemberIdsByRoomId(roomId).single()
                    val lastMessage =
                        findMessageByRoomIdAndStatusAndLimitOne(
                            id,
                            MessageStatus.getValidStatus(),
                            Sort.DESC
                        ).single()
                    val failedMessage =
                        findMessageByRoomIdAndStatusAndLimitOne(
                            id,
                            MessageStatus.getFailedErrorStatus(),
                            Sort.DESC
                        ).single()
                    val members = mutableListOf<UserProfileEntity>() // ChatMemberCacheService.getChatMembers(roomId)
                    val index = ChatRoomEntry.getIndex(cursor)
                    val entity =
                        ChatRoomEntity
                            .formatByCursor(
                                index,
                                cursor,
                                false,
                                memberIds,
                                members,
                                lastMessage,
                                failedMessage
                            ).build()

                    if (ChatRoomType.friend == entity.type) {
                        if (memberIds.size > 1) {
                            val userId = TokenPref.getInstance(SdkLib.getAppContext()).userId
                            queryFriendIsBlock(if (memberIds[0] == userId) memberIds[1] else memberIds[0]) { friendIsBlock ->
                                if (!friendIsBlock) {
                                    entities.add(entity)
                                }
                            }
                        }
                    } else {
                        entities.add(entity)
                    }
                }
                callback.invoke(entities)
            } ?: callback.invoke(Lists.newArrayList())
    }

    suspend fun getRoomUnreadNumber(selfId: String): Map<ChatRoomSource, BadgeDataModel> =
        withContext(Dispatchers.IO) {
            withDatabase { db ->
                var badgeData: Map<ChatRoomSource, BadgeDataModel> =
                    Maps.newHashMap(
                        ImmutableMap.of(
                            ChatRoomSource.ALL,
                            BadgeDataModel(),
                            ChatRoomSource.MAIN,
                            BadgeDataModel(),
                            ChatRoomSource.SERVICE,
                            BadgeDataModel()
                        )
                    )
                try {
//                val sql = ("SELECT "
//                        + "SUM(CASE WHEN (r." + ChatRoomEntry.COLUMN_TYPE + "='services' AND r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE + "!='BOSS') OR (r." + ChatRoomEntry.COLUMN_TYPE + "='services' AND r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE + "='Boss' AND r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_ID + "!=?) " + " THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_service_count, "
//                        + "SUM(CASE WHEN (r." + ChatRoomEntry.COLUMN_TYPE + "!='services' AND r." + ChatRoomEntry.COLUMN_TYPE + "!='serviceMember') OR (r." + ChatRoomEntry.COLUMN_TYPE + "='services' AND " + "r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE + "='Boss' AND r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_ID + "=?" + ") THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_main_count, "
//                        + "SUM(CASE WHEN r." + ChatRoomEntry.COLUMN_TYPE + "='serviceMember'" + " AND r." + ChatRoomEntry.COLUMN_LIST_CLASSIFY + "='SERVICE' " + " THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_s_service_count, "
//                        + "SUM(CASE WHEN r." + ChatRoomEntry.COLUMN_TYPE + "='serviceMember'" + " AND r." + ChatRoomEntry.COLUMN_LIST_CLASSIFY + "='MAIN' " + " THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_m_service_count, "
//                        + "SUM(CASE WHEN r." + ChatRoomEntry.COLUMN_CONSULT_ROOM_ID + "!=''  THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_consult_count " //                    + "SUM(CASE WHEN r." + ChatRoomEntry.COLUMN_LIST_CLASSIFY + "='MAIN'" + " AND r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_OPEN_TYPES + " LIKE '%C%' " + " THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_subscribe_consult "
//                        + " FROM " + ChatRoomEntry.TABLE_NAME + " AS r "
//                        + " WHERE ( r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + " > 0 " + " OR r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + " == -1 )"
//                        + " AND r." + ChatRoomEntry.COLUMN_TYPE + "!='BROADCAST'")

                    db
                        .query(
                            ChatRoomEntry.TABLE_NAME + " AS r ",
                            arrayOf(
                                "SUM(CASE WHEN (r." + ChatRoomEntry.COLUMN_TYPE + "='services' AND r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE + "!='BOSS') OR (r." + ChatRoomEntry.COLUMN_TYPE + "='services' AND r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE + "='Boss' AND r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_ID + "!=?) " + " THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_service_count",
                                "SUM(CASE WHEN (r." + ChatRoomEntry.COLUMN_TYPE + "!='services' AND r." + ChatRoomEntry.COLUMN_TYPE + "!='serviceMember') OR (r." + ChatRoomEntry.COLUMN_TYPE + "='services' AND " + "r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE + "='Boss' AND r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_ID + "=?" + ") THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_main_count",
                                "SUM(CASE WHEN r." + ChatRoomEntry.COLUMN_TYPE + "='serviceMember'" + " AND r." + ChatRoomEntry.COLUMN_LIST_CLASSIFY + "='SERVICE' " + " THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_s_service_count",
                                "SUM(CASE WHEN r." + ChatRoomEntry.COLUMN_TYPE + "='serviceMember'" + " AND r." + ChatRoomEntry.COLUMN_LIST_CLASSIFY + "='MAIN' " + " THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_m_service_count",
                                "SUM(CASE WHEN r." + ChatRoomEntry.COLUMN_CONSULT_ROOM_ID + "!=''  THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_consult_count"
                            ),
                            "( r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + " > 0 " + " OR r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + " = -1 )" +
                                " AND r." + ChatRoomEntry.COLUMN_TYPE + "!='BROADCAST'",
                            arrayOf(selfId, selfId),
                            null,
                            null,
                            null
                        )?.use { cursor ->
                            while (cursor.moveToNext()) {
                                var unreadServiceCount = Tools.getDbInt(cursor, "unread_service_count")
                                var unreadMainCount = Tools.getDbInt(cursor, "unread_main_count")

                                val unreadServiceMemberCountByService =
                                    Tools.getDbInt(cursor, "unread_s_service_count")
                                val unreadServiceMemberCountByMain =
                                    Tools.getDbInt(cursor, "unread_m_service_count")
                                unreadServiceCount += unreadServiceMemberCountByService
                                unreadMainCount += unreadServiceMemberCountByMain

                                val unreadConsultCount = Tools.getDbInt(cursor, "unread_consult_count")

                                CELog.d("Kyle1 unreadMainCount=$unreadMainCount, unreadServiceCount=$unreadServiceCount, unreadConsultCount=$unreadConsultCount.")
                                badgeData[ChatRoomSource.ALL]?.unReadNumber = unreadMainCount + unreadServiceCount
                                badgeData[ChatRoomSource.MAIN]?.unReadNumber = unreadMainCount
                                badgeData[ChatRoomSource.SERVICE]?.unReadNumber = unreadServiceCount
                            }
                        } ?: run { badgeData = mapOf() }
                } catch (e: Exception) {
                    badgeData = mapOf()
                }
                badgeData
            }
        }

    suspend fun getServiceNumberChatRoomUnreadNumber(serviceNumberId: String): Int =
        withContext(Dispatchers.IO) {
            withDatabase { db ->
                db
                    .query(
                        ChatRoomEntry.TABLE_NAME,
                        arrayOf("SUM(" + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ")"),
                        ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " =? " +
                            " AND " + ChatRoomEntry.COLUMN_LIST_CLASSIFY + " =? " +
                            " AND " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS + " !=?",
                        arrayOf<String>(
                            serviceNumberId,
                            ChatRoomSource.SERVICE.name,
                            ServiceNumberStatus.ON_LINE.status
                        ),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val unread = cursor.getInt(0)
                            unread
                        } else {
                            0
                        }
                    } ?: 0
            }
        }

    fun getAllServiceNumberChatRoomUnreadNumber() =
        flow {
            openDatabase()
                .query(
                    ChatRoomEntry.TABLE_NAME,
                    arrayOf("SUM(" + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ")"),
                    ChatRoomEntry.COLUMN_LIST_CLASSIFY + " =?" +
                        " AND " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS + " !=?",
                    arrayOf(ChatRoomSource.SERVICE.name, ServiceNumberStatus.ON_LINE.status),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val unread = cursor.getInt(0)
                        emit(unread)
                    } else {
                        emit(0)
                    }
                } ?: emit(0)
        }.flowOn(Dispatchers.IO).catch { emit(0) }

    fun deleteByServiceNumberId(
        serviceNumberId: String,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val sessionSelection = ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " = ?"
                val selectionArgs = arrayOf(serviceNumberId)
                val isDeleted = db.delete(ChatRoomEntry.TABLE_NAME, sessionSelection, selectionArgs) > 0
                db.setTransactionSuccessful()
                callback.invoke(isDeleted)
            } catch (e: Exception) {
                callback.invoke(false)
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun saveChatRoomInfo(
        entity: ChatRoomEntity,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val multimap: Multimap<String, String> = ArrayListMultimap.create()
                if (entity.unReadNum == 0) {
                    if (getUnReadNumberById(db, entity.id) == -1) {
                        entity.unReadNum = -1
                    }
                }
                if (entity.lastMessage == null && entity.unReadNum <= 0) {
                    // 前端針對此條件的聊天室不跟隨Server的update time排序
                    entity.setInitUpdateTime(0L)
                }

                if (entity.lastMessage != null) {
                    val lastMessageValues = ChatRoomEntity.getLastMessageContentValues(entity.lastMessage)
                    db.replace(DBContract.LastMessageEntry.TABLE_NAME, null, lastMessageValues)
                }

                multimap.putAll(entity.id, Sets.newHashSet(entity.memberIds))
                if (ChatRoomSource.ALL == entity.listClassify) {
                    val selfUserId = TokenPref.getInstance(SdkLib.getAppContext()).userId
                    setChatType(entity, selfUserId)
                }
                val values = ChatRoomEntity.getContentValues(entity)
                val isSaved: Long = db.replace(ChatRoomEntry.TABLE_NAME, null, values)
                batchSaveByRoomIdsAndAccountIds(multimap)
                db.setTransactionSuccessful()
                callback.invoke(isSaved > 0)
            } catch (e: Exception) {
                callback.invoke(false)
            } finally {
                db.endTransaction()
            }
        }
    }

    // 聊天室&服務號分類
    private fun setChatType(
        chatRoomEntity: ChatRoomEntity,
        userId: String
    ) {
        // 聊天室或服務號分類
        if (ChatRoomType.serviceMember == chatRoomEntity.type) {
            if (ServiceNumberType.BOSS == chatRoomEntity.serviceNumberType && userId == chatRoomEntity.serviceNumberOwnerId) { // 服務號成員聊天室且是商務號擁有者
                chatRoomEntity.listClassify = ChatRoomSource.MAIN
            } else {
                chatRoomEntity.listClassify = ChatRoomSource.SERVICE
            }
        } else if (ChatRoomType.services == chatRoomEntity.type) {
            if (chatRoomEntity.ownerId == userId) { // 我自己進線的服務號聊天室，服務號並且是擁有者
                chatRoomEntity.type = ChatRoomType.subscribe
                chatRoomEntity.listClassify = ChatRoomSource.MAIN
            } else if (ServiceNumberType.BOSS == chatRoomEntity.serviceNumberType && userId == chatRoomEntity.serviceNumberOwnerId) { // 我的商務號聊天室
                chatRoomEntity.listClassify = ChatRoomSource.MAIN
            } else if (chatRoomEntity.provisionalIds.contains(userId)) {
                chatRoomEntity.listClassify = ChatRoomSource.MAIN // 臨時成員聊天室顯示在一般聊天列表
            } else {
                chatRoomEntity.listClassify = ChatRoomSource.SERVICE
            }
        } else {
            chatRoomEntity.listClassify = ChatRoomSource.MAIN
        }
    }

    @Synchronized
    fun saveChatRoomList(entities: List<ChatRoomEntity>): Boolean {
        if (entities.isEmpty()) {
            return false
        }
        try {
            val dateTime = System.currentTimeMillis()
            var result = true
            for (entity in entities) {
                saveChatRoomInfo(entity) { isSuccess ->
                    result = result && isSuccess
                }
            }
            Log.d(
                "saveChatRoomList",
                String.format(
                    "save room entities and batch replace Account Room Rel, count->%s, use time->%s/second  ",
                    entities.size,
                    (System.currentTimeMillis() - dateTime) / 1000.0
                )
            )
            return result
        } catch (e: java.lang.Exception) {
            CELog.e(e.message)
            return false
        }
    }

    suspend fun syncSave(entities: List<ChatRoomEntity>): Boolean =
        withContext(Dispatchers.IO) {
            if (entities.isEmpty()) {
                return@withContext false
            }
            try {
                val dateTime = System.currentTimeMillis()
                var result = true
                for (entity in entities) {
                    val isSuccess = syncSave(entity)
                    result = result && isSuccess
                }
                CELog.d(
                    String.format(
                        "save room entities and batch replace Account Room Rel, count->%s, use time->%s/second  ",
                        entities.size,
                        (System.currentTimeMillis() - dateTime) / 1000.0
                    )
                )
                result
            } catch (e: java.lang.Exception) {
                CELog.e(e.message)
                false
            }
        }

    // for first sync method
    suspend fun syncSave(entity: ChatRoomEntity): Boolean =
        withContext(Dispatchers.IO) {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val multimap: Multimap<String, String> = ArrayListMultimap.create()
                    if (entity.unReadNum == 0) {
                        if (getUnReadNumberById(db, entity.id) == -1) {
                            entity.unReadNum = -1
                        }
                    }
                    if (entity.lastMessage == null && entity.unReadNum <= 0) {
                        // 前端針對此條件的聊天室不跟隨Server的update time排序
                        entity.setInitUpdateTime(0L)
                    } else if (entity.lastMessage != null) {
                        entity.setInitUpdateTime(entity.lastMessage.sendTime)
                    }
                    for (id in entity.memberIds) {
                        val contentValue = ChatRoomEntity.getMemberId(entity.id, id)
                        db.replace(DBContract.ChatRoomMemberIdsEntry.TABLE_NAME, null, contentValue)
                    }
                    if (entity.lastMessage != null) {
                        val lastMessageValues =
                            ChatRoomEntity.getLastMessageContentValues(entity.lastMessage)
                        db.replace(DBContract.LastMessageEntry.TABLE_NAME, null, lastMessageValues)
                    }
                    multimap.putAll(entity.id, Sets.newHashSet(entity.memberIds))
                    if (ChatRoomSource.ALL == entity.listClassify) {
                        val selfUserId = TokenPref.getInstance(SdkLib.getAppContext()).userId
                        setChatType(entity, selfUserId)
                    }
                    val values = ChatRoomEntity.getContentValues(entity)
                    val isSaved = db.replace(ChatRoomEntry.TABLE_NAME, null, values)
                    getInstance(SdkLib.getAppContext()).batchSaveByRoomIdsAndAccountIds(multimap)
                    db.setTransactionSuccessful()
                    isSaved > 0
                } catch (e: Exception) {
                    false
                } finally {
                    db.endTransaction()
                }
            }
        }

    @Synchronized
    fun saveChatRoomFromSync(entity: ChatRoomEntity) =
        flow {
            val db = openDatabase()
            db.beginTransaction()
            try {
                val multimap: Multimap<String, String> = ArrayListMultimap.create()
                if (entity.unReadNum == 0) {
                    if (getUnReadNumberById(db, entity.id) == -1) {
                        entity.unReadNum = -1
                    }
                }
                if (entity.lastMessage == null && entity.unReadNum <= 0) {
                    // 前端針對此條件的聊天室不跟隨Server的update time排序
                    entity.setInitUpdateTime(0L)
                } else if (entity.lastMessage != null) {
                    entity.setInitUpdateTime(entity.lastMessage.sendTime)
                }
                for (id in entity.memberIds) {
                    val contentValue = ChatRoomEntity.getMemberId(entity.id, id)
                    db.replace(DBContract.ChatRoomMemberIdsEntry.TABLE_NAME, null, contentValue)
                }
                if (entity.lastMessage != null) {
                    val lastMessageValues =
                        ChatRoomEntity.getLastMessageContentValues(entity.lastMessage)
                    db.replace(DBContract.LastMessageEntry.TABLE_NAME, null, lastMessageValues)
                }

                multimap.putAll(entity.id, Sets.newHashSet(entity.memberIds))
                if (ChatRoomSource.ALL == entity.listClassify) {
                    val selfUserId = TokenPref.getInstance(SdkLib.getAppContext()).userId
                    setChatType(entity, selfUserId)
                }
                val values = ChatRoomEntity.getContentValues(entity)
                val isSaved = db.replace(ChatRoomEntry.TABLE_NAME, null, values)
                getInstance(SdkLib.getAppContext()).batchSaveByRoomIdsAndAccountIds(multimap)
                db.setTransactionSuccessful()
                emit(isSaved > 0)
            } catch (e: Exception) {
                emit(false)
            } finally {
                db.endTransaction()
            }
        }.flowOn(Dispatchers.IO)

    fun findTodoEntityById(
        id: String,
        callback: (TodoEntity?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        openDatabase()
            .query(
                DBContract.TodoEntry.TABLE_NAME,
                null,
                DBContract.TodoEntry._ID + "=?",
                arrayOf(id),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val roomId = Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_ROOM_ID)
                    val messageId = Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_MESSAGE_ID)
                    val roomEntity = findChatRoomById("", roomId, false, false, true, false, false).single()
                    val messageEntity =
                        findMessageByIdAndRoomId(
                            messageId,
                            roomId
                        ).single()
                    val entity = formatByCursor(cursor, messageEntity, roomEntity)
                    callback.invoke(entity)
                } else {
                    callback.invoke(null)
                }
            }
    }

    fun findTodoEntitiesByRoomId(
        roomId: String,
        callback: (MutableList<TodoEntity>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        openDatabase()
            .query(
                DBContract.TodoEntry.TABLE_NAME,
                null,
                DBContract.TodoEntry.COLUMN_ROOM_ID + "=? AND " + DBContract.TodoEntry.COLUMN_TODO_STATUS + "!=?",
                arrayOf<String>(roomId, TodoStatus.DELETED.name),
                null,
                null,
                null
            )?.use { cursor ->
                val entities: MutableList<TodoEntity> = mutableListOf()
                while (cursor.moveToNext()) {
                    val messageId = Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_MESSAGE_ID)
                    val roomEntity = findChatRoomById("", roomId, false, false, true, false, false).single()
                    val messageEntity = findMessageByIdAndRoomId(messageId, roomId).single()
                    entities.add(formatByCursor(cursor, messageEntity, roomEntity))
                }
                callback.invoke(entities)
            } ?: callback.invoke(Lists.newArrayList())
    }

    /**
     * 查詢單筆資訊
     */
    fun findMessageByIdAndRoomId(
        messageId: String?,
        roomId: String?,
        callback: (MessageEntity?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        if (Strings.isNullOrEmpty(messageId) || Strings.isNullOrEmpty(roomId)) {
            callback.invoke(null)
        }
        openDatabase()
            .query(
                MessageEntry.TABLE_NAME,
                null,
                MessageEntry._ID + "=? AND " + MessageEntry.COLUMN_ROOM_ID + "=?",
                arrayOf<String?>(messageId, roomId),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = MessageEntry.getIndex(cursor)
                    val entity = formatByCursor(index, cursor)
                    if (MessageType.BROADCAST == entity.type) {
                        val topicEntities =
                            findTopicRelByIdAndType(
                                entity.id
                            ).single()
                        if (topicEntities.isNotEmpty()) {
                            entity.topicArray = topicEntities
                        }
                    }
                    callback.invoke(entity)
                } else {
                    callback.invoke(null)
                }
            } ?: callback.invoke(null)
    }

    /**
     * 查詢單筆資訊 by flow
     */
    private fun findMessageByIdAndRoomId(
        messageId: String?,
        roomId: String?
    ) = flow {
        if (Strings.isNullOrEmpty(messageId) || Strings.isNullOrEmpty(roomId)) {
            emit(null)
        }
        openDatabase()
            .query(
                MessageEntry.TABLE_NAME,
                null,
                MessageEntry._ID + "=? AND " + MessageEntry.COLUMN_ROOM_ID + "=?",
                arrayOf<String?>(messageId, roomId),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = MessageEntry.getIndex(cursor)
                    val entity = formatByCursor(index, cursor)
                    if (MessageType.BROADCAST == entity.type) {
                        val topicEntities =
                            findTopicRelByIdAndType(
                                entity.id
                            ).single()
                        if (topicEntities.isNotEmpty()) {
                            entity.topicArray = topicEntities
                        }
                    }
                    emit(entity)
                }
            } ?: emit(null)
    }.flowOn(Dispatchers.IO).catch { emit(null) }

    /**
     * 依照關聯Id 與關聯類型查詢
     */
    private fun findTopicRelByIdAndType(relId: String?) =
        flow {
            if (Strings.isNullOrEmpty(relId)) return@flow
            openDatabase()
                .query(
                    DBContract.EntityTopicRel.TABLE_NAME,
                    null,
                    DBContract.EntityTopicRel.COLUMN_RELATION_ID + "=?" +
                        " AND " + DBContract.EntityTopicRel.COLUMN_RELATION_TYPE + "=?",
                    arrayOf<String?>(relId, TopicRelType.MESSAGE.name),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    var list: List<TopicEntity> = Lists.newArrayList()
                    val idSet: MutableSet<String> = Sets.newHashSet()
                    while (cursor.moveToNext()) {
                        val topic = Tools.getDbString(cursor, DBContract.EntityTopicRel.COLUMN_TOPIC_ID)
                        idSet.add(topic)
                    }
                    if (idSet.isNotEmpty()) {
                        val topicIds = idSet.toTypedArray<String>()
                        list = findTopicsByIds(topicIds).single()
                    }
                    emit(list)
                } ?: emit(mutableListOf())
        }.flowOn(Dispatchers.IO).catch { emit(Lists.newArrayList()) }

    fun findTodoBySelf(
        roomId: String,
        callback: (List<TodoEntity>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        openDatabase()
            .query(
                DBContract.TodoEntry.TABLE_NAME,
                null,
                DBContract.TodoEntry.COLUMN_TODO_STATUS + "!=? " +
                    " AND (" + DBContract.TodoEntry.COLUMN_ROOM_ID + "=? " +
                    " OR " + DBContract.TodoEntry.COLUMN_ROOM_ID + " IS NULL " +
                    " OR " + DBContract.TodoEntry.COLUMN_ROOM_ID + "='')",
                arrayOf<String>(TodoStatus.DELETED.name, roomId),
                null,
                null,
                null
            )?.use { cursor ->
                val entities: MutableList<TodoEntity> = Lists.newArrayList()
                while (cursor.moveToNext()) {
                    var roomEntity: ChatRoomEntity? = null
                    var messageEntity: MessageEntity? = null
                    val targetRoomId = Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_ROOM_ID)
                    if (!Strings.isNullOrEmpty(targetRoomId)) {
                        roomEntity = findChatRoomById("", roomId, false, false, true, false, false).single()
                        val messageId =
                            Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_MESSAGE_ID)
                        if (!Strings.isNullOrEmpty(messageId)) {
                            messageEntity = findMessageByIdAndRoomId(messageId, roomId).single()
                        }
                    }
                    entities.add(formatByCursor(cursor, messageEntity, roomEntity))
                }
            } ?: callback.invoke(Lists.newArrayList())
    }

    fun findAllTodo(callback: (List<TodoEntity>) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            val entities: MutableList<TodoEntity> = Lists.newArrayList()
            openDatabase()
                .query(
                    DBContract.TodoEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        var messageEntity: MessageEntity? = null
                        val roomId = Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_ROOM_ID)
                        val messageId = Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_MESSAGE_ID)
                        val roomEntity = findChatRoomById("", roomId, false, false, true, false, false).single()
                        messageEntity = findMessageByIdAndRoomId(messageId, roomId).single()
                        entities.add(formatByCursor(cursor, messageEntity, roomEntity))
                    }
                    callback.invoke(entities)
                } ?: callback.invoke(Lists.newArrayList())
        }

    /**
     * 取出全部未同步資料
     */
    fun findNotProcessStatus(
        status: ProcessStatus,
        callback: (List<TodoEntity>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            val entities: MutableList<TodoEntity> = Lists.newArrayList()
            db
                .query(
                    DBContract.TodoEntry.TABLE_NAME,
                    null,
                    DBContract.TodoEntry.COLUMN_PROCESS_STATUS + "!=?",
                    arrayOf<String>(status.name),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        entities.add(formatByCursor(cursor))
                    }
                    callback.invoke(entities)
                } ?: callback.invoke(Lists.newArrayList())
        }
    }

    @Synchronized
    fun saveTodoList(entities: List<TodoEntity>) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    for (entity in entities) {
                        val values = entity.getContentValues()
                        db.replace(DBContract.TodoEntry.TABLE_NAME, null, values)
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    @Synchronized
    fun saveSingleTodo(
        entity: TodoEntity,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val values = entity.getContentValues()
                val isSaved = db.replace(DBContract.TodoEntry.TABLE_NAME, null, values)
                callback.invoke(isSaved > 0)
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                callback.invoke(false)
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateTodo(
        entity: TodoEntity,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val whereClause = DBContract.TodoEntry._ID + " = ?"
                val whereArgs = arrayOf(entity.id)
                val values = entity.getUpdateContentValues()
                //            values.put(DBContract.TodoEntry.COLUMN_PROCESS_STATUS, status.name());
                val isUpdated =
                    db.update(
                        DBContract.TodoEntry.TABLE_NAME,
                        values,
                        whereClause,
                        whereArgs
                    ) > 0
                callback.invoke(isUpdated)
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                callback.invoke(false)
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateRoomIdAndMessageIdOfTodo(
        roomId: String,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val whereClause = DBContract.TodoEntry.COLUMN_ROOM_ID + " = ?"
                val whereArgs = arrayOf(roomId)
                val values = ContentValues()
                values.put(DBContract.TodoEntry.COLUMN_ROOM_ID, "")
                values.put(DBContract.TodoEntry.COLUMN_MESSAGE_ID, "")
                val isUpdated =
                    db.update(
                        DBContract.TodoEntry.TABLE_NAME,
                        values,
                        whereClause,
                        whereArgs
                    ) > 0
                callback.invoke(isUpdated)
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                callback.invoke(false)
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun deleteTodo(
        id: String,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val whereClause = DBContract.TodoEntry._ID + " = ?"
                val isDeleted =
                    db.delete(
                        DBContract.TodoEntry.TABLE_NAME,
                        whereClause,
                        arrayOf<String>(id)
                    ) >= 0
                callback.invoke(isDeleted)
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                callback.invoke(false)
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateProcessStatusOfTodo(
        id: String,
        status: ProcessStatus,
        createTime: Long,
        updateTime: Long,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val whereClause = DBContract.TodoEntry._ID + " = ?"
                val whereArgs = arrayOf(id)
                val values = ContentValues()
                values.put(DBContract.TodoEntry.COLUMN_PROCESS_STATUS, status.name)
                if (createTime > 0) {
                    values.put(DBContract.TodoEntry.COLUMN_CREATE_TIME, createTime)
                }
                if (updateTime > 0) {
                    values.put(DBContract.TodoEntry.COLUMN_UPDATE_TIME, updateTime)
                }
                val isUpdated =
                    db.update(
                        DBContract.TodoEntry.TABLE_NAME,
                        values,
                        whereClause,
                        whereArgs
                    ) > 0
                callback.invoke(isUpdated)
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                callback.invoke(false)
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun saveTodo(todoEntity: tw.com.chainsea.ce.sdk.network.model.response.TodoEntity) =
        flow {
            val db = openDatabase()
            db.beginTransaction()
            try {
                val contentValues = todoEntity.getContentValues()
                val isSaved = db.replace(DBContract.TodoEntry.TABLE_NAME, null, contentValues) > 0
                db.setTransactionSuccessful()
                emit(isSaved)
            } catch (e: Exception) {
                emit(false)
            } finally {
                db.endTransaction()
            }
        }.flowOn(Dispatchers.IO).catch { emit(false) }

    /**
     * find messages by room id with Java
     */
    fun findMessagesByRoomId(
        roomId: String,
        callback: (List<MessageEntity>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        val messages: MutableList<MessageEntity> = Lists.newArrayList()
        openDatabase()
            .query(
                MessageEntry.TABLE_NAME,
                null,
                MessageEntry.COLUMN_ROOM_ID + "=?",
                arrayOf(roomId),
                null,
                null,
                null
            )?.use { cursor ->
                val index = MessageEntry.getIndex(cursor)
                while (cursor.moveToNext()) {
                    val entity = formatByCursor(index, cursor)
                    if (MessageType.BROADCAST == entity.type) {
                        val topicEntities = findTopicRelByIdAndType(entity.id).single()
                        if (topicEntities.isNotEmpty()) {
                            entity.topicArray = topicEntities
                        }
                    }
                    messages.add(entity)
                }
                callback.invoke(messages)
            } ?: callback.invoke(Lists.newArrayList())
    }

    /**
     * find messages by room id with Kotlin Flow
     */
    fun findMessagesByRoomId(roomId: String) =
        flow {
            val messages: MutableList<MessageEntity> = Lists.newArrayList()
            openDatabase()
                .query(
                    MessageEntry.TABLE_NAME,
                    null,
                    MessageEntry.COLUMN_ROOM_ID + "=?",
                    arrayOf(roomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val index = MessageEntry.getIndex(cursor)
                    while (cursor.moveToNext()) {
                        val entity = formatByCursor(index, cursor)
                        if (MessageType.BROADCAST == entity.type) {
                            val topicEntities = findTopicRelByIdAndType(entity.id).single()
                            if (topicEntities.isNotEmpty()) {
                                entity.topicArray = topicEntities
                            }
                        }
                        messages.add(entity)
                    }
                    emit(messages)
                } ?: emit(mutableListOf())
        }.flowOn(Dispatchers.IO).catch { emit(Lists.newArrayList()) }

    /**
     * 尋找該聊天室的圖片、影片訊息
     * @param roomId 需要查找的聊天室 id
     */
    fun filterMediaMessageByRoomId(
        roomId: String,
        sort: String
    ) = flow {
        val messages: MutableList<MessageEntity> = Lists.newArrayList()
        openDatabase()
            .query(
                MessageEntry.TABLE_NAME,
                null,
                MessageEntry.COLUMN_ROOM_ID + "=? AND " +
                    MessageEntry.COLUMN_TYPE + " IN (?, ?)" +
                    " AND " + MessageEntry.COLUMN_FLAG + " != ?" +
                    " AND " + MessageEntry.COLUMN_FLAG + " != ?",
                arrayOf(roomId, MessageType.IMAGE.value, MessageType.VIDEO.value, MessageFlag.RETRACT.flag.toString(), MessageFlag.DELETED.flag.toString()),
                null,
                null,
                MessageEntry.COLUMN_SEND_TIME + " " + sort
            )?.use { cursor ->
                val index = MessageEntry.getIndex(cursor)
                while (cursor.moveToNext()) {
                    val entity = formatByCursor(index, cursor)
                    if (MessageType.BROADCAST == entity.type) {
                        val topicEntities = findTopicRelByIdAndType(entity.id).single()
                        if (topicEntities.isNotEmpty()) {
                            entity.topicArray = topicEntities
                        }
                    }
                    messages.add(entity)
                }
                emit(messages)
            } ?: emit(mutableListOf())
    }.flowOn(Dispatchers.IO).catch { emit(Lists.newArrayList()) }

    fun filterMessageByRoomId(
        roomId: String,
        messageType: MessageType,
        sortType: String
    ) = flow {
        openDatabase()
            .query(
                MessageEntry.TABLE_NAME,
                null,
                MessageEntry.COLUMN_ROOM_ID + "=? AND " +
                    MessageEntry.COLUMN_FLAG + " != " + MessageFlag.RETRACT.flag +
                    " AND " + MessageEntry.COLUMN_FLAG + " != " + MessageFlag.DELETED.flag +
                    " AND " + MessageEntry.COLUMN_TYPE + "=?",
                arrayOf(roomId, messageType.type),
                null,
                null,
                MessageEntry.COLUMN_SEND_TIME + " " + sortType
            )?.use { cursor ->
                val index = MessageEntry.getIndex(cursor)
                val messages: MutableList<MessageEntity> = mutableListOf()
                while (cursor.moveToNext()) {
                    val entity = formatByCursor(index, cursor)
                    if (MessageType.BROADCAST == entity.type) {
                        val topicEntities =
                            findTopicRelByIdAndType(
                                entity.id
                            ).single()
                        if (topicEntities.isNotEmpty()) {
                            entity.topicArray = topicEntities
                        }
                    }
                    messages.add(entity)
                }
                emit(messages)
            } ?: emit(mutableListOf())
    }.flowOn(Dispatchers.IO).catch { emit(Lists.newArrayList()) }

    fun findByBroadcastRoomId(
        broadcastRoomId: String,
        callback: (MutableList<MessageEntity>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        val list: MutableList<MessageEntity> = Lists.newArrayList()
        val typeIn =
            buildString {
                append(MessageEntry.COLUMN_TYPE)
                append(" IN (")
                append({ "'${MessageType.BROADCAST.value }'" })
                append({ ", '${MessageType.TEXT.value }'" })
                append(")")
            }
        openDatabase()
            .query(
                MessageEntry.TABLE_NAME,
                null,
                MessageEntry.COLUMN_ROOM_ID + "=? AND ?",
                arrayOf(broadcastRoomId, typeIn),
                null,
                null,
                null
            )?.use { cursor ->
                val index = MessageEntry.getIndex(cursor)
                while (cursor.moveToNext()) {
                    val entity = formatByCursor(index, cursor)
                    if (MessageType.BROADCAST == entity.type) {
                        val topicEntities = findTopicRelByIdAndType(entity.id).single()
                        if (topicEntities.isNotEmpty()) {
                            entity.topicArray = topicEntities
                        }
                    }
                    list.add(entity)
                }
                callback.invoke(list)
            } ?: callback.invoke(Lists.newArrayList())
    }

    fun findByIdAndBroadcastRoomId(
        id: String,
        broadcastRoomId: String,
        callback: (MessageEntity?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        val typeIn =
            buildString {
                append(MessageEntry.COLUMN_TYPE)
                append(" IN (")
                append({ "'${MessageType.BROADCAST.value }'" })
                append({ ", '${MessageType.TEXT.value }'" })
                append(")")
            }
        openDatabase()
            .query(
                MessageEntry.TABLE_NAME,
                null,
                MessageEntry.COLUMN_ROOM_ID + "=? AND " + MessageEntry._ID + "=? AND ?",
                arrayOf(broadcastRoomId, id, typeIn),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = MessageEntry.getIndex(cursor)
                    val entity = formatByCursor(index, cursor)
                    if (MessageType.BROADCAST == entity.type) {
                        val topicEntities = findTopicRelByIdAndType(entity.id).single()
                        if (topicEntities.isNotEmpty()) {
                            entity.topicArray = topicEntities
                        }
                    }
                    callback.invoke(entity)
                } else {
                    callback.invoke(null)
                }
            } ?: callback.invoke(null)
    }

    fun findUnreadByRoomIdAndSendTime(
        roomId: String,
        lastTime: Long,
        callback: (List<String>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            val messageId: MutableList<String> = Lists.newArrayList()
            db
                .query(
                    MessageEntry.TABLE_NAME,
                    null,
                    MessageEntry.COLUMN_ROOM_ID + "=? " +
                        " AND " + MessageEntry.COLUMN_SEND_TIME + " <=? " +
                        " AND " + MessageEntry.COLUMN_FLAG + " < ? ",
                    arrayOf(roomId, lastTime.toString(), "2"),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val id = Tools.getDbString(cursor, MessageEntry._ID)
                        messageId.add(id)
                    }
                    callback.invoke(messageId)
                } ?: callback.invoke(Lists.newArrayList())
        }
    }

    /**
     * 儲存 topic 關聯資訊
     */
    @Synchronized
    fun saveByRelIdAndTopicIdsAndType(
        relId: String?,
        topicIds: Array<String>,
        type: TopicRelType
    ) = CoroutineScope(Dispatchers.IO).launch {
        relId?.let {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val whereClause = DBContract.EntityTopicRel.COLUMN_RELATION_ID + " = ?"
                    val whereArgs = arrayOf(relId)
                    db.delete(
                        DBContract.EntityTopicRel.TABLE_NAME,
                        whereClause,
                        whereArgs
                    )

                    for (topicId in topicIds) {
                        val values = ContentValues()
                        values.put(DBContract.EntityTopicRel._ID, relId + topicId)
                        values.put(DBContract.EntityTopicRel.COLUMN_RELATION_ID, relId)
                        values.put(DBContract.EntityTopicRel.COLUMN_TOPIC_ID, topicId)
                        values.put(DBContract.EntityTopicRel.COLUMN_RELATION_TYPE, type.name)
                        db.replace(DBContract.EntityTopicRel.TABLE_NAME, null, values)
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }
    }

    @Synchronized
    fun deleteTopicRelByRelIdAndType(relId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val whereClause = (
                        DBContract.EntityTopicRel.COLUMN_RELATION_ID + " = ? " +
                            " AND " + DBContract.EntityTopicRel.COLUMN_RELATION_TYPE + " =? "
                    )
                    val whereArgs = arrayOf(relId, TopicRelType.MESSAGE.name)
                    db.delete(
                        DBContract.EntityTopicRel.TABLE_NAME,
                        whereClause,
                        whereArgs
                    )
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    /**
     * 依照 topic ids 查詢
     */
    private fun findTopicsByIds(topicIds: Array<String>) =
        flow {
            val list: MutableList<TopicEntity> = Lists.newArrayList()
            val topicIdIn =
                buildString {
                    append(DBContract.BroadcastTopicEntry._ID)
                    append(" IN (")
                    append(topicIds.joinToString(",") { "'$it'" })
                    append(")")
                }
            openDatabase()
                .query(
                    DBContract.BroadcastTopicEntry.TABLE_NAME,
                    null,
                    topicIdIn,
                    null,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val entity = TopicEntity.formatByCursor(cursor).build()
                        list.add(entity)
                    }
                    emit(list)
                } ?: emit(mutableListOf())
        }.flowOn(Dispatchers.IO).catch { emit(Lists.newArrayList()) }

    /**
     * 批量儲存Topic
     */
    @Synchronized
    fun saveAllTopics(entities: List<TopicEntity>) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    entities.forEach { entity ->
                        val values = TopicEntity.getContentValues(entity)
                        db.replace(
                            DBContract.BroadcastTopicEntry.TABLE_NAME,
                            null,
                            values
                        )
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    fun findAllTopics(callback: (MutableList<TopicEntity>) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                val list: MutableList<TopicEntity> = Lists.newArrayList()
                db
                    .query(
                        DBContract.BroadcastTopicEntry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        while (cursor.moveToNext()) {
                            val entity = TopicEntity.formatByCursor(cursor).build()
                            list.add(entity)
                        }
                        callback.invoke(list)
                    } ?: callback.invoke(Lists.newArrayList())
            }
        }

    @Synchronized
    fun saveMessageEntity(
        roomId: String?,
        entity: MessageEntity,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            roomId?.let {
                db.beginTransaction()
                try {
                    val values = entity.getContentValues(roomId)
                    if (MessageType.BROADCAST == entity.type && entity.topicArray!!.isNotEmpty()) {
                        saveByRelIdAndTopicIdsAndType(
                            entity.id,
                            entity.getTopicIds(),
                            TopicRelType.MESSAGE
                        )
                    }
                    val isSaved = db.replace(MessageEntry.TABLE_NAME, null, values) > 0
                    callback.invoke(isSaved)
                    db.setTransactionSuccessful()
                } catch (e: Exception) {
                    callback.invoke(false)
                } finally {
                    db.endTransaction()
                }
            }
        }
    }

    @Synchronized
    fun saveMessageEntityByChatRoom(entity: ChatRoomEntity?) =
        CoroutineScope(Dispatchers.IO).launch {
            entity?.let {
                it.lastMessage?.let { lastMsg ->
                    withDatabase { db ->
                        db.beginTransaction()
                        try {
                            val values = entity.lastMessage.getContentValues(entity.id)
                            db.replace(MessageEntry.TABLE_NAME, null, values)
                            if (MessageType.BROADCAST == lastMsg.type && lastMsg.topicArray!!.isNotEmpty()) {
                                saveByRelIdAndTopicIdsAndType(
                                    entity.id,
                                    lastMsg.getTopicIds(),
                                    TopicRelType.MESSAGE
                                )
                            }
                            val lastMessageValues =
                                ChatRoomEntity.getLastMessageContentValues(lastMsg)
                            db.replace(
                                DBContract.LastMessageEntry.TABLE_NAME,
                                null,
                                lastMessageValues
                            )
                            db.setTransactionSuccessful()
                        } finally {
                            db.endTransaction()
                        }
                    }
                }
            }
        }

    /**
     * MessageReference saveByRoomId方法, 簡化邏輯列入觀察
     * 存入多筆信息
     */
    @Synchronized
    fun saveMessagesByRoomId(
        roomId: String,
        entities: List<MessageEntity>
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                for (message in entities) {
                    val values = message.getContentValues(roomId)
                    if (MessageType.BROADCAST == message.type && message.topicArray!!.isNotEmpty()) {
                        saveByRelIdAndTopicIdsAndType(
                            message.id,
                            message.getTopicIds(),
                            TopicRelType.MESSAGE
                        )
                    }
                    db.replace(MessageEntry.TABLE_NAME, null, values)
                }

                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    /**
     * 依照RoomId 跟 Message MessageType
     */
    fun findMessageByRoomIdAndMessageType(
        roomId: String,
        messageType: MessageType?,
        callback: (MutableList<MessageEntity>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            val messages: MutableList<MessageEntity> = Lists.newArrayList()
            val args = arrayOf(roomId, messageType!!.value)
            db
                .query(
                    MessageEntry.TABLE_NAME,
                    null,
                    MessageEntry.COLUMN_ROOM_ID + "=? AND " + MessageEntry.COLUMN_TYPE + " IN (?)",
                    args,
                    null,
                    null,
                    MessageEntry.COLUMN_ROOM_ID + " ASC"
                )?.use { cursor ->
                    val index = MessageEntry.getIndex(cursor)
                    while (cursor.moveToNext()) {
                        val msg = formatByCursor(index, cursor)
                        messages.add(msg)
                    }
                    callback.invoke(messages)
                } ?: callback.invoke(Lists.newArrayList())
        }
    }

    fun findAllMediaMessageByRoomId(roomId: String) =
        flow {
            val messages: MutableList<MessageEntity> = mutableListOf()
            val args = arrayOf(roomId)
            openDatabase()
                .query(
                    MessageEntry.TABLE_NAME,
                    null,
                    MessageEntry.COLUMN_ROOM_ID + "=?" +
                        " AND " + MessageEntry.COLUMN_TYPE + " IN ('" + MessageType.IMAGE.value + "', '" + MessageType.VIDEO.value + "')" +
                        " AND " + MessageEntry.COLUMN_FLAG + " NOT IN (" + MessageFlag.RETRACT.flag + ", " + MessageFlag.DELETED.flag + ")",
                    args,
                    null,
                    null,
                    MessageEntry.COLUMN_SEND_TIME + " ASC"
                )?.use { cursor ->
                    val index = MessageEntry.getIndex(cursor)
                    while (cursor.moveToNext()) {
                        val msg = formatByCursor(index, cursor)
                        messages.add(msg)
                    }
                    emit(messages)
                } ?: emit(mutableListOf())
        }.flowOn(Dispatchers.IO).catch { emit(Lists.newArrayList()) }

    /**
     * update message read flag by room id
     */
    @Synchronized
    fun updateReadFlagByRoomId(roomId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val whereClause =
                        MessageEntry.COLUMN_ROOM_ID + " = ? AND " + MessageEntry.COLUMN_FLAG + " IN (0,1)"
                    val whereArgs = arrayOf(roomId)
                    val values = ContentValues()
                    values.put(MessageEntry.COLUMN_FLAG, MessageFlag.READ.flag)
                    db.update(MessageEntry.TABLE_NAME, values, whereClause, whereArgs)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    fun findRoomLastMessage(roomId: String) =
        flow {
            openDatabase()
                .query(
                    MessageEntry.TABLE_NAME,
                    null,
                    MessageEntry.COLUMN_ROOM_ID + "=? AND " + MessageEntry.COLUMN_FLAG + " != " + MessageFlag.DELETED.flag,
                    arrayOf(roomId),
                    null,
                    null,
                    MessageEntry.COLUMN_SEND_TIME + " DESC",
                    "1"
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = MessageEntry.getIndex(cursor)
                        val message = formatByCursor(index, cursor)
                        emit(message)
                    } else {
                        emit(null)
                    }
                }
        }.flowOn(Dispatchers.IO).catch { emit(null) }

    /**
     * 主要取得 Local 已儲存在 SQLite 內的訊息 for flow
     */
    fun findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(
        roomId: String,
        time: Long,
        sort: Sort,
        limit: Int
    ) = flow {
        val useTime = System.currentTimeMillis()
        val args =
            if ((time > 0)
            ) {
                if ((limit > 0)) {
                    arrayOf(
                        roomId,
                        (time + 1).toString(),
                        limit.toString()
                    )
                } else {
                    arrayOf(roomId, (time + 1).toString())
                }
            } else if ((limit > 0)) {
                arrayOf(roomId, limit.toString())
            } else {
                arrayOf(roomId)
            }
        openDatabase()
            .query(
                MessageEntry.TABLE_NAME,
                null,
                MessageEntry.COLUMN_ROOM_ID + " =? " +
                    (if (time > 0) " AND " + MessageEntry.COLUMN_SEND_TIME + (if (Sort.DESC == sort) " <=? " else " >? ") else "") +
                    " AND " + MessageEntry.COLUMN_FLAG + " != '" + MessageFlag.DELETED.flag + "'",
                args,
                null,
                null,
                MessageEntry.COLUMN_SEND_TIME + " " + sort.name,
                if (limit > 0) "?" else ""
            )?.use { cursor ->
                val entities: MutableList<MessageEntity> = Lists.newArrayList()
                val index = MessageEntry.getIndex(cursor)
                while (cursor.moveToNext()) {
                    val builder = formatByCursor(index, cursor)
                    entities.add(builder)
                }
                CELog.w(
                    String.format(
                        "MessageReference:: findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName roomId -->%s, count-->%s, use time->%s /s",
                        roomId,
                        entities.size,
                        ((System.currentTimeMillis() - useTime) / 1000.0)
                    )
                )
                emit(entities)
            } ?: emit(mutableListOf())
    }.flowOn(Dispatchers.IO).catch { emit(Lists.newArrayList()) }

    /**
     * 主要取得 Local 已儲存在 SQLite 內的訊息 for Java
     */
    fun findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(
        roomId: String,
        time: Long,
        sort: Sort,
        limit: Int,
        callback: (List<MessageEntity>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            val useTime = System.currentTimeMillis()
            val args =
                if ((time > 0)
                ) {
                    if ((limit > 0)) {
                        arrayOf(
                            roomId,
                            (time + 1).toString(),
                            limit.toString()
                        )
                    } else {
                        arrayOf(roomId, (time + 1).toString())
                    }
                } else if ((limit > 0)) {
                    arrayOf(roomId, limit.toString())
                } else {
                    arrayOf(roomId)
                }
            openDatabase()
                .query(
                    MessageEntry.TABLE_NAME,
                    null,
                    MessageEntry.COLUMN_ROOM_ID + " =? " +
                        (if (time > 0) " AND " + MessageEntry.COLUMN_SEND_TIME + (if (Sort.DESC == sort) " <=? " else " >? ") else "") +
                        " AND " + MessageEntry.COLUMN_FLAG + " != '" + MessageFlag.DELETED.flag + "'",
                    args,
                    null,
                    null,
                    MessageEntry.COLUMN_SEND_TIME + " " + sort.name,
                    if (limit > 0) "?" else ""
                )?.use { cursor ->
                    val entities: MutableList<MessageEntity> = Lists.newArrayList()
                    val index = MessageEntry.getIndex(cursor)
                    while (cursor.moveToNext()) {
                        val builder = formatByCursor(index, cursor)
                        entities.add(builder)
                    }
                    callback.invoke(entities)
                    CELog.w(
                        String.format(
                            "MessageReference:: findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName roomId -->%s, count-->%s, use time->%s /s",
                            roomId,
                            entities.size,
                            ((System.currentTimeMillis() - useTime) / 1000.0)
                        )
                    )
                } ?: callback.invoke(Lists.newArrayList<MessageEntity>())
        }
    }

    fun findRoomMessageList(
        roomId: String,
        sequence: Int,
        sort: Sort,
        limit: Int
    ) = flow {
        openDatabase()
            .query(
                MessageEntry.TABLE_NAME,
                null,
                MessageEntry.COLUMN_ROOM_ID + " =? AND " + MessageEntry.COLUMN_SEQUENCE + " <= ?",
                arrayOf(roomId, sequence.toString()),
                null,
                null,
                MessageEntry.COLUMN_SEND_TIME + " " + sort.name,
                limit.toString()
            )?.use { cursor ->
                val entities: MutableList<MessageEntity> = Lists.newArrayList()
                val index = MessageEntry.getIndex(cursor)
                while (cursor.moveToNext()) {
                    val builder = formatByCursor(index, cursor)
                    entities.add(builder)
                }
                emit(entities)
            } ?: emit(mutableListOf())
    }.flowOn(Dispatchers.IO).catch { emit(Lists.newArrayList()) }

    /**
     * Find the first message according to room id and MessageStatus[] and Sort
     */
    fun findMessageByRoomIdAndStatusAndLimitOne(
        roomId: String,
        status: Array<Int>,
        sort: Sort,
        callback: (MessageEntity?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            val dateTime = System.currentTimeMillis()
            val statusIn =
                buildString {
                    append(MessageEntry.COLUMN_STATUS)
                    append(" IN (")
                    append(status.joinToString(",") { "'$it'" })
                    append(")")
                }
            db
                .query(
                    MessageEntry.TABLE_NAME,
                    null,
                    MessageEntry.COLUMN_ROOM_ID + "=?" +
                        (if (status.isNotEmpty()) " AND $statusIn" else "") +
                        " AND " + MessageEntry.COLUMN_FLAG + " !='" + MessageFlag.DELETED.flag + "'",
                    arrayOf(roomId),
                    null,
                    null,
                    MessageEntry.COLUMN_SEND_TIME + " " + sort.name,
                    "1"
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = MessageEntry.getIndex(cursor)
                        val message = formatByCursor(index, cursor)
                        CELog.w(String.format("room find last or failed Message  by %s, use time->%s/秒  ", roomId, (System.currentTimeMillis() - dateTime) / 1000.0))
                        callback.invoke(message)
                    } else {
                        callback.invoke(null)
                    }
                } ?: callback.invoke(null)
        }
    }

    fun findIdByRoomIdAndSotFormLimitOne(
        roomId: String,
        sort: Sort,
        callback: (String) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db
                .query(
                    MessageEntry.TABLE_NAME,
                    arrayOf(MessageEntry._ID),
                    MessageEntry.COLUMN_ROOM_ID + "=?",
                    arrayOf<String>(roomId),
                    null,
                    null,
                    MessageEntry.COLUMN_SEND_TIME + " " + sort.name,
                    "1"
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val id = Tools.getDbString(cursor, MessageEntry._ID)
                        callback.invoke(id)
                    } else {
                        callback.invoke("")
                    }
                } ?: callback.invoke("")
        }
    }

    fun findSendTimeByRoomIdAndSotFormLimitOne(
        roomId: String,
        sort: Sort,
        callback: (Long) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db
                .query(
                    MessageEntry.TABLE_NAME,
                    arrayOf(MessageEntry.COLUMN_SEND_TIME),
                    MessageEntry.COLUMN_ROOM_ID + "=?",
                    arrayOf(roomId, sort.name),
                    null,
                    null,
                    MessageEntry.COLUMN_SEND_TIME + "?",
                    "1"
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val sendTime = Tools.getDbLong(cursor, MessageEntry.COLUMN_SEND_TIME)
                        callback.invoke(sendTime)
                    } else {
                        callback.invoke(0L)
                    }
                } ?: callback.invoke(0L)
        }
    }

    @Synchronized
    fun updateSendName(
        roomId: String,
        name: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val whereClause = MessageEntry.COLUMN_ROOM_ID + " = ?"
                val args = arrayOf(roomId)
                val values = ContentValues()
                values.put(MessageEntry.COLUMN_ROOM_ID, roomId)
                values.put(MessageEntry.COLUMN_SENDER_NAME, name)
                db.update(MessageEntry.TABLE_NAME, values, whereClause, args)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateSendNameBySenderId(
        senderId: String,
        name: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val whereClause = MessageEntry.COLUMN_SENDER_ID + " = ?"
                val args = arrayOf(senderId)
                val values = ContentValues()
                values.put(MessageEntry.COLUMN_SENDER_ID, senderId)
                values.put(MessageEntry.COLUMN_SENDER_NAME, name)
                db.update(MessageEntry.TABLE_NAME, values, whereClause, args)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateMessageFormat(
        messageId: String?,
        formatContent: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        messageId?.let {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val whereClause = MessageEntry._ID + " = ?"
                    val whereArgs = arrayOf(messageId)
                    val values = ContentValues()
                    values.put(MessageEntry._ID, messageId)
                    values.put(MessageEntry.COLUMN_CONTENT, formatContent)
                    db.update(MessageEntry.TABLE_NAME, values, whereClause, whereArgs)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }
    }

    fun findDoesNotExistIdsByIds(
        roomId: String,
        messageIds: Set<String>,
        callback: (Set<String>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            val newMessageIds: MutableSet<String> = mutableSetOf()
            newMessageIds.addAll(messageIds)
            val statusIn =
                buildString {
                    append(MessageEntry._ID)
                    append(" IN (")
                    append(messageIds.joinToString(",") { "'$it'" })
                    append(")")
                }
            db
                .query(
                    MessageEntry.TABLE_NAME,
                    arrayOf(MessageEntry._ID),
                    MessageEntry.COLUMN_ROOM_ID + " =? AND ?",
                    arrayOf(roomId, statusIn),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val id = Tools.getDbString(cursor, MessageEntry._ID)
                        newMessageIds.remove(id)
                    }
                    callback.invoke(newMessageIds)
                } ?: callback.invoke(mutableSetOf())
        }
    }

    @Synchronized
    fun updateNearMessage(message: MessageEntity) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val values = ContentValues()
                    values.put(MessageEntry.COLUMN_THEME_ID, message.themeId)
                    values.put(MessageEntry.COLUMN_NEAR_MESSAGE_ID, message.nearMessageId)
                    values.put(MessageEntry.COLUMN_NEAR_MESSAGE_TYPE, message.nearMessageType!!.value)
                    values.put(MessageEntry.COLUMN_NEAR_MESSAGE_AVATAR_ID, message.nearMessageAvatarId)
                    values.put(MessageEntry.COLUMN_NEAR_MESSAGE_CONTENT, message.nearMessageContent)
                    values.put(MessageEntry.COLUMN_NEAR_MESSAGE_SEND_ID, message.nearMessageSenderId)
                    values.put(
                        MessageEntry.COLUMN_NEAR_MESSAGE_SEND_NAME,
                        message.nearMessageSenderName
                    )
                    db.update(
                        MessageEntry.TABLE_NAME,
                        values,
                        MessageEntry._ID + " = ?",
                        arrayOf(message.id)
                    )
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    @Synchronized
    fun updateFacebookMessageContent(message: MessageEntity) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val values = ContentValues()
                    values.put(MessageEntry.COLUMN_CONTENT, message.content)
                    values.put(MessageEntry.COLUMN_TAG, message.tag)
                    db.update(
                        MessageEntry.TABLE_NAME,
                        values,
                        MessageEntry._ID + " = ?",
                        arrayOf(message.id)
                    )
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    @Synchronized
    fun updateFacebookPostStatus(
        roomId: String,
        messageId: String?,
        facebookPostStatus: FacebookPostStatus
    ) = CoroutineScope(Dispatchers.IO).launch {
        messageId?.let {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val values = ContentValues()
                    values.put(MessageEntry.COLUMN_FACEBOOK_POST_STATUS, facebookPostStatus.name)
                    db.update(
                        MessageEntry.TABLE_NAME,
                        values,
                        MessageEntry.COLUMN_ROOM_ID + " = ? AND " + MessageEntry._ID + " = ?",
                        arrayOf(roomId, messageId)
                    )
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }
    }

    @Synchronized
    fun updateFacebookCommentStatus(
        roomId: String,
        messageId: String?,
        facebookCommentStatus: FacebookCommentStatus
    ) = CoroutineScope(Dispatchers.IO).launch {
        messageId?.let {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val values = ContentValues()
                    values.put(
                        MessageEntry.COLUMN_FACEBOOK_COMMENT_STATUS,
                        facebookCommentStatus.name
                    )
                    db.update(
                        MessageEntry.TABLE_NAME,
                        values,
                        MessageEntry.COLUMN_ROOM_ID + " = ? AND " + MessageEntry._ID + " = ?",
                        arrayOf(roomId, messageId)
                    )
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }
    }

    @Synchronized
    fun updateFacebookPrivateReplyStatus(
        roomId: String,
        messageId: String?,
        status: Boolean
    ) = CoroutineScope(Dispatchers.IO).launch {
        messageId?.let {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val values = ContentValues()
                    values.put(
                        MessageEntry.COLUMN_IS_FACEBOOK_PRIVATE_REPLIED,
                        if (status) 1 else 0
                    )
                    db.update(
                        MessageEntry.TABLE_NAME,
                        values,
                        MessageEntry.COLUMN_ROOM_ID + " = ? AND " + MessageEntry._ID + " = ?",
                        arrayOf(roomId, messageId)
                    )
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }
    }

    @Synchronized
    fun deleteMessageByRoomId(roomId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val whereClause = MessageEntry.COLUMN_ROOM_ID + " = ?"
                    val args = arrayOf(roomId)
                    db.delete(MessageEntry.TABLE_NAME, whereClause, args)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    @Synchronized
    fun deleteMessageByRoomIdByFlow(roomId: String) =
        flow {
            val db = openDatabase()
            db.beginTransaction()
            try {
                val whereClause = MessageEntry.COLUMN_ROOM_ID + " = ?"
                val args = arrayOf(roomId)
                val isDeleted = db.delete(MessageEntry.TABLE_NAME, whereClause, args) > 0
                db.setTransactionSuccessful()
                emit(isDeleted)
            } catch (ignored: Exception) {
                emit(false)
            } finally {
                db.endTransaction()
            }
        }.flowOn(Dispatchers.IO)

    @Synchronized
    fun updateBroadcastFlag(
        broadcastRoomId: String,
        messageId: String,
        flag: BroadcastFlag
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val whereClause = (
                    MessageEntry.COLUMN_ROOM_ID + " =? " +
                        " AND " + MessageEntry._ID + " =? "
                )
                val whereArgs = arrayOf(broadcastRoomId, messageId)
                val values = ContentValues()
                values.put(MessageEntry.COLUMN_BROADCAST_FLAG, flag.name)
                db.update(
                    MessageEntry.TABLE_NAME,
                    values,
                    whereClause,
                    whereArgs
                )
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun deleteByRoomIdAndMessageIds(
        roomId: String,
        messageIds: Array<String>
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val whereClause =
                    buildString {
                        append(MessageEntry.COLUMN_ROOM_ID)
                        append("=? AND ")
                        append(MessageEntry._ID)
                        append(" IN (")
                        append(messageIds.joinToString(",") { "'$it'" })
                        append(")")
                    }
                val whereArgs = arrayOf(roomId)
                db.delete(MessageEntry.TABLE_NAME, whereClause, whereArgs)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun deleteMessageByIds(messageIds: Array<String?>) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val whereClause =
                        buildString {
                            append(MessageEntry._ID)
                            append(" IN (")
                            append(messageIds.joinToString(",") { "'$it'" })
                            append(")")
                        }
                    val values = ContentValues()
                    values.put(MessageEntry.COLUMN_FLAG, MessageFlag.DELETED.flag)
                    db.update(MessageEntry.TABLE_NAME, values, whereClause, messageIds)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    fun deleteMessageById(messageId: String?) =
        flow {
            if (Strings.isNullOrEmpty(messageId)) return@flow
            val db = openDatabase()
            try {
                db.beginTransaction()
                val whereClause = MessageEntry._ID + " = ?"
                val values = ContentValues()
                values.put(MessageEntry.COLUMN_FLAG, MessageFlag.DELETED.flag)
                val number = db.update(MessageEntry.TABLE_NAME, values, whereClause, arrayOf(messageId))
                emit(number > 0)
            } catch (ignored: Exception) {
                emit(false)
            } finally {
                db.endTransaction()
            }
        }.flowOn(Dispatchers.IO)

    fun findAllMessagesByTypeAndKeyWord(
        types: List<MessageType>,
        keyword: String
    ) = flow {
        val dateTime = System.currentTimeMillis()
        val messages: MutableList<MessageEntity> = Lists.newArrayList()
        openDatabase()
            .query(
                MessageEntry.TABLE_NAME,
                null,
                "UPPER(" + MessageEntry.COLUMN_TYPE + ") IN (" + generatePlaceholdersForIn(types.size) +
                    ") AND " + MessageEntry.COLUMN_FLAG + "!= " + MessageFlag.DELETED.flag +
                    " AND ((" + MessageEntry.COLUMN_CONTENT + " NOT LIKE '%\"text\":\"%'" +
                    " AND " + MessageEntry.COLUMN_CONTENT + " LIKE ?)" +
                    " OR " + MessageEntry.COLUMN_CONTENT + " LIKE '%\"text\":?')" +
                    " AND " + MessageEntry.COLUMN_SOURCE_TYPE + " NOT LIKE '" + SourceType.SYSTEM.name + "'" +
                    " AND " + MessageEntry.COLUMN_SOURCE_TYPE + " NOT LIKE '" + SourceType.LOGIN.name + "'",
                toUpperCaseOfMessageType(types).plus("%$keyword%").plus("\"%$keyword%\"%"),
                null,
                null,
                MessageEntry.COLUMN_ROOM_ID + " ASC"
            )?.use { cursor ->
                val index = MessageEntry.getIndex(cursor)
                while (cursor.moveToNext()) {
                    val msg = formatByCursor(index, cursor)
                    messages.add(msg)
                }
                Log.d("Kyle116", String.format("findAllMessagesByTypeAndKeyWord count->%s, use time->%s/秒  ", messages.size, (System.currentTimeMillis() - dateTime) / 1000.0))
                emit(messages)
            } ?: emit(mutableListOf())
    }.flowOn(Dispatchers.IO).catch { emit(Lists.newArrayList()) }

    private fun toUpperCaseOfMessageType(types: List<MessageType>): Array<String?> {
        val result = arrayOfNulls<String>(types.size)
        for (i in types.indices) {
            result[i] = types[i].name.uppercase(Locale.getDefault())
        }
        return result
    }

    private fun toUpperCaseOfChatRoomType(types: List<ChatRoomType>): Array<String?> {
        val result = arrayOfNulls<String>(types.size)
        for (i in types.indices) {
            result[i] = types[i].name.uppercase(Locale.getDefault())
        }
        return result
    }

    private fun generatePlaceholdersForIn(length: Int): String {
        val builder = StringBuilder()
        for (i in 0 until length) {
            builder.append("UPPER(?), ")
        }
        // Remove the last comma and space
        builder.setLength(builder.length - 2)
        return builder.toString()
    }

    fun findByIdsAndRoomId(
        messageIds: Array<String>,
        roomId: String,
        callback: (MutableList<MessageEntity>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        if (messageIds.isNotEmpty()) {
            withDatabase { db ->
                val entities: MutableList<MessageEntity> = Lists.newArrayList()
                val messageIdIn =
                    buildString {
                        append(MessageEntry._ID)
                        append(" IN (")
                        append(messageIds.joinToString(",") { "'$it'" })
                        append(")")
                    }
                db
                    .query(
                        MessageEntry.TABLE_NAME,
                        null,
                        MessageEntry.COLUMN_ROOM_ID + " =? AND ?",
                        arrayOf(roomId, messageIdIn),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        val index = MessageEntry.getIndex(cursor)
                        while (cursor.moveToNext()) {
                            val builder = formatByCursor(index, cursor)
                            entities.add(builder)
                        }
                        callback.invoke(entities)
                    } ?: callback.invoke(Lists.newArrayList())
            }
        } else {
            callback.invoke(Lists.newArrayList())
        }
    }

    fun findLastMessageIdByRoomId(
        roomId: String,
        callback: (String) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db
                .query(
                    MessageEntry.TABLE_NAME,
                    null,
                    MessageEntry.COLUMN_ROOM_ID + " =?" +
                        " AND " + MessageEntry.COLUMN_FLAG + " !='" + MessageFlag.DELETED.flag + "'",
                    arrayOf(roomId),
                    null,
                    null,
                    MessageEntry.COLUMN_SEND_TIME + " DESC",
                    "1"
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val topId = Tools.getDbString(cursor, MessageEntry._ID)
                        callback.invoke(topId)
                    } else {
                        callback.invoke("")
                    }
                } ?: callback.invoke("")
        }
    }

    fun findTopMessageIdByRoomId(
        roomId: String,
        callback: (String) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db
                .query(
                    MessageEntry.TABLE_NAME,
                    null,
                    MessageEntry.COLUMN_ROOM_ID + " =?" +
                        " AND " + MessageEntry.COLUMN_FLAG + " !='" + MessageFlag.DELETED.flag + "'",
                    arrayOf(roomId),
                    null,
                    null,
                    MessageEntry.COLUMN_SEND_TIME + " ASC",
                    "1"
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val topId = Tools.getDbString(cursor, MessageEntry._ID)
                        callback.invoke(topId)
                    } else {
                        callback.invoke("")
                    }
                } ?: callback.invoke("")
        }
    }

    fun findUnreadFirstMessageIdByRoomId(
        roomId: String,
        callback: (String) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db
                .query(
                    MessageEntry.TABLE_NAME,
                    null,
                    MessageEntry.COLUMN_ROOM_ID + "=? AND " + MessageEntry.COLUMN_FLAG + " IN (0, 1)",
                    arrayOf(roomId),
                    null,
                    null,
                    MessageEntry.COLUMN_SEND_TIME + " ASC",
                    "1"
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val unreadFirstMessageId = Tools.getDbString(cursor, MessageEntry._ID)
                        callback.invoke(unreadFirstMessageId)
                    } else {
                        callback.invoke("")
                    }
                } ?: callback.invoke("")
        }
    }

    fun findMessageById(
        messageId: String?,
        callback: (MessageEntity?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        messageId?.let {
            withDatabase { db ->
                val args = arrayOf(messageId)
                db
                    .query(
                        MessageEntry.TABLE_NAME,
                        null,
                        MessageEntry._ID + "= ?",
                        args,
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val index = MessageEntry.getIndex(cursor)
                            val messageEntityBuilder = formatByCursor(index, cursor)
                            callback.invoke(messageEntityBuilder)
                        } else {
                            callback.invoke(null)
                        }
                    } ?: callback.invoke(null)
            }
        } ?: callback.invoke(null)
    }

    fun findMessageById(messageId: String?) =
        flow {
            if (Strings.isNullOrEmpty(messageId)) return@flow
            val args = arrayOf(messageId)
            openDatabase()
                .query(
                    MessageEntry.TABLE_NAME,
                    null,
                    MessageEntry._ID + "= ?",
                    args,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = MessageEntry.getIndex(cursor)
                        val messageEntityBuilder = formatByCursor(index, cursor)
                        emit(messageEntityBuilder)
                    } else {
                        emit(null)
                    }
                }
        }.flowOn(Dispatchers.IO).catch { emit(null) }

    private suspend fun findMessageByIdWithContext(messageId: String?): MessageEntity? =
        withContext(Dispatchers.IO) {
            var builder: MessageEntity? = null
            withDatabase { db ->
                val args = arrayOf(messageId)
                db
                    .query(
                        MessageEntry.TABLE_NAME,
                        null,
                        MessageEntry._ID + "= ?",
                        args,
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val index = MessageEntry.getIndex(cursor)
                            val messageEntityBuilder = formatByCursor(index, cursor)
                            builder = messageEntityBuilder
                        }
                    } ?: builder
            }
            builder
        }

    fun queryMessageById(messageId: String?): CompletableFuture<MessageEntity?> = GlobalScope.future { findMessageByIdWithContext(messageId) }

    fun hasLocalDataOfMessage(
        roomId: String,
        messageId: String,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db
                .query(
                    MessageEntry.TABLE_NAME,
                    arrayOf(MessageEntry._ID),
                    MessageEntry.COLUMN_ROOM_ID + "=? AND " + MessageEntry._ID + "=?",
                    arrayOf(roomId, messageId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    callback.invoke(cursor.count > 0)
                } ?: callback.invoke(false)
        }
    }

    @Synchronized
    fun deleteFriendLabelByAccountId(accountId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val whereClause = DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID + " =? "
                    db.delete(
                        DBContract.FriendsLabelRel.TABLE_NAME,
                        whereClause,
                        arrayOf<String>(accountId)
                    )
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    @Synchronized
    fun deleteLabelByIdAndUserId(
        labelId: String,
        userId: String,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                var newLabelId = labelId
                var status = false
                db
                    .query(
                        DBContract.LabelEntry.TABLE_NAME + " AS l " +
                            " INNER JOIN " + DBContract.FriendsLabelRel.TABLE_NAME + " AS f " +
                            " ON l." + DBContract.LabelEntry._ID + "= f." + DBContract.FriendsLabelRel.COLUMN_LABEL_ID,
                        arrayOf("l.*"),
                        "l." + DBContract.LabelEntry.COLUMN_READ_ONLY + "=? " +
                            " AND f." + DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID + "=?",
                        arrayOf("true", userId),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            newLabelId = Tools.getDbString(cursor, DBContract.LabelEntry._ID)

                            val whereClause = DBContract.FriendsLabelRel.COLUMN_ID + " =? "
                            val whereArgs = arrayOf(userId + newLabelId)
                            status = db.delete(DBContract.FriendsLabelRel.TABLE_NAME, whereClause, whereArgs) > 0
                        }
                    }

                if (!Strings.isNullOrEmpty(newLabelId)) {
                    db
                        .query(
                            DBContract.FriendsLabelRel.TABLE_NAME,
                            null,
                            DBContract.FriendsLabelRel.COLUMN_LABEL_ID + " =?",
                            arrayOf(newLabelId),
                            null,
                            null,
                            null
                        )?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                val whereClause = DBContract.LabelEntry._ID + " =? "
                                status =
                                    if (status) {
                                        db.delete(
                                            DBContract.LabelEntry.TABLE_NAME,
                                            whereClause,
                                            arrayOf(newLabelId)
                                        ) > 0
                                    } else {
                                        false
                                    }
                            }
                        }
                }
                callback.invoke(status)
                db.setTransactionSuccessful()
            } catch (ignored: Exception) {
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    private fun saveAgentsRelByServiceNumber(entity: ServiceNumberEntity) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val whereClause = DBContract.ServiceNumberAgentRel.COLUMN_SERVICE_NUMBER_ID + "=?"
                    val whereArgs = arrayOf<String>(entity.serviceNumberId)
                    db.delete(
                        DBContract.ServiceNumberAgentRel.TABLE_NAME,
                        whereClause,
                        whereArgs
                    )

                    var result = true
                    val values = ContentValues()
                    for (profile in entity.memberItems) {
                        values.put(DBContract.ServiceNumberAgentRel._ID, entity.serviceNumberId + profile.id)
                        values.put(
                            DBContract.ServiceNumberAgentRel.COLUMN_SERVICE_NUMBER_ID,
                            entity.serviceNumberId
                        )
                        values.put(
                            DBContract.ServiceNumberAgentRel.COLUMN_BROADCAST_ROOM_ID,
                            entity.broadcastRoomId
                        )
                        values.put(DBContract.ServiceNumberAgentRel.COLUMN_AGENT_ID, profile.id)
                        values.put(
                            DBContract.ServiceNumberAgentRel.COLUMN_AGENT_PRIVILEGE,
                            profile.privilege.type
                        )
                        val id =
                            db.replace(
                                DBContract.ServiceNumberAgentRel.TABLE_NAME,
                                null,
                                values
                            ) > 0
                        result = result && id
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    fun findIsFavouriteById(
        id: String?,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        id?.let {
            withDatabase { db ->
                db
                    .query(
                        DBContract.FriendsLabelRel.TABLE_NAME + " AS f " +
                            " INNER JOIN " + DBContract.LabelEntry.TABLE_NAME + " AS l " +
                            " ON l." + DBContract.LabelEntry._ID + "= f." + DBContract.FriendsLabelRel.COLUMN_LABEL_ID,
                        arrayOf("f.*"),
                        "l." + DBContract.LabelEntry.COLUMN_READ_ONLY + "=? " +
                            " AND f." + DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID + "=?",
                        arrayOf<String>("true", id),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        callback.invoke(cursor.count > 0)
                    } ?: run {
                    callback.invoke(false)
                }
            }
        }
    }

    // 找商業服務號
    fun findAllServiceNumber(userId: String) =
        flow {
            val list: MutableList<ServiceNumberEntity> = Lists.newArrayList()
            openDatabase()
                .query(
                    DBContract.ServiceNumEntry.TABLE_NAME,
                    null,
                    DBContract.ServiceNumEntry.COLUMN_STATUS + " !=?" +
                        " AND (" + DBContract.ServiceNumEntry.COLUMN_OWNER_ID + " != ?" +
                        " OR " + DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE + " != '" + ServiceNumberType.BOSS.type + "')",
                    arrayOf(User.Status.DISABLE, userId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val index = DBContract.ServiceNumEntry.getIndex(cursor)
                    while (cursor.moveToNext()) {
                        val entity = ServiceNumberEntity.formatByCursor(index, cursor).build()
                        list.add(entity)
                    }
                    emit(list)
                } ?: run {
                emit(mutableListOf<ServiceNumberEntity>())
            }
        }.flowOn(Dispatchers.IO).catch { emit(mutableListOf()) }

    fun findSelfBossServiceNumber(callback: (ServiceNumberEntity?) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db
                    .query(
                        DBContract.ServiceNumEntry.TABLE_NAME,
                        null,
                        DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE + "= 'Boss' " +
                            " AND " + DBContract.ServiceNumEntry.COLUMN_IS_OWNER + "= 'true' " +
                            " AND " + DBContract.ServiceNumEntry.COLUMN_STATUS + "= 'Enable'",
                        null,
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val entity = ServiceNumberEntity.formatByCursor(cursor).build()
                            callback.invoke(entity)
                        } else {
                            callback.invoke(null)
                        }
                    } ?: run { callback.invoke(null) }
            }
        }

    fun findManageServiceNumber(callback: (ServiceNumberEntity?) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db
                    .query(
                        DBContract.ServiceNumEntry.TABLE_NAME,
                        null,
                        DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE + "= '" + ServiceNumberType.MANAGER.type + "'",
                        null,
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val entity = ServiceNumberEntity.formatByCursor(cursor).build()
                            callback.invoke(entity)
                        } else {
                            callback.invoke(null)
                        }
                    } ?: run { callback.invoke(null) }
            }
        }

    fun findBroadcastRoomByIdAndServiceNumberId(
        broadcastRoomId: String,
        serviceNumberId: String,
        callback: (ServiceNumberEntity?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            val sql = (
                "SELECT * FROM " + DBContract.ServiceNumEntry.TABLE_NAME +
                    " WHERE " + DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + "=?" +
                    " AND " + DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID + "=?"
            )
            db
                .query(
                    DBContract.ServiceNumEntry.TABLE_NAME,
                    null,
                    DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + "=? AND " + DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID + "=?",
                    arrayOf(serviceNumberId, broadcastRoomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val entity = ServiceNumberEntity.formatByCursor(cursor).build()
                        callback.invoke(entity)
                    } else {
                        callback.invoke(null)
                    }
                } ?: run { callback.invoke(null) }
        }
    }

    fun findSubscribeNumberById(
        subscribeNumberId: String,
        callback: (ServiceNumberEntity?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db
                .query(
                    DBContract.ServiceNumEntry.TABLE_NAME,
                    null,
                    DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + "=? AND " + DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE + "=?",
                    arrayOf(subscribeNumberId, "true"),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val entity = ServiceNumberEntity.formatByCursor(cursor).build()
                        callback.invoke(entity)
                    } else {
                        callback.invoke(null)
                    }
                } ?: run { callback.invoke(null) }
        }
    }

    fun findServiceNumberById(
        serviceNumberId: String,
        callback: (ServiceNumberEntity?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db
                .query(
                    DBContract.ServiceNumEntry.TABLE_NAME,
                    null,
                    DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + "=?",
                    arrayOf(serviceNumberId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val entity = ServiceNumberEntity.formatByCursor(cursor).build()
                        callback.invoke(entity)
                    } else {
                        callback.invoke(null)
                    }
                } ?: run { callback.invoke(null) }
        }
    }

    fun findBroadcastServiceNumberById(
        serviceNumberId: String,
        callback: (ServiceNumberEntity?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db
                .query(
                    DBContract.ServiceNumEntry.TABLE_NAME,
                    null,
                    DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + "=? AND " + DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID + "!=?",
                    arrayOf<String>(serviceNumberId, ""),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val entity = ServiceNumberEntity.formatByCursor(cursor).build()
                        callback.invoke(entity)
                    } else {
                        callback.invoke(null)
                    }
                } ?: run { callback.invoke(null) }
        }
    }

    @Synchronized
    fun saveAgentsRelByServiceNumberInfo(entity: ServiceNumberEntity?) =
        CoroutineScope(Dispatchers.IO).launch {
            entity?.let {
                withDatabase { db ->
                    db.beginTransaction()
                    try {
                        val serviceNumberId = entity.serviceNumberId
                        val broadcastRoomId = entity.broadcastRoomId

                        val entities = entity.memberItems
                        val whereClause =
                            DBContract.ServiceNumberAgentRel.COLUMN_SERVICE_NUMBER_ID + "=?"
                        val whereArgs = arrayOf(entity.serviceNumberId)
                        db.delete(
                            DBContract.ServiceNumberAgentRel.TABLE_NAME,
                            whereClause,
                            whereArgs
                        )
                        val values = ContentValues()
                        for (profile in entities) {
                            values.put(
                                DBContract.ServiceNumberAgentRel._ID,
                                serviceNumberId + profile.id
                            )
                            values.put(
                                DBContract.ServiceNumberAgentRel.COLUMN_SERVICE_NUMBER_ID,
                                serviceNumberId
                            )
                            values.put(
                                DBContract.ServiceNumberAgentRel.COLUMN_BROADCAST_ROOM_ID,
                                broadcastRoomId
                            )
                            values.put(DBContract.ServiceNumberAgentRel.COLUMN_AGENT_ID, profile.id)
                            values.put(
                                DBContract.ServiceNumberAgentRel.COLUMN_AGENT_PRIVILEGE,
                                profile.privilege.type
                            )
                            db.replace(DBContract.ServiceNumberAgentRel.TABLE_NAME, null, values)
                        }
                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                }
            }
        }

    /**
     * 儲存服務號
     */
    @Synchronized
    fun save(entity: ServiceNumberEntity?) =
        CoroutineScope(Dispatchers.IO).launch {
            entity?.let {
                withDatabase { db ->
                    db.beginTransaction()
                    try {
                        val values = ServiceNumberEntity.getContentValues(entity)
                        if (!Strings.isNullOrEmpty(entity.broadcastRoomId) && entity.memberIds().isNotEmpty()) {
                            saveAgentsRelByServiceNumber(entity)
                        }
                        val entities = entity.statisticsEntities
                        val endTimeSet: MutableSet<Long> = Sets.newHashSet()
                        for (stat in entities) {
                            endTimeSet.add(stat.endTime)
                        }
                        replace(entity.serviceNumberId, entities, endTimeSet)
                        db.replace(DBContract.ServiceNumEntry.TABLE_NAME, null, values)
                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                }
            }
        }

    /**
     * 批量 更換
     */
    @Synchronized
    fun replace(
        relId: String,
        entities: List<StatisticsEntity>,
        endTimeSet: Set<Long?>
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val endTimeIn =
                    buildString {
                        append(DBContract.StatisticsEntry.COLUMN_END_TIME)
                        append(" IN (")
                        append(endTimeSet.joinToString(",") { "'$it'" })
                        append(")")
                    }
                val messageSelection =
                    DBContract.StatisticsEntry.COLUMN_RELATION_ID + "=? AND " + endTimeIn
                val selectionArgs = arrayOf<String>(relId)
                db.delete(
                    DBContract.StatisticsEntry.TABLE_NAME,
                    messageSelection,
                    selectionArgs
                )
                for (entity in entities) {
                    val values = StatisticsEntity.getContentValues(entity, System.currentTimeMillis())
                    db.insert(DBContract.StatisticsEntry.TABLE_NAME, null, values)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    fun querySequenceFromLastMessage(roomId: String) =
        flow {
            openDatabase()
                .query(
                    DBContract.LastMessageEntry.TABLE_NAME,
                    arrayOf(DBContract.LastMessageEntry.COLUMN_SEQUENCE),
                    DBContract.LastMessageEntry.COLUMN_ROOM_ID + " = ?",
                    arrayOf(roomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val sequence = Tools.getDbInt(cursor, DBContract.LastMessageEntry.COLUMN_SEQUENCE)
                        emit(sequence)
                    } else {
                        emit(-1)
                    }
                } ?: emit(-1)
        }.flowOn(Dispatchers.IO).catch { emit(-1) }

    fun hasEmojiData(callback: (Boolean) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                var packageId = ""
                var itemCount = -1
                var emojiPackage = false
                var emojiItem = false
                db
                    .query(
                        DBContract.StickerPackageEntry.TABLE_NAME,
                        null,
                        DBContract.StickerPackageEntry.COLUMN_EMOTICON_TYPE + "=?",
                        arrayOf(EmoticonType.EMOJI.name),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.count > 0) {
                            emojiPackage = true
                            cursor.moveToFirst()
                            packageId = Tools.getDbString(cursor, DBContract.StickerPackageEntry._ID)
                            itemCount = Tools.getDbInt(cursor, DBContract.StickerPackageEntry.COLUMN_ITEM_COUNT)
                        }
                    }
                db
                    .query(
                        DBContract.StickerItemEntry.TABLE_NAME,
                        null,
                        DBContract.StickerItemEntry.COLUMN_STICKER_PACKAGE_ID + "=?",
                        arrayOf(packageId),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.count >= itemCount && itemCount > 0) {
                            emojiItem = true
                        }
                    } ?: run { callback.invoke(false) }
                callback.invoke(emojiPackage && emojiItem)
            }
        }

    @Synchronized
    fun emojiSave(entity: StickerPackageEntity) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val values =
                        StickerPackageEntity.getContentValues(
                            entity,
                            System.currentTimeMillis(),
                            EmoticonType.EMOJI
                        )
                    db.replace(DBContract.StickerPackageEntry.TABLE_NAME, null, values)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    @Synchronized
    fun packageSave(entities: List<StickerPackageEntity>) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val updateTime = System.currentTimeMillis()
                    val oldEntities = entities.map { it }
                    for (entity in oldEntities) {
                        val values =
                            StickerPackageEntity.getContentValues(
                                entity,
                                updateTime,
                                EmoticonType.STICKER
                            )
                        db.replace(DBContract.StickerPackageEntry.TABLE_NAME, null, values)
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    @Synchronized
    fun updateDisablePackageByIds(
        ids: Set<String>,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                var status = true
                for (id in ids) {
                    val whereClause = DBContract.StickerPackageEntry._ID + " = ?"
                    val whereArgs = arrayOf(id)
                    val values = ContentValues()
                    values.put(DBContract.StickerPackageEntry.COLUMN_IS_ENABLE, EnableType.N.name)
                    val isUpdated =
                        db.update(
                            DBContract.StickerPackageEntry.TABLE_NAME,
                            values,
                            whereClause,
                            whereArgs
                        )
                    status = if (status) isUpdated > 0 else status
                }
                callback.invoke(status)
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                callback.invoke(false)
            } finally {
                db.endTransaction()
            }
        }
    }

    fun packageFindAllIds(callback: (Set<String>) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db
                    .query(
                        DBContract.StickerPackageEntry.TABLE_NAME,
                        arrayOf(DBContract.StickerPackageEntry._ID),
                        DBContract.StickerPackageEntry.COLUMN_EMOTICON_TYPE + "=?",
                        arrayOf(EmoticonType.STICKER.name),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        val ids: MutableSet<String> = Sets.newHashSet()
                        while (cursor.moveToNext()) {
                            val id = Tools.getDbString(cursor, DBContract.StickerPackageEntry._ID)
                            ids.add(id)
                        }
                        callback.invoke(ids)
                    } ?: run {
                    callback.invoke(Sets.newHashSet())
                }
            }
        }

    fun packageFindByIds(
        ids: Set<String?>,
        callback: (List<StickerPackageEntity>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            val selectIn =
                buildString {
                    append(DBContract.StickerPackageEntry._ID)
                    append(" IN (")
                    append(ids.joinToString(",") { "'$it'" })
                    append(")")
                }
            val list: MutableList<StickerPackageEntity> = Lists.newArrayList()
            db
                .query(
                    DBContract.StickerPackageEntry.TABLE_NAME,
                    null,
                    "? AND " + DBContract.StickerPackageEntry.COLUMN_EMOTICON_TYPE + "=?",
                    arrayOf(selectIn, EnableType.Y.name),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val entity = StickerPackageEntity.formatByCursor(cursor, Lists.newArrayList()).build()
                        itemFindAll(entity.id) { items ->
                            if (items.isNotEmpty()) {
                                items.sort()
                                entity.stickerItems = items
                            }
                            list.add(entity)
                        }
                    }
                    callback.invoke(list)
                } ?: run {
                callback.invoke(Lists.newArrayList())
            }
        }
    }

    fun packageFindAll(callback: (List<StickerPackageEntity>) -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                val list: MutableList<StickerPackageEntity> = Lists.newArrayList()
                db
                    .query(
                        DBContract.StickerPackageEntry.TABLE_NAME,
                        null,
                        DBContract.StickerPackageEntry.COLUMN_IS_ENABLE + "=?",
                        arrayOf(EnableType.Y.name),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        while (cursor.moveToNext()) {
                            val entity = StickerPackageEntity.formatByCursor(cursor, Lists.newArrayList()).build()
                            itemFindAll(entity.id) { items ->
                                if (items.isNotEmpty()) {
                                    items.sort()
                                    entity.stickerItems = items
                                }
                                list.add(entity)
                            }
                        }
                        callback.invoke(list)
                    } ?: run {
                    callback.invoke(Lists.newArrayList())
                }
            }
        }

    fun itemFindAll(
        stickerPackageId: String,
        callback: (MutableList<StickerItemEntity>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            val list: MutableList<StickerItemEntity> = mutableListOf()
            db
                .query(
                    DBContract.StickerItemEntry.TABLE_NAME,
                    null,
                    DBContract.StickerItemEntry.COLUMN_STICKER_PACKAGE_ID + "=?",
                    arrayOf(stickerPackageId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        list.add(StickerItemEntity.formatByCursor(cursor))
                    }
                    callback.invoke(list)
                } ?: run { callback.invoke(mutableListOf()) }
        }
    }

    @Synchronized
    fun itemSave(entities: List<StickerItemEntity?>) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val updateTime = System.currentTimeMillis()
                    for (entity in entities) {
                        val values = StickerItemEntity.getContentValues(entity, updateTime)
                        db.replace(DBContract.StickerItemEntry.TABLE_NAME, null, values)
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    /**
     * UserProfileReference functions at beginning
     */
    fun hasUserProfileLocalData(
        userId: String,
        callback: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db
                .query(
                    UserProfileEntry.TABLE_NAME,
                    arrayOf(UserProfileEntry.COLUMN_ID),
                    UserProfileEntry.COLUMN_ID + "=?",
                    arrayOf<String>(userId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    callback.invoke(cursor.count > 0)
                } ?: run { callback.invoke(false) }
        }
    }

    fun findAvatarIdByUserId(
        userId: String?,
        callback: (String) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        if (Strings.isNullOrEmpty(userId)) return@launch
        withDatabase { db ->
            val sql =
                (
                    "SELECT " + UserProfileEntry.COLUMN_AVATAR_URL + " FROM " + UserProfileEntry.TABLE_NAME +
                        " WHERE " + UserProfileEntry.COLUMN_ID + "=?"
                )
            db
                .query(
                    UserProfileEntry.TABLE_NAME,
                    arrayOf(UserProfileEntry.COLUMN_AVATAR_URL),
                    UserProfileEntry.COLUMN_ID + "=?",
                    arrayOf(userId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val avatarId = Tools.getDbString(cursor, UserProfileEntry.COLUMN_AVATAR_URL)
                        callback.invoke(avatarId)
                    } else {
                        callback.invoke("")
                    }
                } ?: run { callback.invoke("") }
        }
    }

    @Synchronized
    fun saveUserProfile(account: UserProfileEntity) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val friendValues = UserProfileEntity.getFriendValues(account)
                    val friendSelection = UserProfileEntry.COLUMN_ID + " = ?"
                    val friendSelectionArgs = arrayOf<String>(account.id)
                    db
                        .query(
                            UserProfileEntry.TABLE_NAME,
                            null,
                            friendSelection,
                            friendSelectionArgs,
                            null,
                            null,
                            null
                        )?.use { cursor ->
                            if (cursor.count <= 0) {
                                friendValues.put(UserProfileEntry.COLUMN_ID, account.id)
                                db.insert(UserProfileEntry.TABLE_NAME, null, friendValues)
                            } else {
                                db.update(
                                    UserProfileEntry.TABLE_NAME,
                                    friendValues,
                                    friendSelection,
                                    friendSelectionArgs
                                )
                            }
                        }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    @Synchronized
    fun saveUserProfiles(profileEntities: Set<UserProfileEntity>) =
        CoroutineScope(Dispatchers.IO).launch {
            withDatabase { db ->
                db.beginTransaction()
                try {
                    val friendSelection = UserProfileEntry.COLUMN_ID + " = ?"
                    for (entity in profileEntities) {
                        val id = entity.id
                        val selectionArgs = arrayOf(id)
                        db
                            .query(
                                UserProfileEntry.TABLE_NAME,
                                null,
                                friendSelection,
                                selectionArgs,
                                null,
                                null,
                                null
                            )?.use { cursor ->
                                if (cursor.count <= 0) {
                                    db.insert(
                                        UserProfileEntry.TABLE_NAME,
                                        null,
                                        UserProfileEntity.getFriendValues(entity)
                                    )
                                } else {
                                    db.update(
                                        UserProfileEntry.TABLE_NAME,
                                        UserProfileEntity.getFriendValues(entity),
                                        friendSelection,
                                        selectionArgs
                                    )
                                }
                            }
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

    fun findByUserIdsTypeAndRelation(
        userType: String,
        relation: Int,
        callback: (Set<String>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            val ids: MutableSet<String> = Sets.newHashSet()
            val args =
                if (relation != -1) arrayOf(userType, relation.toString()) else arrayOf(userType)
            db
                .query(
                    UserProfileEntry.TABLE_NAME,
                    null,
                    UserProfileEntry.COLUMN_USER_TYPE + " =?" + if (relation != -1) " AND " + UserProfileEntry.COLUMN_RELATION + " =? " else "",
                    args,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val userId = Tools.getDbString(cursor, UserProfileEntry.COLUMN_ID)
                        ids.add(userId)
                    }
                    callback.invoke(ids)
                } ?: run { callback.invoke(setOf()) }
        }
    }

    fun findRoomIdByAccountId(
        accountId: String?,
        callback: (String?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        if (Strings.isNullOrEmpty(accountId)) return@launch
        withDatabase { db ->
            db
                .query(
                    UserProfileEntry.TABLE_NAME,
                    arrayOf(UserProfileEntry.COLUMN_ROOM_ID),
                    UserProfileEntry.COLUMN_ID + " =? ",
                    arrayOf(accountId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val roomId = Tools.getDbString(cursor, UserProfileEntry.COLUMN_ROOM_ID)
                        callback.invoke(roomId)
                    } else {
                        callback.invoke(null)
                    }
                } ?: run {
                callback.invoke(null)
            }
        }
    }

    fun findAccountNameFromUsers(accountId: String) =
        flow {
            openDatabase()
                .query(
                    UserProfileEntry.TABLE_NAME,
                    arrayOf(UserProfileEntry.COLUMN_ALIAS, UserProfileEntry.COLUMN_NICKNAME),
                    UserProfileEntry.COLUMN_ID + " =?",
                    arrayOf(accountId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val alias = Tools.getDbString(cursor, UserProfileEntry.COLUMN_ALIAS)
                        val nickname = Tools.getDbString(cursor, UserProfileEntry.COLUMN_NICKNAME)
                        emit(alias?.ifEmpty { nickname } ?: "")
                    } else {
                        emit("")
                    }
                } ?: emit("")
        }.flowOn(Dispatchers.IO).catch { emit("") }

    fun findAccountNameFromUsers(
        accountId: String,
        callback: (String) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            openDatabase()
                .query(
                    UserProfileEntry.TABLE_NAME,
                    arrayOf(UserProfileEntry.COLUMN_ALIAS, UserProfileEntry.COLUMN_NICKNAME),
                    UserProfileEntry.COLUMN_ID + " =?",
                    arrayOf(accountId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val alias = Tools.getDbString(cursor, UserProfileEntry.COLUMN_ALIAS)
                        val nickname = Tools.getDbString(cursor, UserProfileEntry.COLUMN_NICKNAME)
                        callback.invoke(alias?.ifEmpty { nickname } ?: "")
                    }
                } ?: run { callback.invoke("") }
        }
    }

    fun findAccountAvatarId(accountId: String) =
        flow {
            openDatabase()
                .query(
                    UserProfileEntry.TABLE_NAME,
                    arrayOf(UserProfileEntry.COLUMN_AVATAR_URL),
                    UserProfileEntry.COLUMN_ID + " =?",
                    arrayOf(accountId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val avatarId = Tools.getDbString(cursor, UserProfileEntry.COLUMN_AVATAR_URL)
                        emit(avatarId)
                    } else {
                        emit(null)
                    }
                }
        }.flowOn(Dispatchers.IO).catch { emit(null) }

    fun findUserProfileById(id: String?) =
        flow {
            if (Strings.isNullOrEmpty(id)) return@flow
            openDatabase()
                .query(
                    UserProfileEntry.TABLE_NAME,
                    null,
                    UserProfileEntry.COLUMN_ID + "=?",
                    arrayOf(id),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val entity =
                            UserProfileEntity
                                .Build()
                                .id(Tools.getDbString(cursor, UserProfileEntry.COLUMN_ID))
                                .avatarId(Tools.getDbString(cursor, UserProfileEntry.COLUMN_AVATAR_URL))
                                .nickName(Tools.getDbString(cursor, UserProfileEntry.COLUMN_NICKNAME))
                                .name(Tools.getDbString(cursor, UserProfileEntry.COLUMN_NAME))
                                .customerName(Tools.getDbString(cursor, UserProfileEntry.COLUMN_CUSTOMER_NAME))
                                .customerDescription(Tools.getDbString(cursor, UserProfileEntry.COLUMN_CUSTOMER_DESCRIPTION))
                                .customerBusinessCardUrl(Tools.getDbString(cursor, UserProfileEntry.COLUMN_CUSTOMER_BUSINESS_CARD_URL))
                                .mood(Tools.getDbString(cursor, UserProfileEntry.COLUMN_SIGNATURE))
                                .type(AccountType.of(Tools.getDbInt(cursor, UserProfileEntry.COLUMN_RELATION)))
                                .alias(Tools.getDbString(cursor, UserProfileEntry.COLUMN_ALIAS))
                                .roomId(Tools.getDbString(cursor, UserProfileEntry.COLUMN_ROOM_ID))
                                .isBlock(1 == Tools.getDbInt(cursor, UserProfileEntry.COLUMN_BLOCK))
                                .isCollection("true" == Tools.getDbString(cursor, UserProfileEntry.COLUMN_COLLECTION))
                                .otherPhone(Tools.getDbString(cursor, UserProfileEntry.COLUMN_OTHER_PHONE))
                                .userType(UserType.of(Tools.getDbString(cursor, UserProfileEntry.COLUMN_USER_TYPE)))
                                .department(Tools.getDbString(cursor, UserProfileEntry.COLUMN_DEPARTMENT))
                                .duty(Tools.getDbString(cursor, UserProfileEntry.COLUMN_DUTY))
                                .build()
                        emit(entity)
                    } else {
                        emit(null)
                    }
                }
        }.flowOn(Dispatchers.IO).catch { emit(null) }

    fun findUserProfileById(
        id: String?,
        callback: (UserProfileEntity?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        if (Strings.isNullOrEmpty(id)) {
            callback.invoke(null)
            return@launch
        }
        withDatabase { db ->
            db
                .query(
                    UserProfileEntry.TABLE_NAME,
                    null,
                    UserProfileEntry.COLUMN_ID + "=?",
                    arrayOf(id),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val entity =
                            UserProfileEntity
                                .Build()
                                .id(Tools.getDbString(cursor, UserProfileEntry.COLUMN_ID))
                                .avatarId(Tools.getDbString(cursor, UserProfileEntry.COLUMN_AVATAR_URL))
                                .nickName(Tools.getDbString(cursor, UserProfileEntry.COLUMN_NICKNAME))
                                .name(Tools.getDbString(cursor, UserProfileEntry.COLUMN_NAME))
                                .customerName(Tools.getDbString(cursor, UserProfileEntry.COLUMN_CUSTOMER_NAME))
                                .customerDescription(Tools.getDbString(cursor, UserProfileEntry.COLUMN_CUSTOMER_DESCRIPTION))
                                .customerBusinessCardUrl(Tools.getDbString(cursor, UserProfileEntry.COLUMN_CUSTOMER_BUSINESS_CARD_URL))
                                .mood(Tools.getDbString(cursor, UserProfileEntry.COLUMN_SIGNATURE))
                                .type(AccountType.of(Tools.getDbInt(cursor, UserProfileEntry.COLUMN_RELATION)))
                                .alias(Tools.getDbString(cursor, UserProfileEntry.COLUMN_ALIAS))
                                .roomId(Tools.getDbString(cursor, UserProfileEntry.COLUMN_ROOM_ID))
                                .isBlock(1 == Tools.getDbInt(cursor, UserProfileEntry.COLUMN_BLOCK))
                                .isCollection("true" == Tools.getDbString(cursor, UserProfileEntry.COLUMN_COLLECTION))
                                .otherPhone(Tools.getDbString(cursor, UserProfileEntry.COLUMN_OTHER_PHONE))
                                .userType(UserType.of(Tools.getDbString(cursor, UserProfileEntry.COLUMN_USER_TYPE)))
                                .department(Tools.getDbString(cursor, UserProfileEntry.COLUMN_DEPARTMENT))
                                .duty(Tools.getDbString(cursor, UserProfileEntry.COLUMN_DUTY))
                                .build()
                        callback.invoke(entity)
                    } else {
                        callback.invoke(null)
                    }
                } ?: run { callback.invoke(null) }
        }
    }

    private suspend fun getChatRoomDiscussTitle(
        roomId: String,
        selfId: String,
        limit: Int,
        originalQuantity: Int
    ): String =
        withContext(Dispatchers.IO) {
            var title = ""
            withDatabase { db ->
                val dateTime = System.currentTimeMillis()
                db
                    .query(
                        AccountRoomRel.TABLE_NAME + " AS r " +
                            " INNER JOIN " + UserProfileEntry.TABLE_NAME + " AS f " +
                            " ON f.id = r.account_id ",
                        arrayOf("DISTINCT f.id", "f.nickname", "f.alias"),
                        "r.room_id =? AND r.account_id !=?",
                        arrayOf(roomId, selfId, limit.toString()),
                        null,
                        null,
                        "f.id ASC",
                        "?"
                    )?.use { cursor ->
                        val index = UserProfileEntry.getIndex(cursor)
                        val builder = java.lang.StringBuilder()
                        while (cursor.moveToNext()) {
                            val nickname =
                                cursor.getString(index[UserProfileEntry.COLUMN_NICKNAME]!!)
                            val alias =
                                cursor.getString(index[UserProfileEntry.COLUMN_ALIAS]!!)
                            builder.append(if (!Strings.isNullOrEmpty(alias)) alias else nickname)
                            builder.append(", ")
                        }

                        if (builder.length > 2) {
                            builder.replace(builder.length - 2, builder.length, "")
                        }

                        if (originalQuantity > limit) {
                            builder.append("...")
                        }
                        CELog.w(
                            String.format(
                                "room find chat member avatar data by %s, count->%s, use time->%s/秒  ",
                                roomId,
                                builder.toString(),
                                (System.currentTimeMillis() - dateTime) / 1000.0
                            )
                        )
                        title = builder.toString()
                    } ?: run { title = "" }
            }
            title
        }

    fun getDiscussTitle(
        roomId: String,
        selfId: String,
        limit: Int,
        originalQuantity: Int
    ): CompletableFuture<String> = GlobalScope.future { getChatRoomDiscussTitle(roomId, selfId, limit, originalQuantity) }

    fun getMemberAvatarData(
        roomId: String,
        selfId: String,
        limit: Int,
        callback: (MutableMap<String, String>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db
                .query(
                    AccountRoomRel.TABLE_NAME + " AS r " +
                        " INNER JOIN " + UserProfileEntry.TABLE_NAME + " AS f " +
                        " ON f.id = r.account_id ",
                    arrayOf("DISTINCT f.id", "f.avatar_url"),
                    "r.room_id =? AND r.account_id !=?",
                    arrayOf<String>(roomId, selfId, limit.toString()),
                    null,
                    null,
                    "f.id ASC",
                    "?"
                )?.use { cursor ->
                    val index = UserProfileEntry.getIndex(cursor)
                    val data: MutableMap<String, String> = Maps.newLinkedHashMap()
                    while (cursor.moveToNext()) {
                        index[UserProfileEntry.COLUMN_AVATAR_URL]?.let {
                            data[cursor.getString(index[UserProfileEntry.COLUMN_ID]!!)] = cursor.getString(it) ?: ""
                        }
                    }
                    callback.invoke(data)
                } ?: callback.invoke(mutableMapOf())
        }
    }

    suspend fun findUserProfilesByRoomId(roomId: String): MutableList<UserProfileEntity> =
        withContext(Dispatchers.IO) {
            val profiles: MutableList<UserProfileEntity> = mutableListOf()
            withDatabase { db ->
                val dateTime = System.currentTimeMillis()
                db
                    .query(
                        AccountRoomRel.TABLE_NAME +
                            " AS r INNER JOIN " + UserProfileEntry.TABLE_NAME + " AS f ON f.id = r.account_id",
                        arrayOf("DISTINCT f.*"),
                        "r.room_id =? ",
                        arrayOf(roomId),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        val index = UserProfileEntry.getIndex(cursor)
                        while (cursor.moveToNext()) {
                            val entity =
                                UserProfileEntity
                                    .Build()
                                    .id(cursor.getString(index[UserProfileEntry.COLUMN_ID]!!))
                                    .avatarId(
                                        (
                                            if (cursor.getString(index[UserProfileEntry.COLUMN_AVATAR_URL]!!) == null) {
                                                ""
                                            } else {
                                                cursor.getString(
                                                    index[UserProfileEntry.COLUMN_AVATAR_URL]!!
                                                )
                                            }
                                        )
                                    ).nickName(cursor.getString(index[UserProfileEntry.COLUMN_NICKNAME]!!))
                                    .name(cursor.getString(index[UserProfileEntry.COLUMN_NAME]!!))
                                    .customerName(cursor.getString(index[UserProfileEntry.COLUMN_CUSTOMER_NAME]!!))
                                    .customerDescription(cursor.getString(index[UserProfileEntry.COLUMN_CUSTOMER_DESCRIPTION]!!))
                                    .customerBusinessCardUrl(cursor.getString(index[UserProfileEntry.COLUMN_CUSTOMER_BUSINESS_CARD_URL]!!))
                                    .mood(cursor.getString(index[UserProfileEntry.COLUMN_SIGNATURE]!!))
                                    .type(AccountType.of(cursor.getInt(index[UserProfileEntry.COLUMN_RELATION]!!)))
                                    .alias(cursor.getString(index[UserProfileEntry.COLUMN_ALIAS]!!))
                                    .roomId(cursor.getString(index[UserProfileEntry.COLUMN_ROOM_ID]!!))
                                    .isBlock(1 == cursor.getInt(index[UserProfileEntry.COLUMN_BLOCK]!!))
                                    .isCollection("true" == cursor.getString(index[UserProfileEntry.COLUMN_COLLECTION]!!))
                                    .otherPhone(cursor.getString(index[UserProfileEntry.COLUMN_OTHER_PHONE]!!))
                                    .userType(UserType.of(cursor.getString(index[UserProfileEntry.COLUMN_USER_TYPE]!!)))
                                    .department(cursor.getString(index[UserProfileEntry.COLUMN_DEPARTMENT]!!))
                                    .duty(cursor.getString(index[UserProfileEntry.COLUMN_DUTY]!!))
                                    .openId(cursor.getString(index[UserProfileEntry.COLUMN_OPEN_ID]!!))
                                    .build()
                            profiles.add(entity)
                        }
                        Log.w(
                            "DatabaseManager",
                            String.format(
                                "room find chat member by %s, count->%s, use time->%s/秒  ",
                                roomId,
                                profiles.size,
                                (System.currentTimeMillis() - dateTime) / 1000.0
                            )
                        )
                    } ?: run { profiles }
            }
            profiles
        }

    fun findUserProfilesByRoomIdSync(roomId: String): CompletableFuture<MutableList<UserProfileEntity>> = GlobalScope.future { findUserProfilesByRoomId(roomId) }

    @Synchronized
    fun updateUserAvatar(
        userId: String?,
        avatarId: String?
    ) = CoroutineScope(Dispatchers.IO).launch {
        if (Strings.isNullOrEmpty(userId)) return@launch
        withDatabase { db ->
            db.beginTransaction()
            try {
                val contentValues = ContentValues()
                contentValues.put(UserProfileEntry.COLUMN_AVATAR_URL, avatarId)
                val whereClause = UserProfileEntry.COLUMN_ID + " = ?"
                val whereArgs = arrayOf(userId)
                db.update(
                    UserProfileEntry.TABLE_NAME,
                    contentValues,
                    whereClause,
                    whereArgs
                )
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateCustomerAlias(
        userId: String?,
        alias: String?
    ) = flow {
        if (Strings.isNullOrEmpty(userId)) return@flow
        val db = openDatabase()
        db.beginTransaction()
        try {
            val contentValues = ContentValues()
            contentValues.put(UserProfileEntry.COLUMN_ALIAS, alias)

            val whereClause = UserProfileEntry.COLUMN_ID + " = ?"
            val whereArgs = arrayOf<String?>(userId)
            val isUpdated =
                db.update(
                    UserProfileEntry.TABLE_NAME,
                    contentValues,
                    whereClause,
                    whereArgs
                ) > 0
            db.setTransactionSuccessful()
            emit(isUpdated)
        } catch (ignored: Exception) {
            emit(false)
        } finally {
            db.endTransaction()
        }
    }.flowOn(Dispatchers.IO)

    @Synchronized
    fun updateByCursorNameAndValues(
        userId: String,
        cursorName: String,
        values: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val contentValues = ContentValues()
                contentValues.put(cursorName, values)
                val whereClause = UserProfileEntry.COLUMN_ID + " = ?"
                val whereArgs = arrayOf<String>(userId)
                db.update(
                    UserProfileEntry.TABLE_NAME,
                    contentValues,
                    whereClause,
                    whereArgs
                )
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateByCursorNameAndValuesMap(
        id: String,
        cursorData: Map<String, Any>
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val values = ContentValues()
                for ((key, value) in cursorData) {
                    if (value is String) {
                        values.put(key, value as String?)
                    } else if (value is Boolean) {
                        values.put(key, value as Boolean?)
                    } else if (value is Long) {
                        values.put(key, value as Long?)
                    } else if (value is Double) {
                        values.put(key, value as Double?)
                    } else if (value is Int) {
                        values.put(key, value as Int?)
                    } else if (value is Float) {
                        values.put(key, value as Float?)
                    } else if (value is Byte) {
                        values.put(key, value as Byte?)
                    } else if (value is ByteArray) {
                        values.put(key, value as ByteArray?)
                    } else if (value is Short) {
                        values.put(key, value as Short?)
                    }
                }

                val whereClause = UserProfileEntry.COLUMN_ID + " = ?"
                val whereArgs = arrayOf(id)
                db.update(
                    UserProfileEntry.TABLE_NAME,
                    values,
                    whereClause,
                    whereArgs
                )
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateUserRoomId(
        userId: String,
        roomId: String?
    ) = CoroutineScope(Dispatchers.IO).launch {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put(UserProfileEntry.COLUMN_ROOM_ID, roomId)
                db.update(
                    UserProfileEntry.TABLE_NAME,
                    values,
                    UserProfileEntry.COLUMN_ID + " = ?",
                    arrayOf<String>(userId)
                )
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    @Synchronized
    fun updateNickNameById(
        userId: String,
        nickName: String
    ) = flow {
        val db = openDatabase()
        db.beginTransaction()
        try {
            val values = ContentValues()
            values.put(UserProfileEntry.COLUMN_NICKNAME, nickName)
            val isUpdated =
                db.update(
                    UserProfileEntry.TABLE_NAME,
                    values,
                    UserProfileEntry.COLUMN_ID + " = ?",
                    arrayOf<String>(userId)
                ) > 0
            db.setTransactionSuccessful()
            emit(isUpdated)
        } catch (ignored: Exception) {
            emit(false)
        } finally {
            db.endTransaction()
        }
    }.flowOn(Dispatchers.IO)

    fun findAllMessagesByTypeAndKeyWordForServiceNumber(
        types: List<MessageType>,
        keyword: String
    ) = flow {
        val dateTime = System.currentTimeMillis()
        val messages: MutableList<MessageEntity> = Lists.newArrayList()
        openDatabase()
            .query(
                ChatRoomEntry.TABLE_NAME + " AS c INNER JOIN " + MessageEntry.TABLE_NAME + " AS m ON c." + ChatRoomEntry._ID + " = m." + MessageEntry.COLUMN_ROOM_ID +
                    " AND (c." + ChatRoomEntry.COLUMN_TYPE + " = 'services' OR c." + ChatRoomEntry.COLUMN_TYPE + " = 'subscrible' OR c." + ChatRoomEntry.COLUMN_TYPE + " = 'serviceMember')",
                arrayOf<String>("c." + ChatRoomEntry._ID, "c." + ChatRoomEntry.COLUMN_TYPE, "m.*"),
                (
                    "UPPER(m." + MessageEntry.COLUMN_TYPE + ") IN (" + generatePlaceholdersForIn(types.size) +
                        ") AND m." + MessageEntry.COLUMN_FLAG + "!= " + MessageFlag.DELETED.flag +
                        " AND ((m." + MessageEntry.COLUMN_CONTENT + " NOT LIKE '%\"text\":\"%'" +
                        " AND m." + MessageEntry.COLUMN_CONTENT + " LIKE '%" + keyword + "%')" +
                        " OR m." + MessageEntry.COLUMN_CONTENT + " LIKE '%\"text\":\"%" + keyword + "%\"%')" +
                        " AND m." + MessageEntry.COLUMN_SOURCE_TYPE + " NOT LIKE '" + SourceType.SYSTEM.name + "'" +
                        " AND m." + MessageEntry.COLUMN_SOURCE_TYPE + " NOT LIKE '" + SourceType.LOGIN.name + "'"
                ),
                toUpperCaseOfMessageType(types),
                null,
                null,
                "m." + MessageEntry.COLUMN_ROOM_ID + " ASC"
            )?.use { cursor ->
                val index = MessageEntry.getIndex(cursor)
                while (cursor.moveToNext()) {
                    val msg = formatByCursor(index, cursor)
                    messages.add(msg)
                }
                Log.d(
                    "Kyle116",
                    String.format(
                        "findAllMessagesByTypeAndKeyWord count->%s, use time->%s/秒  ",
                        messages.size,
                        (System.currentTimeMillis() - dateTime) / 1000.0
                    )
                )
            }
        emit(messages)
    }.flowOn(Dispatchers.IO).catch { emit(mutableListOf()) }

    fun findSelfRoom(ownerId: String?) =
        flow {
            if (Strings.isNullOrEmpty(ownerId)) return@flow
            openDatabase()
                .query(
                    ChatRoomEntry.TABLE_NAME,
                    null,
                    ChatRoomEntry.COLUMN_OWNER_ID + " =? " +
                        " AND " + ChatRoomEntry.COLUMN_TYPE + " =?",
                    arrayOf<String?>(ownerId, ChatRoomType.person.name),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = ChatRoomEntry.getIndex(cursor)
                        val entity = ChatRoomEntity.formatByCursor(index, cursor, false).build()
                        emit(entity)
                    }
                } ?: emit(null)
        }.flowOn(Dispatchers.IO).catch { emit(null) }

    fun queryMembersFromRoomId(roomId: String?) =
        flow {
            if (Strings.isNullOrEmpty(roomId)) return@flow
            val members: MutableList<UserProfileEntity> = mutableListOf()
            openDatabase()
                .query(
                    DBContract.ChatRoomMemberIdsEntry.TABLE_NAME,
                    null,
                    DBContract.ChatRoomMemberIdsEntry.COLUMN_ROOM_ID + " = ?",
                    arrayOf(roomId),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val memberId = Tools.getDbString(cursor, DBContract.ChatRoomMemberIdsEntry.COLUMN_MEMBER_ID)
                        val user = queryUser(memberId).single()
                        user?.let {
                            members.add(it)
                        }
                    }
                }
            emit(members)
        }.flowOn(Dispatchers.IO).catch { emit(mutableListOf()) }

    @Synchronized
    fun updateSubscribeServiceNumberTime(serviceNumberId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            val db = openDatabase()
            try {
                db.beginTransaction()
                val values = ContentValues()
                values.put(
                    DBContract.ServiceNumEntry.COLUMN_UPDATE_TIME,
                    System.currentTimeMillis()
                )
                db.update(
                    DBContract.ServiceNumEntry.TABLE_NAME,
                    values,
                    DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + " = ?",
                    arrayOf(serviceNumberId)
                )
                db.setTransactionSuccessful()
            } catch (ignored: java.lang.Exception) {
            } finally {
                db.endTransaction()
            }
        }

    fun createDbTable(db: SQLiteDatabase) {
        // Create chat room entity
        db.execSQL(
            " CREATE TABLE IF NOT EXISTS " + ChatRoomEntry.TABLE_NAME + "( " +
                ChatRoomEntry._ID + " TEXT PRIMARY KEY, " +
                ChatRoomEntry.COLUMN_TITLE + " TEXT DEFAULT '', " +
                ChatRoomEntry.COLUMN_TYPE + " TEXT, " +
                ChatRoomEntry.COLUMN_AVATAR_ID + " TEXT DEFAULT '', " +
                ChatRoomEntry.COLUMN_OWNER_ID + " TEXT DEFAULT '', " +
                ChatRoomEntry.COLUMN_UNREAD_NUMBER + " INTEGER DEFAULT 0, " +
                ChatRoomEntry.COLUMN_IS_TOP + " TEXT DEFAULT 'N', " +
                ChatRoomEntry.COLUMN_TOP_TIME + " LONG, " +
                ChatRoomEntry.COLUMN_IS_CUSTOM_NAME + " TEXT DEFAULT 'N', " +
                ChatRoomEntry.COLUMN_IS_MUTE + " TEXT DEFAULT 'N', " +
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " TEXT DEFAULT '', " +
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_NAME + " TEXT DEFAULT '', " +
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_AVATAR_ID + " TEXT DEFAULT '', " +
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE + " TEXT DEFAULT 'NONE', " +
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID + " TEXT DEFAULT '', " +
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_ID + " TEXT DEFAULT '', " +
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS + " TEXT DEFAULT 'OFF_LINE', " +
                ChatRoomEntry.COLUMN_BUSINESS_ID + " TEXT DEFAULT '', " +
                ChatRoomEntry.COLUMN_BUSINESS_EXECUTOR_ID + " TEXT DEFAULT '', " +
                ChatRoomEntry.COLUMN_BUSINESS_NAME + " TEXT DEFAULT '', " +
                ChatRoomEntry.COLUMN_BUSINESS_CODE + " TEXT DEFAULT 'UNDEF', " +
                ChatRoomEntry.COLUMN_LAST_MESSAGE_ENTITY + " TEXT DEFAULT '{}', " +
                ChatRoomEntry.COLUMN_UPDATE_TIME + " LONG, " +
                ChatRoomEntry.COLUMN_UNFINISHED_EDITED + " TEXT DEFAULT '', " +
                ChatRoomEntry.COLUMN_UNFINISHED_EDITED_TIME + " LONG , " +
                ChatRoomEntry.COLUMN_LIST_CLASSIFY + " TEXT DEFAULT 'ALL', " +
                ChatRoomEntry.COLUMN_SORT_WEIGHTS + " INTEGER DEFAULT 0 , " +
                ChatRoomEntry.COLUMN_CONSULT_ROOM_ID + " TEXT DEFAULT '', " +
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_OPEN_TYPES + " TEXT DEFAULT '[]'," +
                ChatRoomEntry.COLUMN_AI_SERVICE_WARNED + " TEXT DEFAULT 'N'," +
                ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " TEXT DEFAULT 'N'," +
                ChatRoomEntry.COLUMN_PROVISIONAL_IDS + " TEXT DEFAULT '[]'," +
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_STOP + " INTEGER DEFAULT 0," +
                ChatRoomEntry.COLUMN_OWNER_USER_TYPE + " TEXT DEFAULT '', " +
                ChatRoomEntry.COLUMN_MEMBER_IDS + " TEXT DEFAULT '[]', " +
                ChatRoomEntry.COLUMN_LAST_END_SERVICE_TIME + " LONG DEFAULT 0," +
                ChatRoomEntry.COLUMN_INTERACTION_TIME + " LONG DEFAULT 0," +
                ChatRoomEntry.COLUMN_TRANSFER_FLAG + " INTEGER DEFAULT 0, " +
                ChatRoomEntry.COLUMN_TRANSFER_REASON + " TEXT DEFAULT '', " +
                ChatRoomEntry.COLUMN_CHAT_ROOM_MEMBER + " TEXT DEFAULT ''," +
                ChatRoomEntry.COLUMN_DFR_TIME + " LONG," +
                ChatRoomEntry.COLUMN_IS_AT_ME + " INTEGER DEFAULT 0, " +
                ChatRoomEntry.COLUMN_LAST_SEQUENCE + " INTEGER DEFAULT 0)"
        )

        // Create a chat room entity reference
        db.execSQL(
            (
                "CREATE INDEX " + DBContract.ChatRoomEntryIndex.IDX_COLUMN +
                    " ON " + ChatRoomEntry.TABLE_NAME + "( " +
                    ChatRoomEntry.COLUMN_TITLE + "," +
                    ChatRoomEntry.COLUMN_OWNER_ID + ", " +
                    ChatRoomEntry.COLUMN_TYPE +
                    " );"
            )
        )

        // Create chat room message entity
        db.execSQL(
            (
                " CREATE TABLE IF NOT EXISTS " + MessageEntry.TABLE_NAME + "(" +
                    MessageEntry._ID + " TEXT PRIMARY KEY, " +
                    MessageEntry.COLUMN_ROOM_ID + " TEXT, " +
                    MessageEntry.COLUMN_CONTENT + " TEXT, " +
                    MessageEntry.COLUMN_STATUS + " INTEGER, " +
                    MessageEntry.COLUMN_SEND_NUM + " INTEGER, " +
                    MessageEntry.COLUMN_RECEIVED_NUM + " INTEGER, " +
                    MessageEntry.COLUMN_FLAG + " INTEGER, " +
                    MessageEntry.COLUMN_READED_NUM + " INTEGER, " +
                    MessageEntry.COLUMN_SENDER_ID + " TEXT, " +
                    MessageEntry.COLUMN_FROM + " TEXT, " +
                    MessageEntry.COLUMN_SENDER_NAME + " TEXT, " +
                    MessageEntry.COLUMN_SOURCE_TYPE + " TEXT, " +
                    MessageEntry.COLUMN_SEQUENCE + " INTEGER, " +
                    MessageEntry.COLUMN_AVATAR_ID + " TEXT, " +
                    MessageEntry.COLUMN_TYPE + " TEXT, " +
                    MessageEntry.COLUMN_NEAR_MESSAGE_CONTENT + " TEXT, " +
                    MessageEntry.COLUMN_NEAR_MESSAGE_TYPE + " TEXT, " +
                    MessageEntry.COLUMN_NEAR_MESSAGE_ID + " TEXT, " +
                    MessageEntry.COLUMN_NEAR_MESSAGE_SEND_ID + " TEXT, " +
                    MessageEntry.COLUMN_NEAR_MESSAGE_SEND_NAME + " TEXT, " +
                    MessageEntry.COLUMN_NEAR_MESSAGE_AVATAR_ID + " TEXT, " +
                    MessageEntry.COLUMN_THEME_ID + " TEXT, " +
                    MessageEntry.COLUMN_OS_TYPE + " TEXT, " +
                    MessageEntry.COLUMN_DEVICE_TYPE + " TEXT, " + // 1.13.0 Add broadcast message information

                    MessageEntry.COLUMN_CREATE_USER_ID + " TEXT DEFAULT '' , " +
                    MessageEntry.COLUMN_UPDATE_USER_ID + " TEXT DEFAULT '' , " +
                    MessageEntry.COLUMN_BROADCAST_TIME + " INTEGER , " +
                    MessageEntry.COLUMN_BROADCAST_FLAG + " TEXT DEFAULT 'BOOKING' , " +

                    MessageEntry.COLUMN_IS_ENABLE + " TEXT DEFAULT 'Y' , " +
                    MessageEntry.COLUMN_PREVIOUS_MESSAGE_ID + " TEXT DEFAULT '' , " +
                    MessageEntry.COLUMN_SEND_TIME + " INTEGER , " +
                    MessageEntry.COLUMN_TAG + " TEXT DEFAULT '', " +
                    MessageEntry.COLUMN_IS_FACEBOOK_PRIVATE_REPLIED + " INTEGER DEFAULT 0, " +
                    MessageEntry.COLUMN_FACEBOOK_COMMENT_STATUS + " TEXT DEFAULT '', " +
                    MessageEntry.COLUMN_FACEBOOK_POST_STATUS + " TEXT DEFAULT '');"
            )
        )

        // Create an entity reference for chat room messages
        db.execSQL(
            (
                "CREATE INDEX " + DBContract.MessageEntryIndex.IDX_COLUMN +
                    " ON " + MessageEntry.TABLE_NAME + "( " +
                    MessageEntry.COLUMN_TYPE + "," +
                    MessageEntry.COLUMN_ROOM_ID + ", " +
                    MessageEntry.COLUMN_SEND_TIME +
                    " );"
            )
        )

        // Establish account chat room connection
        db.execSQL(
            (
                "CREATE TABLE IF NOT EXISTS " + AccountRoomRel.TABLE_NAME + "(" +
                    AccountRoomRel.COLUMN_ID + " TEXT PRIMARY KEY, " +
                    AccountRoomRel.COLUMN_ACCOUNT_ID + " TEXT, " +
                    AccountRoomRel.COLUMN_ROOM_ID + " TEXT);"
            )
        )

        // Create an account and chat room link reference
        db.execSQL(
            (
                "CREATE INDEX " + DBContract.AccountRoomRelIndex.IDX_COLUMN +
                    " ON " + AccountRoomRel.TABLE_NAME + "( " +
                    AccountRoomRel.COLUMN_ACCOUNT_ID + "," +
                    AccountRoomRel.COLUMN_ROOM_ID +
                    " );"
            )
        )

        db.execSQL(
            (
                "CREATE TABLE IF NOT EXISTS " + DBContract.GroupEntry.TABLE_NAME + "(" +
                    DBContract.GroupEntry._ID + " TEXT PRIMARY KEY, " +
                    DBContract.GroupEntry.COLUMN_CUSTOM_NAME + " INTEGER, " +
                    DBContract.GroupEntry.COLUMN_KIND + " TEXT, " +
                    DBContract.GroupEntry.COLUMN_NAME + " TEXT, " +
                    DBContract.GroupEntry.COLUMN_AVATAR_URL + " TEXT, " +
                    DBContract.GroupEntry.COLUMN_MEMBER_IDS + " TEXT, " +
                    DBContract.GroupEntry.COLUMN_OWNER_ID + " TEXT);"
            )
        )

        createServiceNumberTable(db)

        db.execSQL(
            (
                "CREATE TABLE IF NOT EXISTS " + DBContract.GroupMemberEntry.TABLE_NAME + "(" +
                    DBContract.GroupMemberEntry._ID + " TEXT PRIMARY KEY, " +
                    DBContract.GroupMemberEntry.COLUMN_ACCOUNT_ID + " TEXT, " +
                    DBContract.GroupMemberEntry.COLUMN_NICKNAME + " TEXT, " +
                    DBContract.GroupMemberEntry.COLUMN_ALIAS + " TEXT, " +
                    DBContract.GroupMemberEntry.COLUMN_AVATAR + " TEXT, " +
                    DBContract.GroupMemberEntry.COLUMN_SIGNTURE + " TEXT);"
            )
        )

        db.execSQL(
            (
                "CREATE TABLE IF NOT EXISTS " + DBContract.SearchRecordEntry.TABLE_NAME + "(" +
                    DBContract.SearchRecordEntry.COLUMN_NAME + " TEXT PRIMARY KEY);"
            )
        )

        db.execSQL(
            (
                "CREATE TABLE IF NOT EXISTS " + DBContract.SearchLabelHistoryEntry.TABLE_NAME + "(" +
                    DBContract.SearchLabelHistoryEntry.COLUMN_NAME + " TEXT PRIMARY KEY);"
            )
        )

        db.execSQL(
            (
                "CREATE TABLE IF NOT EXISTS " + DBContract.LabelEntry.TABLE_NAME + "(" +
                    DBContract.LabelEntry._ID + " TEXT PRIMARY KEY, " +
                    DBContract.LabelEntry.COLUMN_NAME + " TEXT, " +
                    DBContract.LabelEntry.COLUMN_USER_IDS + " TEXT, " +
                    DBContract.LabelEntry.COLUMN_USERS + " TEXT, " +
                    DBContract.LabelEntry.COLUMN_CREATE_TIME + " TEXT, " +
                    DBContract.LabelEntry.COLUMN_OWNER_ID + " TEXT, " +
                    DBContract.LabelEntry.COLUMN_READ_ONLY + " TEXT, " +
                    DBContract.LabelEntry.COLUMN_DELETED + " TEXT);"
            )
        )

        db.execSQL(
            (
                "CREATE TABLE IF NOT EXISTS " + DBContract.RecommendEntry.TABLE_NAME + "(" +
                    DBContract.RecommendEntry._ID + " TEXT PRIMARY KEY, " +
                    DBContract.RecommendEntry.COLUMN_NAME + " TEXT, " +
                    DBContract.RecommendEntry.COLUMN_URL + " TEXT);"
            )
        )

        // New account table
        db.execSQL(
            (
                "CREATE TABLE IF NOT EXISTS " + DBContract.USER_INFO.TABLE_NAME + "( " +
                    DBContract.USER_INFO._ID + " TEXT PRIMARY KEY, " +
                    DBContract.USER_INFO.COLUMN_GENDER + " INTEGER, " +
                    DBContract.USER_INFO.COLUMN_OTHER_PHONE + " TEXT, " +
                    DBContract.USER_INFO.COLUMN_MOBILE + " INTEGER, " +
                    DBContract.USER_INFO.COLUMN_LOGIN_NAME + " TEXT, " +
                    DBContract.USER_INFO.COLUMN_BIRTHDAY + " TEXT, " +
                    DBContract.USER_INFO.COLUMN_EMAIL + " TEXT, " +
                    DBContract.USER_INFO.COLUMN_MOOD + " TEXT, " +
                    DBContract.USER_INFO.COLUMN_OPEN_ID + " TEXT );"
            )
        )

        // New friend list
        createUsersTable(db)

        // New friend-tag table
        db.execSQL(
            (
                "CREATE TABLE IF NOT EXISTS " + DBContract.FriendsLabelRel.TABLE_NAME + "(" +
                    DBContract.FriendsLabelRel.COLUMN_ID + " TEXT PRIMARY KEY, " +
                    DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID + " TEXT, " +
                    DBContract.FriendsLabelRel.COLUMN_LABEL_ID + " TEXT);"
            )
        )

        db.execSQL(
            (
                "CREATE TABLE IF NOT EXISTS " + DBContract.SEARCH_HISTORY.TABLE_NAME + "(" +
                    DBContract.SEARCH_HISTORY.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DBContract.SEARCH_HISTORY.COLUMN_CONTENT + " TEXT," +
                    DBContract.SEARCH_HISTORY.COLUMN_TIME + " TEXT);"
            )
        )

        // Create
        createTodoTable(db)

        // topic  entity
        db.execSQL(
            (
                " CREATE TABLE IF NOT EXISTS " + DBContract.BroadcastTopicEntry.TABLE_NAME + "(" +
                    DBContract.BroadcastTopicEntry._ID + " TEXT PRIMARY KEY, " +
                    DBContract.BroadcastTopicEntry.COLUMN_AVATAR_ID + " TEXT DEFAULT '', " +
                    DBContract.BroadcastTopicEntry.COLUMN_NAME + " TEXT DEFAULT '', " +
                    DBContract.BroadcastTopicEntry.COLUMN_DESCRIPTION + " TEXT DEFAULT '');"
            )
        )

        // entity topic rel
        db.execSQL(
            (
                " CREATE TABLE IF NOT EXISTS " + DBContract.EntityTopicRel.TABLE_NAME + "(" +
                    DBContract.EntityTopicRel._ID + " TEXT PRIMARY KEY, " +
                    DBContract.EntityTopicRel.COLUMN_RELATION_ID + " TEXT, " +
                    DBContract.EntityTopicRel.COLUMN_RELATION_TYPE + " TEXT DEFAULT 'UNDEF', " +
                    DBContract.EntityTopicRel.COLUMN_TOPIC_ID + " TEXT );"
            )
        )

        // entity topic rel index
        db.execSQL(
            (
                "CREATE INDEX " + DBContract.EntityTopicRelIndex.IDX_COLUMN +
                    " ON " + DBContract.EntityTopicRel.TABLE_NAME + "( " +
                    DBContract.EntityTopicRel.COLUMN_RELATION_ID + "," +
                    DBContract.EntityTopicRel.COLUMN_RELATION_TYPE +
                    " );"
            )
        )

        db.execSQL(
            (
                " CREATE TABLE IF NOT EXISTS " + DBContract.BusinessEntry.TABLE_NAME + "(" +
                    DBContract.BusinessEntry._ID + " TEXT PRIMARY KEY, " +
                    DBContract.BusinessEntry.COLUMN_BUSINESS_CODE + " TEXT DEFAULT 'Ecp.Task', " +
                    DBContract.BusinessEntry.COLUMN_NAME + " TEXT DEFAULT '', " +
                    DBContract.BusinessEntry.COLUMN_END_TIME + " TEXT, " +
                    DBContract.BusinessEntry.COLUMN_END_TIMESTAMP + " INTEGER, " +
                    DBContract.BusinessEntry.COLUMN_PRIMARY_ID + " TEXT DEFAULT '', " +
                    DBContract.BusinessEntry.COLUMN_PRIMARY_NAME + " TEXT DEFAULT '', " +
                    DBContract.BusinessEntry.COLUMN_MANAGER_ID + " TEXT DEFAULT '', " +
                    DBContract.BusinessEntry.COLUMN_MANAGER_NAME + " TEXT DEFAULT '', " +
                    DBContract.BusinessEntry.COLUMN_MANAGER_AVATAR_ID + " TEXT DEFAULT '', " +
                    DBContract.BusinessEntry.COLUMN_EXECUTOR_ID + " TEXT DEFAULT '', " +
                    DBContract.BusinessEntry.COLUMN_EXECUTOR_NAME + " TEXT DEFAULT '', " +
                    DBContract.BusinessEntry.COLUMN_EXECUTOR_AVATAR_ID + " TEXT DEFAULT '', " +
                    DBContract.BusinessEntry.COLUMN_CUSTOMER_ID + " TEXT DEFAULT '', " +
                    DBContract.BusinessEntry.COLUMN_CUSTOMER_NAME + " TEXT DEFAULT '', " +
                    DBContract.BusinessEntry.COLUMN_CUSTOMER_AVATAR_ID + " TEXT DEFAULT '', " +
                    DBContract.BusinessEntry.COLUMN_IS_ENABLE + " TEXT DEFAULT 'Y', " +
                    DBContract.BusinessEntry.COLUMN_DESCRIPTION + " TEXT DEFAULT '' );"
            )
        )

        // statistical data
        db.execSQL(
            (
                " CREATE TABLE IF NOT EXISTS " + DBContract.StatisticsEntry.TABLE_NAME + "(" +
                    DBContract.StatisticsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    DBContract.StatisticsEntry.COLUMN_RELATION_ID + " TEXT, " +
                    DBContract.StatisticsEntry.COLUMN_ASCRIPTION + " TEXT DEFAULT '', " +
                    DBContract.StatisticsEntry.COLUMN_ORIGINAL_CONTENT + " TEXT , " +
                    DBContract.StatisticsEntry.COLUMN_TOTAL_ROW + " INTEGER, " +
                    DBContract.StatisticsEntry.COLUMN_ROW_COUNT + " INTEGER, " +
                    DBContract.StatisticsEntry.COLUMN_STAT_TYPE + " TEXT, " +
                    DBContract.StatisticsEntry.COLUMN_START_TIME + " INTEGER, " +
                    DBContract.StatisticsEntry.COLUMN_END_TIME + " INTEGER, " +
                    DBContract.StatisticsEntry.COLUMN_UPDATE_TIME + " INTEGER );"
            )
        )

        // statistical data index
        db.execSQL(
            (
                "CREATE INDEX " + DBContract.StatisticsEntryIndex.IDX_COLUMN +
                    " ON " + DBContract.StatisticsEntry.TABLE_NAME + "( " +
                    DBContract.StatisticsEntry.COLUMN_RELATION_ID + "," +
                    DBContract.StatisticsEntry.COLUMN_ASCRIPTION + "," +
                    DBContract.StatisticsEntry.COLUMN_ROW_COUNT + "," +
                    DBContract.StatisticsEntry.COLUMN_STAT_TYPE +
                    " );"
            )
        )

        // Sticker pack information
        db.execSQL(
            (
                " CREATE TABLE IF NOT EXISTS " + DBContract.StickerPackageEntry.TABLE_NAME + "(" +
                    DBContract.StickerPackageEntry._ID + " TEXT PRIMARY KEY, " +
                    DBContract.StickerPackageEntry.COLUMN_PACKAGE_NAME + " TEXT, " +
                    DBContract.StickerPackageEntry.COLUMN_ICON_ID + " TEXT, " +
                    DBContract.StickerPackageEntry.COLUMN_ICON_URL + " TEXT, " +
                    DBContract.StickerPackageEntry.COLUMN_JOIN_TIME + " INTEGER, " +
                    DBContract.StickerPackageEntry.COLUMN_ITEM_COUNT + " INTEGER, " +
                    DBContract.StickerPackageEntry.COLUMN_EMOTICON_TYPE + " TEXT DEFAULT 'STICKER', " +
                    DBContract.StickerPackageEntry.COLUMN_ITEM_LINE + " INTEGER, " +
                    DBContract.StickerPackageEntry.COLUMN_ITEM_ROW + " INTEGER, " +
                    DBContract.StickerPackageEntry.COLUMN_SELECT_ACTION + " TEXT DEFAULT 'UNDEF', " +
                    DBContract.StickerPackageEntry.COLUMN_UPDATE_TIME + " INTEGER, " +

                    DBContract.StickerPackageEntry.COLUMN_IS_ENABLE + " TEXT DEFAULT 'Y' );"
            )
        )

        // Sticker Information
        db.execSQL(
            (
                " CREATE TABLE IF NOT EXISTS " + DBContract.StickerItemEntry.TABLE_NAME + "(" +
                    DBContract.StickerItemEntry._ID + " TEXT PRIMARY KEY, " +
                    DBContract.StickerItemEntry.COLUMN_NAME + " TEXT, " +
                    DBContract.StickerItemEntry.COLUMN_DISPLAY_NAME + " TEXT, " +
                    DBContract.StickerItemEntry.COLUMN_SORT_INDEX + " INTEGER, " +
                    DBContract.StickerItemEntry.COLUMN_KEYWORDS + " TEXT, " +
                    DBContract.StickerItemEntry.COLUMN_PICTURE_ID + " TEXT, " +
                    DBContract.StickerItemEntry.COLUMN_PICTURE_URL + " TEXT, " +
                    DBContract.StickerItemEntry.COLUMN_THUMBNAIL_PICTURE_ID + " TEXT, " +
                    DBContract.StickerItemEntry.COLUMN_THUMBNAIL_PICTURE_URL + " TEXT, " +
                    DBContract.StickerItemEntry.COLUMN_STICKER_PACKAGE_ID + " TEXT, " +
                    DBContract.StickerItemEntry.COLUMN_UPDATE_TIME + " INTEGER, " +
                    DBContract.StickerItemEntry.COLUMN_IS_ENABLE + " TEXT DEFAULT 'Y' );"
            )
        )

        // Sticker Information index
        db.execSQL(
            (
                "CREATE INDEX " + DBContract.StickerItemEntryIndex.IDX_COLUMN +
                    " ON " + DBContract.StickerItemEntry.TABLE_NAME + "( " +
                    DBContract.StickerItemEntry.COLUMN_STICKER_PACKAGE_ID + "," +
                    DBContract.StickerItemEntry.COLUMN_KEYWORDS +
                    " );"
            )
        )

        // entity service number agent rel table
        db.execSQL(
            (
                " CREATE TABLE IF NOT EXISTS " + DBContract.ServiceNumberAgentRel.TABLE_NAME + "(" +
                    DBContract.ServiceNumberAgentRel._ID + " TEXT PRIMARY KEY, " +
                    DBContract.ServiceNumberAgentRel.COLUMN_SERVICE_NUMBER_ID + " TEXT, " +
                    DBContract.ServiceNumberAgentRel.COLUMN_BROADCAST_ROOM_ID + " TEXT, " +
                    DBContract.ServiceNumberAgentRel.COLUMN_AGENT_ID + " TEXT, " +
                    DBContract.ServiceNumberAgentRel.COLUMN_AGENT_PRIVILEGE + " TEXT DEFAULT 'UNDEF');"
            )
        )

        // entity service number agent rel index
        db.execSQL(
            (
                "CREATE INDEX " + DBContract.ServiceNumberAgentRelIndex.IDX_COLUMN +
                    " ON " + DBContract.ServiceNumberAgentRel.TABLE_NAME + "( " +
                    DBContract.ServiceNumberAgentRel.COLUMN_SERVICE_NUMBER_ID + "," +
                    DBContract.ServiceNumberAgentRel.COLUMN_AGENT_ID +
                    " );"
            )
        )

        createApiInfo(db)
        createBossServiceNumberContactTable(db)
        createGroupTable(db)
        createLastMessageTable(db)
        createChatMemberTable(db)
        createChatRoomMemberIdsTable(db)
    }

    fun upgradeDbVersion(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        Log.d("DatabaseManager", "DataBase Version [oldVersion: $oldVersion, newVersion: $newVersion]")
        when (oldVersion) {
            49 -> v49ToV50(db)
            50 -> v50ToV51(db)
            51, 52 -> v52ToV53(db)
            53 -> v53ToV54(db)
            54 -> v54ToV55(db)
            55 -> v55ToV56(db)
            56 -> v56ToV57(db)
            57 -> v57ToV58(db)
            58, 59 -> v59ToV60(db)
            60 -> v60ToV61(db)
            61, 62, 63, 64, 65 -> v65ToV66(db)
            66 -> v66ToV67(db)
            67, 68, 69 -> v69ToV70(db)
            70 -> v70ToV71(db)
            71, 72 -> v71ToV73(db)
            73, 74 -> v73ToV75(db)
            75 -> v75ToV76(db)
            76 -> v76ToV77(db)
            77 -> v77ToV78(db)
            78 -> v78ToV79(db)
            79 -> v79ToV80(db)
            80 -> v80ToV81(db)
            81 -> v81ToV82(db)
            82 -> v82ToV83(db)
            83 -> v83ToV84(db)
        }
    }

    private fun v49ToV50(db: SQLiteDatabase) {
        try {
            db.execSQL(
                "ALTER TABLE " + MessageEntry.TABLE_NAME +
                    " ADD COLUMN " + MessageEntry.COLUMN_PREVIOUS_MESSAGE_ID + " TEXT  DEFAULT '' "
            )
        } catch (e: Exception) {
            CELog.e(e.message)
        }
    }

    private fun v50ToV51(db: SQLiteDatabase) {
        try {
            db.execSQL(
                (
                    "ALTER TABLE " + ChatRoomEntry.TABLE_NAME +
                        " ADD COLUMN " + ChatRoomEntry.COLUMN_IS_MUTE + " TEXT  DEFAULT 'false' "
                )
            )
        } catch (e: Exception) {
            CELog.e(e.message)
        }
    }

    private fun v52ToV53(db: SQLiteDatabase) {
        try {
            // topic  entity
            db.execSQL(
                (
                    " CREATE TABLE IF NOT EXISTS " + DBContract.BroadcastTopicEntry.TABLE_NAME + "(" +
                        DBContract.BroadcastTopicEntry._ID + " TEXT PRIMARY KEY, " +
                        DBContract.BroadcastTopicEntry.COLUMN_AVATAR_ID + " TEXT DEFAULT '', " +
                        DBContract.BroadcastTopicEntry.COLUMN_NAME + " TEXT DEFAULT '', " +
                        DBContract.BroadcastTopicEntry.COLUMN_DESCRIPTION + " TEXT DEFAULT '');"
                )
            )

            // entity topic rel
            db.execSQL(
                (
                    " CREATE TABLE IF NOT EXISTS " + DBContract.EntityTopicRel.TABLE_NAME + "(" +
                        DBContract.EntityTopicRel._ID + " TEXT PRIMARY KEY, " +
                        DBContract.EntityTopicRel.COLUMN_RELATION_ID + " TEXT, " +
                        DBContract.EntityTopicRel.COLUMN_RELATION_TYPE + " TEXT DEFAULT 'UNDEF', " +
                        DBContract.EntityTopicRel.COLUMN_TOPIC_ID + " TEXT );"
                )
            )

            // entity topic rel index
            db.execSQL(
                (
                    "CREATE INDEX " + DBContract.EntityTopicRelIndex.IDX_COLUMN +
                        " ON " + DBContract.EntityTopicRel.TABLE_NAME + "( " +
                        DBContract.EntityTopicRel.COLUMN_RELATION_ID + "," +
                        DBContract.EntityTopicRel.COLUMN_RELATION_TYPE +
                        " );"
                )
            )

            // 1.13.0 Add broadcast message information
            val columnNames: List<String> =
                Lists.newArrayList(
                    MessageEntry.COLUMN_CREATE_USER_ID + " TEXT DEFAULT '' ",
                    MessageEntry.COLUMN_UPDATE_USER_ID + " TEXT DEFAULT '' ",
                    MessageEntry.COLUMN_BROADCAST_TIME + " INTEGER ",
                    MessageEntry.COLUMN_BROADCAST_FLAG + " TEXT DEFAULT 'BOOKING' "
                )

            for (column: String in columnNames) {
                db.execSQL(
                    (
                        "ALTER TABLE " + MessageEntry.TABLE_NAME +
                            " ADD COLUMN " + column
                    )
                )
            }
        } catch (e: Exception) {
            CELog.e(e.message)
        }
    }

    private fun v53ToV54(db: SQLiteDatabase) {
        try {
            db.execSQL(
                (
                    " CREATE TABLE IF NOT EXISTS " + DBContract.BusinessEntry.TABLE_NAME + "(" +
                        DBContract.BusinessEntry._ID + " TEXT PRIMARY KEY, " +
                        DBContract.BusinessEntry.COLUMN_BUSINESS_CODE + " TEXT DEFAULT 'Ecp.Task', " +

                        DBContract.BusinessEntry.COLUMN_NAME + " TEXT DEFAULT '', " +
                        DBContract.BusinessEntry.COLUMN_END_TIME + " TEXT, " +
                        DBContract.BusinessEntry.COLUMN_END_TIMESTAMP + " INTEGER, " +

                        DBContract.BusinessEntry.COLUMN_PRIMARY_ID + " TEXT DEFAULT '', " +
                        DBContract.BusinessEntry.COLUMN_PRIMARY_NAME + " TEXT DEFAULT '', " +

                        DBContract.BusinessEntry.COLUMN_MANAGER_ID + " TEXT DEFAULT '', " +
                        DBContract.BusinessEntry.COLUMN_MANAGER_NAME + " TEXT DEFAULT '', " +
                        DBContract.BusinessEntry.COLUMN_MANAGER_AVATAR_ID + " TEXT DEFAULT '', " +

                        DBContract.BusinessEntry.COLUMN_EXECUTOR_ID + " TEXT DEFAULT '', " +
                        DBContract.BusinessEntry.COLUMN_EXECUTOR_NAME + " TEXT DEFAULT '', " +
                        DBContract.BusinessEntry.COLUMN_EXECUTOR_AVATAR_ID + " TEXT DEFAULT '', " +

                        DBContract.BusinessEntry.COLUMN_CUSTOMER_ID + " TEXT DEFAULT '', " +
                        DBContract.BusinessEntry.COLUMN_CUSTOMER_NAME + " TEXT DEFAULT '', " +
                        DBContract.BusinessEntry.COLUMN_CUSTOMER_AVATAR_ID + " TEXT DEFAULT '', " +
                        DBContract.BusinessEntry.COLUMN_IS_ENABLE + " TEXT DEFAULT 'Y', " +
                        DBContract.BusinessEntry.COLUMN_DESCRIPTION + " TEXT DEFAULT '' );"
                )
            )
        } catch (e: Exception) {
            CELog.e(e.message)
        }
    }

    private fun v54ToV55(db: SQLiteDatabase) {
        // statistical data

        db.execSQL(
            (
                " CREATE TABLE IF NOT EXISTS " + DBContract.StatisticsEntry.TABLE_NAME + "(" +
                    DBContract.StatisticsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    DBContract.StatisticsEntry.COLUMN_RELATION_ID + " TEXT, " +
                    DBContract.StatisticsEntry.COLUMN_ASCRIPTION + " TEXT DEFAULT '', " +
                    DBContract.StatisticsEntry.COLUMN_ORIGINAL_CONTENT + " TEXT , " +
                    DBContract.StatisticsEntry.COLUMN_TOTAL_ROW + " INTEGER, " +
                    DBContract.StatisticsEntry.COLUMN_ROW_COUNT + " INTEGER, " +
                    DBContract.StatisticsEntry.COLUMN_STAT_TYPE + " TEXT, " +
                    DBContract.StatisticsEntry.COLUMN_START_TIME + " INTEGER, " +
                    DBContract.StatisticsEntry.COLUMN_END_TIME + " INTEGER, " +
                    DBContract.StatisticsEntry.COLUMN_UPDATE_TIME + " INTEGER );"
            )
        )

        // statistical data index
        db.execSQL(
            (
                "CREATE INDEX " + DBContract.StatisticsEntryIndex.IDX_COLUMN +
                    " ON " + DBContract.StatisticsEntry.TABLE_NAME + "( " +
                    DBContract.StatisticsEntry.COLUMN_RELATION_ID + "," +
                    DBContract.StatisticsEntry.COLUMN_ASCRIPTION + "," +
                    DBContract.StatisticsEntry.COLUMN_ROW_COUNT + "," +
                    DBContract.StatisticsEntry.COLUMN_STAT_TYPE +
                    " );"
            )
        )

        // Sticker pack information
        db.execSQL(
            (
                " CREATE TABLE IF NOT EXISTS " + DBContract.StickerPackageEntry.TABLE_NAME + "(" +
                    DBContract.StickerPackageEntry._ID + " TEXT PRIMARY KEY, " +
                    DBContract.StickerPackageEntry.COLUMN_PACKAGE_NAME + " TEXT, " +
                    DBContract.StickerPackageEntry.COLUMN_ICON_ID + " TEXT, " +
                    DBContract.StickerPackageEntry.COLUMN_ICON_URL + " TEXT, " +
                    DBContract.StickerPackageEntry.COLUMN_JOIN_TIME + " INTEGER, " +
                    DBContract.StickerPackageEntry.COLUMN_ITEM_COUNT + " INTEGER, " +
                    DBContract.StickerPackageEntry.COLUMN_EMOTICON_TYPE + " TEXT DEFAULT 'STICKER', " +
                    DBContract.StickerPackageEntry.COLUMN_ITEM_LINE + " INTEGER, " +
                    DBContract.StickerPackageEntry.COLUMN_ITEM_ROW + " INTEGER, " +
                    DBContract.StickerPackageEntry.COLUMN_SELECT_ACTION + " TEXT DEFAULT 'UNDEF', " +
                    DBContract.StickerPackageEntry.COLUMN_UPDATE_TIME + " INTEGER, " +
                    DBContract.StickerPackageEntry.COLUMN_IS_ENABLE + " TEXT DEFAULT 'Y' );"
            )
        )

        // Sticker Information
        db.execSQL(
            (
                " CREATE TABLE IF NOT EXISTS " + DBContract.StickerItemEntry.TABLE_NAME + "(" +
                    DBContract.StickerItemEntry._ID + " TEXT PRIMARY KEY, " +
                    DBContract.StickerItemEntry.COLUMN_NAME + " TEXT, " +
                    DBContract.StickerItemEntry.COLUMN_DISPLAY_NAME + " TEXT, " +
                    DBContract.StickerItemEntry.COLUMN_SORT_INDEX + " INTEGER, " +
                    DBContract.StickerItemEntry.COLUMN_KEYWORDS + " TEXT, " +
                    DBContract.StickerItemEntry.COLUMN_PICTURE_ID + " TEXT, " +
                    DBContract.StickerItemEntry.COLUMN_PICTURE_URL + " TEXT, " +
                    DBContract.StickerItemEntry.COLUMN_THUMBNAIL_PICTURE_ID + " TEXT, " +
                    DBContract.StickerItemEntry.COLUMN_THUMBNAIL_PICTURE_URL + " TEXT, " +
                    DBContract.StickerItemEntry.COLUMN_STICKER_PACKAGE_ID + " TEXT, " +
                    DBContract.StickerItemEntry.COLUMN_UPDATE_TIME + " INTEGER, " +
                    DBContract.StickerItemEntry.COLUMN_IS_ENABLE + " TEXT DEFAULT 'Y' );"
            )
        )

        // Sticker Information index
        db.execSQL(
            (
                "CREATE INDEX " + DBContract.StickerItemEntryIndex.IDX_COLUMN +
                    " ON " + DBContract.StickerItemEntry.TABLE_NAME + "( " +
                    DBContract.StickerItemEntry.COLUMN_STICKER_PACKAGE_ID + "," +
                    DBContract.StickerItemEntry.COLUMN_KEYWORDS +
                    " );"
            )
        )
    }

    private fun v55ToV56(db: SQLiteDatabase) {
        db.execSQL(
            (
                "ALTER TABLE " + ChatRoomEntry.TABLE_NAME +
                    " ADD COLUMN " + ChatRoomEntry.COLUMN_BUSINESS_EXECUTOR_ID + " TEXT DEFAULT '' "
            )
        )

        db.execSQL(
            (
                "ALTER TABLE " + ChatRoomEntry.TABLE_NAME +
                    " ADD COLUMN " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_ID + " TEXT DEFAULT '' "
            )
        )
    }

    private fun v56ToV57(db: SQLiteDatabase) {
        // entity service number agent rel table
        db.execSQL(
            (
                " CREATE TABLE IF NOT EXISTS " + DBContract.ServiceNumberAgentRel.TABLE_NAME + "(" +
                    DBContract.ServiceNumberAgentRel._ID + " TEXT PRIMARY KEY, " +
                    DBContract.ServiceNumberAgentRel.COLUMN_SERVICE_NUMBER_ID + " TEXT, " +
                    DBContract.ServiceNumberAgentRel.COLUMN_BROADCAST_ROOM_ID + " TEXT, " +
                    DBContract.ServiceNumberAgentRel.COLUMN_AGENT_ID + " TEXT, " +
                    DBContract.ServiceNumberAgentRel.COLUMN_AGENT_PRIVILEGE + " TEXT DEFAULT 'UNDEF');"
            )
        )

        // entity service number agent rel index
        db.execSQL(
            (
                "CREATE INDEX " + DBContract.ServiceNumberAgentRelIndex.IDX_COLUMN +
                    " ON " + DBContract.ServiceNumberAgentRel.TABLE_NAME + "( " +
                    DBContract.ServiceNumberAgentRel.COLUMN_SERVICE_NUMBER_ID + "," +
                    DBContract.ServiceNumberAgentRel.COLUMN_AGENT_ID +
                    " );"
            )
        )
    }

    private fun v57ToV58(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS " + ChatRoomEntry.TABLE_NAME)
        db.execSQL(
            (
                " CREATE TABLE IF NOT EXISTS " + ChatRoomEntry.TABLE_NAME + "( " +
                    ChatRoomEntry._ID + " TEXT PRIMARY KEY, " +
                    ChatRoomEntry.COLUMN_TITLE + " TEXT DEFAULT '', " +
                    ChatRoomEntry.COLUMN_TYPE + " TEXT, " +
                    ChatRoomEntry.COLUMN_AVATAR_ID + " TEXT DEFAULT '', " +
                    ChatRoomEntry.COLUMN_OWNER_ID + " TEXT DEFAULT '', " +
                    ChatRoomEntry.COLUMN_UNREAD_NUMBER + " INTEGER DEFAULT 0, " +

                    ChatRoomEntry.COLUMN_IS_TOP + " TEXT DEFAULT 'N', " +
                    ChatRoomEntry.COLUMN_TOP_TIME + " LONG, " +
                    ChatRoomEntry.COLUMN_IS_CUSTOM_NAME + " TEXT DEFAULT 'N', " +
                    ChatRoomEntry.COLUMN_IS_MUTE + " TEXT DEFAULT 'N', " +

                    ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " TEXT DEFAULT '', " +
                    ChatRoomEntry.COLUMN_SERVICE_NUMBER_NAME + " TEXT DEFAULT '', " +
                    ChatRoomEntry.COLUMN_SERVICE_NUMBER_AVATAR_ID + " TEXT DEFAULT '', " +
                    ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE + " TEXT DEFAULT 'NONE', " +
                    ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID + " TEXT DEFAULT '', " +
                    ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_ID + " TEXT DEFAULT '', " +
                    ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS + " TEXT DEFAULT 'OFF_LINE', " +

                    ChatRoomEntry.COLUMN_BUSINESS_ID + " TEXT DEFAULT '', " +
                    ChatRoomEntry.COLUMN_BUSINESS_EXECUTOR_ID + " TEXT DEFAULT '', " +
                    ChatRoomEntry.COLUMN_BUSINESS_NAME + " TEXT DEFAULT '', " +
                    ChatRoomEntry.COLUMN_BUSINESS_CODE + " TEXT DEFAULT 'UNDEF', " +

                    ChatRoomEntry.COLUMN_LAST_MESSAGE_ENTITY + " TEXT DEFAULT '{}', " +

                    ChatRoomEntry.COLUMN_UPDATE_TIME + " LONG, " +
                    ChatRoomEntry.COLUMN_UNFINISHED_EDITED + " TEXT DEFAULT '', " +
                    ChatRoomEntry.COLUMN_UNFINISHED_EDITED_TIME + " LONG , " +
                    ChatRoomEntry.COLUMN_LIST_CLASSIFY + " TEXT DEFAULT 'ALL', " +
                    ChatRoomEntry.COLUMN_SORT_WEIGHTS + " INTEGER DEFAULT 0 ); "
            )
        )

        db.execSQL(
            (
                "CREATE INDEX " + DBContract.ChatRoomEntryIndex.IDX_COLUMN +
                    " ON " + ChatRoomEntry.TABLE_NAME + "( " +
                    ChatRoomEntry.COLUMN_TITLE + "," +
                    ChatRoomEntry.COLUMN_OWNER_ID + ", " +
                    ChatRoomEntry.COLUMN_TYPE +
                    " );"
            )
        )
    }

    private fun v59ToV60(db: SQLiteDatabase) {
        db.execSQL(
            (
                "ALTER TABLE " + ChatRoomEntry.TABLE_NAME +
                    " ADD COLUMN " + ChatRoomEntry.COLUMN_CONSULT_ROOM_ID + " TEXT DEFAULT '' "
            )
        )
    }

    private fun v60ToV61(db: SQLiteDatabase) {
        db.execSQL(
            (
                "ALTER TABLE " + ChatRoomEntry.TABLE_NAME +
                    " ADD COLUMN " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_OPEN_TYPES + " TEXT DEFAULT '[]' "
            )
        )
    }

    private fun v65ToV66(db: SQLiteDatabase) {
        db
            .query(
                DBContract.USER_INFO.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
            ).use { cursor ->
                if (cursor.getColumnIndex(DBContract.USER_INFO.COLUMN_OPEN_ID) < 0) {
                    db.execSQL(
                        (
                            "ALTER TABLE " + DBContract.USER_INFO.TABLE_NAME +
                                " ADD COLUMN " + DBContract.USER_INFO.COLUMN_OPEN_ID + " TEXT DEFAULT '' "
                        )
                    )
                }
            }
    }

    private fun v66ToV67(db: SQLiteDatabase) {
        db
            .query(
                DBContract.USER_INFO.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
            ).use { cursor ->
                if (cursor.getColumnIndex(DBContract.USER_INFO.COLUMN_MOOD) < 0) {
                    db.execSQL(
                        (
                            "ALTER TABLE " + DBContract.USER_INFO.TABLE_NAME +
                                " ADD COLUMN " + DBContract.USER_INFO.COLUMN_MOOD + " TEXT DEFAULT '' "
                        )
                    )
                }
            }
    }

    private fun v69ToV70(db: SQLiteDatabase) {
        db
            .query(
                DBContract.LabelEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
            ).use { cursor ->
                if (cursor.getColumnIndex(DBContract.LabelEntry.COLUMN_USER_IDS) < 0) {
                    db.execSQL(
                        (
                            "ALTER TABLE " + DBContract.LabelEntry.TABLE_NAME +
                                " ADD COLUMN " + DBContract.LabelEntry.COLUMN_USER_IDS + " TEXT DEFAULT '' "
                        )
                    )
                }
                if (cursor.getColumnIndex(DBContract.LabelEntry.COLUMN_CREATE_TIME) < 0) {
                    db.execSQL(
                        (
                            "ALTER TABLE " + DBContract.LabelEntry.TABLE_NAME +
                                " ADD COLUMN " + DBContract.LabelEntry.COLUMN_CREATE_TIME + " TEXT DEFAULT '' "
                        )
                    )
                }
                if (cursor.getColumnIndex(DBContract.LabelEntry.COLUMN_OWNER_ID) < 0) {
                    db.execSQL(
                        (
                            "ALTER TABLE " + DBContract.LabelEntry.TABLE_NAME +
                                " ADD COLUMN " + DBContract.LabelEntry.COLUMN_OWNER_ID + " TEXT DEFAULT '' "
                        )
                    )
                }
                if (cursor.getColumnIndex(DBContract.LabelEntry.COLUMN_DELETED) < 0) {
                    db.execSQL(
                        (
                            "ALTER TABLE " + DBContract.LabelEntry.TABLE_NAME +
                                " ADD COLUMN " + DBContract.LabelEntry.COLUMN_DELETED + " TEXT DEFAULT '' "
                        )
                    )
                }
            }
    }

    private fun v70ToV71(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE " + DBContract.API_INFO.TABLE_NAME)
        createApiInfo(db)
        db.execSQL("DROP TABLE " + DBContract.TodoEntry.TABLE_NAME)
        createTodoTable(db)
        db.execSQL("DROP TABLE " + DBContract.ServiceNumEntry.TABLE_NAME)
        createServiceNumberTable(db)
        db.execSQL("DROP TABLE " + UserProfileEntry.TABLE_NAME)
        createUsersTable(db)
        db.execSQL("DROP TABLE " + DBContract.BossServiceNumberContactEntry.TABLE_NAME)
        createBossServiceNumberContactTable(db)
    }

    private fun v71ToV73(db: SQLiteDatabase) {
        try {
            db.execSQL(
                (
                    "ALTER TABLE " + DBContract.API_INFO.TABLE_NAME +
                        " ADD COLUMN " + DBContract.API_INFO.COLUMN_USER_ID + " TEXT  DEFAULT '' "
                )
            )
            db.execSQL(
                (
                    "ALTER TABLE " + ChatRoomEntry.TABLE_NAME +
                        " ADD COLUMN " + ChatRoomEntry.COLUMN_AI_SERVICE_WARNED + " TEXT  DEFAULT 'N' "
                )
            )
        } catch (e: Exception) {
            CELog.e(e.message)
        }
    }

    private fun v73ToV75(db: SQLiteDatabase) {
        try {
            db.execSQL(
                (
                    "ALTER TABLE " + ChatRoomEntry.TABLE_NAME +
                        " ADD COLUMN " + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " TEXT  DEFAULT 'N' "
                )
            )
            db.execSQL(
                (
                    "ALTER TABLE " + ChatRoomEntry.TABLE_NAME +
                        " ADD COLUMN " + ChatRoomEntry.COLUMN_PROVISIONAL_IDS + " TEXT  DEFAULT '[]' "
                )
            )
        } catch (e: Exception) {
            CELog.e(e.message)
        }
    }

    private fun v75ToV76(db: SQLiteDatabase) {
        try {
            db.execSQL(
                (
                    "ALTER TABLE " + ChatRoomEntry.TABLE_NAME +
                        " ADD COLUMN " + ChatRoomEntry.COLUMN_OWNER_USER_TYPE + " TEXT DEFAULT '' "
                )
            )
            db.execSQL(
                (
                    "ALTER TABLE " + ChatRoomEntry.TABLE_NAME +
                        " ADD COLUMN " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_STOP + " INTEGER DEFAULT 0"
                )
            )
            db.execSQL(
                (
                    "ALTER TABLE " + ChatRoomEntry.TABLE_NAME +
                        " ADD COLUMN " + ChatRoomEntry.COLUMN_MEMBER_IDS + " TEXT  DEFAULT '[]' "
                )
            )
            db.execSQL(
                (
                    "ALTER TABLE " + ChatRoomEntry.TABLE_NAME +
                        " ADD COLUMN " + ChatRoomEntry.COLUMN_TRANSFER_FLAG + " INTEGER DEFAULT 0"
                )
            )
            db.execSQL(
                (
                    "ALTER TABLE " + ChatRoomEntry.TABLE_NAME +
                        " ADD COLUMN " + ChatRoomEntry.COLUMN_TRANSFER_REASON + " TEXT DEFAULT '' "
                )
            )
            db.execSQL(
                (
                    "ALTER TABLE " + ChatRoomEntry.TABLE_NAME +
                        " ADD COLUMN " + ChatRoomEntry.COLUMN_LAST_END_SERVICE_TIME + " LONG DEFAULT 0"
                )
            )
            db.execSQL(
                (
                    "ALTER TABLE " + ChatRoomEntry.TABLE_NAME +
                        " ADD COLUMN " + ChatRoomEntry.COLUMN_INTERACTION_TIME + " LONG DEFAULT 0"
                )
            )
        } catch (e: Exception) {
            CELog.e(e.message)
        }
    }

    private fun v76ToV77(db: SQLiteDatabase) {
        try {
            db.execSQL(
                (
                    "ALTER TABLE " + ChatRoomEntry.TABLE_NAME +
                        " ADD COLUMN " + ChatRoomEntry.COLUMN_CHAT_ROOM_MEMBER + " TEXT DEFAULT '' "
                )
            )
        } catch (e: Exception) {
            CELog.e(e.message)
        }
    }

    private fun v77ToV78(db: SQLiteDatabase) {
        try {
            db.execSQL(
                (
                    "ALTER TABLE " + DBContract.BossServiceNumberContactEntry.TABLE_NAME +
                        " ADD COLUMN " + DBContract.BossServiceNumberContactEntry.MOBILE_VISIBLE + " INTEGER DEFAULT 0"
                )
            )
            db.execSQL(
                (
                    "ALTER TABLE " + DBContract.BossServiceNumberContactEntry.TABLE_NAME +
                        " ADD COLUMN " + DBContract.BossServiceNumberContactEntry.STATUS + " TEXT DEFAULT '' "
                )
            )
        } catch (e: Exception) {
            CELog.e(e.message)
        }
    }

    private fun v78ToV79(db: SQLiteDatabase) {
        try {
            db.execSQL(
                (
                    "ALTER TABLE " + ChatRoomEntry.TABLE_NAME +
                        " ADD COLUMN " + ChatRoomEntry.COLUMN_DFR_TIME + " LONG DEFAULT 0"
                )
            )
        } catch (e: Exception) {
            CELog.e(e.message)
        }
    }

    private fun v79ToV80(db: SQLiteDatabase) {
        try {
            db.execSQL(
                (
                    "ALTER TABLE " + ChatRoomEntry.TABLE_NAME +
                        " ADD COLUMN " + ChatRoomEntry.COLUMN_IS_AT_ME + " INTEGER DEFAULT 0"
                )
            )
        } catch (e: Exception) {
            CELog.e(e.message)
        }
    }

    private fun v80ToV81(db: SQLiteDatabase) {
        try {
            db.execSQL(
                (
                    "ALTER TABLE " + MessageEntry.TABLE_NAME +
                        " ADD COLUMN " + MessageEntry.COLUMN_TAG + " TEXT DEFAULT ''"
                )
            )
            db.execSQL(
                (
                    "ALTER TABLE " + MessageEntry.TABLE_NAME +
                        " ADD COLUMN " + MessageEntry.COLUMN_IS_FACEBOOK_PRIVATE_REPLIED + " INTEGER DEFAULT 0"
                )
            )
            db.execSQL(
                (
                    "ALTER TABLE " + MessageEntry.TABLE_NAME +
                        " ADD COLUMN " + MessageEntry.COLUMN_FACEBOOK_COMMENT_STATUS + " TEXT DEFAULT ''"
                )
            )
            db.execSQL(
                (
                    "ALTER TABLE " + MessageEntry.TABLE_NAME +
                        " ADD COLUMN " + MessageEntry.COLUMN_FACEBOOK_POST_STATUS + " TEXT DEFAULT ''"
                )
            )
        } catch (e: Exception) {
            CELog.e(e.message)
        }
    }

    // Put json format to a single table
    private fun v81ToV82(db: SQLiteDatabase) {
        try {
            createLastMessageTable(db)
            createChatMemberTable(db)
            createChatRoomMemberIdsTable(db)
        } catch (e: java.lang.Exception) {
            CELog.e(e.message)
        }
    }

    private fun v82ToV83(db: SQLiteDatabase) {
        try {
            db.execSQL(
                (
                    "ALTER TABLE " + ChatRoomEntry.TABLE_NAME +
                        " ADD COLUMN " + ChatRoomEntry.COLUMN_LAST_SEQUENCE
                ) + " INGEGER DEFAULT 0 "
            )
        } catch (e: java.lang.Exception) {
            CELog.e(e.message)
        }
    }

    private fun v83ToV84(db: SQLiteDatabase) {
        try {
            db.execSQL(
                "ALTER TABLE " + DBContract.ServiceNumEntry.TABLE_NAME +
                    " ADD COLUMN " + DBContract.ServiceNumEntry.COLUMN_UPDATE_TIME + " LONG DEFAULT 0 "
            )
        } catch (e: java.lang.Exception) {
            CELog.e(e.message)
        }
    }

    private fun createServiceNumberTable(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + DBContract.ServiceNumEntry.TABLE_NAME + "(" +
                DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + " TEXT PRIMARY KEY," +
                DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE + " TEXT," +
                DBContract.ServiceNumEntry.COLUMN_DESCRIPTION + " INTEGER ," +
                DBContract.ServiceNumEntry.COLUMN_ROOM_ID + " TEXT, " +
                DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE + " TEXT, " +
                DBContract.ServiceNumEntry.COLUMN_NAME + " TEXT, " +
                DBContract.ServiceNumEntry.COLUMN_AVATAR_URL + " TEXT, " +
                DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID + " TEXT DEFAULT ''," +
                DBContract.ServiceNumEntry.COLUMN_OWNER_ID + " TEXT DEFAULT ''," +
                DBContract.ServiceNumEntry.COLUMN_FIRST_WELCOME_MESSAGE + " TEXT DEFAULT ''," +
                DBContract.ServiceNumEntry.COLUMN_EACH_WELCOME_MESSAGE + " TEXT DEFAULT ''," +
                DBContract.ServiceNumEntry.COLUMN_INTERVAL_WELCOME_MESSAGE + " TEXT DEFAULT ''," +
                DBContract.ServiceNumEntry.COLUMN_IS_ENABLE + " TEXT DEFAULT 'Y'," +
                DBContract.ServiceNumEntry.COLUMN_IS_OWNER + " TEXT DEFAULT 'false'," +
                DBContract.ServiceNumEntry.COLUMN_IS_MANAGER + " TEXT DEFAULT 'false'," +
                DBContract.ServiceNumEntry.COLUMN_IS_COMMON + " TEXT DEFAULT 'false'," +
                DBContract.ServiceNumEntry.COLUMN_IS_IN_SITE_SERVICE + " TEXT DEFAULT 'N'," +
                DBContract.ServiceNumEntry.COLUMN_IS_OUT_SITE_SERVICE + " TEXT DEFAULT 'N'," +
                DBContract.ServiceNumEntry.COLUMN_INTERNAL_SUBSCRIBE_COUNT + " INTEGER," +
                DBContract.ServiceNumEntry.COLUMN_EXTERNAL_SUBSCRIBE_COUNT + " INTEGER," +
                DBContract.ServiceNumEntry.COLUMN_SERVICE_IDEL_TIME + " INTEGER," +
                DBContract.ServiceNumEntry.COLUMN_SERVICE_TIMEOUT_TIME + " INTEGER," +
                DBContract.ServiceNumEntry.COLUMN_STATUS + " TEXT," +
                DBContract.ServiceNumEntry.COLUMN_SERVICE_OPEN_TYPE + " TEXT," +
                DBContract.ServiceNumEntry.COLUMN_ROBOT_SERVICE_FLAG + " TEXT," +
                DBContract.ServiceNumEntry.COLUMN_ROBOT_ID + " TEXT," +
                DBContract.ServiceNumEntry.COLUMN_ROBOT_NAME + " TEXT," +
                DBContract.ServiceNumEntry.COLUMN_MEMBER_ITEMS + " TEXT," +
                DBContract.ServiceNumEntry.COLUMN_SERVICE_MEMBER_ROOM_ID + " TEXT DEFAULT ''," +
                DBContract.ServiceNumEntry.COLUMN_UPDATE_TIME + " LONG DEFAULT 0 );"
        )
    }

    private fun createUsersTable(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + UserProfileEntry.TABLE_NAME + "(" +
                UserProfileEntry.COLUMN_ID + " TEXT PRIMARY KEY, " +
                UserProfileEntry.COLUMN_NICKNAME + " TEXT, " +
                UserProfileEntry.COLUMN_NAME + " TEXT, " +
                UserProfileEntry.COLUMN_AVATAR_URL + " TEXT, " +
                UserProfileEntry.COLUMN_USER_TYPE + " TEXT, " +
                UserProfileEntry.COLUMN_EXTENSION + " TEXT, " +
                UserProfileEntry.COLUMN_DUTY + " TEXT, " +
                UserProfileEntry.COLUMN_DEPARTMENT + " TEXT, " +
                UserProfileEntry.COLUMN_OPEN_ID + " TEXT, " +
                UserProfileEntry.COLUMN_MOOD + " TEXT, " +
                UserProfileEntry.COLUMN_SERVICE_NUMBER_IDS + " TEXT, " +
                UserProfileEntry.COLUMN_SCOPE_ARRAY + " TEXT, " +
                UserProfileEntry.COLUMN_STATUS + " TEXT, " +
                UserProfileEntry.COLUMN_CUSTOMER_NAME + " TEXT, " +
                UserProfileEntry.COLUMN_CUSTOMER_DESCRIPTION + " TEXT, " +
                UserProfileEntry.COLUMN_CUSTOMER_BUSINESS_CARD_URL + " TEXT, " +
                UserProfileEntry.COLUMN_ALIAS + " TEXT, " +
                UserProfileEntry.COLUMN_ROOM_ID + " TEXT, " +
                UserProfileEntry.COLUMN_BLOCK + " INTEGER DEFAULT 0, " +
                UserProfileEntry.COLUMN_COLLECTION + " INTEGER, " +
                UserProfileEntry.COLUMN_RELATION + " INTEGER, " +
                UserProfileEntry.COLUMN_OTHER_PHONE + " TEXT DEFAULT '', " +
                UserProfileEntry.COLUMN_SIGNATURE + " TEXT );"
        )
    }

    private fun createTodoTable(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + DBContract.TodoEntry.TABLE_NAME + "(" +
                DBContract.TodoEntry._ID + " TEXT PRIMARY KEY, " +
                DBContract.TodoEntry.COLUMN_TITLE + " TEXT DEFAULT '', " +
                DBContract.TodoEntry.COLUMN_ROOM_ID + " TEXT DEFAULT '', " +
                DBContract.TodoEntry.COLUMN_MESSAGE_ID + " TEXT DEFAULT '', " +
                DBContract.TodoEntry.COLUMN_USER_ID + " TEXT DEFAULT '', " +
                DBContract.TodoEntry.COLUMN_TODO_STATUS + " TEXT, " +
                DBContract.TodoEntry.COLUMN_PUBLIC_TYPE + " TEXT, " +
                DBContract.TodoEntry.COLUMN_REMIND_TIME + " INTEGER, " +
                DBContract.TodoEntry.COLUMN_CREATE_TIME + " INTEGER, " +
                DBContract.TodoEntry.COLUMN_UPDATE_TIME + " INTEGER, " +
                DBContract.TodoEntry.COLUMN_PROCESS_STATUS + " TEXT DEFAULT 'UNDEF', " +
                DBContract.TodoEntry.COLUMN_OPEN_CLOCK + " TEXT DEFAULT 'N');"
        )
    }

    private fun createApiInfo(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + DBContract.API_INFO.TABLE_NAME + "( " +
                DBContract.API_INFO.COLUMN_SOURCE + " TEXT  PRIMARY KEY, " +
                DBContract.API_INFO.COLUMN_USER_ID + " TEXT, " +
                DBContract.API_INFO.COLUMN_LAST_REFRESH_TIME + " LONG DEFAULT 0 ); "
        )
    }

    private fun createBossServiceNumberContactTable(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + DBContract.BossServiceNumberContactEntry.TABLE_NAME + "(" +
                DBContract.BossServiceNumberContactEntry.ID + " TEXT PRIMARY KEY, " +
                DBContract.BossServiceNumberContactEntry.NAME + " TEXT, " +
                DBContract.BossServiceNumberContactEntry.NICKNAME + " TEXT, " +
                DBContract.BossServiceNumberContactEntry.CUSTOMER_NAME + " TEXT, " +
                DBContract.BossServiceNumberContactEntry.CUSTOMER_DESCRIPTION + " TEXT, " +
                DBContract.BossServiceNumberContactEntry.CUSTOMER_BUSINESS_CARD_URL + " TEXT, " +
                DBContract.BossServiceNumberContactEntry.AVATAR_ID + " TEXT, " +
                DBContract.BossServiceNumberContactEntry.IS_ADDRESS_BOOK + " INTEGER, " +
                DBContract.BossServiceNumberContactEntry.IS_MOBILE + " INTEGER, " +
                DBContract.BossServiceNumberContactEntry.USER_TYPE + " TEXT, " +
                DBContract.BossServiceNumberContactEntry.ROOM_ID + " TEXT, " +
                DBContract.BossServiceNumberContactEntry.OPEN_ID + " TEXT, " +
                DBContract.BossServiceNumberContactEntry.UPDATE_TIME + " LONG, " +
                DBContract.BossServiceNumberContactEntry.SCOPE_ARRAY + " TEXT, " +
                DBContract.BossServiceNumberContactEntry.SERVICE_NUMBER_IDS + " TEXT, " +
                DBContract.BossServiceNumberContactEntry.SCOPE_INFOS + " TEXT, " +
                DBContract.BossServiceNumberContactEntry.MOBILE_VISIBLE + " INTEGER, " +
                DBContract.BossServiceNumberContactEntry.STATUS + " TEXT );"
        )
    }

    private fun createGroupTable(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + DBContract.SyncGroupEntry.TABLE_NAME + "(" +
                DBContract.SyncGroupEntry._ID + " TEXT PRIMARY KEY, " +
                DBContract.SyncGroupEntry.COLUMN_LAST_READ_SEQUENCE + " INTEGER, " +
                DBContract.SyncGroupEntry.COLUMN_LAST_RECEIVED_SEQUENCE + " INTEGER, " +
                DBContract.SyncGroupEntry.COLUMN_IS_CUSTOM_NAME + " TEXT DEFAULT 'N', " +
                DBContract.SyncGroupEntry.COLUMN_MEMBER_DELETED + " TEXT DEFAULT 'N', " +
                DBContract.SyncGroupEntry.COLUMN_UPDATE_TIME + " LONG, " +
                DBContract.SyncGroupEntry.COLUMN_TYPE + " TEXT DEFAULT '', " +
                DBContract.SyncGroupEntry.COLUMN_TOP_TIME + " LONG, " +
                DBContract.SyncGroupEntry.COLUMN_DELETED + " TEXT DEFAULT 'N', " +
                DBContract.SyncGroupEntry.COLUMN_AVATAR_URL + " TEXT DEFAULT '', " +
                DBContract.SyncGroupEntry.COLUMN_IS_TOP + " TEXT DEFAULT 'N', " +
                DBContract.SyncGroupEntry.COLUMN_DFR_TIME + " LONG, " +
                DBContract.SyncGroupEntry.COLUMN_NAME + " TEXT, " +
                DBContract.SyncGroupEntry.COLUMN_MEMBER_IDS + " TEXT);"
        )
    }

    private fun createLastMessageTable(db: SQLiteDatabase) {
        db.execSQL(
            " CREATE TABLE IF NOT EXISTS " + DBContract.LastMessageEntry.TABLE_NAME + "(" +
                DBContract.LastMessageEntry.COLUMN_ROOM_ID + " TEXT DEFAULT '' PRIMARY KEY, " +
                DBContract.LastMessageEntry.COLUMN_ID + " TEXT DEFAULT '', " +
                DBContract.LastMessageEntry.COLUMN_DEVICE_TYPE + " TEXT DEFAULT '', " +
                DBContract.LastMessageEntry.COLUMN_FLAG + " INTEGER DEFAULT 0, " +
                DBContract.LastMessageEntry.COLUMN_RECEIVE_NUM + " INTEGER Default 0, " +
                DBContract.LastMessageEntry.COLUMN_CHAT_ID + " TEXT DEFAULT '', " +
                DBContract.LastMessageEntry.COLUMN_MSG_SRC + " TEXT DEFAULT '', " +
                DBContract.LastMessageEntry.COLUMN_TYPE + " TEXT DEFAULT '', " +
                DBContract.LastMessageEntry.COLUMN_CONTENT + " TEXT DEFAULT '', " +
                DBContract.LastMessageEntry.COLUMN_SEND_TIME + " LONG DEFAULT 0, " +
                DBContract.LastMessageEntry.COLUMN_SEQUENCE + " INTEGER DEFAULT 0, " +
                DBContract.LastMessageEntry.COLUMN_SENDER_ID + " TEXT DEFAULT '', " +
                DBContract.LastMessageEntry.COLUMN_SENDER_NAME + " TEXT DEFAULT '', " +
                DBContract.LastMessageEntry.COLUMN_SOURCE_TYPE + " TEXT DEFAULT '', " +
                DBContract.LastMessageEntry.COLUMN_OS_TYPE + " TEXT DEFAULT '', " +
                DBContract.LastMessageEntry.COLUMN_FROM + " TEXT DEFAULT '', " +
                DBContract.LastMessageEntry.COLUMN_TAG + " TEXT DEFAULT '', " +
                DBContract.LastMessageEntry.COLUMN_SEND_NUM + " INTEGER DEFAULT 0, " +
                DBContract.LastMessageEntry.COLUMN_READED_NUM + " INTEGER DEFAULT 0);"
        )
    }

    private fun createChatMemberTable(db: SQLiteDatabase) {
        db.execSQL(
            " CREATE TABLE IF NOT EXISTS " + DBContract.ChatMemberEntry.TABLE_NAME + "(" +
                DBContract.ChatMemberEntry.COLUMN_ROOM_ID + " TEXT DEFAULT '', " +
                DBContract.ChatMemberEntry.COLUMN_FIRST_SEQUENCE + " INTEGER DEFAULT 0, " +
                DBContract.ChatMemberEntry.COLUMN_DELETED + " TEXT DEFAULT 'N', " +
                DBContract.ChatMemberEntry.COLUMN_SOURCE_TYPE + " TEXT Default '', " +
                DBContract.ChatMemberEntry.COLUMN_LAST_READ_SEQUENCE + " INTEGER DEFAULT 0, " +
                DBContract.ChatMemberEntry.COLUMN_JOIN_TIME + " LONG DEFAULT 0, " +
                DBContract.ChatMemberEntry.COLUMN_LAST_RECEIVED_SEQUENCE + " INTEGER DEFAULT 0, " +
                DBContract.ChatMemberEntry.COLUMN_UPDATE_TIME + " LONG DEFAULT 0, " +
                DBContract.ChatMemberEntry.COLUMN_TYPE + " TEXT DEFAULT '', " +
                DBContract.ChatMemberEntry.COLUMN_MEMBER_ID + " TEXT DEFAULT '');"
        )
    }

    private fun createChatRoomMemberIdsTable(db: SQLiteDatabase) {
        db.execSQL(
            " CREATE TABLE IF NOT EXISTS " + DBContract.ChatRoomMemberIdsEntry.TABLE_NAME + "(" +
                DBContract.ChatMemberEntry.COLUMN_ROOM_ID + " TEXT DEFAULT '', " +
                DBContract.ChatMemberEntry.COLUMN_MEMBER_ID + " TEXT DEFAULT '');"
        )
    }
}
