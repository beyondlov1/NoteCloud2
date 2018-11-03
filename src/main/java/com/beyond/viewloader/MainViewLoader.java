package com.beyond.viewloader;

import com.beyond.ApplicationContext;
import com.beyond.MainController;
import com.beyond.f.F;
import com.beyond.f.SyncType;
import com.beyond.service.SyncService;
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
        SyncService syncService = context.getSyncService();
        MainController controller = (MainController) this.getController();

        context.observe(controller, "onMerge");

        if (F.SYNC_TYPE == SyncType.LOOP) {
            syncService.startSynchronize();
        }
        if (F.SYNC_TYPE == SyncType.LAZY) {
            context.addObservable("mainController",controller);
            context.observe(syncService,"mainController");
        }
    }

    private void stopSynchronizeOnClose(){
        ApplicationContext context = this.getContext();
        SyncService syncService = context.getSyncService();
        Stage stage = this.getStage();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                syncService.stopSynchronize();
            }
        });
    }
}
