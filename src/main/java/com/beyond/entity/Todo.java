package com.beyond.entity;

import com.beyond.utils.TimeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class Todo extends Document {
    private Reminder reminder;

    public Todo(){
        reminder = new MicrosoftReminder();
    }

    @Override
    public void setContent(String content) {
        super.setContent(content);
        Date remindTime = TimeUtils.parse(content);
        if (remindTime != null) {
            reminder.setRemindTime(remindTime);
            this.setReminder(reminder);
        }
    }

    public Reminder getReminder() {
        return reminder;
    }

    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
    }

}
