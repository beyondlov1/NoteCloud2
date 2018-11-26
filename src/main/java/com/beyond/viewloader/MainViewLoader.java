package com.beyond.viewloader;

import com.beyond.ApplicationContext;
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
        stopSynchronizeOnClose();
    }

    private void startSynchronize(){
        ApplicationContext context = this.getContext();
        AsynMergeService asynMergeService = context.getAsynMergeService();

        if (F.SYNC_TYPE == SyncType.LOOP) {
            asynMergeService.startSynchronize();
        }
    }

    private void stopSynchronizeOnClose(){
        ApplicationContext context = this.getContext();
        AsynMergeService asynMergeService = context.getAsynMergeService();
        Stage stage = this.getStage();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                asynMergeService.stopSynchronize();
            }
        });
    }
}
