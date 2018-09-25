package com.beyond.utils;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.dom4j.*;

import java.util.*;

public class Dom4jUtils {

    public static Node getNode(String xmlString, Map<String, String> namespaceUris, String xPath) {
        try {
            Document document = DocumentHelper.parseText(xmlString);
            XPath xpath = document.createXPath(xPath);
            if (namespaceUris != null && !namespaceUris.isEmpty())
                xpath.setNamespaceURIs(namespaceUris);
            return xpath.selectSingleNode(document);
        } catch (DocumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getNodeText(String xmlString, Map<String, String> namespaceUris, String xPath) {
        Node node = getNode(xmlString, namespaceUris, xPath);
        if (node != null) {
            return node.getText();
        }
        return null;
    }



    /**
     * 获取所有节点（所有）
     *
     * @param xmlString
     * @return
     */
    public static List<Node> getAllNode(String xmlString) {
        List<Node> list = new ArrayList<>();
        try {
            Document document = DocumentHelper.parseText(xmlString);
            Element rootElement = document.getRootElement();
            getChildElement(rootElement, list);
            return list;
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static void getChildElement(Element element, List<Node> list) {
        Iterator<Node> nodeIterator = element.nodeIterator();
        while (nodeIterator.hasNext()) {
            Node next = nodeIterator.next();
            if (next instanceof Element) {
                Element element1 = (Element) next;
                list.add(next);
                getChildElement(element1, list);
            }
        }
    }

    public static String getNodeText(String content, String xPath) {
        Map<String,String> namespaceUris = new HashMap<>();
        Namespace namespace = DavConstants.NAMESPACE;
        namespaceUris.put(namespace.getPrefix(),namespace.getURI());
        return getNodeText(content, namespaceUris, xPath);
    }
}
