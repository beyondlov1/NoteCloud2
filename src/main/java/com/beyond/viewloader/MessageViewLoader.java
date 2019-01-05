package com.beyond.viewloader;

import com.beyond.ApplicationContext;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * @author beyondlov1
 * @date 2019/01/01
 */
public class MessageViewLoader extends AbstractViewLoader {

    public MessageViewLoader(ApplicationContext context) {
        super(context);
    }

    @Override
    protected void afterLoad() {

    }

    @Override
    protected void stopOnClose() {
        Stage stage = this.getStage();
        final ViewLoader viewLoader = this;
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                context.getRemindingViewLoaderMap().remove(viewLoader);
                if (context.getCurrentStageMap().isEmpty()
                        &&context.getRemindingViewLoaderMap().isEmpty()) {
                    context.exit();
                }
            }
        });
    }
}
