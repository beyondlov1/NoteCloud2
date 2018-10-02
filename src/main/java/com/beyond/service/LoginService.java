package com.beyond.service;

import com.beyond.RemoteBase;
import com.beyond.entity.User;
import com.beyond.f.F;
import com.beyond.repository.RemoteDocumentRepository;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.BasicCredentialsProvider;

/**
 * 登陆服务
 */
public class LoginService extends RemoteBase {

    public User login(User user){
        HttpGet httpGet = new HttpGet(F.DEFAULT_REMOTE_PATH);
        CloseableHttpResponse response = sendRequest(getClient(), httpGet);
        String contentFromResponse = getContentFromResponse(response);
        System.out.println(contentFromResponse);
        return user;
    }
}
