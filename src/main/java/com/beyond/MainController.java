package com.beyond;

import com.beyond.entity.*;
import com.beyond.f.F;
import com.beyond.service.*;
import com.beyond.utils.*;
import com.beyond.viewloader.ConfigViewLoader;
import com.beyond.viewloader.FloatViewLoader;
import com.beyond.viewloader.LoginViewLoader;
import com.beyond.viewloader.MainViewLoader;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
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
        bindService = new BindService(documentTableView,fxDocumentList);
        bindService.bind();
    }

    public void save(KeyEvent keyEvent) {
        String content = contentTextAreaSave.getText();
        if (!DocumentUtils.validContentAndEvent(content, keyEvent)) {
            return;
        }

        try {
            Document document = DocumentUtils.createDocument(content);
            mainService.add(document);
            this.changeViewAfterSave(keyEvent);
        }catch (Exception e){
            F.logger.info("保存错误");
            message.setText("保存错误");
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
        this.refresh();
    }

    public void modify(KeyEvent keyEvent) {
        String content = contentTextAreaUpdate.getText();
        if (!DocumentUtils.validContentAndEvent(content, keyEvent)) {
            return;
        }

        try {
            Document document = this.getSelectedDocument();
            if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.S) {
                document.setContent(content);
            } else {
                String id = document.getId();
                Integer oldVersion = document.getVersion();
                document = DocumentUtils.createDocument(content);
                document.setId(id);
                document.setVersion(oldVersion);
            }
            if (document instanceof Todo){
                Todo todo = (Todo) document;
                todo.setRemindTimeFromContent();
            }
            mainService.update(document);
            changeViewAfterUpdate(keyEvent);
        }catch (Exception e){
            F.logger.info(e.getMessage());
            message.setText("更新出錯");
        }
    }
    private Document getSelectedDocument() {
        FxDocument selectedDocument = documentTableView.getSelectionModel().getSelectedItem();
        return selectedDocument.toNormalDocument();
    }
    private void changeViewAfterUpdate(KeyEvent keyEvent) {
        Object source = keyEvent.getSource();
        documentTableView.requestFocus();
        this.refresh();
        if (source instanceof TextArea) {
            TextArea textArea = (TextArea) source;
            textArea.setText(getSelectedDocument().getContent());
        }

    }

    public void delete() {
        try {
            Document selectedDocument = this.getSelectedDocument();
            mainService.delete(selectedDocument);
            changeViewAfterDelete();
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
    private void changeViewAfterDelete() {
        this.refresh();
    }

    public void openConfig() {
        context.loadView(ConfigViewLoader.class);
    }

    public void logout(){
        F.logger.info("logout");
        //注销
        configService.setProperty("password", "");
        configService.storeProperties();

        //转到登录页面
        context.loadView(LoginViewLoader.class);

        //关闭当前页面
        context.closeView(MainViewLoader.class);

    }

    public void openFloatWindow() {
        context.loadView(FloatViewLoader.class);
    }

    public void switchFloatWindow()  {
        context.loadView(FloatViewLoader.class);
        context.closeView(MainViewLoader.class);
    }

    public void refresh(){
        context.refreshData();
        this.refreshTable();
        this.refreshWebView();
    }
    private void refreshTable() {
        FxDocument selectedItem = documentTableView.getSelectionModel().getSelectedItem();
        ObservableList<FxDocument> fxDocuments = mainService.getFxDocuments();

        //order
        SortUtils.sort(fxDocuments, FxDocument.class, "lastModifyTime", SortUtils.SortType.DESC);

        //刷新
        documentTableView.setItems(fxDocuments);
        documentTableView.refresh();
        if (selectedItem != null) {
            int selectItemIndex = ListUtils.getFxDocumentIndexById(fxDocuments, selectedItem.getId());
            documentTableView.getSelectionModel().select(selectItemIndex ==-1?0: selectItemIndex);
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

        private TableView<FxDocument> documentTableView;
        private ObservableList<FxDocument> fxDocumentList;

        private FxDocument lastSelectedItem;

        BindService(TableView<FxDocument> documentTableView,ObservableList<FxDocument> fxDocumentList) {
            this.documentTableView = documentTableView;
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
                if (StringUtils.equals(column.getId(), "contentTableColumn")||
                        StringUtils.equals(column.getId(), "deletedContentTableColumn")) {
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
                        //避免同步后刷新界面时刷新"更新文本框"
                        if (lastSelectedItem == null){
                            initUpdateTextArea(contentTextAreaUpdate, newValue);
                        }else if (!StringUtils.equals(newValue.getId(), lastSelectedItem.getId())){
                            initUpdateTextArea(contentTextAreaUpdate, newValue);
                        }
                        initWebView(webView, newValue);
                        lastSelectedItem = newValue;
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
                    if (event.getCode() == KeyCode.ESCAPE){
                        focusTo(documentTableView);
                    }else if (event.isControlDown()&& event.getCode() == KeyCode.R){
                        int caretPosition = contentTextAreaUpdate.getCaretPosition();
                        F.logger.info("caretPosition: "+caretPosition);
                        String text = contentTextAreaUpdate.getText();
                        if (isLineHasStrike(caretPosition, text)){
                            text = removeStrikeTagForLineAt(caretPosition,text);
                        }else {
                            text = insertStrikeTagForLineAt(caretPosition, text);
                        }
                        contentTextAreaUpdate.setText(text);
                        KeyEvent eventToSave = new KeyEvent(event.getSource(),
                                event.getTarget(),
                                event.getEventType(),
                                event.getCharacter(),
                                event.getText(),
                                KeyCode.S,
                                false,
                                true,
                                false,
                                false);
                        modify(eventToSave);
                    }
                }

                private boolean isLineHasStrike(int caretPosition, String text){
                    char[] chars = (text+"\n").toCharArray();
                    int start = getLineStart(caretPosition, chars);
                    int end = getLineEnd(caretPosition, chars);
                    String substring = String.valueOf(chars).substring(start, end+1);
                    System.out.println(substring);
                    return substring.contains(" <strike>") && substring.contains("</strike>");
                }
                private int getLineStart(int caretPosition, char[] chars){
                    int start = caretPosition;
                    while (chars[start]!='-'){
                        if (start>0){
                            start--;
                        }else {
                            break;
                        }
                    }
                    start++;
                    return start;
                }
                private int getLineEnd(int caretPosition, char[] chars){
                    int end = caretPosition;
                    while (chars[end]!='\n'){
                        if (end<chars.length-1){
                            end++;
                        }else{
                            break;
                        }
                    }
                    end--;
                    return end;
                }
                private String removeStrikeTagForLineAt(int caretPosition, String text){
                    StringBuilder result = new StringBuilder();
                    char[] chars = (text+"\n").toCharArray();

                    int start = getLineStart(caretPosition, chars);
                    int end = getLineEnd(caretPosition, chars);

                    String substring = String.valueOf(chars).substring(start, end+1);
                    if (substring.contains(" <strike>")&&substring.contains("</strike>")){
                        substring = substring.replace(" <strike>","");
                        substring = substring.replace("</strike>","");
                    }

                    result.append(text, 0, start);
                    result.append(substring);
                    result.append(text, end+1, text.length());

                    return result.toString();
                }
                private String insertStrikeTagForLineAt(int caretPosition, String text) {
                    StringBuilder result = new StringBuilder();
                    char[] chars = (text+"\n").toCharArray();

                    int start = getLineStart(caretPosition, chars);
                    int end = getLineEnd(caretPosition, chars);

                    int index = 0;
                    for (char aChar :chars) {
                        if (index==start){
                            result.append(" <strike>");
                        }
                        result.append(aChar);
                        if (index==end){
                            result.append("</strike>");
                        }
                        index++;
                    }

                    return result.substring(0,result.length()-1);
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
           ViewUtils.loadContentForWebView(fxDocument,webView);
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


