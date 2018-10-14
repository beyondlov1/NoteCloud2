package com.beyond.service;

import com.beyond.entity.MicrosoftReminder;
import com.beyond.entity.Todo;
import com.beyond.f.F;
import com.beyond.utils.TimeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class RemindServiceImpl {

    private AuthService authService;

    private MainService mainService;

    public RemindServiceImpl(AuthService authService, MainService mainService) {
        this.authService = authService;
        this.mainService = mainService;
    }

    public String addEvent(Todo todo, MicrosoftReminder reminder) {
        String remindId = addRemoteEvent(reminder);
        String documentId =  addEventToDocument(todo,remindId);
        F.logger.info(documentId);
        return documentId;
    }

    private String addRemoteEvent(MicrosoftReminder reminder){
        OAuth20Service oAuth20Service = authService.getoAuth20Service();
        String id = null;
        try {
            final OAuthRequest request = new OAuthRequest(Verb.POST, F.MICROSOFT_EVENT_URL);
            ObjectMapper objectMapper = new ObjectMapper();
            String load =objectMapper.writeValueAsString(reminder);
            request.addHeader("Content-Type","application/json");
            request.setPayload(load);

            oAuth20Service.signRequest(authService.getAccessToken(), request);
            final Response response = oAuth20Service.execute(request);
            if (!response.isSuccessful()){
                F.logger.info(response.getCode());
                F.logger.info(response.getBody());
            }
            HashMap hashMap = objectMapper.readValue(response.getBody(), HashMap.class);
            id = (String) hashMap.get("id");
        }catch (Exception e){
            F.logger.info(e.getMessage());
        }
        return id;
    }

    private String addEventToDocument(Todo todo, String remindId){
        todo.setRemindId(remindId);
        return mainService.update(todo);
    }

    public String deleteEvent(Todo todo, String remindId){
        deleteRemoteEvent(remindId);
        todo.setRemindTime(null);
        todo.setRemindId(null);
        return mainService.update(todo);
    }

    public String deleteEvent(String remindId){
        return deleteRemoteEvent(remindId);
    }

    private String deleteRemoteEvent(String remindId){
        OAuth20Service oAuth20Service = authService.getoAuth20Service();
        try {
            final OAuthRequest request = new OAuthRequest(Verb.DELETE, F.MICROSOFT_EVENT_URL+"/"+remindId);
            oAuth20Service.signRequest(authService.getAccessToken(), request);
            final Response response = oAuth20Service.execute(request);
            if (!response.isSuccessful()){
                F.logger.info(response.getCode());
                F.logger.info(response.getBody());
            }
        }catch (Exception e){
            F.logger.info(e.getMessage());
        }
        return remindId;
    }

    public String updateEvent(Todo todo){
        updateEventToDocument(todo);
        updateRemoteEvent(new MicrosoftReminder(todo));
        return todo.getId();
    }
    private String updateEvent(Todo todo, MicrosoftReminder reminder){
        updateEventToDocument(todo);
        updateRemoteEvent(reminder);
        return todo.getId();
    }

    private String updateRemoteEvent(MicrosoftReminder reminder){
        allowMethods("PATCH");
        OAuth20Service oAuth20Service = authService.getoAuth20Service();
        try {
            final OAuthRequest request = new OAuthRequest(Verb.PATCH, F.MICROSOFT_EVENT_URL+"/"+reminder.getId());
            ObjectMapper objectMapper = new ObjectMapper();
            String load =objectMapper.writeValueAsString(reminder);
            request.addHeader("Content-Type","application/json");
            request.setPayload(load);
            oAuth20Service.signRequest(authService.getAccessToken(), request);
            final Response response = oAuth20Service.execute(request);
            if (!response.isSuccessful()){
                F.logger.info(response.getCode());
                F.logger.info(response.getBody());
            }
        }catch (Exception e){
            e.printStackTrace();
            F.logger.info(e.getMessage());
        }
        return reminder.getId();
    }

    private String updateEventToDocument(Todo todo){
        todo.setRemindTime(TimeUtils.parse(todo.getContent()));
        return mainService.update(todo);
    }

    public void readEvent(Todo todo){
        MicrosoftReminder reminder = readRemoteEvent(todo.getRemindId());
        try {
            todo.setRemoteRemindTime(reminder.getStart().toDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mainService.update(todo);
    }

    private MicrosoftReminder readRemoteEvent(String eventId){
        OAuth20Service oAuth20Service = authService.getoAuth20Service();
        MicrosoftReminder reminder = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            final OAuthRequest request = new OAuthRequest(Verb.GET, F.MICROSOFT_EVENT_URL+"/"+eventId+"?$select=subject,start,end");
            oAuth20Service.signRequest(authService.getAccessToken(), request);
            final Response response = oAuth20Service.execute(request);
            reminder = objectMapper.readValue(response.getBody(), MicrosoftReminder.class);
            if (!response.isSuccessful()){
                F.logger.info(response.getCode());
                F.logger.info(response.getBody());
            }
        }catch (Exception e){
            e.printStackTrace();
            F.logger.info(e.getMessage());
        }
        return reminder;
    }

    private void allowMethods(String... methods) {
        try {
            Field methodsField = HttpURLConnection.class.getDeclaredField("methods");

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(methodsField, methodsField.getModifiers() & ~Modifier.FINAL);

            methodsField.setAccessible(true);

            String[] oldMethods = (String[]) methodsField.get(null);
            Set<String> methodsSet = new LinkedHashSet<>(Arrays.asList(oldMethods));
            methodsSet.addAll(Arrays.asList(methods));
            String[] newMethods = methodsSet.toArray(new String[0]);

            methodsField.set(null/*static field*/, newMethods);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }


}
