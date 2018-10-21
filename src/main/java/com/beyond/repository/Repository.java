package com.beyond.repository;

import java.io.IOException;
import java.util.List;

public interface Repository<T> extends BaseDao<T>{

    int save();

    int save(List<T> list);

    int pull();

    void lock();

    void unlock();

    boolean isAvailable();

    String getPath();

}
