package com.beyond;

import com.beyond.entity.User;
import com.beyond.f.F;
import com.beyond.service.ConfigService;
import com.beyond.service.LoginService;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;

public class MainApplication extends Application {

    private Stage primaryStage;

    private Scene mainScene;

    private Scene loginScene;

    private Scene configScene;

    private ConfigService configService;

    private LoginService loginService;


    public static void main(String[] args) {
        launch(args);
    }

    public void init() {
        this.configService = new ConfigService(F.CONFIG_PATH);
        this.loginService = new LoginService();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        //加载配置文件
        if (StringUtils.isNotBlank(configService.getProperty("path.defaultLocalPath"))) {
            F.DEFAULT_LOCAL_PATH = configService.getProperty("path.defaultLocalPath");
        }
        if (StringUtils.isNotBlank(configService.getProperty("path.defaultDeletedPath"))) {
            F.DEFAULT_DELETE_PATH = configService.getProperty("path.defaultDeletedPath");
        }
        if (StringUtils.isNotBlank(configService.getProperty("path.defaultTmpPath"))) {
            F.DEFAULT_TMP_PATH = configService.getProperty("path.defaultTmpPath");
        }
        if (StringUtils.isNotBlank(configService.getProperty("path.defaultRemotePath"))) {
            F.DEFAULT_REMOTE_PATH = configService.getProperty("path.defaultRemotePath");
        }
        if (StringUtils.isNotBlank(configService.getProperty("period.syncPeriod"))) {
            F.SYNC_PERIOD = Long.valueOf(configService.getProperty("period.syncPeriod"));
        }
        if (StringUtils.isNotBlank(configService.getProperty("period.viewRefreshPeriod"))) {
            F.VIEW_REFRESH_PERIOD = Long.valueOf(configService.getProperty("period.viewRefreshPeriod"));
        }
        if (StringUtils.isNotBlank(configService.getProperty("noteSuffix"))) {
            F.NOTE_SUFFIX = configService.getProperty("noteSuffix");
        }
        if (StringUtils.isNotBlank(configService.getProperty("todoSuffix"))) {
            F.TODO_SUFFIX = configService.getProperty("todoSuffix");
        }
        if (StringUtils.isNotBlank(configService.getProperty("docSuffix"))) {
            F.DOC_SUFFIX = configService.getProperty("docSuffix");
        }

        //判斷能否登陸
        if (StringUtils.isNotBlank(configService.getProperty("username")) && StringUtils.isNotBlank(configService.getProperty("password"))) {
            F.USERNAME = configService.getProperty("username");
            F.PASSWORD = configService.getProperty("password");
            User user = new User(F.USERNAME,F.PASSWORD);
            User login = loginService.login(user);
            if (login!=null){
                loadMainView();
            }else {
                loadLoginView();
            }
        }else {
            loadLoginView();
        }
        primaryStage.show();
    }

    public Scene loadLoginView() throws IOException {

        if (loginScene!=null){
            primaryStage.setScene(loginScene);
            return loginScene;
        }

        URL loginResource = MainApplication.class.getClassLoader().getResource("views/login.fxml");
        FXMLLoader loginFxmlLoader = new FXMLLoader();
        loginFxmlLoader.setLocation(loginResource);
        loginFxmlLoader.setController(new LoginController());
        Parent loginParent = loginFxmlLoader.load();

        LoginController loginController= loginFxmlLoader.getController();
        loginController.setApplication(this);

        primaryStage.setTitle("NoteCloud");
        Scene scene = new Scene(loginParent);
        primaryStage.setScene(scene);
        return scene;
    }

    public Scene loadMainView() throws IOException {

        if (mainScene!=null){
            primaryStage.setScene(mainScene);
            return mainScene;
        }

        URL mainResource = MainApplication.class.getClassLoader().getResource("views/main.fxml");

        //加载fxml
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(mainResource);
        fxmlLoader.setController(new MainController());
        Parent parent = fxmlLoader.load();
        //Parent parent = FXMLLoader.load(Objects.requireNonNull(mainResource)); //这种方法不能获取到controller

        primaryStage.setTitle("NoteCloud");
        Scene scene = new Scene(parent);

        MainController controller = fxmlLoader.getController();
        controller.setApplication(this);
        controller.startSynchronize();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                controller.stopSynchronize();
            }
        });

        primaryStage.setScene(scene);
        return scene;
    }


    public Scene loadConfigView() throws IOException {

        if (configScene!=null){
            primaryStage.setScene(configScene);
            return configScene;
        }

        URL configResource = MainApplication.class.getClassLoader().getResource("views/config.fxml");

        //加载fxml
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(configResource);
        fxmlLoader.setController(new ConfigController());
        Parent parent = fxmlLoader.load();
        //Parent parent = FXMLLoader.load(Objects.requireNonNull(mainResource)); //这种方法不能获取到controller

        primaryStage.setTitle("NoteCloud");
        Scene scene = new Scene(parent);

        ConfigController controller = fxmlLoader.getController();
        controller.setApplication(this);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                //TODO: 关闭的时候要关闭同步线程
                controller.cancel();
            }
        });

        primaryStage.setScene(scene);
        return scene;
    }


}