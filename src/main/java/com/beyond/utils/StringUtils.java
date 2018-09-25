package com.beyond.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static boolean isEmpty(String string){
        return string == null || "".equals(string);
    }

    public static String cutAndPretty(String content, int i) {
        if (content.replaceAll(" ","").length()>i){
            return content.substring(0,i)+"...";
        }else {
            return content;
        }
    }

    /**
     * 返回非空字符串， 如果为空则转化为 “”
     * @param object
     * @return
     */
    public static String getNotNullString(Object object){
        return String.valueOf(object==null?"":object);
    }

}
