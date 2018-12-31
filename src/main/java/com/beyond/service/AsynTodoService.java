package com.beyond.service;

import com.beyond.f.F;
import com.beyond.service.TodoService;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author beyondlov1
 * @date 2018/12/31
 */
public class AsynTodoService {

    private Timer timer;

    private TodoService todoService;

    public AsynTodoService(TodoService todoService) {
        this.todoService = todoService;
    }

    public void startDeleteExpiredTodo() {
        if (timer==null){
            timer = new Timer();
        }
        TimerTask timerTask = new DeleteTodoTask();
//        timer.schedule(timerTask,0, F.EXPIRE_TODO_DELETE_PERIOD);
        Calendar calendar = Calendar.getInstance();
        calendar.set(2019,Calendar.JANUARY,0,0,0,0);
        timer.schedule(timerTask,calendar.getTime(),24*60*60*1000L);
    }

    public void stop(){
        if (timer!=null){
            timer.cancel();
        }
    }

    public Timer getTimer() {
        return timer;
    }

    private class DeleteTodoTask extends TimerTask{
        @Override
        public void run() {
            todoService.deleteExpiredTodo();
        }
    }
}
