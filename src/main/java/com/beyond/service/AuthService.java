package com.beyond.service;

import com.beyond.ApplicationContext;
import com.beyond.MainApplication;
import com.beyond.f.F;
import com.beyond.libext.MicrosoftAzureActiveDirectoryApi20;
import com.beyond.viewloader.AuthViewLoader;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClientConfig;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class AuthService {

    private ApplicationContext context;

    private OAuth20Service oAuth20Service;

    public AuthService() {
        MicrosoftAzureActiveDirectoryApi20 api = MicrosoftAzureActiveDirectoryApi20.instance();
        JDKHttpClientConfig jdkHttpClientConfig = new JDKHttpClientConfig();
        jdkHttpClientConfig.setConnectTimeout(8);
        jdkHttpClientConfig.setReadTimeout(8);
        HttpClient httpClient = new JDKHttpClient();
        this.oAuth20Service = new ServiceBuilder(F.CLIENT_ID)
                .httpClient(httpClient)
                .httpClientConfig(jdkHttpClientConfig)
                .scope(F.SCOPE)
                .callback("https://login.microsoftonline.com/common/oauth2/nativeclient")
                .build(api);
    }

    public AuthService(ApplicationContext context){
        this();
        this.context = context;
    }

    public String getAccessToken() throws IOException {
        try {
            long curr = new Date().getTime();
            if (StringUtils.isNotBlank(F.EXPIRE_DATE) && Long.valueOf(F.EXPIRE_DATE) > curr && StringUtils.isNotBlank(F.ACCESS_TOKEN)) {
                return F.ACCESS_TOKEN;
            } else {
                //如果过期了就refresh
                return refreshAccessToken(F.REFRESH_TOKEN);
            }
        } catch (Exception e) {
            F.logger.info(e.getMessage(),e);
            F.logger.info("current thread:"+Thread.currentThread().getName(),e);
            if (Thread.currentThread().getName().equals("JavaFX Application Thread")){
                context.loadView(AuthViewLoader.class);
            }
            throw new RuntimeException("get access token fail");
        }
    }

    public void readAccessToken(String code) {
        try {
            long curr = new Date().getTime();
            OAuth2AccessToken accessToken = oAuth20Service.getAccessToken(code);
            Integer expiresIn = accessToken.getExpiresIn();
            F.EXPIRE_DATE = String.valueOf(curr + expiresIn);
            F.ACCESS_TOKEN = accessToken.getAccessToken();
            F.REFRESH_TOKEN = accessToken.getRefreshToken();

            F.configService.setProperty("expireDate",F.EXPIRE_DATE);
            F.configService.setProperty("accessToken",F.ACCESS_TOKEN);
            F.configService.setProperty("refreshToken",F.REFRESH_TOKEN);
            F.configService.storeProperties();

        } catch (Exception e) {
            F.logger.info(e.getMessage());
        }
    }

    private String refreshAccessToken(String refreshToken) throws InterruptedException, ExecutionException, IOException {
        OAuth2AccessToken refreshAccessToken = oAuth20Service.refreshAccessToken(refreshToken);
        String newAccessToken = refreshAccessToken.getAccessToken();
        if (StringUtils.isNotBlank(newAccessToken)) {
            F.ACCESS_TOKEN = newAccessToken;
            F.configService.setProperty("accessToken",F.ACCESS_TOKEN);
            Integer expiresIn = refreshAccessToken.getExpiresIn();
            F.EXPIRE_DATE = String.valueOf(new Date().getTime()+expiresIn);
            F.configService.storeProperties();
        }else {
            throw new RuntimeException("refresh access fail");
        }
        return newAccessToken;
    }

    public String getAuthorizationUrl() {
        return oAuth20Service.getAuthorizationUrl();
    }

    public OAuth20Service getoAuth20Service() {
        return oAuth20Service;
    }

}
