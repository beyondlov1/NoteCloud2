package com.beyond.entity;

import java.util.Date;

public class Todo extends Document {
    private Date remindTime;

    public Date getRemindTime() {
        return remindTime;
    }

    public void setRemindTime(Date remindTime) {
        this.remindTime = remindTime;
    }
}
