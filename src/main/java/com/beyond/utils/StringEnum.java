package com.beyond.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StringEnum {
    private String[] ss;
    public StringEnum(String...ss){
        this.ss = ss;
    }

    public boolean contains(String target){
        List<String> strings = Arrays.asList(ss);
        if (strings.isEmpty()){
            return false;
        }
        return strings.contains(target);
    }
}
