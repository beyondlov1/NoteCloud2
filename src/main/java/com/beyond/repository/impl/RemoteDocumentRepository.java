package com.beyond.repository.impl;

import com.beyond.RemoteBase;
import com.beyond.entity.Document;
import com.beyond.exception.RemotePullException;
import com.beyond.f.F;
import com.beyond.property.LocalPropertyManager;
import com.beyond.property.PropertyManager;
import com.beyond.repository.Repository;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class RemoteDocumentRepository extends RemoteBase implements Repository<Document> {

    private LocalDocumentRepository localDocumentRepository;

    private PropertyManager localPropertyManager;
    private PropertyManager remotePropertyManager;

    private String path;

    public RemoteDocumentRepository(String url, LocalDocumentRepository localDocumentRepository) {
        super();
        this.path = url;
        this.localDocumentRepository = localDocumentRepository;
    }

    public RemoteDocumentRepository(String url, LocalDocumentRepository localDocumentRepository, LocalPropertyManager localPropertyManager, PropertyManager remotePropertyManager) {
        super();
        this.path = url;
        this.localDocumentRepository = localDocumentRepository;
        this.localPropertyManager = localPropertyManager;
        this.remotePropertyManager = remotePropertyManager;
    }

    public String getPath() {
        return path;
    }

    public Serializable add(Document document) {
        return localDocumentRepository.add(document);
    }

    public Serializable delete(Document document) {
        return localDocumentRepository.delete(document);
    }

    public Serializable delete(Serializable id) {
        return localDocumentRepository.delete(id);
    }

    public Serializable update(Document document) {
        return localDocumentRepository.update(document);
    }

    public Document select(Serializable id) {
        return localDocumentRepository.select(id);
    }

    public List<Document> selectAll() {
        return localDocumentRepository.selectAll();
    }

    public synchronized int save() {
        localDocumentRepository.save();
        try {
            upload();
            return 1;
        } catch (IOException e) {
            F.logger.info(e.getMessage());
        }
        return 0;
    }

    public synchronized int save(List<Document> list) {
        localDocumentRepository.save(list);
        try {
            upload();
            return 1;
        } catch (IOException e) {
            F.logger.info(e.getMessage());
        }
        return 0;
    }

    private synchronized void upload() throws IOException {
        String path = localDocumentRepository.getPath();
        String url = this.getPath();

        CloseableHttpClient client = getClient();
        mkRemoteDir(url);
        HttpPut httpPut = new HttpPut(url);
        httpPut.setEntity(new FileEntity(new File(path)));
        sendRequest(client, httpPut);
        release(client);

        //上传属性
        if (localPropertyManager != null && remotePropertyManager != null) {
            remotePropertyManager.batchSet(localPropertyManager.getAllProperties());
        }
    }

    public synchronized int pull() {
        try {
            String path = localDocumentRepository.getPath();
            String url = this.getPath();

            CloseableHttpClient client = getClient();

            HttpGet httpGet = new HttpGet(url);
            FileOutputStream fileOutputStream = null;
            try {
                CloseableHttpResponse response = client.execute(httpGet);
                int statusCode = response.getStatusLine().getStatusCode();
                F.logger.info(statusCode);
                if (statusCode > 400) {
                    upload();
                    return 0;
                }
                HttpEntity entity = response.getEntity();
                fileOutputStream = new FileOutputStream(path);
                fileOutputStream.write(EntityUtils.toByteArray(entity));

            } catch (IOException e) {
                F.logger.info(e.getMessage());
                throw new RuntimeException(e);
            } finally {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            release(client);

            //刷新下载到本地的文档
            localDocumentRepository.pull();

            //下载属性
            if (localPropertyManager != null && remotePropertyManager != null) {
                localPropertyManager.batchSet(remotePropertyManager.getAllProperties());
            }

        }catch (Exception e){
            throw new RemotePullException();
        }
        return 1;
    }

    public synchronized void lock() {
        remotePropertyManager.set("_lock", "1");
    }

    public synchronized void unlock() {
        remotePropertyManager.set("_lock", "0");
    }

    public synchronized boolean isAvailable() {
        String lock = remotePropertyManager.getProperty("_lock");
        return !StringUtils.equals(lock, "1");
    }

}
