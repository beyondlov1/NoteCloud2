package com.beyond.repository.impl;

import com.beyond.RepositoryFactory;
import com.beyond.property.LocalPropertyManager;
import com.beyond.property.PropertyManager;
import com.beyond.repository.Repository;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;
import com.beyond.entity.Document;
import com.beyond.entity.Note;
import com.beyond.entity.Todo;
import com.beyond.f.F;
import javafx.beans.InvalidationListener;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LocalDocumentRepository extends Observable implements Repository<Document> {

    private XStream xStream;

    private List<Document> list;

    private ReentrantLock lock;
    
    private String path;

    private LocalPropertyManager localPropertyManager;

    public LocalDocumentRepository(String path) {
        super();
        this.path = path;
        localPropertyManager = new LocalPropertyManager(getPath());
        init();
    }

    public String getPath() {
        return path;
    }

    private void init() {
        //init xStream
        if (xStream==null) {
            xStream = new XStream();
            xStream.alias("document", Document.class);
            xStream.alias("note", Note.class);
            xStream.alias("todo", Todo.class);
            xStream.alias("documents", List.class);
            xStream.useAttributeFor(Document.class, "id");
            xStream.useAttributeFor(Note.class, "id");
            xStream.useAttributeFor(Todo.class, "id");
        }

        //init file
        Path path = Paths.get(getPath());
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();
                F.logger.info("IOException");
            }

            list = new ArrayList<>();
            save();
        } else {
            pull();
            list = selectAll();
        }
    }

    public synchronized Serializable add(Document document) {
        list.add(document);
        return document.getId();
    }

    public synchronized Serializable delete(Document document) {
        return delete(document.getId());
    }

    @Override
    public Serializable delete(Serializable id) {
        Document foundDocument = null;
        for (Document doc : list) {
            if (StringUtils.equals((String)id, doc.getId())) {
                foundDocument = doc;
                break;
            }
        }

        if (foundDocument != null) {
            list.remove(foundDocument);
            return foundDocument.getId();
        } else {
            return null;
        }
    }

    public synchronized Serializable update(Document document) {
        int index = -1;
        Document foundDocument= null;
        for (int i = 0; i < list.size(); i++) {
            foundDocument = list.get(i);
            if (StringUtils.equals(document.getId(), foundDocument.getId())) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            document.setTitle(StringUtils.isNotBlank(document.getTitle())?document.getTitle():foundDocument.getTitle());
            document.setContent(StringUtils.isNotBlank(document.getContent())?document.getContent():foundDocument.getContent());
            document.setVersion(foundDocument.getVersion()+1);
            document.setCreateTime(foundDocument.getCreateTime());
            document.setLastModifyTime(new Date());
            document.setType(StringUtils.isNotBlank(document.getType())?document.getType():foundDocument.getType());
            list.set(index,document);
            return document.getId();
        } else {
            return null;
        }
    }

    public Document select(Serializable id) {
        Document foundDocument = null;
        for (Document doc : list) {
            if (StringUtils.equals(id.toString(), doc.getId())) {
                foundDocument = doc;
                break;
            }
        }
        return foundDocument;
    }

    public List<Document> selectAll() {
        return list;
    }

    public synchronized void save() {
        //获取属性
        Map<String, String> propertiesMap = localPropertyManager.getAllProperties();

        try {
            xStream.toXML(list, new FileOutputStream(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            F.logger.info("FileNotFoundException");
        }

        //保存属性
        propertiesMap.putIfAbsent("_version","0");
        propertiesMap.putIfAbsent("_modifyIds","");
        propertiesMap.putIfAbsent("_lastModifyTime","0");
        localPropertyManager.batchSet(propertiesMap);
    }

    public synchronized void save(List<Document> list) {
        this.list = list;
        save();
    }

    public synchronized void pull(){
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(path);
            Object object = xStream.fromXML(fileInputStream);
            if (object instanceof List) {
                list = (List<Document>) object;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            init();
            F.logger.info("FileNotFoundException");
        }catch (StreamException e){
            e.printStackTrace();
            save();
            F.logger.info("StreamException");
        }finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void lock() {
        if (lock == null) {
            lock = new ReentrantLock();
        }
        lock.tryLock();
    }

    public void unlock() {
        lock.unlock();
    }

    public boolean isAvailable() {
        return !lock.isLocked();
    }

    public static void main(String[] args) {
        Repository<Document> repository = RepositoryFactory.getLocalRepository("./document/documents.xml");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                repository.add(new Document("test1", "content1"));
                repository.save();
            }
        });
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                repository.add(new Document("test2", "content1"));
                repository.save();
            }
        });
        Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                repository.add(new Document("5", "content1"));
                repository.add(new Document("6", "content2"));
                repository.save();
            }
        });

        thread.start();
        thread2.start();
//        thread3.start();

    }
}
