package com.beyond.entity;

import com.beyond.utils.TimeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MicrosoftReminder implements Reminder {
    //通用
    @XStreamOmitField
    private String subject;
    @XStreamOmitField
    private ContentBody body = new ContentBody();
    @XStreamOmitField
    private TimeUnit start = new TimeUnit();
    @XStreamOmitField
    private TimeUnit end = new TimeUnit();

    //JSON不序列化
    private String id;
    private String eventId;
    @XStreamOmitField
    private String context;
    @XStreamOmitField
    private String idz;
    @XStreamOmitField
    private String etag;

    private Date remindTime;

    private Date remoteRemindTime;

    private Integer failCount;

    public MicrosoftReminder() {

    }

    public MicrosoftReminder(Todo todo) {
        if (todo.getReminder().getRemindTime() == null) return;
        subject = (StringUtils.isBlank(todo.getTitle()) ? todo.getContent() : todo.getTitle());
        body.setContentType("HTML");
        body.setContent(todo.getContent());
        start.setDateTime(TimeUtils.getDateStringForMicrosoftEvent(todo.getReminder().getRemindTime(), "GMT+8:00"));
        start.setTimeZone("China Standard Time");
        end.setDateTime(TimeUtils.getDateStringForMicrosoftEvent(new Date(todo.getReminder().getRemindTime().getTime() + 1000 * 60 * 60), "GMT+8:00"));
        end.setTimeZone("China Standard Time");
        id = (String) todo.getReminder().getId();
        eventId = todo.getReminder().getEventId();
    }

    public static void main(String[] args) throws IOException {
        Todo todo = new Todo();
        todo.setContent("明天上午九点");
        todo.getReminder().setRemindTime(new Date());
        Reminder reminder = new MicrosoftReminder(todo);
        reminder.setEventId("11232123123");
        ObjectMapper objectMapper = new ObjectMapper();
        String load = objectMapper.writeValueAsString(reminder);
        System.out.println(load);

        String response = "{\"subject\":\"明天上午九点\",\"body\":{\"contentType\":\"HTML\",\"content\":\"明天上午九点\"},\"start\":{\"dateTime\":\"2018-11-29T10:30:12\",\"timeZone\":\"China Standard Time\"},\"end\":{\"dateTime\":\"2018-11-29T11:30:12\",\"timeZone\":\"China Standard Time\"},\"remindTime\":1543487412000,\"@odata.context\":1232,\"@odata.id\":null,\"@odata.etag\":null,\"id\":\"123\"}\n";
        MicrosoftReminder microsoftReminder = objectMapper.readValue(response, MicrosoftReminder.class);
        System.out.println(microsoftReminder.getEventId());


        XStream xStream = new XStream();
        xStream.alias("document", Document.class);
        xStream.alias("note", Note.class);
        xStream.alias("todo", Todo.class);
        xStream.alias("documents", List.class);
        xStream.useAttributeFor(Document.class, "id");
        xStream.useAttributeFor(Note.class, "id");
        xStream.useAttributeFor(Todo.class, "id");
        xStream.autodetectAnnotations(true);

        todo.getReminder().setFailCount(10);
        String s = xStream.toXML(todo.getReminder());
        System.out.println(s);
    }

    @JsonIgnore
    public Date getRemoteRemindTime() {
        return remoteRemindTime;
    }

    @JsonIgnore
    public void setRemoteRemindTime(Date remoteRemindTime) {
        this.remoteRemindTime = remoteRemindTime;
    }

    @JsonIgnore
    public String getContext() {
        return context;
    }

    @JsonProperty("@odata.context")
    public void setContext(String context) {
        this.context = context;
    }

    @JsonIgnore
    public String getIdz() {
        return idz;
    }

    @JsonProperty("@odata.id")
    public void setIdz(String idz) {
        this.idz = idz;
    }

    @JsonIgnore
    public String getEtag() {
        return etag;
    }

    @JsonProperty("@odata.etag")
    public void setEtag(String etag) {
        this.etag = etag;
    }

    @JsonIgnore
    public String getId() {
        return id;
    }

    @JsonIgnore
    public void setId(Serializable id) {
        this.id = (String) id;
    }

    @JsonIgnore
    public String getEventId() {
        return eventId;
    }

    @JsonProperty("id")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @JsonIgnore
    public Date getRemindTime() {
        return remindTime;
    }

    @JsonIgnore
    public void setRemindTime(Date remindTime) {
        this.remindTime = remindTime;
        if (start == null) {
            start  = new TimeUnit();
        }
        start.setDateTime(TimeUtils.getDateStringForMicrosoftEvent(remindTime, "GMT+8:00"));
    }

    @JsonIgnore
    public String getContent() {
        return getBody().getContent();
    }

    @JsonIgnore
    public void setContent(String content) {
        ContentBody contentBody = new ContentBody();
        contentBody.setContent(content);
        contentBody.setContentType("HTML");
        setBody(contentBody);
    }

    @JsonIgnore
    public Integer getFailCount() {
        return failCount;
    }

    @JsonIgnore
    public void setFailCount(Integer count) {
        this.failCount = count;
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
}
