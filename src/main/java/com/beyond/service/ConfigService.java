package com.beyond.service;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigService {
    private String configPath;
    private Properties properties;

    public ConfigService(String configPath) {
        this.configPath = configPath;
        init();
    }

    private void init(){
        InputStream inputStream = null;
        try {
            properties = new Properties();
            inputStream = new FileInputStream(configPath);
            properties.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (properties == null) {
            throw new RuntimeException("配置地址不正确!");
        }
    }

    public Properties getProperties() {
       return properties;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void setProperty(String key,String value){
        properties.setProperty(key,value);
    }

    public void storeProperties() {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(configPath);
            properties.store(fileWriter,"save");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
