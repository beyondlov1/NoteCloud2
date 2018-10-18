package com.beyond.service;

import com.beyond.entity.Reminder;

import java.io.Serializable;
import java.util.Collection;

public interface RemindService<T> {
    Serializable addEvent(T reminder);
    Serializable modifyEvent(T reminder);
    Serializable removeEvent(T reminder);
    Serializable removeEvent(Serializable id);
    T readEvent(Serializable id);
    Collection<T> readAllEvent();
}
