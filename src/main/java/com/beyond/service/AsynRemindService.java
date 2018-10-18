package com.beyond.service;

import com.beyond.callback.Callback;
import com.beyond.entity.Reminder;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.io.Serializable;

public interface AsynRemindService<T> extends RemindService<T>{
    void addEvent(T reminder, EventHandler<WorkerStateEvent> success);
    void addEvent(T reminder,EventHandler<WorkerStateEvent> success,EventHandler<WorkerStateEvent> fail);
    void modifyEvent(T reminder,EventHandler<WorkerStateEvent> success);
    void modifyEvent(T reminder,EventHandler<WorkerStateEvent> success,EventHandler<WorkerStateEvent> fail);
    void removeEvent(T reminder, EventHandler<WorkerStateEvent> success);
    void removeEvent(T reminder, EventHandler<WorkerStateEvent> success, EventHandler<WorkerStateEvent> fail);
    void removeEvent(Serializable id, EventHandler<WorkerStateEvent> success);
    void removeEvent(Serializable id, EventHandler<WorkerStateEvent> success, EventHandler<WorkerStateEvent> fail);
    void readEvent(Serializable id,EventHandler<WorkerStateEvent> success);
    void readEvent(Serializable id,EventHandler<WorkerStateEvent> success,EventHandler<WorkerStateEvent> fail);
    void readAllEvent(EventHandler<WorkerStateEvent> success);
    void readAllEvent(EventHandler<WorkerStateEvent> success,EventHandler<WorkerStateEvent> fail);
}
