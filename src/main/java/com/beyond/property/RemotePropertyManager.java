package com.beyond.property;

import com.beyond.RemoteBase;
import com.beyond.f.F;
import com.beyond.utils.Dom4jUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.client.methods.HttpPropfind;
import org.apache.jackrabbit.webdav.client.methods.HttpProppatch;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.dom4j.Node;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemotePropertyManager extends RemoteBase implements PropertyManager {

    private String path;

    public RemotePropertyManager(String path) {
        super();
        this.path = path;
    }

    @Override
    public synchronized void set(String key, String value) {
        DavPropertySet newProps = new DavPropertySet();
        DavProperty property = new DefaultDavProperty<Object>(key, value, DavConstants.NAMESPACE);
        newProps.add(property);
        DavPropertyNameSet removeProperties = new DavPropertyNameSet();

        CloseableHttpClient client = getClient();
        //发送请求
        try {
            HttpProppatch httpProppatch = new HttpProppatch(path, newProps, removeProperties);
            sendRequest(client,httpProppatch);
        } catch (IOException e) {
            e.printStackTrace();
            F.logger.info(e.getMessage());
        }finally {
            release(client);
        }

    }

    @Override
    public String getProperty(String key) {
        String result = "";
        DavPropertyNameSet set = new DavPropertyNameSet();
        set.add(key, DavConstants.NAMESPACE);

        CloseableHttpClient client = getClient();
        try {
            HttpPropfind httpPropfind = new HttpPropfind(path, DavConstants.PROPFIND_BY_PROPERTY, set, DavConstants.DEPTH_INFINITY);
            CloseableHttpResponse response = sendRequest(client,httpPropfind);
            String content = getContentFromResponse(response);
            result = Dom4jUtils.getNodeText(content, "//" + DavConstants.NAMESPACE.getPrefix() + ":" + key);
        } catch (IOException e) {
            e.printStackTrace();
            F.logger.info(e.getMessage());
        }finally {
            release(client);
        }
        return result;
    }

    @Override
    public synchronized void batchSet(Map<String, String> map) {

        DavPropertySet propertySet = new DavPropertySet();
        for (String key : map.keySet()) {
            String value = map.get(key);
            DavProperty property = new DefaultDavProperty<Object>(key, value, DavConstants.NAMESPACE);
            propertySet.add(property);
        }
        DavPropertyNameSet removeProperties = new DavPropertyNameSet();

        CloseableHttpClient client = getClient();
        //发送请求
        try {
            HttpProppatch httpProppatch = new HttpProppatch(path, propertySet, removeProperties);
            sendRequest(client,httpProppatch);
        } catch (IOException e) {
            e.printStackTrace();
            F.logger.info(e.getMessage());
        }finally {
            release(client);
        }

    }

    @Override
    public Map<String, String> getAllProperties() {
        Map<String, String> result = new HashMap<>();
        CloseableHttpClient client = getClient();
        try {
            HttpPropfind httpPropfind = new HttpPropfind(path, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_0);
            CloseableHttpResponse response = sendRequest(client,httpPropfind);
            String content = getContentFromResponse(response);
            if (StringUtils.isNotBlank(content)) {
                List<Node> allNode = Dom4jUtils.getAllNode(content);
                for (Node node : allNode) {
                    String name = node.getName();
                    if (name.startsWith("_")) {
                        result.put(name, node.getText());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            F.logger.info(e.getMessage());
        }finally {
            release(client);
        }
        return result;
    }

    public static void main(String[] args) {
        RemotePropertyManager remotePropertyManager = new RemotePropertyManager("https://yura.teracloud.jp/dav/NoteCloud/repository/documents.xml");
//        remotePropertyManager.set("_version","2");
//        String version = remotePropertyManager.getProperty("_version");
//        System.out.println(version);

        Map<String, String> allProperties = remotePropertyManager.getAllProperties();
        System.out.println(allProperties);
    }
}
