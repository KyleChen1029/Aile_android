package tw.com.chainsea.android.common.datetime;

import android.annotation.SuppressLint;

import com.google.common.base.Strings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * current by evan on 2020-03-26
 *
 * @author Evan Wang
 * @date 2020-03-26
 */
public class DateTimeHelper {

    /**
     * 預設格式化時間格式 'yyyy/MM/dd HH:mm:ss'，若每次格式化後會回復預設格式
     *
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    private static String PATTERN = "yyyy/MM/dd HH:mm:ss";
    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat SDF = new SimpleDateFormat(PATTERN);

    /**
     * 傳入地區取得目前時間 Calendar 物件
     *
     * @param locale
     * @return Calendar
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Calendar now(Locale locale) {
        if (locale != null) {
            return Calendar.getInstance(locale);
        } else {
            return Calendar.getInstance();
        }
    }

    /**
     * 傳入 Date 物件轉換成 Calendar 物件
     *
     * @param date
     * @return Calendar
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Calendar setDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static String format(long date, String pattern) {
        if (Strings.isNullOrEmpty(pattern)) {
            return "";
        } else {
            SDF.applyPattern(pattern);
            String sDate = SDF.format(date);
            SDF.applyPattern(PATTERN);
            return sDate;
        }
    }

    /**
     * 傳入 Date 物件及格式化時間格式，得到字串時間
     *
     * @param date
     * @param pattern
     * @return String
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static String format(Date date, String pattern) {
        if (Strings.isNullOrEmpty(pattern)) {
            return date.toString();
        } else {
            SDF.applyPattern(pattern);
            String sDate = SDF.format(date);
            SDF.applyPattern(PATTERN);
            return sDate;
        }
    }

    /**
     * 傳入 Calendar 物件及格式化時間格式，得到字串時間
     *
     * @param cal
     * @param pattern
     * @return String
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static String format(Calendar cal, String pattern) {
        return format(cal.getTime(), pattern);
    }

    /**
     * 傳入 字串時間 及格式化時間格式，轉換成 Date 物件
     *
     * @param source
     * @param parsePattern
     * @return Date
     * @throws ParseException
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Date parse(String source, String parsePattern) throws ParseException {
        SDF.applyPattern(parsePattern);
        Date date = SDF.parse(source);
        SDF.applyPattern(PATTERN);
        return date;
    }

    public static long parseToMillis(String source, String parsePattern) throws ParseException {
        SDF.applyPattern(parsePattern);
        Date date = SDF.parse(source);
        SDF.applyPattern(PATTERN);
        return date.getTime();
    }


    public static long tryParseToMillis(String source, String... parsePatterns) {
        Date date = null;
        for (String parsePattern : parsePatterns) {
            SDF.applyPattern(parsePattern);
            if (date == null) {
                try {
                    date = SDF.parse(source);
                } catch (Exception e) {

                }
            }
        }
        SDF.applyPattern(PATTERN);
        return date == null ? 0L : date.getTime();
    }


    /**
     * 傳入 字串時間 及格式化時間格式，轉換成 Calendar 物件
     *
     * @param source
     * @param parsePattern
     * @return Calendar
     * @throws ParseException
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Calendar parseToCal(String source, String parsePattern) throws ParseException {
        return setDate(parse(source, parsePattern));
    }

    /**
     * 傳入 字串時間 及格式化時間格式，得到 字串時間
     *
     * @param source
     * @param parsePattern
     * @param pattern
     * @return String
     * @throws ParseException
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static String parseToString(String source, String parsePattern, String pattern) throws ParseException {
        return format(parse(source, parsePattern), pattern);
    }

    /**
     * 傳入地區取得目前時間 Date 物件
     *
     * @param locale
     * @return Date
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Date getNowDate(Locale locale) {
        return now(locale).getTime();
    }

    /**
     * 傳入地區 及 格式化時間格式，取得目前字串時間
     *
     * @param locale
     * @param pattern
     * @return String
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static String getNowString(Locale locale, String pattern) {
        return format(now(locale), pattern);
    }

    /**
     * 傳入地區，取得目前年第幾天
     *
     * @param locale
     * @return int
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static int getDateOfYear(Locale locale) {
        return now(locale).get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 取得目前年第幾天
     *
     * @return int
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static int getDateOfYear() {
        return getDateOfYear(null);
    }

    /**
     * 傳入地區，取得目前月第幾天
     *
     * @param locale
     * @return int
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static int getDateOfMonth(Locale locale) {
        return now(locale).get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 取得目前月第幾天
     *
     * @return int
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static int getDateOfMonth() {
        return getDateOfMonth(null);
    }

    /**
     * 傳入地區，取得目前週第幾天
     *
     * @param locale
     * @return int
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static int getDateOfWeek(Locale locale) {
        return now(locale).get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 取得目前週第幾天
     *
     * @return int
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static int getDateOfWeek() {
        return getDateOfWeek(null);
    }

    /**
     * 傳入地區，取得目前月有幾天
     *
     * @param locale
     * @return int
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static int getNumberOfMonth(Locale locale) {
        return now(locale).getActualMaximum(Calendar.DATE);
    }

    /**
     * 取得目前月有幾天
     *
     * @return int
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static int getNumberOfMonth() {
        return getNumberOfMonth(null);
    }

    /**
     * 傳入 Calendar 物件，取得該月有幾天
     *
     * @param cal
     * @return int
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static int getNumberOfMonthCalendar(Calendar cal) {
        return cal.getActualMaximum(Calendar.DATE);
    }

    /**
     * 傳入 Calendar 物件，取得該天開始時間物件 Calendar
     *
     * @param cal
     * @return Calendar
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Calendar getStart(Calendar cal) {
        cal.set(11, 0);
        cal.set(13, 0);
        cal.set(12, 0);
        cal.set(14, 0);
        return cal;
    }

    public static Calendar getStartByPlusDay(Calendar cal, int plusDay) {
        return getByPlusDay(getStart(cal), plusDay);
    }

    /**
     * 傳入 Calendar 物件，取得該天結束時間物件 Calendar
     *
     * @param cal
     * @return Calendar
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Calendar getEnd(Calendar cal) {
        cal.set(11, 24);
        cal.set(13, 0);
        cal.set(12, 0);
        cal.set(14, 0);
        cal.set(Calendar.MILLISECOND, cal.get(Calendar.MILLISECOND) - 1);
        return cal;
    }

    /**
     * 傳入 Date 物件，取得該天開始時間物件 Calendar
     *
     * @param date
     * @return Calendar
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Calendar getStart(Date date) {
        return getStart(setDate(date));
    }

    /**
     * 傳入 Date 物件，取得該天結束時間物件 Calendar
     *
     * @param date
     * @return Calendar
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Calendar getEnd(Date date) {
        return getEnd(setDate(date));
    }

    /**
     * 傳入 Calendar 物件，取得該天開始時間物件 Date
     *
     * @param cal
     * @return Date
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Date getStartDate(Calendar cal) {
        return getStart(cal).getTime();
    }

    /**
     * 傳入 Calendar 物件，取得該天結束時間物件 Date
     *
     * @param cal
     * @return Date
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Date getEndDate(Calendar cal) {
        return getEnd(cal).getTime();
    }

    /**
     * 傳入 Date 物件，取得該天開始時間物件 Calendar
     *
     * @param date
     * @return Calendar
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Calendar getStartDate(Date date) {
        return getStart(date);
    }

    /**
     * 傳入 Date 物件，取得該天結束時間物件 Calendar
     *
     * @param date
     * @return Calendar
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Calendar getEndDate(Date date) {
        return getEnd(date);
    }

    /**
     * 傳入 Calendar 物件 及格式化時間格式，取得該天開始字串時間
     *
     * @param cal
     * @param pattern
     * @return String
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static String getStartString(Calendar cal, String pattern) {
        return format(getStart(cal), pattern);
    }

    /**
     * 傳入 Calendar 物件 及格式化時間格式，取得該天結束字串時間
     *
     * @param cal
     * @param pattern
     * @return String
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static String getEndString(Calendar cal, String pattern) {
        return format(getEnd(cal), pattern);
    }

    public static Calendar getEndCalendarByPlusDay(Locale locale, int plusDay) {
        Calendar calendar = getByPlusDay(getTodayEnd(locale), plusDay);
        return calendar;
    }

    public static long getEndMillisByPlusDay(Locale locale, int plusDay) {
        Calendar calendar = getEndCalendarByPlusDay(locale, plusDay);
        return calendar.getTimeInMillis();
    }

    /**
     * 傳入地區，取得今日開始時間物件 Calendar
     *
     * @param locale
     * @return Calendar
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Calendar getTodayStart(Locale locale) {
        return getStart(now(locale));
    }

    /**
     * 傳入地區，取得今日結束時間物件 Calendar
     *
     * @param locale
     * @return Calendar
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Calendar getTodayEnd(Locale locale) {
        return getEnd(now(locale));
    }

    /**
     * 傳入地區，取得今日開始時間物件 Date
     *
     * @param locale
     * @return Date
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Date getTodayStartDate(Locale locale) {
        return getTodayStart(locale).getTime();
    }

    /**
     * 傳入地區，取得今日結束時間物件 Date
     *
     * @param locale
     * @return Date
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Date getTodayEndDate(Locale locale) {
        return getTodayEnd(locale).getTime();
    }

    /**
     * 傳入地區 及格式化時間格式，取得今日開始字串時間
     *
     * @param locale
     * @param pattern
     * @return String
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static String getTodayStartString(Locale locale, String pattern) {
        return format(getTodayStart(locale), pattern);
    }

    /**
     * 傳入地區 及格式化時間格式，取得今日結束字串時間
     *
     * @param locale
     * @param pattern
     * @return String
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static String getTodayEndString(Locale locale, String pattern) {
        return format(getTodayEnd(locale), pattern);
    }

    /**
     * 傳入 Calendar 及 增加或減少天數， 取得處理後時間物件 Calendar
     *
     * @param cal
     * @param plusDay
     * @return Calendar
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Calendar getByPlusDay(Calendar cal, int plusDay) {
        Calendar calendar = (Calendar) cal.clone();
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + plusDay);
        return calendar;
    }

    /**
     * 傳入 Calendar 及 增加或減少月數， 取得處理後時間物件 Calendar
     *
     * @param cal
     * @param plusMonth
     * @return Calendar
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Calendar getByPlusMonth(Calendar cal, int plusMonth) {
        Calendar calendar = (Calendar) cal.clone();
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + plusMonth);
        return calendar;
    }

    /**
     * 傳入區域 及 增加或減少天數， 取得以目前時間處理後時間物件 Calendar
     *
     * @param plusDay
     * @param locale
     * @return Calendar
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Calendar getByPlusDay(int plusDay, Locale locale) {
        return getByPlusDay(now(locale), plusDay);
    }

    /**
     * 傳入區域 及 增加或減少天數， 取得以目前時間處理後時間物件 Date
     *
     * @param plusDay
     * @param locale
     * @return Date
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Date getDateByPlusDay(int plusDay, Locale locale) {
        return getByPlusDay(plusDay, locale).getTime();
    }

    /**
     * 傳入區域、增加或減少天數 及 格式化時間格式， 取得以目前時間處理後字串時間
     *
     * @param plusDay
     * @param locale
     * @param pattern
     * @return String
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static String getDateByPlusDay(int plusDay, Locale locale, String pattern) {
        return format(getByPlusDay(plusDay, locale), pattern);
    }

    /**
     * 傳入Date 物件 及 增加或減少天數， 取得處理後時間物件 Date
     *
     * @param date
     * @param plusDay
     * @return Date
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Date getDateByPlusDay(Date date, int plusDay) {
        return getByPlusDay(setDate(date), plusDay).getTime();
    }

    /**
     * 傳入Calendar 物件 及 增加或減少天數， 取得處理後時間物件 Date
     *
     * @param cal
     * @param plusDay
     * @return Date
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Date getDateByPlusDay(Calendar cal, int plusDay) {
        return getByPlusDay(cal, plusDay).getTime();
    }

    /**
     * 傳入Calendar 物件 及 範圍數量(可處理負值)，回傳已片段完成的集合物件 List
     *
     * @param rangeNum
     * @return List
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static List<Date> getDateRangeByPlusDay(Calendar cal, int rangeNum) {
        List<Date> dates = new ArrayList<Date>();
        for (Calendar c : getRangeByPlusDay(cal, rangeNum)) {
            dates.add(c.getTime());
        }
        return dates;
    }

    /**
     * 傳入Calendar 物件、範圍數量(可處理負值) 及 格式化時間格式，回傳已片段完成的集合物件 List
     *
     * @param cal
     * @param rangeNum
     * @param pattern
     * @return List
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static List<String> getDateStringRangeByPlusDay(Calendar cal, int rangeNum, String pattern) {
        List<String> dates = new ArrayList<String>();
        for (Calendar c : getRangeByPlusDay(cal, rangeNum)) {
            dates.add(format(c, pattern));
        }
        return dates;
    }


    /**
     * 傳入Date 物件 及 增加或減少月數， 取得處理後時間物件 Date
     *
     * @param date
     * @param plusMonthy
     * @return Date
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Date getDateByPlusMonthy(Date date, int plusMonthy) {
        return getByPlusMonth(setDate(date), plusMonthy).getTime();
    }

    /**
     * 傳入Calendar 物件 及 範圍數量(可處理負值)，回傳已片段完成的集合物件 List
     *
     * @param cal
     * @param rangeNum
     * @return List
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static List<Calendar> getRangeByPlusDay(Calendar cal, int rangeNum) {
        List<Calendar> cals = new ArrayList<Calendar>();
        if (rangeNum > 0) {
            for (int i = 0; i < rangeNum; i++) {
                cals.add(getByPlusDay(cal, i));
            }
        } else if (rangeNum < 0) {
            for (int i = rangeNum; i != 0; i++) {
                cals.add(getByPlusDay(cal, i));
            }
        } else {
            cals.add(cal);
        }
        return cals;
    }


    /**
     * Format duration
     */
    @SuppressLint("DefaultLocale")
    public static String strDuration(double duration) {
        int ms, s, m, h, d;
        double dec;
        double time = duration * 1.0f;

        time = (time / 1000.0);
        dec = time % 1;
        time = time - dec;
        ms = (int) (dec * 1000);

        time = (time / 60.0);
        dec = time % 1;
        time = time - dec;
        s = (int) (dec * 60);

        time = (time / 60.0);
        dec = time % 1;
        time = time - dec;
        m = (int) (dec * 60);

        time = (time / 24.0);
        dec = time % 1;
        time = time - dec;
        h = (int) (dec * 24);

        d = (int) time;
        if (d > 0) {
            return (String.format("%d d - %02d:%02d:%02d", d, h, m, s));
        } else if (h > 0) {
            return (String.format("%02d:%02d:%02d", h, m, s));
        } else {
            return (String.format("%02d:%02d", m, s));
        }
    }

