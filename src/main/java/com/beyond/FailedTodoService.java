package com.beyond;

import com.beyond.entity.Todo;
import com.beyond.f.F;

/**
 * @author beyondlov1
 * @date 2018/11/28
 */
public class FailedTodoService {

    private FailedTodoQueue queue;

    private boolean running;

    public FailedTodoService(ApplicationContext context){
        queue = new FailedTodoQueue(context);
    }

    public void init(){
        queue.init();
        Thread thread = new Thread(queue);
        thread.start();
        this.setRunning(true);
    }

    public void add(Todo todo){
        queue.add(todo);
    }

    public void stop(){
        queue.stop();
        this.setRunning(false);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
