package com.beyond.entity;

import com.beyond.utils.TimeUtils;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class MicrosoftReminder implements Reminder{
    private String subject;
    private ContentBody body = new ContentBody();
    private TimeUnit start = new TimeUnit();
    private TimeUnit end = new TimeUnit();
    @JsonIgnore
    private String id;

    @JsonProperty("@odata.context")
    private String context;

    @JsonProperty("@odata.id")
    private String idz;

    @JsonProperty("@odata.etag")
    private String etag;


    @JsonIgnore
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
    @JsonIgnore
    public String getIdz() {
        return idz;
    }

    public void setIdz(String idz) {
        this.idz = idz;
    }
    @JsonIgnore
    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

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

        public Date toDate() throws ParseException {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
            return simpleDateFormat.parse(dateTime);
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
