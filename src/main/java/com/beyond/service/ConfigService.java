package com.beyond.service;

import java.util.Properties;

public interface ConfigService {
    void setProperty(String key,String value);
    String getProperty(String key);
    Properties getProperties();
    void storeProperties();
    void loadPropertiesTo(Class clazz);
}
