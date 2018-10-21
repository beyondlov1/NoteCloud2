package com.beyond;

import com.beyond.callback.Callback;
import com.beyond.entity.*;
import com.beyond.f.F;
import com.beyond.repository.impl.RemoteReminderDao;
import com.beyond.service.*;
import com.beyond.utils.ListUtils;
import com.beyond.utils.SortUtils;
import com.beyond.utils.TimeUtils;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.*;

import static com.beyond.DocumentType.DOC;
import static com.beyond.DocumentType.NOTE;
import static com.beyond.DocumentType.TODO;


public class MainController extends Observable implements Observer {

    //添加组件
    @FXML
    private Text message;

    @FXML
    private TextField titleTextField;

    @FXML
    private TextArea contentTextAreaUpdate;

    @FXML
    private TextArea contentTextAreaSave;


    //展示组件
    @FXML
    private HBox container;

    @FXML
    private TableView<FxDocument> documentTableView;

    @FXML
    private TableView<FxDocument> deletedDocumentTableView;

    @FXML
    private WebView webView;

    @FXML
    private TabPane tabPane;

    @FXML
    private TableColumn<FxDocument, String> contentTableColumn;

    @FXML
    private TableColumn<FxDocument, String> deletedContentTableColumn;


    private ObservableList<FxDocument> fxDocumentList = null;
    private ObservableList<FxDocument> deletedFxDocumentList = null;

    private Timeline timeline;

    private MainService mainService;
    private BindService bindService;
    private ConfigService configService;
    //    private RemindServiceMix remindServiceMix;
    private AsynRemindService<Reminder> asynRemindService;

    private MainApplication application;
    private ApplicationContext context;

    public MainController(ApplicationContext context) {
        this.context = context;
    }

    @FXML
    private void initialize() {
        init();
    }

    private void init() {
        mainService = context.getMainService();
        bindService = context.getBindService();
        bindService.init(this);
        configService = context.getConfigService();
        asynRemindService = context.getAsynRemindService();
    }

    @FXML
    public void save(KeyEvent keyEvent) {
        String content = contentTextAreaSave.getText();
        Object source = keyEvent.getSource();

        //验证能否保存
        Document document;
        if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.S) {
            document = new Document();
            document.setContent(content);
        } else {
            document = validate(content);
        }
        if (document == null) return;
        if (StringUtils.isBlank(document.getContent())) {
            return;
        }

        Date curr = new Date();
        document.setId(UUID.randomUUID().toString().replace("-", ""));
        document.setCreateTime(curr);
        document.setLastModifyTime(curr);
        document.setVersion(1);
        document.setContent(F.CONTENT_PREFIX + document.getContent());

        mainService.add(document);

        //changeView
        if (source instanceof TextArea) {
            TextArea textArea = (TextArea) source;
            textArea.setText(null);
        }
        documentTableView.requestFocus();
        documentTableView.getSelectionModel().select(0);

