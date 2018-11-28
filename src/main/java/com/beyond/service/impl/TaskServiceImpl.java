package com.beyond.service.impl;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * @author beyondlov1
 * @date 2018/10/19
 */
public class TaskServiceImpl extends Service {

    private Task task;

    public TaskServiceImpl() {
        super();
    }

    @Override
    protected Task createTask() {
        return task;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

}
