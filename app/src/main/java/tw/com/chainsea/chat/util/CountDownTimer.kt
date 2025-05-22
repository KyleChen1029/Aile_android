package tw.com.chainsea.chat.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.Unit

class CountDownTimer(
    private val viewModelScope: CoroutineScope,
    private val onTick: (Long) -> Unit,
    private val onFinish: () -> Unit
) {
    private var job: Job? = null
    private var remainingTime: Long = 0

    fun start(totalTimeMillis: Long) {
        cancel()
        remainingTime = totalTimeMillis

        job =
            viewModelScope.launch {
                while (remainingTime > 0) {
                    onTick(remainingTime)
                    delay(1000) // 每秒更新一次
                    remainingTime -= 1000
                }
                remainingTime = 0
                onFinish()
            }
    }

    fun cancel() {
        job?.cancel()
        job = null
    }

    fun isRunning() = job?.isActive == true
}
