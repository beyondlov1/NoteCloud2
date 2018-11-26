package com.beyond.service;

import com.beyond.entity.Reminder;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.io.Serializable;
import java.util.Collection;

public class AsynRemindServiceImpl implements AsynRemindService<Reminder> {

    private SyncRemindService<Reminder> syncRemindService;

    private TaskServiceImpl taskService;

    public AsynRemindServiceImpl(SyncRemindService<Reminder> syncRemindService) {
        this.syncRemindService = syncRemindService;
        this.taskService = new TaskServiceImpl();
    }

    @Override
    public final Serializable addEvent(Reminder reminder) {
        this.addEvent(reminder, null, null);
        return null;
    }

    @Override
    public final Serializable modifyEvent(Reminder reminder) {
        this.modifyEvent(reminder, null, null);
        return null;
    }

    @Override
    public final Serializable removeEvent(Reminder reminder) {
        this.removeEvent(reminder, null, null);
        return null;
    }

    @Override
    public final Serializable removeEvent(Serializable id) {
        this.removeEvent(id, null, null);
        return null;
    }

    @Override
    public final Reminder readEvent(Serializable id) {
        this.readEvent(id, null, null);
        return null;
    }

    @Override
    public final Collection<Reminder> readAllEvent() {
        this.readAllEvent(null, null);
        return null;
    }

    @Override
    public final void addEvent(Reminder reminder, EventHandler<WorkerStateEvent> success) {
        this.addEvent(reminder, success, null);
    }

    @Override
    public synchronized void addEvent(Reminder reminder, EventHandler<WorkerStateEvent> success, EventHandler<WorkerStateEvent> fail) {
        taskService.reset();
        taskService.setTask(new Task() {
            @Override
            protected Object call() {
                return syncRemindService.addEvent(reminder);
            }
        });
        taskService.setOnSucceeded(success);
        taskService.setOnFailed(fail);
        taskService.start();
    }

    @Override
    public final void modifyEvent(Reminder reminder, EventHandler<WorkerStateEvent> success) {
        this.modifyEvent(reminder, success, null);
    }

    @Override
    public synchronized void modifyEvent(Reminder reminder, EventHandler<WorkerStateEvent> success, EventHandler<WorkerStateEvent> fail) {
        taskService.reset();
        taskService.setTask(new Task() {
            @Override
            protected Object call() {
                return syncRemindService.modifyEvent(reminder);
            }
        });
        taskService.setOnSucceeded(success);
        taskService.setOnFailed(fail);
        taskService.start();
        
    }

    @Override
    public final void removeEvent(Reminder reminder, EventHandler<WorkerStateEvent> success) {
        this.removeEvent(reminder, success, null);
    }

    @Override
    public void removeEvent(Reminder reminder, EventHandler<WorkerStateEvent> success, EventHandler<WorkerStateEvent> fail) {
        removeEvent(reminder.getId(),success,fail);
    }

    @Override
    public final void removeEvent(Serializable id, EventHandler<WorkerStateEvent> success) {
        this.removeEvent(id, success, null);
    }

    @Override
    public synchronized void removeEvent(Serializable id, EventHandler<WorkerStateEvent> success, EventHandler<WorkerStateEvent> fail) {
        taskService.reset();
        taskService.setTask(new Task() {
            @Override
            protected Object call() throws Exception {
                return syncRemindService.removeEvent(id);
            }
        });
        taskService.setOnSucceeded(success);
        taskService.setOnFailed(fail);
        taskService.start();
    }

    @Override
    public final void readEvent(Serializable id, EventHandler<WorkerStateEvent> success) {
        this.readEvent(id, success, null);
    }

    @Override
    public synchronized void readEvent(Serializable id, EventHandler<WorkerStateEvent> success, EventHandler<WorkerStateEvent> fail) {
        taskService.reset();taskService.setTask(new Task() {
            @Override
            protected Object call() throws Exception {
                return syncRemindService.readEvent(id);
            }
        });
        taskService.setOnSucceeded(success);
        taskService.setOnFailed(fail);
        taskService.start();
        
    }

    @Override
    public final void readAllEvent(EventHandler<WorkerStateEvent> success) {
        this.readAllEvent(success, null);
    }

    @Override
    public synchronized void readAllEvent(EventHandler<WorkerStateEvent> success, EventHandler<WorkerStateEvent> fail) {
        taskService.reset();taskService.setTask(new Task() {
            @Override
            protected Object call() throws Exception {
                return syncRemindService.readAllEvent();
            }
        });
        taskService.setOnSucceeded(success);
        taskService.setOnFailed(fail);
        taskService.start();
        
    }
}

