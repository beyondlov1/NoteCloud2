package com.beyond;

import com.beyond.entity.Document;
import com.beyond.entity.FxDocument;
import com.beyond.entity.Todo;
import com.beyond.service.MainService;
import com.beyond.utils.MarkDownUtils;
import com.beyond.viewloader.RemindViewLoader;
import com.beyond.viewloader.ViewLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.web.WebView;
import javafx.util.StringConverter;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.MutablePair;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author beyondlov1
 * @date 2019/01/01
 */
public class MessageController {
    @FXML
    private WebView contentWebView;
    @FXML
    private Button deleteAndCloseButton;
    @FXML
    private ChoiceBox<Map.Entry<Integer,String>> delayChoiceBox;
    @FXML
    private Button delayButton;

    private ApplicationContext context;

    private FxDocument fxDocument;
    private ViewLoader viewLoader;

    public MessageController(ApplicationContext context) {
        this.context = context;
    }

    public void initialize(){
        initChoiceBox();
        refreshWebView();
    }
    private void initChoiceBox() {
        List<Map.Entry<Integer,String>> items = new ArrayList<>();
        items.add(new AbstractMap.SimpleEntry<>(10,"十分钟"));
        items.add(new AbstractMap.SimpleEntry<>(30,"三十分钟"));
        items.add(new AbstractMap.SimpleEntry<>(60,"一小时"));
        items.add(new AbstractMap.SimpleEntry<>(-1,"无限"));
        ObservableList<Map.Entry<Integer,String>> observableList = FXCollections.observableList(items);
        delayChoiceBox.setConverter(new StringConverter<Map.Entry<Integer, String>>() {
            @Override
            public String toString(Map.Entry<Integer, String> object) {
                return object.getValue();
            }

            @Override
            public Map.Entry<Integer, String> fromString(String string) {
                return null;
            }
        });
        delayChoiceBox.setItems(observableList);
    }
    private void refreshWebView(){
        //添加事件戳
        String timeStamp = "";
        if (fxDocument==null) return;
        if (fxDocument.toNormalDocument() instanceof Todo
                && ((Todo)fxDocument.toNormalDocument()).getReminder().getRemindTime()!=null){
            Todo todo = (Todo)fxDocument.toNormalDocument();
            Date remoteRemindTime = todo.getReminder().getRemoteRemindTime();
            if (remoteRemindTime !=null){
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                timeStamp = "  \n\n\n***\n提醒时间:"+ simpleDateFormat.format(remoteRemindTime);
            }
            //webview加载内容
            contentWebView.getEngine().loadContent(MarkDownUtils.convertMarkDownToHtml(fxDocument.getContent()+timeStamp));
        }else {
            //webview加载内容
            contentWebView.getEngine().loadContent(MarkDownUtils.convertMarkDownToHtml(fxDocument.getContent()));
        }
    }

    public void deleteAndClose(){
        viewLoader.close();
    }

    public void delay(){
        Map.Entry<Integer, String> selectedItem = delayChoiceBox.getSelectionModel().getSelectedItem();
        if (selectedItem!=null){
            Integer key = selectedItem.getKey();
            Document document = fxDocument.toNormalDocument();
            System.out.println(key);
            if (document instanceof Todo){
                Todo todo = (Todo) document;
                todo.getReminder().setRemindTime(DateUtils.addMinutes(todo.getReminder().getRemindTime(),key));
                context.getMainService().update(todo);
            }
        }
        viewLoader.close();
    }

    public FxDocument getFxDocument() {
        return fxDocument;
    }

    public void setFxDocument(FxDocument fxDocument) {
        this.fxDocument = fxDocument;
    }

    public void setViewLoader(ViewLoader viewLoader) {
        this.viewLoader = viewLoader;
    }

    public ViewLoader getViewLoader() {
        return viewLoader;
    }

}
