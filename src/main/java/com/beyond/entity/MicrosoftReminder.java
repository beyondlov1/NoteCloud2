package com.beyond.entity;

import com.beyond.utils.TimeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class MicrosoftReminder {
    private String subject;
    private ContentBody body = new ContentBody();
    private TimeUnit start = new TimeUnit();
    private TimeUnit end = new TimeUnit();
    @JsonIgnore
    private String id;

    public MicrosoftReminder() {

    }

    public MicrosoftReminder(Todo todo) {
        subject = (StringUtils.isBlank(todo.getTitle()) ? todo.getContent() : todo.getTitle());
        body.setContentType("HTML");
        body.setContent(todo.getContent());
        start.setDateTime(TimeUtils.getDateStringForMicrosoftEvent(todo.getRemindTime(), "GMT+8:00"));
        start.setTimeZone("China Standard Time");
        end.setDateTime(TimeUtils.getDateStringForMicrosoftEvent(new Date(todo.getRemindTime().getTime() + 1000 * 60 * 60), "GMT+8:00"));
        end.setTimeZone("China Standard Time");
        id = (todo.getRemindId());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public class ContentBody {
        private String contentType;
        private String content;

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public class TimeUnit {
        private String dateTime;
        private String timeZone;

        public String getDateTime() {
            return dateTime;
        }

        public void setDateTime(String dateTime) {
            this.dateTime = dateTime;
        }

        public String getTimeZone() {
            return timeZone;
        }

        public void setTimeZone(String timeZone) {
            this.timeZone = timeZone;
        }
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public ContentBody getBody() {
        return body;
    }

    public void setBody(ContentBody body) {
        this.body = body;
    }

    public TimeUnit getStart() {
        return start;
    }

    public void setStart(TimeUnit start) {
        this.start = start;
    }

    public TimeUnit getEnd() {
        return end;
    }

    public void setEnd(TimeUnit end) {
        this.end = end;
    }
}
