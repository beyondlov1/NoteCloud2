package com.beyond.property;

import java.util.Map;

public interface PropertyManager {

    void set(String key, String value);

    String getProperty(String key);

    void batchSet(Map<String, String> map);

    Map<String, String> getAllProperties();
}
