package com.beyond.repository.impl;

import com.beyond.repository.AbstractLocalRepository;
import com.thoughtworks.xstream.XStream;
import com.beyond.entity.Document;
import com.beyond.entity.Note;
import com.beyond.entity.Todo;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;

public class LocalDocumentRepository extends AbstractLocalRepository<Document> {

    public LocalDocumentRepository(String path) {
        super(path);
    }

    public LocalDocumentRepository(String path, XStream xStream) {
        super(path, xStream);
    }

    public XStream getXStream() {
        //bind xStream
        XStream xStream = new XStream();
        xStream.alias("document", Document.class);
        xStream.alias("note", Note.class);
        xStream.alias("todo", Todo.class);
        xStream.alias("documents", List.class);
        xStream.useAttributeFor(Document.class, "id");
        xStream.useAttributeFor(Note.class, "id");
        xStream.useAttributeFor(Todo.class, "id");
        xStream.autodetectAnnotations(true);
        return xStream;
    }

    public synchronized Serializable update(Document document) {
        int index = -1;
        Document foundDocument= null;
        for (int i = 0; i < list.size(); i++) {
            foundDocument = list.get(i);
            if (StringUtils.equals(document.getId(), foundDocument.getId())) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            document.setTitle(StringUtils.isNotBlank(document.getTitle())?document.getTitle():foundDocument.getTitle());
            document.setContent(StringUtils.isNotBlank(document.getContent())?document.getContent():foundDocument.getContent());
            document.setVersion(foundDocument.getVersion()+1);
            document.setCreateTime(foundDocument.getCreateTime());
            document.setLastModifyTime(new Date());
            document.setType(StringUtils.isNotBlank(document.getType())?document.getType():foundDocument.getType());
            list.set(index,document);
            return document.getId();
        } else {
            return null;
        }
    }

}
