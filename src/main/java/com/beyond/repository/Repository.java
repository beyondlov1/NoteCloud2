package com.beyond.repository;

import java.io.Serializable;
import java.util.List;

public interface Repository<T> {
    Serializable add(T t);

    Serializable delete(T t);

    Serializable update(T t);

    T select(Serializable id);

    List<T> selectAll();

    void save();

    void save(List<T> list);

    void pull();

    void lock();

    void unlock();

    boolean isAvailable();

    String getPath();

}
