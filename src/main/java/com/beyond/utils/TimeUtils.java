package com.beyond.utils;

import com.beyond.f.F;
import com.time.nlp.TimeNormalizer;
import com.time.nlp.TimeUnit;

import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtils {

    private static TimeNormalizer normalizer;

    public static Date parse(String chineseWithTimeMean){
        Date date = null;
        try {
            TimeUnit[] timeUnits = getNormalizer().parse(chineseWithTimeMean);
            date = timeUnits[0].getTime();
        }catch (Exception e){
            F.logger.info(e.getMessage());
        }
        return date;
    }

    public static String getTimeExpression(String chineseWithTimeMean){
        String timeExpression = null;
        try {
            TimeUnit[] timeUnits = getNormalizer().parse(chineseWithTimeMean);
            timeExpression = timeUnits[0].Time_Expression;
        }catch (Exception e){
            F.logger.info(e.getMessage());
        }
        return timeExpression;
    }

    public static String getTimeNorm(String chineseWithTimeMean){
        String timeNorm = null;
        try {
            TimeUnit[] timeUnits = getNormalizer().parse(chineseWithTimeMean);
            timeNorm = timeUnits[0].Time_Norm;
        }catch (Exception e){
            F.logger.info(e.getMessage());
        }
        return timeNorm;
    }

    public static String getDateStringForMicrosoftEvent(Date date, String timeZoneId){
        if (date==null) return null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
        return simpleDateFormat.format(date);
    }

    private static TimeNormalizer getNormalizer(){
        if (normalizer==null){
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


}
