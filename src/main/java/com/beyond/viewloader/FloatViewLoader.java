package com.beyond.viewloader;

import com.beyond.ApplicationContext;
import com.beyond.FailedTodoService;
import com.beyond.MainApplication;
import com.beyond.f.F;
import com.beyond.f.SyncType;
import com.beyond.service.AsynMergeService;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
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
        stage.setX(bounds.getWidth()-280);
        stage.setY(bounds.getHeight()-475);
        stage.setScene(new Scene(parent));
        stage.setTitle("NoteCloud");
        stage.show();
        afterLoad();
    }

    @Override
    protected void afterLoad() {
        this.getStage().setAlwaysOnTop(true);

        startSynchronize();
        startFailedTodoService();

        this.stopOnClose();
    }

    protected void stopOnClose() {
        Stage stage = this.getStage();
        Class<? extends ViewLoader> viewLoaderClass = this.getClass();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                try {
                    context.removeCurrentStage(viewLoaderClass);
                    if (!context.getCurrentStageMap().containsKey(MainViewLoader.class)) {
                        context.loadView(MainViewLoader.class);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

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
