package com.beyond.f;

import com.beyond.service.ConfigService;
import com.beyond.service.impl.ConfigServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class F {
    public final static Logger logger = LogManager.getLogger();
    public static final ConfigService configService = new ConfigServiceImpl(F.CONFIG_PATH);

    public static final String CONTENT_PREFIX = "";
    public static final String DEFAULT_REMOTE_RELATIVE_PATH = "/NoteCloud/repository/documents.xml";
    public static final String DEFAULT_REMOTE_FILE_INFO_RELATIVE_PATH = "/NoteCloud/repository/remoteProperty.obj";

    public static String NOTE_SUFFIX = "";
    public static String TODO_SUFFIX = "";
    public static String DOC_SUFFIX = "";

    public static String USERNAME = "";
    public static String PASSWORD = "";

    public static String DEFAULT_LOCAL_PATH = "./repository/documents.xml";
    public static String DEFAULT_DELETE_PATH = "./repository/deletedDocuments.xml";
    public static String DEFAULT_TMP_PATH = "./repository/tmpDocuments.xml";
    public static String DEFAULT_REMOTE_PROPERTY_LOCAL_PATH = "./repository/remoteProperty.obj";

    //    public static String DEFAULT_LOGIN_PATH = "https://yura.teracloud.jp/dav/";
    //    public static String DEFAULT_REMOTE_PATH = "https://yura.teracloud.jp/dav/NoteCloud/repository/documents.xml";
    public static String DEFAULT_REMOTE_ROOT_PATH = "https://dav.jianguoyun.com/dav";
    public static String DEFAULT_LOGIN_PATH = "https://dav.jianguoyun.com/dav/NoteCloud";
    public static String DEFAULT_REMOTE_PATH = "https://dav.jianguoyun.com/dav/NoteCloud/repository/documents.xml";
    public static String DEFAULT_REMOTE_FILE_INFO_PATH = "https://dav.jianguoyun.com/dav/NoteCloud/repository/remoteProperty.obj";

    public static SyncType SYNC_TYPE = SyncType.LOOP;
    public static long SYNC_PERIOD = 10 * 1000;

    public static long VIEW_REFRESH_PERIOD = 5 * 1000;

    public static final String CONFIG_PATH = "config/config.properties";
    //microsoft api access
    public static final String CLIENT_ID = "b1c8c70e-daf3-4bc9-ae1e-50b0f348dd58";

    public static final String SCOPE = "openid Calendars.ReadWrite offline_access";
    public static final String MICROSOFT_EVENT_URL = "https://graph.microsoft.com/v1.0/me/events";
    public static String REFRESH_TOKEN = "";
    public static String EXPIRE_DATE = "";
    public static String ACCESS_TOKEN = "";

    public static String IS_FLOAT_PRIMARY = "false";

    public static long EXPIRE_TODO_DELETE_PERIOD = 1000 * 60;

    public static String FLOAT_POSITION = "left";  // windows left , linux right
}
