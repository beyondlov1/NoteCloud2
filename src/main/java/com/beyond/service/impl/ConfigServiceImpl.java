package com.beyond.service.impl;

import com.beyond.f.F;
import com.beyond.service.ConfigService;
import com.beyond.utils.NameUtils;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

public class ConfigServiceImpl implements ConfigService {
    private String configPath;
    private Properties properties;

    public ConfigServiceImpl(String configPath) {
        this.configPath = configPath;
        init();
    }

    private void init(){
        try (InputStream inputStream = new FileInputStream(configPath)){
            properties = new Properties();
            properties.load(inputStream);
        } catch (Exception e) {
            F.logger.info("config file may not exist, default config has been used");
        }

        if (properties == null) {
            properties = new Properties();
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
        try (FileWriter fileWriter = new FileWriter(configPath);){
            properties.store(fileWriter,"save at "+ new Date());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPropertiesTo(Class clazz)  {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Enumeration<?> propertyNames = properties.propertyNames();
            while (propertyNames.hasMoreElements()){
                Object propertyNameObject = propertyNames.nextElement();
                if (propertyNameObject instanceof String){
                    String propertyName = (String) propertyNameObject;
                    if (Modifier.isStatic(field.getModifiers())&&propertyName.endsWith(NameUtils.camelCase(field.getName()))){
                        field.setAccessible(true);
                        try {
                            if (field.getType() == long.class){
                                field.set(null,Long.valueOf(properties.getProperty(propertyName)));
                            }else if (field.getType() == String.class){
                                field.set(null,properties.getProperty(propertyName));
                            }
                        } catch (IllegalAccessException e) {
                            F.logger.info(e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
