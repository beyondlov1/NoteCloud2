package com.beyond.entity;

import java.io.Serializable;
import java.util.Date;

public interface Reminder {

    Serializable getId();

    void setId(Serializable id);

    String getEventId();

    void setEventId(String eventId);

    Date getRemindTime();

    void setRemindTime(Date remindTime);

    String getContent();

    void setContent(String body);

    Integer getFailCount();

    void setFailCount(Integer count);

    void setRemoteRemindTime(Date date);

    Date getRemoteRemindTime();
}
