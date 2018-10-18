package com.beyond.service;

import com.beyond.RemoteBase;
import com.beyond.entity.User;
import com.beyond.f.F;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * 登陆服务
 */
public class LoginService extends RemoteBase {

    public User login(User user) {
        HttpHead httpHead = new HttpHead(F.DEFAULT_REMOTE_ROOT_PATH);
        CloseableHttpClient client = getClient(user);
        CloseableHttpResponse response = sendRequest(client, httpHead);
        StatusLine statusLine = response.getStatusLine();
        F.logger.info(statusLine);
        this.release(client);
        if (statusLine.getStatusCode() > 400 && statusLine.getStatusCode()<500){
            return null;
        }else {
            F.USERNAME = user.getUsername();
            F.PASSWORD = user.getPassword();
            return user;
        }
    }
}
