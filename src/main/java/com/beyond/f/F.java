package com.beyond.f;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class F {
    public final static Logger logger = LogManager.getLogger();
    public static final String USERNAME = "xxx";
    public static final String PASSWORD = "xxx";
    public static final String DEFAULT_LOCAL_PATH = "./repository/documents.xml";
    public static final String DEFAULT_DELETE_PATH = "./repository/deletedDocuments.xml";
    public static final String DEFAULT_TMP_PATH = "./repository/tmpDocuments.xml";
    public static final String DEFAULT_REMOTE_PATH = "https://yura.teracloud.jp/dav/NoteCloud/repository/documents.xml";
    public static final long SYNC_PERIOD = 10*1000;
}
