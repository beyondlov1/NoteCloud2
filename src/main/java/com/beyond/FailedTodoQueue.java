package com.beyond;

import com.beyond.entity.MicrosoftReminder;
import com.beyond.entity.Reminder;
import com.beyond.entity.Todo;
import com.beyond.f.F;
import com.beyond.service.MainService;
import com.beyond.service.RemindService;
import org.apache.commons.lang3.time.DateUtils;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author beyondlov1
 * @date 2018/11/28
 */
public class FailedTodoQueue implements Runnable {

    private ConcurrentLinkedQueue<Todo> queue;

    private AtomicBoolean isNeedRun = new AtomicBoolean(false);
    private AtomicBoolean isStop = new AtomicBoolean(false);

    private ApplicationContext context;

    private RemindService<Reminder> remindService;
    private MainService mainService;

    public FailedTodoQueue(ApplicationContext context) {
        this.context = context;
        this.queue = new ConcurrentLinkedQueue<>();
    }

    public void init(){
        remindService = context.getSyncRemindService();
        mainService = context.getMainService();
    }

    public void add(Todo todo) {
        queue.add(todo);
        isNeedRun.set(true);
    }

    public Todo get() {
        return queue.remove();
    }

    public void stop() {
        isStop.set(true);
    }

    @Override
    public void run() {
        while (true) {
            if (isStop.get()) {
                break;
            }

            Todo todo = queue.poll();
            if (todo == null) {
                isNeedRun.set(false);
            }
            try {
                if (isNeedRun.get()) {
                    handle(todo);
                } else {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                if (todo != null) {
                    this.add(todo);
                }
                F.logger.error("未处理记录重新处理失败", e);
            }
        }
    }

    private void handle(Todo todo){
        Date remindTime = todo.getRemindTime();
        String remindId = todo.getRemindId();
        Date remoteRemindTime = todo.getRemoteRemindTime();

        try {
            if (remindTime != null) {
                if (remindId == null) {
                    addEvent(todo);
                } else {
                    if (remoteRemindTime == null) {
                        readEvent(todo);
                    } else {
                        if (!DateUtils.isSameInstant(remindTime, remoteRemindTime)) {
                            updateEvent(todo);
                        }
                    }
                }
            }
            deleteEvent(todo);
        } catch (Exception e) {
            this.add(todo);
            F.logger.info("再次失败,再次添加至队列",e);
        }
    }

    private void updateEvent(Todo todo) throws ParseException {
        Reminder reminder = new MicrosoftReminder(todo);
        Serializable id = remindService.modifyEvent(reminder);
        MicrosoftReminder resReminder = (MicrosoftReminder) remindService.readEvent(id);
        todo.setRemoteRemindTime(resReminder.getStart().toDate());
        mainService.updateWithoutEvent(todo);
    }

    private void readEvent(Todo todo) throws ParseException {
        MicrosoftReminder resReminder = (MicrosoftReminder) remindService.readEvent(todo.getRemindId());
        todo.setRemoteRemindTime(resReminder.getStart().toDate());
        mainService.updateWithoutEvent(todo);
    }

    private void addEvent(Todo todo) throws ParseException {
        Reminder reminder = new MicrosoftReminder(todo);
        Serializable id = remindService.addEvent(reminder);
        todo.setRemindId((String) id);
        mainService.updateWithoutEvent(todo);
        MicrosoftReminder resReminder = (MicrosoftReminder) remindService.readEvent(id);
        todo.setRemoteRemindTime(resReminder.getStart().toDate());
        mainService.updateWithoutEvent(todo);
    }

    private void deleteEvent(Todo todo) {
        Reminder reminder = new MicrosoftReminder(todo);
        remindService.removeEvent(reminder);
    }
}
