package com.beyond.service;

import com.beyond.entity.MicrosoftReminder;
import com.beyond.f.F;
import com.beyond.libext.MicrosoftAzureActiveDirectoryApi20;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class RemindService {

    private AuthService authService;

    public RemindService(AuthService authService) {
        this.authService = authService;
    }

    public void remind(MicrosoftReminder reminder) {
        OAuth20Service oAuth20Service = authService.getoAuth20Service();
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
        }catch (Exception e){
            F.logger.info(e.getMessage());
        }
    }

}
