package com.beyond.viewloader;

import com.beyond.ApplicationContext;
import com.beyond.FailedTodoService;
import com.beyond.f.F;
import com.beyond.f.SyncType;
import com.beyond.service.AsynMergeService;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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

        stopOnClose();
    }

    private void stopOnClose() {
        Stage stage = this.getStage();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                stopSynchronize();
                stopFailedTodoService();
            }
        });
    }

    private void startSynchronize() {
        ApplicationContext context = this.getContext();
        AsynMergeService asynMergeService = context.getAsynMergeService();

        if (F.SYNC_TYPE == SyncType.LOOP) {
            asynMergeService.startSynchronize();
        }
    }
    private void stopSynchronize() {
        AsynMergeService asynMergeService = context.getAsynMergeService();
        asynMergeService.stopSynchronize();
    }

    private void startFailedTodoService() {
        FailedTodoService failedTodoService = context.getFailedTodoService();
        failedTodoService.init();
    }
    private void stopFailedTodoService() {
        FailedTodoService failedTodoService = context.getFailedTodoService();
        failedTodoService.stop();
    }
}
