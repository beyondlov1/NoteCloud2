package com.beyond.entity;

import com.beyond.utils.TimeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class Todo extends Document {
    private Date remindTime;

    private String remindId;

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
