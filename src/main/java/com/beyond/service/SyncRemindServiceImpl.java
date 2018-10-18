package com.beyond.service;

import com.beyond.entity.MicrosoftReminder;
import com.beyond.entity.Reminder;
import com.beyond.repository.ReminderDao;
import com.beyond.repository.impl.RemoteReminderDao;

import java.io.Serializable;
import java.util.Collection;

public class SyncRemindServiceImpl implements SyncRemindService<Reminder>{

    private ReminderDao<Reminder> reminderDao;

    public SyncRemindServiceImpl(){
        this.reminderDao = new RemoteReminderDao(new AuthService());
    }

    public SyncRemindServiceImpl(ReminderDao<Reminder> reminderDao){
        this.reminderDao = reminderDao;
    }

    @Override
    public Serializable addEvent(Reminder reminder) {
        return reminderDao.add(reminder);
    }

    @Override
    public Serializable modifyEvent(Reminder reminder) {
        return reminderDao.update(reminder);
    }

    @Override
    public Serializable removeEvent(Reminder reminder) {
        return reminderDao.delete(reminder);
    }

    @Override
    public Serializable removeEvent(Serializable id) {
        return reminderDao.delete(id);
    }

    @Override
    public Reminder readEvent(Serializable id) {
        return reminderDao.select(id);
    }

    @Override
    public Collection<Reminder> readAllEvent() {
        return reminderDao.selectAll();
    }
}
