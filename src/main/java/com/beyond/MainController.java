package com.beyond;

import com.beyond.entity.*;
import com.beyond.f.F;
import com.beyond.service.*;
import com.beyond.utils.ListUtils;
import com.beyond.utils.SortUtils;
import com.beyond.utils.TimeUtils;
import com.beyond.viewloader.ConfigViewLoader;
import com.beyond.viewloader.MainViewLoader;
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

    private MainService mainService;
    private BindService bindService;
    private ConfigService configService;
    private AsynRemindService<Reminder> asynRemindService;

    private ApplicationContext context;

    public MainController(ApplicationContext context) {
        this.context = context;
    }

    public void initialize() {
        initService();
    }

    private void initService() {
        mainService = context.getMainService();
        bindService = context.getBindService();
        bindService.init(this);
        configService = context.getConfigService();
        asynRemindService = context.getAsynRemindService();
    }

    public void save(KeyEvent keyEvent) {
        String content = getSaveContent();
        if (!isValid(content, keyEvent)) {
            return;
        }
        Document document = createDocument(content);
        mainService.add(document);
        changeViewAfterSave(keyEvent);

        if (document instanceof Todo) {
            Todo todo = (Todo) document;
            addEvent(todo);
        }

    }

    private void changeViewAfterSave(KeyEvent keyEvent) {
        Object source = keyEvent.getSource();
        if (source instanceof TextArea) {
            TextArea textArea = (TextArea) source;
            textArea.setText(null);
        }
        documentTableView.requestFocus();
        documentTableView.getSelectionModel().select(0);
        refreshTable();
        refreshWebView();
    }

    private void addEvent(Todo todo) {
        asynRemindService.addEvent(new MicrosoftReminder(todo), new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                Serializable id = (Serializable) event.getSource().getValue();
                todo.setRemindId((String) id);
                readEvent(todo);
            }
        });
    }

    private Document createDocument(String content) {
        Document document;
        int length = content.length();
        if (content.endsWith(NOTE.getType() + "\n")) {
            content = content.substring(0, length - NOTE.getType().length() - 1);
            Note note = new Note();
            note.setContent(content);
            document = note;
        } else if (content.endsWith(TODO.getType() + "\n")) {
            content = content.substring(0, length - TODO.getType().length() - 1);
            Todo todo = new Todo();
            todo.setContent(content);
            document = todo;
        } else if (content.endsWith(DOC.getType() + "\n")) {
            content = content.substring(0, length - DOC.getType().length() - 1);
            document = new Document();
            document.setContent(content);
        } else {
            document = new Document();
            document.setContent(content);
        }

        Date curr = new Date();
        document.setId(UUID.randomUUID().toString().replace("-", ""));
        document.setCreateTime(curr);
        document.setLastModifyTime(curr);
        document.setVersion(1);
        document.setContent(F.CONTENT_PREFIX + document.getContent());

        return document;
    }

    private boolean isValid(String content, KeyEvent keyEvent) {
        if (StringUtils.isNotBlank(content)) {
            int length = content.length();
            if (length > NOTE.getType().length() + 1 && content.endsWith(NOTE.getType() + "\n")) {
                return true;
            }
            if (length > TODO.getType().length() + 1 && content.endsWith(TODO.getType() + "\n")) {
                return true;
            }
            if (length > DOC.getType().length() + 1 && content.endsWith(DOC.getType() + "\n")) {
                return true;
            }
            if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.S) {
                return true;
            }
        }
        return false;
    }

    private String getSaveContent() {
        return contentTextAreaSave.getText();
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

    public void modify(KeyEvent keyEvent) {

        String content = getUpdateContent();
        if (!isValid(content, keyEvent)) {
            return;
        }

        Document document = getSelectedDocument();
        if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.S) {
            document.setContent(content);
        } else {
            String id = document.getId();
            Integer oldVersion = document.getVersion();
            document = createDocument(content);
            document.setId(id);
            document.setVersion(oldVersion);
        }

        mainService.update(document);

        changeViewAfterUpdate(keyEvent);


        if (document instanceof Todo) {
            Todo todo = (Todo) document;
            addOrUpdateEvent(todo);
        }

    }

    private void addOrUpdateEvent(Todo todo) {
        if (StringUtils.isNotBlank(todo.getRemindId())){
            updateEvent(todo);
        }else {
            addEvent(todo);
        }
    }

    private void updateEvent(Todo todo) {
        asynRemindService.modifyEvent(new MicrosoftReminder(todo), new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                Serializable id = (Serializable) event.getSource().getValue();
                todo.setRemindId((String) id);
                readEvent(todo);
            }
        });
    }

    private void readEvent(Todo todo){
        asynRemindService.readEvent(todo.getRemindId(), new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                MicrosoftReminder microsoftReminder = (MicrosoftReminder) event.getSource().getValue();
                try {
                    todo.setRemoteRemindTime(microsoftReminder.getStart().toDate());
                    mainService.update(todo);
                    refreshTable();
                    refreshWebView();
                } catch (ParseException e) {
                    F.logger.info(e.getMessage());
                }
            }
        });
    }

    private void changeViewAfterUpdate(KeyEvent keyEvent) {
        Object source = keyEvent.getSource();

        documentTableView.requestFocus();
        refreshTable();
        refreshWebView();

        if (source instanceof TextArea) {
            TextArea textArea = (TextArea) source;
            textArea.setText(getSelectedDocument().getContent());
        }

    }

    private Document getSelectedDocument() {
        FxDocument selectedDocument = documentTableView.getSelectionModel().getSelectedItem();
        return selectedDocument.toNormalDocument();
    }

    private String getUpdateContent() {
        return contentTextAreaUpdate.getText();
    }

    public void delete() {
        FxDocument selectedItem = documentTableView.getSelectionModel().getSelectedItem();
        String selectedId = selectedItem.getId();
        mainService.deleteById(selectedId);

        changeViewAfterDelete();

        Document selectedDocument = selectedItem.toNormalDocument();
        if (selectedDocument instanceof Todo) {
            Todo todo = (Todo) selectedDocument;
            deleteEvent(todo);
        }

    }

    private void changeViewAfterDelete() {
        refreshTable();
        refreshWebView();
    }

    private void deleteEvent(Todo todo){
        asynRemindService.removeEvent(todo.getRemindId(), new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                refreshTable();
                refreshWebView();
            }
        });
    }

    public void openConfig() throws IOException {
        F.logger.info("open config");
        context.loadView(ConfigViewLoader.class);
    }

    public void logout() throws IOException {
        F.logger.info("logout");
        //注销
        configService.setProperty("password", "");
        configService.storeProperties();

        //关闭当前页面
        context.closeView(MainViewLoader.class);

        //转到登录页面
        context.loadView(ConfigViewLoader.class);
    }

    private void refreshTable() {
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

    private void refreshWebView() {
        FxDocument selectedItem = documentTableView.getSelectionModel().getSelectedItem();
        bindService.initWebView(webView, selectedItem);
        F.logger.info("refreshWebView");
    }

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


}


