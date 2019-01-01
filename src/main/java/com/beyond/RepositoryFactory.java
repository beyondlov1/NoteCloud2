package com.beyond;

import com.beyond.entity.Document;
import com.beyond.f.F;
import com.beyond.property.LocalPropertyManager;
import com.beyond.repository.impl.LocalDocumentRepositoryProxy;
import com.beyond.repository.impl.LocalDocumentRepository;
import com.beyond.repository.impl.RemoteDocumentRepository;
import com.beyond.repository.Repository;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 生产仓库代理对象
 */
public class RepositoryFactory {

    public static Repository getLocalRepository(String path) {
        final LocalDocumentRepository localDocumentRepository = new LocalDocumentRepository(path);
        return (Repository) Proxy.newProxyInstance(localDocumentRepository.getClass().getSuperclass().getClassLoader(), localDocumentRepository.getClass().getSuperclass().getInterfaces(),
                new LocalDocumentRepositoryProxy(localDocumentRepository));
    }

    public static Repository getRemoteRepository(String path, LocalDocumentRepository localDocumentRepository) {
        final RemoteDocumentRepository remoteDocumentRepository = new RemoteDocumentRepository(path, localDocumentRepository);
        return (Repository) Proxy.newProxyInstance(remoteDocumentRepository.getClass().getClassLoader(), remoteDocumentRepository.getClass().getInterfaces(), new InvocationHandler() {
            @Override
            public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (StringUtils.equals(method.getName(), "save") || StringUtils.equals(method.getName(), "pull")) {
                    ExecutorService executorService = Executors.newCachedThreadPool();
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                method.invoke(remoteDocumentRepository, args);
                            } catch (Exception e) {
                                e.printStackTrace();
                                F.logger.info(e.getMessage());
                            }
                        }
                    });
                    return null;
                }
                return method.invoke(remoteDocumentRepository, args);
            }
        });
    }

    public static void main(String[] args) {
        Repository repository = RepositoryFactory.getLocalRepository("./document/tmp.xml");
        repository.add(new Document("3", "content"));

        LocalPropertyManager localPropertyManager = new LocalPropertyManager(repository.getPath());
        Map<String, String> allProperties = localPropertyManager.getAllProperties();
        System.out.println(allProperties);

    }
}

