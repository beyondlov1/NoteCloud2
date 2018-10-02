package com.beyond.service;

import com.beyond.RemoteBase;
import com.beyond.entity.User;
import com.beyond.f.F;
import com.beyond.property.PropertyManager;
import com.beyond.property.RemotePropertyManager;
import com.beyond.repository.RemoteDocumentRepository;
import com.beyond.utils.Dom4jUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.client.methods.HttpPropfind;
import org.dom4j.Node;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登陆服务
 */
public class LoginService extends RemoteBase {

    public User login(User user) {
        HttpHead httpHead = new HttpHead(F.DEFAULT_REMOTE_ROOT_PATH);
        CloseableHttpClient client = getClient(user);
        CloseableHttpResponse response = sendRequest(client, httpHead);
        StatusLine statusLine = response.getStatusLine();
        System.out.println(statusLine);
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
