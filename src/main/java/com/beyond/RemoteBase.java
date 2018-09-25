package com.beyond;

import com.beyond.f.F;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jackrabbit.webdav.client.methods.BaseDavRequest;

import java.io.IOException;
import java.io.InputStream;


public class RemoteBase {

    private CredentialsProvider credentialsProvider;

    public RemoteBase(){
        init();
    }

    protected void init() {
        //初始化登陆
        credentialsProvider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(F.USERNAME, F.PASSWORD);
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
    }

    protected CloseableHttpClient getClient(){
        //initClient
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setDefaultCredentialsProvider(credentialsProvider);
        return builder.build();
    }

    protected synchronized CloseableHttpResponse sendRequest(CloseableHttpClient client, HttpRequestBase request) {

        //sendRequest
        CloseableHttpResponse response = null;
        try {
            response = client.execute(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    protected void release(CloseableHttpClient client){
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected String getContentFromResponse(CloseableHttpResponse response) {
        StringBuilder stringBuilder = new StringBuilder();
        HttpEntity entity = response.getEntity();
        try {
            InputStream content = entity.getContent();
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = content.read(bytes)) != -1) {
                stringBuilder.append(new String(bytes, 0, len));
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
