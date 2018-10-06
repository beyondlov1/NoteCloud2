package com.beyond.libext;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;

public class MicrosoftAzureActiveDirectoryService20 extends OAuth20Service {

    private static final String ACCEPTED_FORMAT
            = "application/json; odata.metadata=minimal; odata.streaming=true; charset=utf-8";

    public MicrosoftAzureActiveDirectoryService20(DefaultApi20 api, String apiKey, String apiSecret, String callback,
                                                String scope, String state, String responseType, String userAgent, HttpClientConfig httpClientConfig,
                                                HttpClient httpClient) {
        super(api, apiKey, apiSecret, callback, scope, state, responseType, userAgent, httpClientConfig, httpClient);
    }

    @Override
    public void signRequest(String accessToken, OAuthRequest request) {
        super.signRequest(accessToken, request);
        request.addHeader("Accept", ACCEPTED_FORMAT);
    }
}
