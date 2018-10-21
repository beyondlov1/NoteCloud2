package com.beyond.entity;

import com.beyond.f.F;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author beyondlov1
 * @date 2018/10/21
 */
public class FileInfo implements Serializable {
    private Map<String,String> properties;

    public FileInfo(){
        properties = new HashMap<>();
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
