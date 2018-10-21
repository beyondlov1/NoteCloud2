package com.beyond.service;

import com.beyond.RemoteBase;
import com.beyond.entity.User;
import com.beyond.f.F;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author beyondlov1
 * @date 2018/10/21
 */
public class LoginServiceNutStoreImpl extends RemoteBase implements LoginService {
    @Override
    public User login(User user) {
        HttpHead httpHead = new HttpHead(F.DEFAULT_LOGIN_PATH);
        CloseableHttpClient client = getClient(user);
        CloseableHttpResponse response = null;
        try {
            response = sendRequest(client, httpHead);
            StatusLine statusLine = response.getStatusLine();
            F.logger.info(statusLine);
            this.release(client);
            if (statusLine.getStatusCode()!=403) {
                return null;
            } else {
                F.USERNAME = user.getUsername();
                F.PASSWORD = user.getPassword();
                return user;
            }
        } catch (Exception e) {
            F.logger.info(e.getMessage());
            return null;
        }
    }
}
