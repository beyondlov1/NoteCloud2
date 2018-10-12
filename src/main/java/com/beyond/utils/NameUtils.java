package com.beyond.utils;

public class NameUtils {
    /**
     * 将下划线的转成驼峰命名
     * @param target
     */
    public static String camelCase(String target){
        StringBuilder result = new StringBuilder();
        target =target.toLowerCase();
        String[] values = target.split("_");
        int index = 0;
        for (String value : values) {
            if (index==0){
                result.append(value);
            }else {
                result.append(value.substring(0, 1).toUpperCase()).append(value.substring(1));
            }
            index++;
        }
        return result.toString();
    }

    /**
     * 将驼峰命名转成下划线命名
     * @param target
     * @return
     */
    public static String underScoreCase(String target){
        StringBuilder result = new StringBuilder();
        char[] chars = target.toCharArray();
        for (char c : chars) {
            if (isUpperCase(c)){
                result.append(("_"+c).toLowerCase());
            }else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private static boolean isUpperCase(char c){
        return (int) c < 91 && (int) c > 64;
    }

}
