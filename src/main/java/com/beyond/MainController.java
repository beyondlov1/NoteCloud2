package com.beyond;

import com.beyond.entity.Document;
import com.beyond.entity.MicrosoftReminder;
import com.beyond.entity.Note;
import com.beyond.entity.Todo;
import com.beyond.f.F;
import com.beyond.service.*;
import com.beyond.utils.ListUtils;
import com.beyond.utils.SortUtils;
import com.beyond.utils.TimeUtils;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
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
import java.util.*;

import static com.beyond.DocumentType.DOC;
import static com.beyond.DocumentType.NOTE;
import static com.beyond.DocumentType.TODO;


public class MainController implements Observer{

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
    private RemindServiceImpl remindService;
    private RemindServiceAsyn remindServiceAsyn;

    private MainApplication application;


    @FXML
    private void initialize() {
        init();
    }

    private void init() {
        mainService = new MainService(this);
        bindService = new BindService(mainService.getFxDocuments());
        bindService.init(this);
        configService = new ConfigService(F.CONFIG_PATH);
        remindService = new RemindServiceImpl(new AuthService(),mainService);
        remindServiceAsyn = new RemindServiceAsynImpl(new RemindServiceRemote(new AuthService()));
    }

    @FXML
    private void save(KeyEvent keyEvent) {
        String content = contentTextAreaSave.getText();
        Object source = keyEvent.getSource();

        //验证能否保存
        Document document;
        if (keyEvent.isControlDown()&&keyEvent.getCode()==KeyCode.S){
            document = new Document();
            document.setContent(content);
        }else{
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
        document.setContent(F.CONTENT_PREFIX+document.getContent());

        mainService.add(document);

        //changeView
        if (source instanceof TextArea) {
            TextArea textArea = (TextArea) source;
            textArea.setText(null);
        }
        documentTableView.requestFocus();
        documentTableView.getSelectionModel().select(0);

        //设置提醒
//        if (document instanceof Todo) {
//            Todo todo = (Todo) document;
//            remindServiceAsyn.addEvent(new MicrosoftReminder(todo), new Callback<String>() {
//                @Override
//                public String call(Object object) {
//                    String remindId = object.toString();
//                    todo.setRemindId(remindId);
//                    todo.setRemindTime(TimeUtils.parse(todo.getContent()));
//                    mainService.update(todo);
//                    refreshTable();
//                    remindServiceAsyn.readEvent(remindId, new Callback() {
//                        @Override
//                        public Object call(Object object)  {
//                            try {
//                                MicrosoftReminder reminder = (MicrosoftReminder) object;
//                                todo.setRemoteRemindTime(reminder.getStart().toDate());
//                                mainService.update(todo);
//                                refreshTable();
//                                refreshWebView();
//                            } catch (ParseException e) {
//                                e.printStackTrace();
//                            }
//                            return null;
//                        }
//                    });
//                    return null;
//                }
//            });
//        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Todo todo = null;
                if (document instanceof Todo) todo = (Todo) document;
                if (todo == null) return;
                remindService.addEvent(todo,new MicrosoftReminder(todo));
                remindService.readEvent(todo);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        refreshTable();
                        refreshWebView();
                    }
                });
            }
        });
        thread.start();
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
                if (remindTime!=null){
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
    private void update(KeyEvent keyEvent) {
        String content = contentTextAreaUpdate.getText();
        Object source = keyEvent.getSource();

        FxDocument selectedDocument = documentTableView.getSelectionModel().getSelectedItem();
        int selectedIndex = documentTableView.getSelectionModel().getSelectedIndex();

        //验证能否保存
        Document document;
        if (keyEvent.isControlDown()&&keyEvent.getCode()==KeyCode.S){
            document = selectedDocument.toNormalDocument();
            document.setContent(content);
        }else{
             document = validate(content);
        }
        if (document == null) return;
        if (StringUtils.isBlank(document.getContent())) {
            return;
        }
        selectedDocument.setContent(document.getContent());
        mainService.update(selectedDocument.toNormalDocument());

        //更新事件
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Todo todo = null;
                if (selectedDocument.toNormalDocument() instanceof Todo) todo = (Todo) selectedDocument.toNormalDocument();
                if (todo == null||todo.getRemindId()==null) return;
                remindService.updateEvent(todo);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        refreshTable();
                        refreshWebView();
                    }
                });
            }
        });
        thread.start();

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
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Todo todo = null;
                if (selectedItem.toNormalDocument() instanceof Todo) todo = (Todo) selectedItem.toNormalDocument();
                if (todo == null||todo.getRemindId()==null) return;
                remindService.deleteEvent(todo.getRemindId());

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        refreshTable();
                        refreshWebView();
                    }
                });
            }
        });
        thread.start();
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
        configService.setProperty("password","");
        configService.storeProperties();

        //转到登录页面
        application.loadLoginView();
    }

    public void startObserve(Observable observable) {
        observable.addObserver(this);
    }

    public void refreshTable(){
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
        }else {
            if (!documentTableView.getItems().isEmpty()){
                documentTableView.getSelectionModel().select(0);
            }
        }
        F.logger.info("refreshTable");
    }

    public void refreshWebView(){
        FxDocument selectedItem = documentTableView.getSelectionModel().getSelectedItem();
        bindService.initWebView(webView,selectedItem);
        F.logger.info("refreshWebView");
    }

    /**
     * 监控刷新
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


