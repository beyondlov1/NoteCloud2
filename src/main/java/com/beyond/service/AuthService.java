package com.beyond.service;

import com.beyond.MainApplication;
import com.beyond.f.F;
import com.beyond.libext.MicrosoftAzureActiveDirectoryApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class AuthService {

    private OAuth20Service oAuth20Service;

    private MainApplication application;

    public AuthService() {
        MicrosoftAzureActiveDirectoryApi20 api = MicrosoftAzureActiveDirectoryApi20.instance();
        this.oAuth20Service = new ServiceBuilder(F.CLIENT_ID)
                .scope(F.SCOPE)
                .callback("https://login.microsoftonline.com/common/oauth2/nativeclient")
                .build(api);
    }

    public String getAccessToken() {
        try {
            long curr = new Date().getTime();
            if (StringUtils.isNotBlank(F.EXPIRE_DATE) && Long.valueOf(F.EXPIRE_DATE) > curr && StringUtils.isNotBlank(F.ACCESS_TOKEN)) {
                return F.ACCESS_TOKEN;
            } else {
                //如果过期了就refresh
                String newAccessToken = refreshAccessToken(F.REFRESH_TOKEN);
                if (StringUtils.isNotBlank(newAccessToken)) {
                    F.ACCESS_TOKEN = newAccessToken;
                    F.configService.setProperty("accessToken",F.ACCESS_TOKEN);
                    F.configService.storeProperties();
                    return newAccessToken;
                }

                //如果refresh_token也过期了,重新获取
                application.loadMicrosoftAuth();
                return "";
            }
        } catch (Exception e) {
            F.logger.info(e.getMessage());
            return "";
        }
    }

    public String getAccessToken(String code) {
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

            return F.ACCESS_TOKEN;
        } catch (Exception e) {
            F.logger.info(e.getMessage());
            return "";
        }
    }

    private String refreshAccessToken(String refreshToken) throws InterruptedException, ExecutionException, IOException {
        return oAuth20Service.refreshAccessToken(refreshToken).getAccessToken();
    }

    public String getAuthorizationUrl() {
        return oAuth20Service.getAuthorizationUrl();
    }

    public OAuth20Service getoAuth20Service() {
        return oAuth20Service;
    }

    public void setApplication(MainApplication application) {
        this.application = application;
    }
}
