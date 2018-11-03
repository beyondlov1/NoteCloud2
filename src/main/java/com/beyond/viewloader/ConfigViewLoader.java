package com.beyond.viewloader;

import com.beyond.ApplicationContext;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * @author beyondlov1
 * @date 2018/11/03
 */
public class ConfigViewLoader extends AbstractViewLoader {

    public ConfigViewLoader(ApplicationContext context) {
        super(context);
    }

    @Override
    protected void afterLoad() {
        Stage stage = getStage();
        stage.setTitle("Configure");
    }
}
