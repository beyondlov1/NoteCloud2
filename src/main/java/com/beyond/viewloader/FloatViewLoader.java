package com.beyond.viewloader;

import com.beyond.ApplicationContext;
import com.beyond.FailedTodoService;
import com.beyond.MainApplication;
import com.beyond.f.F;
import com.beyond.f.SyncType;
import com.beyond.service.AsynMergeService;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;

/**
 * @author beyondlov1
 * @date 2018/12/08
 */
public class FloatViewLoader extends AbstractViewLoader {

    public FloatViewLoader(ApplicationContext context) {
        super(context);
    }

    @Override
    public void load() throws IOException {
        Stage stage = this.getStage();
        String location = this.getLocation();
        Object controller = this.getController();
        if (stage == null) {
            stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);//状态栏不显示图标和窗口不显示装饰不可兼得
            this.setStage(stage);
        }
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL resource = MainApplication.class.getClassLoader().getResource(location);
        fxmlLoader.setLocation(resource);
        if (controller != null) {
            fxmlLoader.setController(controller);
        }
        Parent parent = fxmlLoader.load();
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getBounds();
        stage.setX(bounds.getWidth());
        stage.setY(bounds.getHeight() - 475);
        stage.setOpacity(0.3);
        stage.setScene(new Scene(parent));
        stage.setTitle("NoteCloud");
        stage.show();
        afterLoad();
    }

    @Override
    protected void afterLoad() {
        Stage stage = this.getStage();
        stage.setAlwaysOnTop(true);

        this.startSynchronize();
        this.startFailedTodoService();

        this.initEventHandler(stage);
        this.stopOnClose();
    }

    private void initEventHandler(Stage stage) {
        stage.getScene().setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Screen screen = Screen.getPrimary();
                Rectangle2D bounds = screen.getBounds();
                stage.setX(bounds.getWidth() - 290 - 35);
                stage.setOpacity(1);
                stage.requestFocus();
            }
        });

        stage.getScene().setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Screen screen = Screen.getPrimary();
                Rectangle2D bounds = screen.getBounds();
                stage.setX(bounds.getWidth());
                stage.setOpacity(0.3);
            }
        });
    }

    //关闭时打开主页面
//    protected void stopOnClose() {
//        Stage stage = this.getStage();
//        Class<? extends ViewLoader> viewLoaderClass = this.getClass();
//        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
//            @Override
//            public void handle(WindowEvent event) {
//                    context.removeCurrentStage(viewLoaderClass);
//                    if (!context.getCurrentStageMap().containsKey(MainViewLoader.class)) {
//                        context.loadView(MainViewLoader.class);
//                    }
//            }
//        });
//    }

    private void startSynchronize() {
        ApplicationContext context = this.getContext();
        AsynMergeService asynMergeService = context.getAsynMergeService();

        if (F.SYNC_TYPE == SyncType.LOOP) {
            if (asynMergeService.getTimer() == null) {
                asynMergeService.startSynchronize();
            }
        }
    }

    private void startFailedTodoService() {
        FailedTodoService failedTodoService = context.getFailedTodoService();
        if (!failedTodoService.isRunning()) {
            failedTodoService.init();
        }
    }


}
