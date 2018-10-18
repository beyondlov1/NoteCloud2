package com.beyond.repository;

import java.io.Serializable;
import java.util.List;

public interface BaseDao<T> {
    Serializable add(T t);

    Serializable delete(T t);

    Serializable delete(Serializable id);

    Serializable update(T t);

    T select(Serializable id);

    List<T> selectAll();
}