    public static String convertSecondsToHMmSs(long millis) {
        long days = millis / (1000 * 60 * 60 * 24);
        long hours = millis / (1000 * 60 * 60) % 24;
        long minutes = (millis / (1000 * 60)) % 60;
        long seconds = (millis / 1000) % 60;
        StringBuilder b = new StringBuilder();
        b.append(days == 0 ? "" : days + "d - ");
        b.append(hours == 0 ? "00" : hours < 10 ? "0" + hours :
            String.valueOf(hours));
        b.append(":");
        b.append(minutes == 0 ? "00" : minutes < 10 ? "0" + minutes :
            String.valueOf(minutes));
        b.append(":");
        b.append(seconds == 0 ? "00" : seconds < 10 ? "0" + seconds :
            String.valueOf(seconds));
        return b.toString();
    }

    /**
     * 得到指定月的天數
     *
     * @param year
     * @param month
     * @return
     */
    public static int getMonthLastDay(int year, int month) {
        Calendar a = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"), Locale.TAIWAN);
        a.set(Calendar.YEAR, year);
        a.set(Calendar.MONTH, month);
        a.set(Calendar.DATE, 1);//把日期設定為當月第一天
        a.roll(Calendar.DATE, -1);//日期回滾一天，也就是最後一天
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

}
