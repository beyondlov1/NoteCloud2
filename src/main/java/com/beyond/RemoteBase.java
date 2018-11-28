package com.beyond;

import com.beyond.entity.User;
import com.beyond.f.F;
import org.apache.commons.lang3.StringUtils;
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
import org.apache.jackrabbit.webdav.client.methods.HttpMkcol;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 远程服务基类
 * 提供基本的远程连接方法
 */
public class RemoteBase {

    protected CredentialsProvider getCredentialsProvider(User user) {
        //初始化登陆
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user.getUsername(), user.getPassword());
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
        return credentialsProvider;
    }

    protected CloseableHttpClient getClient() {
        //initClient
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setDefaultCredentialsProvider(getCredentialsProvider(new User(F.USERNAME, F.PASSWORD)));
        return builder.build();
    }

    protected CloseableHttpClient getClient(CredentialsProvider credentialsProvider) {
        //initClient
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setDefaultCredentialsProvider(credentialsProvider);
        return builder.build();
    }

    protected CloseableHttpClient getClient(User user) {
        //initClient
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setDefaultCredentialsProvider(getCredentialsProvider(user));
        return builder.build();
    }

    protected synchronized CloseableHttpResponse sendRequest(CloseableHttpClient client, HttpRequestBase request) throws IOException {
        //sendRequest
        CloseableHttpResponse response = null;
        response = client.execute(request);
        return response;
    }

    protected void release(CloseableHttpClient client) {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected String getContentFromResponse(CloseableHttpResponse response) {
        if (response == null) {
            return "";
        }
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

    protected int mkRemoteDir(String url) throws IOException {
        CloseableHttpClient client = getClient();
        //获取文件夹路径
        int index = StringUtils.lastIndexOf(url, "/");
        String parentUrl = StringUtils.substring(url, 0, index);

        HttpMkcol httpMkcol = new HttpMkcol(parentUrl);
        String root = "https://" + URI.create(url).getHost();
        if (!StringUtils.equalsIgnoreCase(parentUrl, root)) {
            mkRemoteDir(parentUrl);
        }
        CloseableHttpResponse closeableHttpResponse = sendRequest(client, httpMkcol);
        int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
        release(client);
        return statusCode;
    }

    protected void allowMethods(String... methods) {
        try {
            Field methodsField = HttpURLConnection.class.getDeclaredField("methods");

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(methodsField, methodsField.getModifiers() & ~Modifier.FINAL);

            methodsField.setAccessible(true);

            String[] oldMethods = (String[]) methodsField.get(null);
            Set<String> methodsSet = new LinkedHashSet<>(Arrays.asList(oldMethods));
            methodsSet.addAll(Arrays.asList(methods));
            String[] newMethods = methodsSet.toArray(new String[0]);

            methodsField.set(null/*static field*/, newMethods);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
