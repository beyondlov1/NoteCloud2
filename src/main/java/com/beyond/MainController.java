package com.beyond;

import com.beyond.entity.Document;
import com.beyond.entity.Note;
import com.beyond.entity.Todo;
import com.beyond.f.F;
import com.beyond.filter.Filter;
import com.beyond.filter.FilterContainer;
import com.beyond.filter.impl.ContentSuffixFilter;
import com.beyond.filter.impl.KeyFilter;
import com.beyond.service.BindService;
import com.beyond.service.MainService;
import com.beyond.service.MergeService;
import com.beyond.utils.ListUtils;
import com.beyond.utils.SortUtils;
import com.beyond.utils.TimeUtils;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private MainService mainService;

    private BindService bindService;

    private MergeService mergeService;

    private ExecutorService executorService;


    @FXML
    private void initialize() {
        init();
    }

    private void init() {
        mainService = new MainService(this);
        bindService = new BindService(mainService.getFxDocuments());
        bindService.init(this);
        mergeService = new MergeService(F.DEFAULT_LOCAL_PATH,
                F.DEFAULT_REMOTE_PATH,
                F.DEFAULT_TMP_PATH);
        executorService = Executors.newCachedThreadPool();
        timer = new Timer();
    }

    @FXML
    private void save(KeyEvent keyEvent) {
        String content = contentTextAreaSave.getText();
        Object source = keyEvent.getSource();

        //验证能否保存
        Document document = validate(content);
        if (document==null) return;
        if (StringUtils.isBlank(document.getContent())&&!(keyEvent.isControlDown()&&keyEvent.getCode()==KeyCode.S)) {
            return;
        }

        Date curr = new Date();
        document.setId(UUID.randomUUID().toString().replace("-", ""));
        document.setCreateTime(curr);
        document.setLastModifyTime(curr);
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

    private Document validate(String content) {
        if (StringUtils.isNotBlank(content)){
            int length = content.length();
            if (length>NOTE.getType().length()+1&&content.endsWith(NOTE.getType()+"\n")){
                String validContent = content.substring(0, length - NOTE.getType().length() - 1);
                Note note = new Note();
                note.setContent(validContent);
                return note;
            }
            if (length >TODO.getType().length()+1&&content.endsWith(TODO.getType()+"\n")){
                String validContent =  content.substring(0, length - TODO.getType().length() - 1);
                Todo todo = new Todo();
                todo.setContent(validContent);
                todo.setRemindTime(TimeUtils.parse(validContent));
                return todo;
            }
            if (length >DOC.getType().length()+1&&content.endsWith(DOC.getType()+"\n")){
                String validContent =  content.substring(0, length - DOC.getType().length() - 1);
                Document document = new Document();
                document.setContent(validContent);
                return document;
            }
        }
        return null;
    }

    @FXML
    private void saveOrUpdate(KeyEvent keyEvent) {
        String content = contentTextAreaSaveOrUpdate.getText();
        Object source = keyEvent.getSource();

        //验证能否保存
        Document document = validate(content);
        if (document==null) return;
        if (StringUtils.isBlank(document.getContent())&&!(keyEvent.isControlDown()&&keyEvent.getCode()==KeyCode.S)) {
            return;
        }

        FxDocument selectedDocument = documentTableView.getSelectionModel().getSelectedItem();
        int selectedIndex = documentTableView.getSelectionModel().getSelectedIndex();
        selectedDocument.setContent(document.getContent());

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
    public void delete() {
        String selectedId = documentTableView.getSelectionModel().getSelectedItem().getId();
        mainService.deleteById(selectedId);
    }

    public void startSynchronize(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                mergeService.handle();
            }
        };
        timer.schedule(timerTask,0,F.SYNC_PERIOD);
        startRefresh();
    }

    public void stopSynchronize(){
        timer.cancel();
        timeline.stop();
    }

    private void startRefresh(){
        timeline = new Timeline(new KeyFrame(Duration.millis(F.VIEW_REFRESH_PERIOD), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int mergeFlag = mergeService.getMergeFlag();
                if (mergeFlag == 1){
                    FxDocument selectedItem = documentTableView.getSelectionModel().getSelectedItem();
                    //从文件获取文档
                    mainService.pull();
                    mainService.setFxDocuments();
                    ObservableList<FxDocument> fxDocuments = mainService.getFxDocuments();

                    //order
                    SortUtils.sort(fxDocuments,FxDocument.class,"lastModifyTime",SortUtils.SortType.DESC);

                    //刷新
                    documentTableView.setItems(fxDocuments);
                    documentTableView.getSelectionModel().select(ListUtils.getFxDocumentIndexById(fxDocuments,selectedItem.getId()));
                    documentTableView.refresh();
                    F.logger.info("refresh");
                    mergeService.setMergeFlag(0);
                }
            }
        }));
        timeline.setDelay(new Duration(0));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
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


