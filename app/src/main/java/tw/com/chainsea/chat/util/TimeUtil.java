//package tw.com.chainsea.chat.util;
//
//import org.apache.commons.net.ntp.NTPUDPClient;
//import org.apache.commons.net.ntp.TimeInfo;
//
//import java.math.BigDecimal;
//import java.net.InetAddress;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.GregorianCalendar;
//import java.util.Locale;
//import java.util.TimeZone;
//
//import tw.com.chainsea.android.common.log.CELog;
//
//public class TimeUtil {
//
//    private static final String TIME_SERVER = "time-a.nist.gov";
//
//    public static boolean isEarly(int days, long time) {
//        return (currentTimeMillis() - time) > (days * 24 * 3600 * 1000);
//    }
//
//    public static int currentTimeSecond() {
//        return (int) (System.currentTimeMillis() / 1000);
//    }
//
//    public static long currentTimeMillis() {
//        return System.currentTimeMillis();
//    }
//
//    public static long[] getTsTimes() {
//        long[] times = new long[2];
//
//        Calendar calendar = Calendar.getInstance();
//
//        times[0] = calendar.getTimeInMillis() / 1000;
//
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//
//        times[1] = calendar.getTimeInMillis() / 1000;
//
//        return times;
//    }
//
//    public static String getFormatDatetime(int year, int month, int day) {
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//        return formatter.format(new GregorianCalendar(year, month, day).getTime());
//    }
//
//    public static Date getDateFromFormatString(String formatDate) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        try {
//            return sdf.parse(formatDate);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//    public static String getNowDatetime() {
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//        return (formatter.format(new Date()));
//    }
//
//    public static int getNow() {
//        return (int) ((new Date()).getTime() / 1000);
//    }
//
//    public static String getNowDateTime(String format) {
//        Date date = new Date();
//
//        SimpleDateFormat df = new SimpleDateFormat(format, Locale.getDefault());
//        return df.format(date);
//    }
//
//    public static String getDateString(long milliseconds) {
//        return getDateTimeString(milliseconds, "yyyyMMdd");
//    }
//
//    public static String getHHmm(long millseconds) {
//        return getDateTimeString(millseconds, "HH:mm");
//    }
//
//    public static String getBeijingNowTimeString(String format) {
//        TimeZone timezone = TimeZone.getTimeZone("Asia/Shanghai");
//
//        Date date = new Date(currentTimeMillis());
//        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
//        formatter.setTimeZone(timezone);
//
//        GregorianCalendar gregorianCalendar = new GregorianCalendar();
//        gregorianCalendar.setTimeZone(timezone);
//        String prefix = gregorianCalendar.get(Calendar.AM_PM) == Calendar.AM ? "上午" : "下午";
//
//        return prefix + formatter.format(date);
//    }
//
//    public static String getBeijingNowTime(String format) {
//        TimeZone timezone = TimeZone.getTimeZone("Asia/Shanghai");
//
//        Date date = new Date(currentTimeMillis());
//        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
//        formatter.setTimeZone(timezone);
//
//        return formatter.format(date);
//    }
//
//    public static String getDateTimeString(long milliseconds, String format) {
//        Date date = new Date(milliseconds);
//        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
//        return formatter.format(date);
//    }
//
//
//    public static String getFavoriteCollectTime(long milliseconds) {
//        String showDataString = "";
//        Date today = new Date();
//        Date date = new Date(milliseconds);
//        Date firstDateThisYear = new Date(today.getYear(), 0, 0);
//        if (!date.before(firstDateThisYear)) {
//            SimpleDateFormat dateformatter = new SimpleDateFormat("MM-dd", Locale.getDefault());
//            showDataString = dateformatter.format(date);
//        } else {
//            SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//            showDataString = dateformatter.format(date);
//        }
//        return showDataString;
//    }
//
//
//    public static String getDateShowString(long milliseconds, boolean abbreviate) {
//        String dataString = "";
//        String timeStringBy24 = "";
//
//        Date currentTime = new Date(milliseconds);
//        Date today = new Date();
//        Calendar todayStart = Calendar.getInstance();
//        todayStart.set(Calendar.HOUR_OF_DAY, 0);
//        todayStart.set(Calendar.MINUTE, 0);
//        todayStart.set(Calendar.SECOND, 0);
//        todayStart.set(Calendar.MILLISECOND, 0);
//        Date todaybegin = todayStart.getTime();
//        Date yesterdaybegin = new Date(todaybegin.getTime() - 3600 * 24 * 1000);
//        Date preyesterday = new Date(yesterdaybegin.getTime() - 3600 * 24 * 1000);
//
//        if (!currentTime.before(todaybegin)) {
//            dataString = "今天";
//        } else if (!currentTime.before(yesterdaybegin)) {
//            dataString = "昨天";
//        }
////        else if (!currentTime.before(preyesterday)) {
////            dataString = "前天";
////        }
//        else if (getYear(today, 0) == getYear(currentTime, 0)) {
//            dataString = getDayOfWeek(currentTime, 0);
//        } else {
//            SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//            dataString = dateformatter.format(currentTime);
//        }
//        return dataString;
//    }
//
//    private static int getYear(Date date, int plusYear) {
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(date);
//        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + plusYear);
//        int year = cal.get(Calendar.YEAR);
//        return year;
//    }
//
//
//    private static String getDayOfWeek(Date date, int plusDate) {
//        String[] CHT_NUM = new String[]{"日", "一", "二", "三", "四", "五", "六"};
//        SimpleDateFormat sdf = new SimpleDateFormat("M/d");
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(date);
//        cal.set(Calendar.DATE, cal.get(Calendar.DATE) + plusDate);
//        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
//        return String.format("%s (%s)", sdf.format(cal.getTime()), CHT_NUM[dayOfWeek]);
//    }
//
//    public static String getTimeShowString(long milliseconds, boolean abbreviate) {
//        String dataString = "";
//        String timeStringBy24 = "";
//
//        Date currentTime = new Date(milliseconds);
//        Calendar currentCal = new GregorianCalendar();
//        currentCal.setTime(currentTime);
//
//        Calendar todayStart = Calendar.getInstance();
//        todayStart.set(Calendar.HOUR_OF_DAY, 0);
//        todayStart.set(Calendar.MINUTE, 0);
//        todayStart.set(Calendar.SECOND, 0);
//        todayStart.set(Calendar.MILLISECOND, 0);
//
//        int year = todayStart.get(Calendar.YEAR);
//
//        Date todaybegin = todayStart.getTime();
//        Date yesterdaybegin = new Date(todaybegin.getTime() - 3600 * 24 * 1000);
//        Date preyesterday = new Date(yesterdaybegin.getTime() - 3600 * 24 * 1000);
//
//        if (!currentTime.before(todaybegin)) {
//            dataString = "今天";
//        } else if (!currentTime.before(yesterdaybegin)) {
//            dataString = "昨天";
//        } else if (!currentTime.before(preyesterday)) {
//            dataString = "前天";
//        }
//        else {
//            if (currentCal.get(Calendar.YEAR) == year) {
//                dataString = new SimpleDateFormat("MM-dd", Locale.getDefault()).format(currentTime);
//            }else {
//                dataString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentTime);
//            }
//        }
//
//        SimpleDateFormat timeformatter24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
//        timeStringBy24 = timeformatter24.format(currentTime);
//
//        if (abbreviate) {
//            if (!currentTime.before(todaybegin)) {
//                return timeStringBy24;
//            } else {
//                return dataString;
//            }
//        } else {
//            return dataString + " " + timeStringBy24;
//        }
//    }
//
//    /**
//     * 根据不同时间段，顯示不同时间
//     *
//     * @param date
//     * @return
//     */
//    public static String getTodayTimeBucket(Date date) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        SimpleDateFormat timeformatter0to11 = new SimpleDateFormat("KK:mm", Locale.getDefault());
//        SimpleDateFormat timeformatter1to12 = new SimpleDateFormat("HH:mm", Locale.getDefault());
//        int hour = calendar.get(Calendar.HOUR_OF_DAY);
//        if (hour >= 0 && hour < 5) {
//            return "" + timeformatter0to11.format(date);
////            return "凌晨 " + timeformatter0to11.format(name);
//        } else if (hour >= 5 && hour < 12) {
//            return "" + timeformatter0to11.format(date);
////            return "上午 " + timeformatter0to11.format(name);
//        } else if (hour >= 12 && hour < 18) {
//            return "" + timeformatter1to12.format(date);
////            return "下午 " + timeformatter1to12.format(name);
//        } else if (hour >= 18 && hour < 24) {
//            return "" + timeformatter1to12.format(date);
////            return "晚上 " + timeformatter1to12.format(name);
//        }
//        return "";
//    }
//
//    /**
//     * 根据日期获得星期
//     *
//     * @param date
//     * @return
//     */
//    public static String getWeekOfDate(Date date) {
//        String[] weekDaysName = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
//        // String[] weekDaysCode = { "0", "1", "2", "3", "4", "5", "6" };
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        int intWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
//        return weekDaysName[intWeek];
//    }
//
//    public static boolean isSameDay(long time1, long time2) {
//        return isSameDay(new Date(time1), new Date(time2));
//    }
//
//    public static boolean isSameDay(Date date1, Date date2) {
//        Calendar cal1 = Calendar.getInstance();
//        Calendar cal2 = Calendar.getInstance();
//        cal1.setTime(date1);
//        cal2.setTime(date2);
//
//        boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
//                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
//        return sameDay;
//    }
//
//    /**
//     * 判断两个日期是否在同一周
//     *
//     * @param date1
//     * @param date2
//     * @return
//     */
//    public static boolean isSameWeekDates(Date date1, Date date2) {
//        Calendar cal1 = Calendar.getInstance();
//        Calendar cal2 = Calendar.getInstance();
//        cal1.setTime(date1);
//        cal2.setTime(date2);
//        int subYear = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
//        if (0 == subYear) {
//            if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)) {
//                return true;
//            }
//        } else if (1 == subYear && 11 == cal2.get(Calendar.MONTH)) {
//            // 如果12月的最后一周横跨来年第一周的話则最后一周即算做来年的第一周
//            if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)) {
//                return true;
//            }
//        } else if (-1 == subYear && 11 == cal1.get(Calendar.MONTH)) {
//            if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static long getSecondsByMilliseconds(long milliseconds) {
//        long seconds = new BigDecimal((float) ((float) milliseconds / (float) 1000)).setScale(0,
//                BigDecimal.ROUND_HALF_UP).intValue();
//        // if (seconds == 0) {
//        // seconds = 1;
//        // }
//        return seconds;
//    }
//
//    public static String secToTime(int time) {
//        String timeStr = null;
//        int hour = 0;
//        int minute = 0;
//        int second = 0;
//        if (time <= 0) {
//            return "00:00";
//        } else {
//            minute = time / 60;
//            if (minute < 60) {
//                second = time % 60;
//                timeStr = unitFormat(minute) + ":" + unitFormat(second);
//            } else {
//                hour = minute / 60;
//                if (hour > 99) {
//                    return "99:59:59";
//                }
//                minute = minute % 60;
//                second = time - hour * 3600 - minute * 60;
//                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
//            }
//        }
//        return timeStr;
//    }
//
//    public static String unitFormat(int i) {
//        String retStr = null;
//        if (i >= 0 && i < 10) {
//            retStr = "0" + Integer.toString(i);
//        } else {
//            retStr = "" + i;
//        }
//        return retStr;
//    }
//
//    public static String getElapseTimeForShow(int milliseconds) {
//        StringBuilder sb = new StringBuilder();
//        int seconds = milliseconds / 1000;
//        if (seconds < 1) {
//            seconds = 1;
//        }
//        int hour = seconds / (60 * 60);
//        if (hour != 0) {
//            sb.append(hour).append("小时");
//        }
//        int minute = (seconds - 60 * 60 * hour) / 60;
//        if (minute != 0) {
//            sb.append(minute).append("分");
//        }
//        int second = (seconds - 60 * 60 * hour - 60 * minute);
//        if (second != 0) {
//            sb.append(second).append("秒");
//        }
//        return sb.toString();
//    }
//
//    /**
//     * 调此方法输入所要转换的时间输入例如（"2014-06-14"）返回时间戳
//     *
//     * @param time
//     * @return
//     */
//    public static long getDayBegin(long time) {
//        SimpleDateFormat sdr = new SimpleDateFormat("yyyy-MM-dd");
//        String times = sdr.format(new Date(time));
//
//        String[] s = times.split("-");
//        Calendar cal = Calendar.getInstance();
//        cal.set(Integer.parseInt(s[0]), Integer.parseInt(s[1]) - 1, Integer.parseInt(s[2]));
//        cal.set(Calendar.HOUR_OF_DAY, 0);
//        cal.set(Calendar.SECOND, 0);
//        cal.set(Calendar.MINUTE, 0);
//        cal.set(Calendar.MILLISECOND, 0);
//        long timeInMillis = cal.getTimeInMillis();
//        return timeInMillis;
//
//    }
//
//    public static void getNetworkTime(OnNetworkTimeListener onNetworkTimeListener) {
//        new Thread(() -> {
//            try {
//                NTPUDPClient client = new NTPUDPClient();
//                InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
//                TimeInfo timeInfo = client.getTime(inetAddress);
//                onNetworkTimeListener.onNetworkTime(timeInfo.getMessage().getTransmitTimeStamp().getTime());
//            } catch (Exception e) {
//                CELog.e("getNetworkTime Error", e);
//            }
//            onNetworkTimeListener.onNetworkTime(System.currentTimeMillis());
//        }).start();
//    }
//
//    public interface OnNetworkTimeListener {
//        void onNetworkTime(long time);
//    }
//}
