package com.beyond.utils;

import com.beyond.f.F;
import com.time.nlp.TimeNormalizer;
import com.time.nlp.TimeUnit;
import org.apache.commons.lang3.time.DateUtils;

import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {

    private static TimeNormalizer normalizer;

    public static Date parse(String chineseWithTimeMean) {
        Date date = null;
        try {
            TimeUnit[] timeUnits = getNormalizer().parse(chineseWithTimeMean);
            date = timeUnits[0].getTime();
        } catch (Exception e) {
            F.logger.info(e.getMessage());
        }
        return date;
    }

    public static String getTimeExpression(String chineseWithTimeMean) {
        String timeExpression = null;
        try {
            TimeUnit[] timeUnits = getNormalizer().parse(chineseWithTimeMean);
            timeExpression = timeUnits[0].Time_Expression;
        } catch (Exception e) {
            F.logger.info(e.getMessage());
        }
        return timeExpression;
    }

    public static String getTimeNorm(String chineseWithTimeMean) {
        String timeNorm = null;
        try {
            TimeUnit[] timeUnits = getNormalizer().parse(chineseWithTimeMean);
            timeNorm = timeUnits[0].Time_Norm;
        } catch (Exception e) {
            F.logger.info(e.getMessage());
        }
        return timeNorm;
    }

    public static String getDateStringForMicrosoftEvent(Date date, String timeZoneId) {
        if (date == null) return null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
        return simpleDateFormat.format(date);
    }

    private static TimeNormalizer getNormalizer() {
        if (normalizer == null) {
            normalizer = initNormalizer();
        }
        return normalizer;
    }

    private static TimeNormalizer initNormalizer() {
        URL url = TimeNormalizer.class.getResource("/TimeExp.m");
        TimeNormalizer normalizer = null;
        try {
            normalizer = new TimeNormalizer(url.toURI().toString());
            normalizer.setPreferFuture(true);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return normalizer;
    }

    //将目标字符串中识别出来的时间转化为以当前时间为基准的称呼，比如昨天记录的“明天上午九点”在今天则显示“今天上午九点”
    public static String getContentBaseOnNow(String target, Date remindTime){
        try {
            Calendar calendar = Calendar.getInstance();
            Date curr = calendar.getTime();
            String timeExpression = getTimeExpression(target);
            String chineseTimeExpressions = getChineseTimeExpressions(timeExpression);
            String result = chineseTimeExpressions;

            //转化明天，后天，大后天字样
            int datePeriod = DateUtils.toCalendar(remindTime).get(Calendar.DATE) - DateUtils.toCalendar(curr).get(Calendar.DATE);
            if (datePeriod == 0) {
                result = chineseTimeExpressions.replace("明天", "今天");
            }
            if (datePeriod == 1) {
                result = chineseTimeExpressions.replace("后天", "明天");
            }
            if (datePeriod == 2) {
                result = chineseTimeExpressions.replace("大后天", "后天");
            }

            //转化 ×天后 字样
            Pattern pattern = Pattern.compile("(\\d+)天后");
            Matcher matcher = pattern.matcher(timeExpression);
            if (matcher.find()) {
                String day = matcher.group(1);
                String remindDayWord = numToWord(day);
                remindDayWord = "二".equals(remindDayWord) ? "两" : remindDayWord;
                String realDayWord = numToWord(datePeriod + "");
                realDayWord = "二".equals(realDayWord) ? "两" : realDayWord;
                if (datePeriod == 0) {
                    result = chineseTimeExpressions.replace(remindDayWord + "天后","今天");
                } else if (datePeriod == 1) {
                    result = chineseTimeExpressions.replace(remindDayWord + "天后", "明天");
                } else if (datePeriod == 2) {
                    result = chineseTimeExpressions.replace(remindDayWord + "天后", "后天");
                } else {
                    result = chineseTimeExpressions.replace(remindDayWord + "天后", realDayWord + "天后");
                }
            }

            return target.replace(chineseTimeExpressions, result);
        } catch (Exception e) {
            F.logger.info(e.getMessage());
        }
        return target;
    }

    public static String getDateTrimContent(String target,Date remindTime){
        try {
            String timeExpression = getTimeExpression(target);
            String chineseTimeExpressions = getChineseTimeExpressions(timeExpression);
            return target.replace(chineseTimeExpressions, "");
        } catch (Exception e) {
            F.logger.info(e.getMessage());
        }
        return target;
    }
    private static String getChineseTimeExpressions(String timeExpression) {
        Pattern pattern = Pattern.compile("\\d");
        Matcher matcher = pattern.matcher(timeExpression);
        int lastEndIndex = -1;
        while (matcher.find()) {
            String group = matcher.group();
            if (matcher.end() - lastEndIndex == 1) {
                timeExpression = timeExpression.replace(group, "十" + numToWord(group));
            } else {
                timeExpression = timeExpression.replace(group, numToWord(group));
            }
            lastEndIndex = matcher.end();
        }
        timeExpression = timeExpression.replace("十零", "十");
        timeExpression = timeExpression.replace("零十", "零");
        timeExpression = timeExpression.replace("一十", "十");
        timeExpression = timeExpression.replace("二点", "两点");
        timeExpression = timeExpression.replace("十两点", "十二点");
        timeExpression = timeExpression.replace("二天", "两天");
        timeExpression = timeExpression.replace("十两天", "十二天");
        return timeExpression;
    }
    private static String numToWord(String s) {
        if (s.contains("0"))
            return "零";
        else if (s.contains("1"))
            return "一";
        else if (s.contains("2"))
            return "二";
        else if (s.contains("3"))
            return "三";
        else if (s.contains("4"))
            return "四";
        else if (s.contains("5"))
            return "五";
        else if (s.contains("6"))
            return "六";
        else if (s.contains("7"))
            return "七";
        else if (s.contains("8"))
            return "八";
        else if (s.contains("9"))
            return "九";
        else return s;
    }


    public static void main(String[] args) {
        String target = "两天后下午三点四十sdofakdjfo,后天";
    }
}
