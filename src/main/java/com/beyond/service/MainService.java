package com.beyond.service;

import com.beyond.ApplicationContext;
import com.beyond.FailedTodoService;
import com.beyond.entity.*;
import com.beyond.RepositoryFactory;
import com.beyond.f.F;
import com.beyond.repository.Repository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class MainService {
    private Repository<Document> defaultLocalRepository;
    private Repository<Document> deletedLocalRepository;

    private AsynRemindService<Reminder> asynRemindService;
    private FailedTodoService failedTodoService;

    private ObservableList<FxDocument> fxDocuments;

    private ApplicationContext context;

    @SuppressWarnings("unchecked")
    public MainService(AsynRemindService asynRemindService) {
        this.asynRemindService = asynRemindService;
        this.defaultLocalRepository = RepositoryFactory.getLocalRepository(F.DEFAULT_LOCAL_PATH);
        this.deletedLocalRepository = RepositoryFactory.getLocalRepository(F.DEFAULT_DELETE_PATH);
        initFxDocument();
    }

    @SuppressWarnings("unchecked")
    public MainService(@NotNull ApplicationContext context) {
        this.context = context;
        this.defaultLocalRepository = RepositoryFactory.getLocalRepository(F.DEFAULT_LOCAL_PATH);
        this.deletedLocalRepository = RepositoryFactory.getLocalRepository(F.DEFAULT_DELETE_PATH);
        initFxDocument();
    }

    public void init(){
        this.asynRemindService = context.getAsynRemindService();
        this.failedTodoService = context.getFailedTodoService();
    }

    @SuppressWarnings("unchecked")
    public MainService(@NotNull ApplicationContext context,String path) {
        this.context = context;
        this.asynRemindService = context.getAsynRemindService();
        this.defaultLocalRepository = RepositoryFactory.getLocalRepository(path);
        this.deletedLocalRepository = RepositoryFactory.getLocalRepository(F.DEFAULT_DELETE_PATH);
        initFxDocument();
    }

    public void initFxDocument() {
        List<FxDocument> fxDocuments = new ArrayList<>();
        List<Document> documents = findAll();
        for (Document document : documents) {
            FxDocument fxDocument = new FxDocument(document);
            fxDocuments.add(fxDocument);
        }
        this.fxDocuments = FXCollections.observableList(fxDocuments);
    }

    public String add(Document document) {
        Serializable id = addWithoutEvent(document);
        if (document instanceof Todo) {
            Todo todo = (Todo) document;
            addEvent(todo);
        }
        return (String) id;
    }
    private Serializable addWithoutEvent(Document document) {
        defaultLocalRepository.lock();
        Serializable id = defaultLocalRepository.add(document);
        defaultLocalRepository.save();
        defaultLocalRepository.unlock();

        //同步fxDocument
        FxDocument fxDocument = new FxDocument(document);
        fxDocuments.add(0, fxDocument);
        return id;
    }
    private void addEvent(Todo todo) {
        asynRemindService.addEvent(new MicrosoftReminder(todo), new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                F.logger.info("add event");
                Serializable id = (Serializable) event.getSource().getValue();
                todo.getReminder().setEventId((String) id);
                updateWithoutEvent(todo);
                readEvent(todo);
            }
        }, new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                F.logger.error("add event fail");
                failedTodoService.add(todo);
                F.logger.info("添加至队列");
            }
        });
    }

    public void delete(Document document) {
        deleteById(document.getId());

        if (document instanceof Todo) {
            Todo todo = (Todo) document;
            deleteEvent(todo);
        }
    }
    private void deleteEvent(@NotNull Todo todo) {
        asynRemindService.removeEvent(todo.getReminder().getEventId(), new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                F.logger.info("delete event");
            }
        }, new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                F.logger.error("delete event fail");
                failedTodoService.add(todo);
                F.logger.info("添加至队列");
            }
        });
    }
    private void deleteById(String id) {
        Document document = new Document();
        document.setId(id);

        Document foundDocument = defaultLocalRepository.select(id);

        if (foundDocument == null) {
            return;
        }

        defaultLocalRepository.lock();
        defaultLocalRepository.delete(foundDocument);
        defaultLocalRepository.save();
        defaultLocalRepository.unlock();

        deletedLocalRepository.lock();
        deletedLocalRepository.add(foundDocument);
        deletedLocalRepository.save();
        deletedLocalRepository.unlock();

        //同步fxDocument
        int index = -1;
        for (int i = 0; i < fxDocuments.size(); i++) {
            if (StringUtils.equals(fxDocuments.get(i).getId(), id)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            fxDocuments.remove(index);
        }
    }
    public void deleteWithoutEvent(Document document){
        deleteById(document.getId());
    }
    public void bulkDeleteWithoutEvent(List<? extends Document> documents){
        defaultLocalRepository.lock();
        defaultLocalRepository.pull();
        for (Document document : documents) {
            defaultLocalRepository.delete(document);
        }
        defaultLocalRepository.save();
        defaultLocalRepository.unlock();
    }

    public String update(Document document) {
        Serializable id = updateWithoutEvent(document);
        if (document instanceof Todo) {
            Todo todo = (Todo) document;
            addOrUpdateEvent(todo);
        }
        return (String) id;
    }
    private void addOrUpdateEvent(@NotNull Todo todo) {
        if (StringUtils.isNotBlank(todo.getReminder().getEventId())) {
            updateEvent(todo);
        } else {
            addEvent(todo);
        }
    }
    private void updateEvent(Todo todo) {
        asynRemindService.modifyEvent(new MicrosoftReminder(todo), new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                F.logger.error("update event");
                Serializable id = (Serializable) event.getSource().getValue();
                todo.getReminder().setEventId((String)id);
                updateWithoutEvent(todo);
                readEvent(todo);
            }
        }, new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                F.logger.error("update event fail");
                failedTodoService.add(todo);
                F.logger.info("添加至队列");
            }
        });
    }
    public Serializable updateWithoutEvent(Document document) {
        defaultLocalRepository.lock();
        Serializable id = defaultLocalRepository.update(document);
        defaultLocalRepository.save();
        defaultLocalRepository.unlock();

        //同步fxDocument
        int index = -1;
        for (int i = 0; i < fxDocuments.size(); i++) {
            if (StringUtils.equals(fxDocuments.get(i).getId(), document.getId())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            fxDocuments.set(index, new FxDocument(document));
        }

        context.refresh();
        return id;
    }

    private List<Document> findAll() {
        defaultLocalRepository.pull();
        return defaultLocalRepository.selectAll();
    }
    private void readEvent(@NotNull Todo todo) {
        asynRemindService.readEvent(todo.getReminder().getEventId(), new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                F.logger.info("read event");
                MicrosoftReminder microsoftReminder = (MicrosoftReminder) event.getSource().getValue();
                try {
                    todo.getReminder().setRemoteRemindTime(microsoftReminder.getStart().toDate());
                    updateWithoutEvent(todo);
                } catch (ParseException e) {
                    F.logger.info(e.getMessage());
                }
            }
        }, new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                F.logger.error("read event fail");
                failedTodoService.add(todo);
                F.logger.info("添加至队列");
            }
        });
    }

    public ObservableList<FxDocument> getFxDocuments() {
        return fxDocuments;
    }

    public void pull() {
        defaultLocalRepository.pull();
    }

    public List<Document> findAllFromCache() {
        return defaultLocalRepository.selectAll();
    }
}
