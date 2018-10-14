package com.beyond.service;

import com.beyond.entity.MicrosoftReminder;
import com.beyond.f.F;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 未使用
 */
public class RemindServiceRemote implements RemindService {

    private AuthService authService;

    public RemindServiceRemote(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public String addEvent(MicrosoftReminder reminder) {
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
            return id;
        }catch (Exception e){
            F.logger.info(e.getMessage());
        }
        return null;
    }

    @Override
    public String deleteEvent(MicrosoftReminder reminder) {
        OAuth20Service oAuth20Service = authService.getoAuth20Service();
        try {
            final OAuthRequest request = new OAuthRequest(Verb.DELETE, F.MICROSOFT_EVENT_URL+"/"+reminder.getId());
            oAuth20Service.signRequest(authService.getAccessToken(), request);
            final Response response = oAuth20Service.execute(request);
            if (!response.isSuccessful()){
                F.logger.info(response.getCode());
                F.logger.info(response.getBody());
            }
            return reminder.getId();
        }catch (Exception e){
            F.logger.info(e.getMessage());
        }
        return null;
    }

    @Override
    public String deleteEvent(String eventId) {
        OAuth20Service oAuth20Service = authService.getoAuth20Service();
        try {
            final OAuthRequest request = new OAuthRequest(Verb.DELETE, F.MICROSOFT_EVENT_URL+"/"+eventId);
            oAuth20Service.signRequest(authService.getAccessToken(), request);
            final Response response = oAuth20Service.execute(request);
            if (!response.isSuccessful()){
                F.logger.info(response.getCode());
                F.logger.info(response.getBody());
            }
            return eventId;
        }catch (Exception e){
            F.logger.info(e.getMessage());
        }
        return null;
    }

    @Override
    public String updateEvent(MicrosoftReminder reminder) {
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
            return reminder.getId();
        }catch (Exception e){
            e.printStackTrace();
            F.logger.info(e.getMessage());
        }
        return null;
    }

    @Override
    public MicrosoftReminder readEvent(String eventId) {
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
