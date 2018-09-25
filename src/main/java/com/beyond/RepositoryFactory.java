package com.beyond;

import com.beyond.entity.Document;
import com.beyond.f.F;
import com.beyond.property.LocalPropertyManager;
import com.beyond.repository.LocalDocumentRepository;
import com.beyond.repository.RemoteDocumentRepository;
import com.beyond.repository.Repository;
import org.apache.commons.lang3.StringUtils;

import javax.print.Doc;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RepositoryFactory {

    public static Repository getLocalRepository(String path) {
        final LocalDocumentRepository localDocumentRepository = new LocalDocumentRepository(path);
        return (Repository)Proxy.newProxyInstance(localDocumentRepository.getClass().getClassLoader(), localDocumentRepository.getClass().getInterfaces(), new InvocationHandler() {
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
                    }else if (method.getName().startsWith("update")){
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
        });
    }

    public static Repository getRemoteRepository(String path,LocalDocumentRepository localDocumentRepository) {
        final RemoteDocumentRepository remoteDocumentRepository = new RemoteDocumentRepository(path,localDocumentRepository);
        return (Repository)Proxy.newProxyInstance(remoteDocumentRepository.getClass().getClassLoader(), remoteDocumentRepository.getClass().getInterfaces(), new InvocationHandler() {
            @Override
            public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (StringUtils.equals(method.getName(),"save")||StringUtils.equals(method.getName(),"pull")){
                    ExecutorService executorService = Executors.newCachedThreadPool();
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                method.invoke(remoteDocumentRepository,args);
                            } catch (Exception e) {
                                e.printStackTrace();
                                F.logger.info(e.getMessage());
                            }
                        }
                    });
                    return null;
                }
                return method.invoke(remoteDocumentRepository,args);
            }
        });
    }

    public static void main(String[] args) {
        Repository repository = RepositoryFactory.getLocalRepository("./document/tmp.xml");
        repository.add(new Document("3","content"));

        LocalPropertyManager localPropertyManager = new LocalPropertyManager(repository.getPath());
        Map<String, String> allProperties = localPropertyManager.getAllProperties();
        System.out.println(allProperties);

    }
}
