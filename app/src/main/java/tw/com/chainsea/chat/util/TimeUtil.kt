package tw.com.chainsea.chat.util

import android.annotation.SuppressLint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.net.ntp.NTPUDPClient
import org.apache.commons.net.ntp.TimeInfo
import tw.com.chainsea.android.common.log.CELog
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale

object TimeUtil {
    private val TIME_SERVER: String = "time-a.nist.gov"

    fun getHHmm(milliseconds: Long): String = getDateTimeString(milliseconds, "HH:mm")

    fun getDateTimeString(
        milliseconds: Long,
        format: String
    ): String {
        val date = Date(milliseconds)
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(date)
    }

    fun getDateShowString(
        milliseconds: Long,
        abbreviate: Boolean
    ): String {
        var dataString = ""
        val timeStringBy24 = ""

        val currentTime = Date(milliseconds)
        val today = Date()
        val todayStart = Calendar.getInstance()
        todayStart[Calendar.HOUR_OF_DAY] = 0
        todayStart[Calendar.MINUTE] = 0
        todayStart[Calendar.SECOND] = 0
        todayStart[Calendar.MILLISECOND] = 0
        val todaybegin = todayStart.time
        val yesterdaybegin = Date(todaybegin.time - 3600 * 24 * 1000)
        val preyesterday = Date(yesterdaybegin.time - 3600 * 24 * 1000)

        if (!currentTime.before(todaybegin)) {
            dataString = "今天"
        } else if (!currentTime.before(yesterdaybegin)) {
            dataString = "昨天"
        } else if (getYear(today, 0) == getYear(currentTime, 0)) {
            dataString = getDayOfWeek(currentTime, 0)
        } else {
            val dateformatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dataString = dateformatter.format(currentTime)
        }
        return dataString
    }

    fun getYear(
        date: Date,
        plusYear: Int
    ): Int {
        val cal = Calendar.getInstance()
        cal.time = date
        cal[Calendar.YEAR] = cal[Calendar.YEAR] + plusYear
        val year = cal[Calendar.YEAR]
        return year
    }

    @SuppressLint("SimpleDateFormat")
    fun getDayOfWeek(
        date: Date,
        plusDate: Int
    ): String {
        val chtNum = arrayOf("日", "一", "二", "三", "四", "五", "六")
        val sdf = SimpleDateFormat("M/d")
        val cal = Calendar.getInstance()
        cal.time = date
        cal[Calendar.DATE] = cal[Calendar.DATE] + plusDate
        val dayOfWeek = cal[Calendar.DAY_OF_WEEK] - 1
        return String.format("%s (%s)", sdf.format(cal.time), chtNum[dayOfWeek])
    }

    fun getTimeShowString(
        milliseconds: Long,
        abbreviate: Boolean
    ): String {
        var dataString = ""
        var timeStringBy24 = ""

        val currentTime = Date(milliseconds)
        val currentCal: Calendar = GregorianCalendar()
        currentCal.time = currentTime

        val todayStart = Calendar.getInstance()
        todayStart[Calendar.HOUR_OF_DAY] = 0
        todayStart[Calendar.MINUTE] = 0
        todayStart[Calendar.SECOND] = 0
        todayStart[Calendar.MILLISECOND] = 0

        val year = todayStart[Calendar.YEAR]

        val todaybegin = todayStart.time
        val yesterdaybegin = Date(todaybegin.time - 3600 * 24 * 1000)
        val preyesterday = Date(yesterdaybegin.time - 3600 * 24 * 1000)

        dataString =
            if (!currentTime.before(todaybegin)) {
                "今天"
            } else if (!currentTime.before(yesterdaybegin)) {
                "昨天"
            } else if (!currentTime.before(preyesterday)) {
                "前天"
            } else {
                if (currentCal[Calendar.YEAR] == year) {
                    SimpleDateFormat("MM-dd", Locale.getDefault()).format(currentTime)
                } else {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentTime)
                }
            }

        val timeformatter24 = SimpleDateFormat("HH:mm", Locale.getDefault())
        timeStringBy24 = timeformatter24.format(currentTime)

        return if (abbreviate) {
            if (!currentTime.before(todaybegin)) {
                timeStringBy24
            } else {
                dataString
            }
        } else {
            "$dataString $timeStringBy24"
        }
    }

    fun isSameDay(
        date1: Date,
        date2: Date
    ): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = date1
        cal2.time = date2

        val sameDay =
            cal1[Calendar.YEAR] == cal2[Calendar.YEAR] &&
                cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR]
        return sameDay
    }

    fun unitFormat(i: Int): String {
        var retStr: String? = null
        retStr =
            if (i in 0..9) {
                "0$i"
            } else {
                "" + i
            }
        return retStr
    }

    /**
     * 调此方法输入所要转换的时间输入例如（"2014-06-14"）返回时间戳
     *
     * @param time
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    fun getDayBegin(time: Long): Long {
        val sdr = SimpleDateFormat("yyyy-MM-dd")
        val times = sdr.format(Date(time))

        val s = times.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val cal = Calendar.getInstance()
        cal[s[0].toInt(), s[1].toInt() - 1] = s[2].toInt()
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.MILLISECOND] = 0
        val timeInMillis = cal.timeInMillis
        return timeInMillis
    }

    fun getNetworkTime(onNetworkTimeListener: OnNetworkTimeListener) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = NTPUDPClient()
                val inetAddress =
                    InetAddress.getByName(TIME_SERVER)
                val timeInfo: TimeInfo = client.getTime(inetAddress)
                withContext(Dispatchers.Main) {
                    onNetworkTimeListener.onNetworkTime(timeInfo.message.transmitTimeStamp.time)
                }
            } catch (e: Exception) {
                CELog.e("getNetworkTime Error", e)
                withContext(Dispatchers.Main) {
                    onNetworkTimeListener.onNetworkTime(System.currentTimeMillis())
                }
            }
        }

    interface OnNetworkTimeListener {
        fun onNetworkTime(time: Long)
    }
}
