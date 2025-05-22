package tw.com.chainsea.chat.network.todo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tw.com.chainsea.ce.sdk.bean.label.Label
import tw.com.chainsea.ce.sdk.database.DBContract
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.common.BaseRequest
import tw.com.chainsea.ce.sdk.network.services.TodoService
import tw.com.chainsea.ce.sdk.reference.TodoReference

class TodoRepository(private val todoService: TodoService) {


    /**
     * 取得記事列表
     *
     * @param refreshTime 上一次更新時間
     * */
    fun syncTodo(refreshTime: Long) = flow {
        emit(ApiResult.Loading(true))
        val response = todoService.syncTodo(BaseRequest(refreshTime = refreshTime))
        response.body()?.let {
            DBManager.getInstance().updateOrInsertApiInfoField(DBContract.REFRESH_TIME_SOURCE.SYNC_TODO, it.refreshTime)
            it.items.forEach {todoEntity ->
                val status = TodoReference.save(todoEntity)
                if (status) {
                    emit(ApiResult.SaveStatus(true))
                }
            }
            // 取得下一頁資料
            it.hasNextPage?.let { has ->
                emit(ApiResult.NextPage<List<Label>>(has))
            }?: run {
                emit(ApiResult.NextPage<List<Label>>(false))
            }
            emit(ApiResult.Success(it))
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }
}