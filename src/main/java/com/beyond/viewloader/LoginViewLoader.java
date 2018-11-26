package com.beyond.viewloader;

import com.beyond.ApplicationContext;
import com.beyond.service.AsynMergeService;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * @author beyondlov1
 * @date 2018/11/03
 */
public class LoginViewLoader extends AbstractViewLoader {
    public LoginViewLoader(ApplicationContext context) {
        super(context);
    }

    @Override
    protected void afterLoad() {
        Stage stage = this.getStage();
        AsynMergeService asynMergeService = this.getContext().getAsynMergeService();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                asynMergeService.stopSynchronize();
            }
        });
    }
}
