package com.beyond.repository;

import com.beyond.RemoteBase;
import com.beyond.entity.Document;
import com.beyond.property.LocalPropertyManager;
import com.beyond.property.PropertyManager;
import com.beyond.property.RemotePropertyManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.jackrabbit.webdav.client.methods.HttpMkcol;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.List;

public class RemoteDocumentRepository extends RemoteBase implements Repository<Document> {

    private LocalDocumentRepository localDocumentRepository;

    private PropertyManager localPropertyManager;
    private PropertyManager remotePropertyManager;

    private String path;

    public RemoteDocumentRepository(String url,LocalDocumentRepository localDocumentRepository){
        super();
        this.path = url;
        this.localDocumentRepository = localDocumentRepository;
    }

    public RemoteDocumentRepository(String url,LocalDocumentRepository localDocumentRepository, LocalPropertyManager localPropertyManager){
        super();
        this.path = url;
        this.localDocumentRepository = localDocumentRepository;
        this.localPropertyManager = localPropertyManager;
        this.remotePropertyManager = new RemotePropertyManager(path);
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

    public Serializable update(Document document) {
        return localDocumentRepository.update(document);
    }

    public Document select(Serializable id) {
        return localDocumentRepository.select(id);
    }

    public List<Document> selectAll() {
        return localDocumentRepository.selectAll();
    }

    public synchronized void save() {
        localDocumentRepository.save();
        upload();
    }

    public synchronized void save(List<Document> list) {
        localDocumentRepository.save(list);
        upload();
    }

    private synchronized void upload() {
        String path = localDocumentRepository.getPath();
        String url = this.getPath();

        CloseableHttpClient client =getClient();
        mkRemoteDir(url);
        HttpPut httpPut = new HttpPut(url);
        httpPut.setEntity(new FileEntity(new File(path)));
        sendRequest(client,httpPut);
        release(client);

        //上传属性
        if (localPropertyManager!=null&&remotePropertyManager!=null){
            remotePropertyManager.batchSet(localPropertyManager.getAllProperties());
        }
    }

    public synchronized void pull(){
        String path = localDocumentRepository.getPath();
        String url = this.getPath();

        CloseableHttpClient client =getClient();

        HttpGet httpGet = new HttpGet(url);
        FileOutputStream fileOutputStream = null;
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            fileOutputStream = new FileOutputStream(path);
            fileOutputStream.write(EntityUtils.toByteArray(entity));
        } catch (IOException e) {
            e.printStackTrace();
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

        //下载属性
        if (localPropertyManager!=null&&remotePropertyManager!=null){
            localPropertyManager.batchSet(remotePropertyManager.getAllProperties());
        }
    }

    private void mkRemoteDir(String url){
        CloseableHttpClient client = getClient();
        //获取文件夹路径
        int index = StringUtils.lastIndexOf(url, "/");
        String parentUrl = StringUtils.substring(url, 0, index);

        HttpMkcol httpMkcol = new HttpMkcol(parentUrl);
        String root = "https://" + URI.create(url).getHost();
        if (!StringUtils.equalsIgnoreCase(parentUrl, root)) {
            mkRemoteDir(parentUrl);
        }
        sendRequest(client,httpMkcol);
        release(client);
    }

    public synchronized void lock() {
        remotePropertyManager.set("_lock","1");
    }

    public synchronized void unlock() {
        remotePropertyManager.set("_lock","0");
    }

    public synchronized boolean isAvailable(){
        String lock = remotePropertyManager.getProperty("_lock");
        return !StringUtils.equals(lock, "1");
    }

    public static void main(String[] args) {
        LocalDocumentRepository localDocumentRepository = new LocalDocumentRepository("./document/tmp.xml");
        RemoteDocumentRepository remoteDocumentRepository = new RemoteDocumentRepository("https://yura.teracloud.jp/dav/NoteCloud/repository/documents.xml",localDocumentRepository);
        remoteDocumentRepository.add(new Document("5","content"));
        remoteDocumentRepository.save();
        System.out.println(remoteDocumentRepository.selectAll().size());
        remoteDocumentRepository.pull();
        List<Document> documents = remoteDocumentRepository.selectAll();
        System.out.println(documents.size());
    }
}
