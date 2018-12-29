package com.beyond.viewloader;

import com.beyond.ApplicationContext;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public interface ViewLoader {

    void load() throws IOException;

    void close();

    void setController(Object controller);

    void setLocation(String location);

    void setStage(Stage stage);

    Stage getStage();

    void setContext(ApplicationContext context);
}
