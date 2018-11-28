package com.beyond;

import com.beyond.entity.Todo;
import com.beyond.f.F;

/**
 * @author beyondlov1
 * @date 2018/11/28
 */
public class FailedTodoService {

    private FailedTodoQueue queue;

    public FailedTodoService(ApplicationContext context){
        queue = new FailedTodoQueue(context);
    }

    public void init(){
        queue.init();
        Thread thread = new Thread(queue);
        thread.start();
    }

    public void add(Todo todo){
        queue.add(todo);
    }

    public void stop(){
        queue.stop();
    }

}
