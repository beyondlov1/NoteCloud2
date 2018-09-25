package com.beyond;

import com.beyond.entity.Document;
import com.beyond.entity.Note;
import com.beyond.entity.Todo;
import com.beyond.filter.Filter;
import com.beyond.filter.FilterContainer;
import com.beyond.filter.impl.ContentSuffixFilter;
import com.beyond.filter.impl.KeyFilter;
import com.beyond.service.BindService;
import com.beyond.service.MainService;
import com.beyond.utils.ListUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.beyond.DocumentType.DOC;
import static com.beyond.DocumentType.NOTE;
import static com.beyond.DocumentType.TODO;


public class MainController {

    //添加组件
    @FXML
    private Text message;

    @FXML
    private TextField titleTextField;

    @FXML
    private TextArea contentTextAreaSaveOrUpdate;

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

    private Timer timer;
    private Timeline timeline;

    private String selectedId;

    private MainService mainService;

    private BindService bindService;

    @FXML
    private void initialize() {
        init();
    }

    private void init() {
        mainService = new MainService(this);
        bindService = new BindService(mainService.getFxDocuments());
        bindService.init(this);
    }

    @FXML
    private void save(KeyEvent keyEvent) {
        String content = contentTextAreaSave.getText();
        Object source = keyEvent.getSource();

        //验证能否保存
        String validContent = validate(content);
        if (StringUtils.isBlank(validContent)&&!(keyEvent.isControlDown()&&keyEvent.getCode()==KeyCode.S)) {
            return;
        }

        Document document = new Document();
        Date curr = new Date();
        document.setId(UUID.randomUUID().toString().replace("-", ""));
        document.setCreateTime(curr);
        document.setLastModifyTime(curr);
        document.setContent(validContent);
        document.setVersion(1);

        mainService.add(document);

        //changeView
        if (source instanceof TextArea){
            TextArea textArea = (TextArea)source;
            textArea.setText(null);
        }
        documentTableView.requestFocus();
        documentTableView.getSelectionModel().select(0);
    }

    private String validate(String content) {
        if (StringUtils.isNotBlank(content)){
            int length = content.length();
            if (length>NOTE.getType().length()+1&&content.endsWith(NOTE.getType()+"\n")){
                return content.substring(0, length - NOTE.getType().length() - 1);
            }
            if (length >TODO.getType().length()+1&&content.endsWith(TODO.getType()+"\n")){
                return content.substring(0, length - TODO.getType().length() - 1);
            }
            if (length >DOC.getType().length()+1&&content.endsWith(DOC.getType()+"\n")){
                return content.substring(0, length - DOC.getType().length() - 1);
            }
        }
        return null;

    }

    @FXML
    private void saveOrUpdate(KeyEvent keyEvent) {
        String content = contentTextAreaSaveOrUpdate.getText();
        Object source = keyEvent.getSource();

        //验证能否保存
        String validContent = validate(content);
        if (StringUtils.isBlank(validContent)&&!(keyEvent.isControlDown()&&keyEvent.getCode()==KeyCode.S)) {
            return;
        }

        FxDocument selectedDocument = documentTableView.getSelectionModel().getSelectedItem();
        int selectedIndex = documentTableView.getSelectionModel().getSelectedIndex();
        selectedDocument.setContent(validContent);

        mainService.update(selectedDocument.toNormalDocument());

        //changeView
        if (source instanceof TextArea){
            TextArea textArea = (TextArea)source;
            textArea.setText(selectedDocument.getContent());
        }
        documentTableView.getSelectionModel().select(selectedIndex);
        documentTableView.requestFocus();
    }

    @FXML
    private void delete() {

    }

    public Text getMessage() {
        return message;
    }

    public TextField getTitleTextField() {
        return titleTextField;
    }

    public TextArea getContentTextAreaSaveOrUpdate() {
        return contentTextAreaSaveOrUpdate;
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


