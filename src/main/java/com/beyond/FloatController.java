package com.beyond;


import com.beyond.entity.Document;
import com.beyond.entity.FxDocument;
import com.beyond.entity.Note;
import com.beyond.entity.Todo;
import com.beyond.f.F;
import com.beyond.service.MainService;
import com.beyond.utils.DocumentUtils;
import com.beyond.utils.HtmlUtils;
import com.beyond.utils.SortUtils;
import com.beyond.utils.TimeUtils;
import com.beyond.viewloader.FloatViewLoader;
import com.beyond.viewloader.MainViewLoader;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.UUID;

import static com.beyond.DocumentType.DOC;
import static com.beyond.DocumentType.NOTE;
import static com.beyond.DocumentType.TODO;

/**
 * @author beyondlov1
 * @date 2018/12/08
 */
public class FloatController {

    @FXML
    private TextArea contentTextAreaSave;

    @FXML
    private ListView<FxDocument> contentListView;

    @FXML
    private Button backToMainButton;

    @FXML
    private Button frontButton;

    @FXML
    private Button exitButton;

    private ApplicationContext context;

    private MainService mainService;

    public FloatController(ApplicationContext context) {
        this.context = context;
    }

    public void initialize() {
        mainService = context.getMainService();
        this.initListView();
    }

    private void initListView() {
        //order
        SortUtils.sort(this.getFxDocuments(), FxDocument.class, "lastModifyTime", SortUtils.SortType.DESC);

        contentListView.setItems(this.getFxDocuments());
        contentListView.setCellFactory(new Callback<ListView<FxDocument>, ListCell<FxDocument>>() {
            @Override
            public ListCell<FxDocument> call(ListView<FxDocument> param) {
                return new ListCell<FxDocument>() {
                    @Override
                    protected void updateItem(FxDocument item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            String content = HtmlUtils.parseHtml2Text(item.getContent());
                            content = com.beyond.utils.StringUtils.cutAndPretty(content, 100);
                            Text text = new Text(content);
                            if (item.toNormalDocument() instanceof Todo) {
                                text.setFill(Color.RED);
                            }
                            TextFlow textFlow = new TextFlow(text);
                            textFlow.setPadding(new Insets(5, 10, 5, 10));
                            textFlow.setPrefWidth(250);
                            setGraphic(textFlow);
                        } else {
                            setGraphic(null);
                        }
                    }
                };
            }
        });
    }

    private ObservableList<FxDocument> getFxDocuments() {
        return mainService.getFxDocuments();
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
        }
    }
    private void changeViewAfterSave(KeyEvent keyEvent) {
        Object source = keyEvent.getSource();
        if (source instanceof TextArea) {
            TextArea textArea = (TextArea) source;
            textArea.setText(null);
        }
        context.refresh();
    }

    public void backToMain() {
        if (!context.getCurrentStageMap().containsKey(MainViewLoader.class)) {
            context.loadView(MainViewLoader.class);
            context.closeView(FloatViewLoader.class);
        }
    }

    public void changeFront(){
        Stage stage = context.getCurrentStageMap().get(FloatViewLoader.class);
        if (stage.isAlwaysOnTop()){
            stage.setAlwaysOnTop(false);
            stage.toBack();
            frontButton.setText("置顶");
        }else {
            stage.setAlwaysOnTop(true);
            stage.toFront();
            frontButton.setText("置底");
        }
    }

    public void exit() {
        context.closeView(FloatViewLoader.class);
    }

    public void refresh(){
        context.refreshData();
        this.refreshList();
    }

    private void refreshList(){
        ObservableList<FxDocument> fxDocuments = mainService.getFxDocuments();
        SortUtils.sort(fxDocuments,FxDocument.class,"lastModifyTime", SortUtils.SortType.DESC);
        contentListView.setItems(fxDocuments);
    }
}
