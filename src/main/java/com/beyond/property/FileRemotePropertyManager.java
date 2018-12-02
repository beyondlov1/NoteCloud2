package com.beyond.property;

import com.beyond.RemoteBase;
import com.beyond.entity.FileInfo;
import com.beyond.exception.NoInternetException;
import com.beyond.exception.RemotePullException;
import com.beyond.f.F;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.jackrabbit.webdav.client.methods.HttpMkcol;

import java.io.*;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Map;


/**
 * @author beyondlov1
 * @date 2018/10/21
 */
public class FileRemotePropertyManager extends RemoteBase implements PropertyManager {
    private FileInfo fileInfo;

    private String url;

    public FileRemotePropertyManager(String url) {
        this.url = url;
        fileInfo = new FileInfo();
    }

    @Override
    public void set(String key, String value) {
        pull();
        fileInfo.getProperties().put(key, value);
        save();
    }

    @Override
    public String getProperty(String key) {
        pull();
        return fileInfo.getProperties().getOrDefault(key,"");
    }

    @Override
    public void batchSet(Map<String, String> map) {
        fileInfo.setProperties(map);
        save();
    }

    @Override
    public Map<String, String> getAllProperties() {
        pull();
        return fileInfo.getProperties();
    }

    private void pull() {
        CloseableHttpClient client = getClient();
        HttpGet httpGet = new HttpGet(url);
        ObjectInputStream objectInputStream = null;
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(EntityUtils.toByteArray(entity));
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            fileInfo = (FileInfo) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            F.logger.info(e.getMessage(),e);
            if (e instanceof UnknownHostException){
                throw new NoInternetException();
            }else {
                throw new RemotePullException();
            }
        } finally {
            try {
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        release(client);
    }

    private void save() {
        if (fileInfo != null) {
            CloseableHttpClient client = getClient();
            ObjectOutputStream objectOutputStream = null;
            try {
                mkRemoteDir(url);
                HttpPut httpPut = new HttpPut(url);
                objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(F.DEFAULT_REMOTE_PROPERTY_LOCAL_PATH)));
                objectOutputStream.writeObject(fileInfo);
                httpPut.setEntity(new FileEntity(new File(F.DEFAULT_REMOTE_PROPERTY_LOCAL_PATH)));
                sendRequest(client, httpPut);
            } catch (IOException e) {
                F.logger.error(e.getMessage());
                throw new RuntimeException("远程属性上传失败");
            } finally {
                try {
                    if (objectOutputStream != null) {
                        objectOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            release(client);
        }
    }
}
