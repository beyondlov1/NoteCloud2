package com.beyond.libext;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor;
import com.github.scribejava.core.extractors.TokenExtractor;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication;
import com.github.scribejava.core.oauth2.clientauthentication.RequestBodyAuthenticationScheme;

import java.io.OutputStream;

public class MicrosoftAzureActiveDirectoryApi20 extends DefaultApi20 {
    private static final String MSFT_GRAPH_URL = "https://graph.microsoft.com";

    private static final String MSFT_LOGIN_URL = "https://login.microsoftonline.com";
    private static final String SLASH = "/";
    private static final String COMMON = "common";
    private static final String TOKEN_URI = "oauth2/v2.0/token";
    private static final String AUTH_URI = "oauth2/v2.0/authorize";

    private static class InstanceHolder {

        private static final MicrosoftAzureActiveDirectoryApi20 INSTANCE = new MicrosoftAzureActiveDirectoryApi20();
    }

    public static MicrosoftAzureActiveDirectoryApi20 instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return MSFT_LOGIN_URL + SLASH + COMMON + SLASH + TOKEN_URI;
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return MSFT_LOGIN_URL + SLASH + COMMON + SLASH + AUTH_URI;
    }

    @Override
    public MicrosoftAzureActiveDirectoryService20 createService(String apiKey, String apiSecret, String callback,
                                                              String scope, OutputStream debugStream, String state, String responseType, String userAgent,
                                                              HttpClientConfig httpClientConfig, HttpClient httpClient) {
        return new MicrosoftAzureActiveDirectoryService20(this, apiKey, apiSecret, callback, scope, state, responseType,
                userAgent, httpClientConfig, httpClient);
    }

    @Override
    public ClientAuthentication getClientAuthentication() {
        return RequestBodyAuthenticationScheme.instance();
    }

    @Override
    public TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
        return OAuth2AccessTokenJsonExtractor.instance();
    }
}