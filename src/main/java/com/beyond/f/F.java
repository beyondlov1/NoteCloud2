package com.beyond.f;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class F {
    public final static Logger logger = LogManager.getLogger();
    public static String USERNAME = "";
    public static String PASSWORD = "";
    public static String DEFAULT_LOCAL_PATH = "./repository/documents.xml";
    public static String DEFAULT_DELETE_PATH = "./repository/deletedDocuments.xml";
    public static String DEFAULT_TMP_PATH = "./repository/tmpDocuments.xml";
    public static String DEFAULT_REMOTE_PATH = "https://yura.teracloud.jp/dav/NoteCloud/repository/documents.xml";
    public static long SYNC_PERIOD = 10 * 1000;
    public static long VIEW_REFRESH_PERIOD = 5 * 1000;
}
