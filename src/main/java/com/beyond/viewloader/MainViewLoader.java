package com.beyond.viewloader;

import com.beyond.ApplicationContext;
import com.beyond.FailedTodoService;
import com.beyond.f.F;
import com.beyond.f.SyncType;
import com.beyond.service.AsynMergeService;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

/**
 * @author beyondlov1
 * @date 2018/11/03
 */
public class MainViewLoader extends AbstractViewLoader {

    public MainViewLoader(ApplicationContext context) {
        super(context);
    }

    @Override
    protected void afterLoad() {
        startSynchronize();
        startFailedTodoService();

        this.stopOnClose();
    }

    //关闭时打开悬浮窗
    protected void stopOnClose() {
        Stage stage = this.getStage();
        Class<? extends ViewLoader> viewLoaderClass = this.getClass();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                context.removeCurrentStage(viewLoaderClass);
                if (!context.getCurrentStageMap().containsKey(FloatViewLoader.class)) {
                    context.loadView(FloatViewLoader.class);
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
