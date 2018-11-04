package com.beyond.repository.impl;

import com.beyond.entity.Document;
import com.beyond.property.LocalPropertyManager;
import com.beyond.repository.impl.LocalDocumentRepository;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

/**
 * @author beyondlov1
 * @date 2018/10/19
 */
public class LocalDocumentRepositoryProxy implements InvocationHandler {

    private LocalDocumentRepository localDocumentRepository;

    public LocalDocumentRepositoryProxy(LocalDocumentRepository localDocumentRepository) {
        this.localDocumentRepository = localDocumentRepository;
    }

    @Override
    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().startsWith("add")||method.getName().startsWith("delete")||method.getName().startsWith("update")){

            Object invoke = method.invoke(localDocumentRepository, args);

            //添加每个document的版本信息
            Document document = null;
            if (args[0] instanceof Document){
                document = (Document)args[0];
            }

            if (document==null) return null;

            Date currentTime = new Date();
            if (method.getName().startsWith("add")){
                document.setCreateTime(currentTime);
                document.setLastModifyTime(currentTime);
            }else if (method.getName().startsWith("modify")){
                document.setLastModifyTime(currentTime);
            }

            //更改文件属性
            LocalPropertyManager localPropertyManager = new LocalPropertyManager(localDocumentRepository.getPath());
            Map<String, String> propertiesMap = localPropertyManager.getAllProperties();
            propertiesMap.put("_lastModifyTime",String.valueOf(currentTime.getTime()));
            propertiesMap.put("_version",String.valueOf(Integer.parseInt(propertiesMap.getOrDefault("_version","0"))+1));
            propertiesMap.put("_modifyIds",propertiesMap.getOrDefault("_modifyIds","")+((Document)args[0]).getId()+",");
            localPropertyManager.batchSet(propertiesMap);

            return invoke;
        }
        return method.invoke(localDocumentRepository, args);
    }
}
