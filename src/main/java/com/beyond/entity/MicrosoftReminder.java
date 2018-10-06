package com.beyond.entity;

import java.util.Date;

public class MicrosoftReminder {
    private String subject;
    private ContentBody body = new ContentBody();
    private TimeUnit start = new TimeUnit();
    private TimeUnit end = new TimeUnit();

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

    public class TimeUnit{
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
