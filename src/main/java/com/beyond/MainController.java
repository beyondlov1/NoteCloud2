package com.beyond;

import com.beyond.entity.*;
import com.beyond.f.F;
import com.beyond.service.*;
import com.beyond.utils.*;
import com.beyond.viewloader.ConfigViewLoader;
import com.beyond.viewloader.MainViewLoader;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.beyond.DocumentType.DOC;
import static com.beyond.DocumentType.NOTE;
import static com.beyond.DocumentType.TODO;


public class MainController{

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

    private ObservableList<FxDocument> deletedFxDocumentList;

    private MainService mainService;
    private BindService bindService;
    private ConfigService configService;

    private ApplicationContext context;

    public MainController(ApplicationContext context) {
        this.context = context;
    }

    public void initialize() {
        mainService = context.getMainService();
        configService = context.getConfigService();

        ObservableList<FxDocument> fxDocumentList = mainService.getFxDocuments();
        bindService = new BindService(fxDocumentList);
        bindService.bind();
    }

    public void save(KeyEvent keyEvent) {
        String content = getSaveContent();
        if (!isValid(content, keyEvent)) {
            return;
        }

        try {
            Document document = createDocument(content);
            mainService.add(document);
            postSave(keyEvent);
        }catch (Exception e){
            F.logger.info("保存错误");
            message.setText("保存错误");
        }
    }
    private String getSaveContent() {
        return contentTextAreaSave.getText();
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
    private void postSave(KeyEvent keyEvent) {
        Object source = keyEvent.getSource();
        if (source instanceof TextArea) {
            TextArea textArea = (TextArea) source;
            textArea.setText(null);
        }
        documentTableView.requestFocus();
        documentTableView.getSelectionModel().select(0);
        refresh();
    }

    public void modify(KeyEvent keyEvent) {
        String content = getUpdateContent();
        if (!isValid(content, keyEvent)) {
            return;
        }

        try {
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
            postUpdate(keyEvent);
        }catch (Exception e){
            F.logger.info(e.getMessage());
            message.setText("更新出錯");
        }
    }
    private String getUpdateContent() {
        return contentTextAreaUpdate.getText();
    }
    private Document getSelectedDocument() {
        FxDocument selectedDocument = documentTableView.getSelectionModel().getSelectedItem();
        return selectedDocument.toNormalDocument();
    }
    private void postUpdate(KeyEvent keyEvent) {
        Object source = keyEvent.getSource();

        documentTableView.requestFocus();
        refresh();

        if (source instanceof TextArea) {
            TextArea textArea = (TextArea) source;
            textArea.setText(getSelectedDocument().getContent());
        }

    }

    public void delete() {
        try {
            FxDocument selectedItem = documentTableView.getSelectionModel().getSelectedItem();
            Document selectedDocument = selectedItem.toNormalDocument();
            mainService.delete(selectedDocument);
            postDelete();
        }catch (Exception e){
            e.printStackTrace();
            F.logger.info(e.getMessage());
            if ("提醒删除错误".equals(e.getMessage())){
                message.setText("提醒删除错误");
            }else {
                message.setText("刪除出錯");
            }
        }

    }
    private void postDelete() {
        refresh();
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

    public void refresh(){
        refreshTable();
        refreshWebView();
    }
    private void refreshTable() {
        FxDocument selectedItem = documentTableView.getSelectionModel().getSelectedItem();
        //从文件获取文档
        mainService.pull();
        mainService.initFxDocument();
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

    private class BindService{
        private ObservableList<FxDocument> fxDocumentList;

        BindService(ObservableList<FxDocument> fxDocumentList) {
            this.fxDocumentList = fxDocumentList;
        }

        void bind(){
            initOrder();
            initTable();
            initListener();
            initFocus();
            initStyle();
        }

        private void initOrder() {
            SortUtils.sort(fxDocumentList,FxDocument.class,"lastModifyTime",SortUtils.SortType.DESC);
        }

        private void initTable() {
            documentTableView.setItems(fxDocumentList);
            ObservableList<TableColumn<FxDocument, ?>> columns = documentTableView.getColumns();
            for (TableColumn<FxDocument, ?> column : columns) {
                if (StringUtils.equals(column.getId(), "contentTableColumn")) {
                    column.setCellFactory(getCellFactory());
                    column.setCellValueFactory(getCellValueFactory(FxDocument.class, "content"));
                }
            }
        }

        private void initStyle() {
            contentTextAreaUpdate.setWrapText(true);
            contentTextAreaSave.setWrapText(true);
        }

        private void initFocus() {
            contentTextAreaSave.setFocusTraversable(false);
            contentTextAreaUpdate.setFocusTraversable(false);
            tabPane.setFocusTraversable(false);
            focusTo(documentTableView);
            documentTableView.getSelectionModel().selectFirst();
        }

        private void initListener() {

        /*
         * container E D Listener
         */
        documentTableView.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    KeyCode code = event.getCode();
                    switch (code) {
                        case E:
                            documentTableView.requestFocus();
                            documentTableView.getSelectionModel().selectPrevious();
                            documentTableView.scrollTo(documentTableView.getSelectionModel().getSelectedIndex());
                            break;
                        case D:
                            documentTableView.requestFocus();
                            documentTableView.getSelectionModel().selectNext();
                            documentTableView.scrollTo(documentTableView.getSelectionModel().getSelectedIndex());
                            break;
                        default:
                            break;
                    }
                }
            });

        /*
         * table某一行选中后的 Listener
         */
        documentTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FxDocument>() {
                @Override
                public void changed(ObservableValue<? extends FxDocument> observable, FxDocument oldValue, FxDocument newValue) {
                    if (newValue != null) {
                        initUpdateTextArea(contentTextAreaUpdate, newValue);
                        initWebView(webView, newValue);
                    }
                }
            });

        /*
         * tableView 快捷键
         */
        documentTableView.setOnKeyReleased(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    KeyCode code = event.getCode();
                    switch (code) {
                        case DELETE:
                            delete();
                            break;
                        case I:
                            contentTextAreaSave.requestFocus();
                            //TODO: changeInputMethod
                            changeInputMethod();
                            break;
                        case U:
                        case LEFT:
                            contentTextAreaUpdate.requestFocus();
                            contentTextAreaUpdate.positionCaret(contentTextAreaUpdate.getText().length());
                            //TODO: changeInputMethod
                            changeInputMethod();
                            break;
                        default:
                            break;
                    }
                }
            });
        /*
         * OnInputMethodTextChanged这个方法是用来检测输入法是否发生变化, 这里用它来放置tableView获得焦点后, 切换输入法, 再切换到
         * 别的节点时再次切换输入法的问题, 这样可以保证tableView一直是无输入法的状态(或者是默认输入法??没有试)
         */
        documentTableView.setOnInputMethodTextChanged(new EventHandler<InputMethodEvent>() {
                @Override
                public void handle(InputMethodEvent event) {
                    if (documentTableView.isFocused()) {
                        changeInputMethod();
                    }
                }
            });
            contentTextAreaSave.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    if (event.getCode() == KeyCode.ESCAPE)
                        focusTo(documentTableView);
                }
            });
            contentTextAreaUpdate.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    if (event.getCode() == KeyCode.ESCAPE)
                        focusTo(documentTableView);
                }
            });
        }

        private void changeInputMethod() {

        }

        private void focusTo(Node node) {
            node.requestFocus();
        }

        private void initUpdateTextArea(TextArea updateTextArea, FxDocument fxDocument) {
            updateTextArea.setText(fxDocument.getContent());
        }

        private void initWebView(WebView webView, FxDocument fxDocument) {
            //添加事件戳
            String timeStamp = "";
            if (fxDocument==null) return;
            if (fxDocument.toNormalDocument() instanceof Todo
                    && ((Todo)fxDocument.toNormalDocument()).getRemindTime()!=null
                    && StringUtils.isNotBlank(TimeUtils.getTimeNorm(fxDocument.getContent()))){
                Todo todo = (Todo)fxDocument.toNormalDocument();
                Date remoteRemindTime = todo.getRemoteRemindTime();
                if (remoteRemindTime !=null){
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    timeStamp = "  \n\n\n***\n提醒时间:"+ simpleDateFormat.format(remoteRemindTime);
                }
            }

            //webview加载内容
            webView.getEngine().loadContent(MarkDownUtils.convertMarkDownToHtml(fxDocument.getContent()+timeStamp));
        }

        private <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> getCellFactory() {
            return new Callback<TableColumn<S, T>, TableCell<S, T>>() {
                @Override
                public TableCell<S, T> call(TableColumn<S, T> param) {
                    return new TableCell<S, T>() {
                        @Override
                        protected void updateItem(T item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item instanceof String) {
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    String content = HtmlUtils.parseHtml2Text(item.toString());
                                    content = com.beyond.utils.StringUtils.cutAndPretty(content, 100);
                                    Text text = new Text(content);
                                    TextFlow textFlow = new TextFlow(text);
                                    textFlow.setPadding(new Insets(5, 10, 5, 10));
                                    textFlow.setPrefHeight(40);
                                    textFlow.setMaxHeight(100);
                                    setGraphic(textFlow);
                                }
                            }
                        }
                    };
                }
            };
        }

        @SuppressWarnings("unchecked")
        private <S, T> Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> getCellValueFactory(Class<FxDocument> sClass, String propertyName) {
            return new Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>>() {
                @Override
                public ObservableValue<T> call(TableColumn.CellDataFeatures<S, T> param) {
                    return (Property) ReflectUtils.getValueByField(sClass, param.getValue(), propertyName);
                }
            };
        }
    }
}


