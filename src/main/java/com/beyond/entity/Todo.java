package com.beyond.entity;

import com.beyond.utils.TimeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class Todo extends Document {
    private Date remindTime;
    private Date remoteRemindTime;
    private String remindId;
    private Reminder reminder;

    @Override
    public void setContent(String content) {
        super.setContent(content);
        Date remindTime = TimeUtils.parse(content);
        if (remindTime != null) {
            this.setRemindTime(remindTime);
        }
    }

    public Reminder getReminder() {
        return reminder;
    }

    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
    }

    public Date getRemoteRemindTime() {
        return remoteRemindTime;
    }

    public void setRemoteRemindTime(Date remoteRemindTime) {
        this.remoteRemindTime = remoteRemindTime;
    }

    public String getRemindId() {
        return remindId;
    }

    public void setRemindId(String remindId) {
        this.remindId = remindId;
    }

    public Date getRemindTime() {
        return remindTime;
    }

    public void setRemindTime(Date remindTime) {
        this.remindTime = remindTime;
    }
}
