
package com.yhfund.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * @author wanwenbo
 * @desc
 * @date 2018/3/24
 */
public class DateUtil {
    private DateUtil() {
        throw new UnsupportedOperationException(" 工具类不能实例化！");
    }

    private static DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;

    /**
     * 获取当前8位日期字符串
     *
     * @return
     */
    public static String getNatureDate() {
        return formatter.format(LocalDate.now());
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static String getNowTime() {
        return DateTimeFormatter.ISO_LOCAL_TIME.format(LocalTime.now().withNano(0));
    }

    /**
     * @param
     * @return 202324
     * @desc 去掉冒号的时间，八位(获取结果为六位)
     */
    public static String getNowTimeNoLoNon() {
        return getNowTime().replaceAll(":", "");
    }

    /**
     * @param
     * @return
     * @desc 当前日期加指定天数得到的8位日期字符串
     */
    public static String getPlusDays(long days) {
        return DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.now().plusDays(days));
    }

    /**
     * @param months 月份数
     * @return
     * @desc 当前日期加指定月份得到的8位日期字符串
     */
    public static String getPlusMonth(long months) {
        return DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.now().plusMonths(months));
    }

    /**
     * @param weeks 周份数
     * @return
     * @desc 当前日期加指定周份得到的8位日期字符串
     */
    public static String getPlusWeek(long weeks) {
        return DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.now().plusWeeks(weeks));
    }

    /**
     * 获取最近一个工作日期和下一个工作日
     *
     * @return
     */

    /**
     * @param beginday 开始日期
     * @param endday   结束日期
     * @return 返回两个日期之间相差的天数
     */
    public static long dateDiff(String beginday, String endday) {
        LocalDate startDate = LocalDate.parse(beginday, formatter);
        LocalDate endDate = LocalDate.parse(endday, formatter);
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * @param date 指定日期
     * @param days 指定天数
     * @return 返回指定日期加上指定天数后的日期
     */
    public static String getPlusByDate(String date, long days) {
        String str = DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.parse(date, formatter).plusDays(days));
        return str;
    }

    /**
     * @param date
     * @param months
     * @return 返回指定日期加指定月份得到的8位日期字符串
     */
    public static String getPlusMonthByDate(String date, long months) {
        return DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.parse(date, formatter).plusMonths(months));
    }

    /**
     * @param date
     * @param weeks
     * @return 返回指定日期加指定周份得到的8位日期字符串
     */
    public static String getPlusWeekByDate(String date, long weeks) {
        return DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.parse(date, formatter).plusWeeks(weeks));
    }

    /**
     * 获取当前日期这个星期的星期几
     *
     * @param date
     * @param planperiodsvalue
     * @return
     */
    public static String getDayOfWeek(String date, int planperiodsvalue) {
        // 当前日期是周几
        int dayofweek = getDayOfWeekValue(date);
        if (dayofweek == 0) {
            dayofweek = 7;
        }
        // 获取相差天数
        int offset = planperiodsvalue - dayofweek;
        // 获取周几
        return getPlusByDate(date, offset);
    }

    /**
     * 获取当前日期是周几
     *
     * @param day
     * @return
     */
    public static int getDayOfWeekValue(String day) {
        LocalDate date = LocalDate.parse(day, DateTimeFormatter.BASIC_ISO_DATE);
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek.getValue();
    }


    /**
     * 将Long类型的时间戳转换成String 类型的时间格式，时间格式为：yyyy-MM-dd HH:mm:ss
     */
    public static String convertTimeToString(Long time) {
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
        return ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
    }

    /**
     * 2018115090909 to 09:09:09
     *
     * @param dateTime
     * @return
     */
    public static String getTimeByString(String dateTime) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime parse = LocalDateTime.parse(dateTime, dtf);
        return DateTimeFormatter.ISO_LOCAL_TIME.format(parse);
    }

    /**
     * 2018115090909 to 20181115
     *
     * @param dateTime
     * @return
     */
    public static String getDateByString(String dateTime) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime parse = LocalDateTime.parse(dateTime, dtf);
        return DateTimeFormatter.BASIC_ISO_DATE.format(parse);
    }


    /**
     * 日期比较-大于
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean moreThan(String date1, String date2) {
        return date1.compareTo(date2) > 0;
    }

    /**
     * 日期比较-小于
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean lessThan(String date1, String date2) {
        return date1.compareTo(date2) < 0;
    }

    /**
     * 日期比较-小于等于
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean lessOrEqual(String date1, String date2) {
        return date1.compareTo(date2) <= 0;
    }
}
