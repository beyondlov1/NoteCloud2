package com.beyond.service;

import com.beyond.FxDocument;
import com.beyond.MainController;
import com.beyond.entity.Todo;
import com.beyond.utils.*;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.apache.http.client.methods.RequestBuilder.delete;

/**
 * 绑定控件与数据服务
 */
public class BindService {

    private ObservableList<FxDocument> fxDocumentList;

    public BindService( ObservableList<FxDocument> fxDocumentList) {
        this.fxDocumentList = fxDocumentList;
    }

    public void init(MainController mainController){

        //order
        SortUtils.sort(fxDocumentList,FxDocument.class,"lastModifyTime",SortUtils.SortType.DESC);

        //init table
        initTable(mainController.getDocumentTableView());

        //initListener
        initListener(mainController);

        //init focus
        mainController.getContentTextAreaUpdate().setFocusTraversable(false);
        mainController.getContentTextAreaSave().setFocusTraversable(false);
        mainController.getTabPane().setFocusTraversable(false);
        focusTo(mainController.getDocumentTableView());
        mainController.getDocumentTableView().getSelectionModel().selectFirst();

        //init style
        mainController.getContentTextAreaUpdate().setWrapText(true);
        mainController.getContentTextAreaSave().setWrapText(true);

    }

    public void initTable(TableView<FxDocument> tableView) {
        tableView.setItems(fxDocumentList);
        ObservableList<TableColumn<FxDocument, ?>> columns = tableView.getColumns();
        for (TableColumn<FxDocument, ?> column : columns) {
            if (StringUtils.equals(column.getId(), "contentTableColumn")) {
                column.setCellFactory(getCellFactory());
                column.setCellValueFactory(getCellValueFactory(FxDocument.class, "content"));
            }
        }

    }

    public void initListener(MainController mainController) {
        TableView<FxDocument> documentTableView = mainController.getDocumentTableView();
        WebView webView = mainController.getWebView();
        TextArea updateTextArea = mainController.getContentTextAreaUpdate();
        TextArea saveTextArea = mainController.getContentTextAreaSave();

        /*
         * container E D Listener
         */
        mainController.getDocumentTableView().setOnKeyPressed(new EventHandler<KeyEvent>() {
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
                    initUpdateTextArea(updateTextArea, newValue);
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
                        mainController.delete();
                        break;
                    case I:
                        saveTextArea.requestFocus();
                        //TODO: changeInputMethod
                        changeInputMethod();
                        break;
                    case U:
                    case LEFT:
                        updateTextArea.requestFocus();
                        updateTextArea.positionCaret(updateTextArea.getText().length());
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
        saveTextArea.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ESCAPE)
                    focusTo(documentTableView);
            }
        });
        updateTextArea.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ESCAPE)
                    focusTo(documentTableView);
            }
        });
    }

    public ObservableList<FxDocument> getFxDocumentList() {
        return fxDocumentList;
    }

    public void setFxDocumentList(ObservableList<FxDocument> fxDocumentList) {
        this.fxDocumentList = fxDocumentList;
    }

    private void changeInputMethod() {

    }

    private void focusTo(Node node) {
        node.requestFocus();
    }

    private void initUpdateTextArea(TextArea updateTextArea, FxDocument fxDocument) {
        updateTextArea.setText(fxDocument.getContent());
    }

    public void initWebView(WebView webView, FxDocument fxDocument) {
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

    private <S, T> Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> getCellValueFactory(Class<FxDocument> sClass, String propertyName) {
        return new Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>>() {
            @Override
            public ObservableValue<T> call(TableColumn.CellDataFeatures<S, T> param) {
                return (Property) ReflectUtils.getValueByField(sClass, param.getValue(), propertyName);
            }
        };
    }

}
