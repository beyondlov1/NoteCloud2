package com.beyond.utils;

import com.time.nlp.TimeNormalizer;
import com.time.nlp.TimeUnit;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

public class TimeUtils {

    private static TimeNormalizer normalizer;

    public static Date parse(String chineseWithTimeMean){
        Date date;
        try {
            TimeUnit[] timeUnits = getNormalizer().parse(chineseWithTimeMean);
            date = timeUnits[0].getTime();
        }catch (Exception e){
            return null;
        }
        return date;
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