        //设置提醒
        Todo todo = null;
        if (document instanceof Todo) todo = (Todo) document;
        if (todo == null) {
            setChanged();
            notifyObservers();
            refreshTable();
            refreshWebView();
            return;
        }
        Todo finalTodo = todo;
        asynRemindService.addEvent(new MicrosoftReminder(todo), new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                Serializable id = (Serializable) event.getSource().getValue();
                finalTodo.setRemindId((String) id);
                asynRemindService.readEvent(id, new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        MicrosoftReminder microsoftReminder = (MicrosoftReminder) event.getSource().getValue();
                        try {
                            finalTodo.setRemoteRemindTime(microsoftReminder.getStart().toDate());
                            mainService.update(finalTodo);
                            setChanged();
                            notifyObservers();
                            refreshTable();
                            refreshWebView();
                        } catch (ParseException e) {
                            F.logger.info(e.getMessage());
                        }
                    }
                });
            }
        });

        //设置提醒
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Todo todo = null;
//                if (document instanceof Todo) todo = (Todo) document;
//                if (todo == null) return;
//                remindServiceMix.addEvent(todo,new MicrosoftReminder(todo));
//                remindServiceMix.readEvent(todo);
//
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        refreshTable();
//                        refreshWebView();
//                    }
//                });
//            }
//        });
//        thread.start();
    }

    private Document validate(String content) {
        if (StringUtils.isNotBlank(content)) {
            int length = content.length();
            if (length > NOTE.getType().length() + 1 && content.endsWith(NOTE.getType() + "\n")) {
                String validContent = content.substring(0, length - NOTE.getType().length() - 1);
                Note note = new Note();
                note.setContent(validContent);
                return note;
            }
            if (length > TODO.getType().length() + 1 && content.endsWith(TODO.getType() + "\n")) {
                String validContent = content.substring(0, length - TODO.getType().length() - 1);

                Todo todo = new Todo();
                Date remindTime = TimeUtils.parse(validContent);
                if (remindTime != null) {
                    todo.setRemindTime(remindTime);
                }
                todo.setContent(validContent);
                return todo;
            }
            if (length > DOC.getType().length() + 1 && content.endsWith(DOC.getType() + "\n")) {
                String validContent = content.substring(0, length - DOC.getType().length() - 1);
                Document document = new Document();
                document.setContent(validContent);
                return document;
            }
        }
        return null;
    }

    @FXML
    public void modify(KeyEvent keyEvent) {
        String content = contentTextAreaUpdate.getText();
        Object source = keyEvent.getSource();

        FxDocument selectedDocument = documentTableView.getSelectionModel().getSelectedItem();
        int selectedIndex = documentTableView.getSelectionModel().getSelectedIndex();

        //验证能否保存
        Document document;
        if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.S) {
            document = selectedDocument.toNormalDocument();
            document.setContent(content);
        } else {
            document = validate(content);
        }
        if (document == null) return;
        if (StringUtils.isBlank(document.getContent())) {
            return;
        }
        //更新时会根据后缀重新定义类型
        document.setId(selectedDocument.getId());
        mainService.update(document);

        //更新事件
        Todo todo = null;
        if (document instanceof Todo) todo = (Todo) document;
        if (todo == null) {
            setChanged();
            notifyObservers();
            refreshTable();
            refreshWebView();
            return;
        }
        Todo finalTodo = todo;
        if (StringUtils.isBlank(todo.getRemindId())) {
            asynRemindService.addEvent(new MicrosoftReminder(todo), new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    Serializable id = (Serializable) event.getSource().getValue();
                    finalTodo.setRemindId((String) id);
                    asynRemindService.readEvent(id, new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            MicrosoftReminder microsoftReminder = (MicrosoftReminder) event.getSource().getValue();
                            try {
                                finalTodo.setRemoteRemindTime(microsoftReminder.getStart().toDate());
                                mainService.update(finalTodo);
                                setChanged();
                                notifyObservers();
                                refreshTable();
                                refreshWebView();
                            } catch (ParseException e) {
                                F.logger.info(e.getMessage());
                            }
                        }
                    });
                }
            });
        } else {
            asynRemindService.modifyEvent(new MicrosoftReminder(todo), new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    Serializable id = (Serializable) event.getSource().getValue();
                    asynRemindService.readEvent(id, new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            MicrosoftReminder microsoftReminder = (MicrosoftReminder) event.getSource().getValue();
                            try {
                                finalTodo.setRemoteRemindTime(microsoftReminder.getStart().toDate());
                                mainService.update(finalTodo);
                                setChanged();
                                notifyObservers();
                                refreshTable();
                                refreshWebView();
                            } catch (ParseException e) {
                                F.logger.info(e.getMessage());
                            }
                        }
                    });
                }
            });
        }

        //更新事件
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Todo todo = null;
//                if (document instanceof Todo) todo = (Todo) document;
//                if (todo == null) return;
//                if (StringUtils.isBlank(todo.getRemindId())) {
//                    remindServiceMix.addEvent(todo, new MicrosoftReminder(todo));
//                } else {
//                    remindServiceMix.updateEvent(todo);
//                }
//                remindServiceMix.readEvent(todo);
//
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        refreshTable();
//                        refreshWebView();
//                    }
//                });
//            }
//        });
//        thread.start();

        //changeView
        if (source instanceof TextArea) {
            TextArea textArea = (TextArea) source;
            textArea.setText(selectedDocument.getContent());
        }
        documentTableView.getSelectionModel().select(selectedIndex);
        documentTableView.requestFocus();
    }

    @FXML
    public void delete() {
        FxDocument selectedItem = documentTableView.getSelectionModel().getSelectedItem();
        String selectedId = selectedItem.getId();
        mainService.deleteById(selectedId);

        //删除提醒
        Todo todo = null;
        if (selectedItem.toNormalDocument() instanceof Todo) todo = (Todo) selectedItem.toNormalDocument();
        if (todo == null || todo.getRemindId() == null) {
            setChanged();
            notifyObservers();
            refreshTable();
            refreshWebView();
            return;
        }
        asynRemindService.removeEvent(todo.getRemindId(), new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                setChanged();
                notifyObservers();
                refreshTable();
                refreshWebView();
            }
        });

        //删除提醒
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Todo todo = null;
//                if (selectedItem.toNormalDocument() instanceof Todo) todo = (Todo) selectedItem.toNormalDocument();
//                if (todo == null || todo.getRemindId() == null) return;
//                remindServiceMix.deleteEvent(todo.getRemindId());
//
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        refreshTable();
//                        refreshWebView();
//                    }
//                });
//            }
//        });
//        thread.start();
    }

    @FXML
    public void openConfig() throws IOException {
        F.logger.info("open config");
        application.loadConfigView();
    }

    @FXML
    public void logout() throws IOException {
        F.logger.info("logout");
        //注销
        configService.setProperty("password", "");
        configService.storeProperties();

        //关闭当前页面
        application.getMainStage().close();

        //转到登录页面
        application.loadLoginView();
    }

    public void refreshTable() {
        FxDocument selectedItem = documentTableView.getSelectionModel().getSelectedItem();
        //从文件获取文档
        mainService.pull();
        mainService.setFxDocuments();
        ObservableList<FxDocument> fxDocuments = mainService.getFxDocuments();

        //order
        SortUtils.sort(fxDocuments, FxDocument.class, "lastModifyTime", SortUtils.SortType.DESC);

        //刷新
        documentTableView.setItems(fxDocuments);
        documentTableView.refresh();
        if (selectedItem != null) {
            documentTableView.getSelectionModel().select(ListUtils.getFxDocumentIndexById(fxDocuments, selectedItem.getId()));
        } else {
            if (!documentTableView.getItems().isEmpty()) {
                documentTableView.getSelectionModel().select(0);
            }
        }
        F.logger.info("refreshTable");
    }

    public void refreshWebView() {
        FxDocument selectedItem = documentTableView.getSelectionModel().getSelectedItem();
        bindService.initWebView(webView, selectedItem);
        F.logger.info("refreshWebView");
    }

    /**
     * 监控刷新
     *
     * @param o
     * @param arg
     */
    @Override
    public void update(Observable o, Object arg) {
        refreshTable();
        refreshWebView();
    }

    public Text getMessage() {
        return message;
    }

    public TextField getTitleTextField() {
        return titleTextField;
    }

    public TextArea getContentTextAreaUpdate() {
        return contentTextAreaUpdate;
    }

    public TextArea getContentTextAreaSave() {
        return contentTextAreaSave;
    }

    public HBox getContainer() {
        return container;
    }

    public TableView<FxDocument> getDocumentTableView() {
        return documentTableView;
    }

    public TableView<FxDocument> getDeletedDocumentTableView() {
        return deletedDocumentTableView;
    }

    public WebView getWebView() {
        return webView;
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    public TableColumn<FxDocument, String> getContentTableColumn() {
        return contentTableColumn;
    }

    public TableColumn<FxDocument, String> getDeletedContentTableColumn() {
        return deletedContentTableColumn;
    }

    public ObservableList<FxDocument> getFxDocumentList() {
        return fxDocumentList;
    }

    public ObservableList<FxDocument> getDeletedFxDocumentList() {
        return deletedFxDocumentList;
    }

    public void setApplication(MainApplication application) {
        this.application = application;
    }

    public MainApplication getApplication() {
        return application;
    }

}


