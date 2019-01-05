package com.beyond;

import com.beyond.entity.Document;
import com.beyond.entity.FxDocument;
import com.beyond.entity.Todo;
import com.beyond.utils.MarkDownUtils;
import com.beyond.utils.TimeUtils;
import com.beyond.utils.ViewUtils;
import com.beyond.viewloader.ViewLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.apache.commons.lang3.time.DateUtils;

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

    private FxDocument fxDocument;

    private ViewLoader viewLoader;

    private ApplicationContext context;

    public MessageController(ApplicationContext context) {
        this.context = context;
    }

    public void initialize(){
        this.initDelayView();
        this.loadMessage();
        this.initCloseTimer();
    }

    private void initCloseTimer() {
        ScheduledService service = new ScheduledService() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        return null;
                    }
                };
            }
        };
        service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            private int remainMinutesBeforeClose = 30;
            @Override
            public void handle(WorkerStateEvent event) {
                deleteAndCloseButton.setText(remainMinutesBeforeClose+"分钟后自动删除");
                remainMinutesBeforeClose--;
                if (remainMinutesBeforeClose < 1){
                    deleteAndClose();
                }
            }
        });
        service.setPeriod(Duration.minutes(1));
        service.start();
    }

    private void initDelayView() {
        List<Map.Entry<Integer,String>> items = new ArrayList<>();
        items.add(new AbstractMap.SimpleEntry<>(10,"十分钟"));
        items.add(new AbstractMap.SimpleEntry<>(30,"三十分钟"));
        items.add(new AbstractMap.SimpleEntry<>(60,"一小时"));
        items.add(new AbstractMap.SimpleEntry<>(24*60*365*10,"无限"));
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

    private void loadMessage() {
        ViewUtils.loadContentForWebView(fxDocument,contentWebView);
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

    public void setFxDocument(FxDocument fxDocument) {
        this.fxDocument = fxDocument;
    }

    public void setViewLoader(ViewLoader viewLoader) {
        this.viewLoader = viewLoader;
    }

}
