package com.beyond.repository;

import com.beyond.entity.Element;
import com.beyond.f.F;
import com.beyond.property.LocalPropertyManager;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author beyondlov1
 * @date 2018/10/20
 */
public abstract class AbstractLocalRepository<T extends Element> implements Repository<T> {

    private XStream xStream;

    protected List<T> list;

    private ReentrantLock lock;

    private String path;

    private LocalPropertyManager localPropertyManager;

    public AbstractLocalRepository(String path){
        super();
        this.path = path;
        this.localPropertyManager = new LocalPropertyManager(getPath());
        init();
    }

    public AbstractLocalRepository(String path, XStream xStream) {
        super();
        this.path = path;
        this.xStream = xStream;
        this.localPropertyManager = new LocalPropertyManager(getPath());
    }

    public String getPath() {
        return path;
    }

    private void init() {

        xStream = getXStream();

        //bind file
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

    protected abstract XStream getXStream();

    public synchronized Serializable add(T element) {
        list.add(element);
        return element.getId();
    }

    public synchronized Serializable delete(T element) {
        return delete(element.getId());
    }

    @Override
    public Serializable delete(Serializable id) {
        T foundElement = null;
        for (T element : list) {
            if (StringUtils.equals((String) id, element.getId())) {
                foundElement = element;
                break;
            }
        }

        if (foundElement != null) {
            list.remove(foundElement);
            return foundElement.getId();
        } else {
            return null;
        }
    }

    public synchronized Serializable update(T element) {
        try {
            int index = -1;
            T foundElement = null;
            for (int i = 0; i < list.size(); i++) {
                foundElement = list.get(i);
                if (StringUtils.equals(element.getId(), foundElement.getId())) {
                    index = i;
                    break;
                }
            }

            if (index != -1) {
                Field[] declaredFields = element.getClass().getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    declaredField.setAccessible(true);
                    Object foundElementProperty = declaredField.get(foundElement);
                    Object newElementProperty = declaredField.get(element);
                    declaredField.set(element, newElementProperty == null ? foundElementProperty : newElementProperty);
                }
                list.set(index, element);
                return element.getId();
            } else {
                return null;
            }
        } catch (Exception e) {
            F.logger.info(e.getMessage());
            return null;
        }
    }

    public T select(Serializable id) {
        T foundT = null;
        for (T element : list) {
            if (StringUtils.equals((String) id, element.getId())) {
                foundT = element;
                break;
            }
        }
        return foundT;
    }

    public List<T> selectAll() {
        return list;
    }

    public synchronized int save() {
        //获取属性
        Map<String, String> propertiesMap = localPropertyManager.getAllProperties();

        try {
            xStream.toXML(list, new FileOutputStream(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            F.logger.info("FileNotFoundException");
        }

        //保存属性
        propertiesMap.putIfAbsent("_version", "0");
        propertiesMap.putIfAbsent("_modifyIds", "");
        propertiesMap.putIfAbsent("_lastModifyTime", "0");
        localPropertyManager.batchSet(propertiesMap);

        return 1;
    }

    public synchronized int save(List<T> list) {
        this.list = list;
        return save();
    }

    @SuppressWarnings("unchecked")
    public synchronized int pull() {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(path);
            Object object = xStream.fromXML(fileInputStream);
            if (object instanceof List) {
                list = (List) object;
            }
            return 1;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            init();
            F.logger.info("FileNotFoundException");
        } catch (StreamException e) {
            e.printStackTrace();
            save();
            F.logger.info("StreamException");
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
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
}
