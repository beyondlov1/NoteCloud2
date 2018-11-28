package com.beyond.viewloader;

import com.beyond.ApplicationContext;
import com.beyond.MainApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * @author beyondlov1
 * @date 2018/11/03
 */
public abstract class AbstractViewLoader implements ViewLoader{

    private Stage stage;

    private String location;

    private Object controller;

    protected ApplicationContext context;

    public AbstractViewLoader(ApplicationContext context){
        this.context = context;
    }

    public AbstractViewLoader(Stage stage, String location, Object controller, ApplicationContext context) {
        this.stage = stage;
        this.location = location;
        this.controller = controller;
        this.context = context;
    }

    @Override
    public void load() throws IOException {
        if (stage==null){
            stage = new Stage();
        }
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL resource = MainApplication.class.getClassLoader().getResource(location);
        fxmlLoader.setLocation(resource);
        if (controller!=null){
            fxmlLoader.setController(controller);
        }
        Parent parent = fxmlLoader.load();
        stage.setScene(new Scene(parent));
        stage.setTitle("NoteCloud");
        stage.show();
        afterLoad();
    }

    @Override
    public void close(){
        stage.close();
    }

    protected abstract void afterLoad();

    public Object getController() {
        return controller;
    }

    public Stage getStage() {
        return stage;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public void setController(Object controller) {
        this.controller = controller;
    }

    @Override
    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setContext(ApplicationContext context) {
        this.context = context;
    }

    public ApplicationContext getContext() {
        return context;
    }
}
