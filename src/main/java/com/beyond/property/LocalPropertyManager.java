package com.beyond.property;

import com.beyond.f.F;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalPropertyManager implements PropertyManager {

    private String path;

    public LocalPropertyManager(String path) {
        this.path = path;
    }

    @Override
    public synchronized void set(String key, String value){
        UserDefinedFileAttributeView userDefinedFileAttributeView = Files.getFileAttributeView(Paths.get(path), UserDefinedFileAttributeView.class);
        ByteBuffer byteBuffer = Charset.defaultCharset().encode(StringUtils.defaultIfBlank(value,""));
        try {
            userDefinedFileAttributeView.write(key, byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getProperty(String key) {
        UserDefinedFileAttributeView userDefinedFileAttributeView = Files.getFileAttributeView(Paths.get(path), UserDefinedFileAttributeView.class);
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(userDefinedFileAttributeView.size(key));
            userDefinedFileAttributeView.read(key, byteBuffer);
            byteBuffer.flip();
            return Charset.defaultCharset().decode(byteBuffer).toString();
        } catch (IOException e) {
            e.printStackTrace();
            F.logger.info(e.getMessage());
        }
        return "";
    }

    @Override
    public synchronized void batchSet(Map<String,String> map){
        for (String key : map.keySet()) {
            String value = map.get(key);
            set(key,value);
        }
    }

    @Override
    public Map<String,String> getAllProperties(){
        Map<String,String> map = new HashMap<>();
        UserDefinedFileAttributeView userDefinedFileAttributeView = Files.getFileAttributeView(Paths.get(path), UserDefinedFileAttributeView.class);
        try {
            List<String> list = userDefinedFileAttributeView.list();
            for (String key : list) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(userDefinedFileAttributeView.size(key));
                userDefinedFileAttributeView.read(key,byteBuffer);
                byteBuffer.flip();
                String value = Charset.defaultCharset().decode(byteBuffer).toString();
                map.put(key,value);
            }
        } catch (IOException e) {
            e.printStackTrace();
            F.logger.info(e.getMessage());
        }
        return map;
    }


    public static void main(String[] args) {
        LocalPropertyManager localPropertyManager = new LocalPropertyManager("./document/tmp.xml");
//        localPropertyManager.set("_createTime",String.valueOf(new Date().getTime()));
        Map<String, String> properties = localPropertyManager.getAllProperties();
//        String createTime = properties.get("_createTime");
        System.out.println(properties);
    }

}
