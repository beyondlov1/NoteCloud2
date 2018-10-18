package com.beyond.repository;

import java.util.List;

public interface Repository<T> extends BaseDao<T>{

    void save();

    void save(List<T> list);

    void pull();

    void lock();

    void unlock();

    boolean isAvailable();

    String getPath();

}
