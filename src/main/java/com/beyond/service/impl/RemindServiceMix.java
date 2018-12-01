package com.beyond.service.impl;

import com.beyond.entity.MicrosoftReminder;
import com.beyond.entity.Reminder;
import com.beyond.entity.Todo;
import com.beyond.f.F;
import com.beyond.repository.ReminderDao;
import com.beyond.repository.impl.RemoteReminderDao;
import com.beyond.service.AuthService;
import com.beyond.service.MainService;
import com.beyond.utils.TimeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

@Deprecated
public class RemindServiceMix {

    private MainService mainService;

    private ReminderDao<Reminder> reminderDao;

    public RemindServiceMix(AuthService authService, MainService mainService) {
        this.mainService = mainService;
        this.reminderDao = new RemoteReminderDao(authService);
    }

    public String addEvent(Todo todo, MicrosoftReminder reminder) {
        String remindId = (String) reminderDao.add(reminder);
        String documentId = addEventToDocument(todo,remindId);
        F.logger.info(documentId);
        return documentId;
    }

    private String addEventToDocument(Todo todo, String remindId){
        todo.getReminder().setEventId(remindId);
        return mainService.update(todo);
    }

    public String deleteEvent(String remindId){
        return (String) reminderDao.delete(remindId);
    }

    public String updateEvent(Todo todo){
        updateEventToDocument(todo);
        reminderDao.update(new MicrosoftReminder(todo));
        return todo.getId();
    }


    private String updateEventToDocument(Todo todo){
        todo.getReminder().setRemindTime(TimeUtils.parse(todo.getContent()));
        return mainService.update(todo);
    }

    public void readEvent(Todo todo){
        MicrosoftReminder reminder = (MicrosoftReminder) reminderDao.select(todo.getReminder().getEventId());
        try {
            todo.getReminder().setRemoteRemindTime(reminder.getStart().toDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mainService.update(todo);
    }

}
