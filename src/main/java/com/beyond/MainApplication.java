package com.beyond;

import com.beyond.entity.User;
import com.beyond.f.F;
import com.beyond.service.ConfigService;
import com.beyond.service.LoginService;
import com.beyond.service.SyncService;
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

    private Scene authScene;

    private Stage authStage;

    private LoginService loginService;

    private SyncService syncService;


    public static void main(String[] args) {
        launch(args);
    }

    public void init() {
        this.loginService = new LoginService();
        this.syncService = new SyncService();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        //加载配置文件
        if (StringUtils.isNotBlank(F.configService.getProperty("path.defaultLocalPath"))) {
            F.DEFAULT_LOCAL_PATH = F.configService.getProperty("path.defaultLocalPath");
        }
        if (StringUtils.isNotBlank(F.configService.getProperty("path.defaultDeletedPath"))) {
            F.DEFAULT_DELETE_PATH = F.configService.getProperty("path.defaultDeletedPath");
        }
        if (StringUtils.isNotBlank(F.configService.getProperty("path.defaultTmpPath"))) {
            F.DEFAULT_TMP_PATH = F.configService.getProperty("path.defaultTmpPath");
        }
        if (StringUtils.isNotBlank(F.configService.getProperty("path.defaultRemotePath"))) {
            F.DEFAULT_REMOTE_PATH = F.configService.getProperty("path.defaultRemotePath");
        }
        if (StringUtils.isNotBlank(F.configService.getProperty("period.syncPeriod"))) {
            F.SYNC_PERIOD = Long.valueOf(F.configService.getProperty("period.syncPeriod"));
        }
        if (StringUtils.isNotBlank(F.configService.getProperty("period.viewRefreshPeriod"))) {
            F.VIEW_REFRESH_PERIOD = Long.valueOf(F.configService.getProperty("period.viewRefreshPeriod"));
        }
        if (StringUtils.isNotBlank(F.configService.getProperty("noteSuffix"))) {
            F.NOTE_SUFFIX = F.configService.getProperty("noteSuffix");
        }
        if (StringUtils.isNotBlank(F.configService.getProperty("todoSuffix"))) {
            F.TODO_SUFFIX = F.configService.getProperty("todoSuffix");
        }
        if (StringUtils.isNotBlank(F.configService.getProperty("docSuffix"))) {
            F.DOC_SUFFIX = F.configService.getProperty("docSuffix");
        }
        if (StringUtils.isNotBlank(F.configService.getProperty("ACCESS_TOKEN"))) {
            F.ACCESS_TOKEN = F.configService.getProperty("ACCESS_TOKEN");
        }
        if (StringUtils.isNotBlank(F.configService.getProperty("EXPIRE_DATE"))) {
            F.EXPIRE_DATE = F.configService.getProperty("EXPIRE_DATE");
        }
        if (StringUtils.isNotBlank(F.configService.getProperty("REFRESH_TOKEN"))) {
            F.REFRESH_TOKEN = F.configService.getProperty("REFRESH_TOKEN");
        }

        //判斷能否登陸
        if (StringUtils.isNotBlank(F.configService.getProperty("username")) && StringUtils.isNotBlank(F.configService.getProperty("password"))) {
            F.USERNAME = F.configService.getProperty("username");
            F.PASSWORD = F.configService.getProperty("password");
            User user = new User(F.USERNAME, F.PASSWORD);
            User login = loginService.login(user);
            if (login != null) {
                loadMainView();
            } else {
                loadLoginView();
            }
        } else {
            loadLoginView();
        }
        primaryStage.show();
    }

    public Scene loadLoginView() throws IOException {

        if (loginScene != null) {
            primaryStage.setScene(loginScene);
            return loginScene;
        }

        URL loginResource = MainApplication.class.getClassLoader().getResource("views/login.fxml");
        FXMLLoader loginFxmlLoader = new FXMLLoader();
        loginFxmlLoader.setLocation(loginResource);
        loginFxmlLoader.setController(new LoginController());
        Parent loginParent = loginFxmlLoader.load();

        LoginController loginController = loginFxmlLoader.getController();
        loginController.setApplication(this);

        primaryStage.setTitle("NoteCloud");
        loginScene = new Scene(loginParent);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                syncService.stopSynchronize();
            }
        });

        primaryStage.setScene(loginScene);
        return loginScene;
    }

    public Scene loadMainView() throws IOException {

        if (mainScene != null) {
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
        mainScene = new Scene(parent);

        MainController controller = fxmlLoader.getController();
        controller.setApplication(this);
        controller.startRefresh(syncService.getMergeService());
        syncService.startSynchronize();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                controller.stopRefresh();
                syncService.stopSynchronize();
            }
        });

        primaryStage.setScene(mainScene);
        return mainScene;
    }


    public Scene loadConfigView() throws IOException {

        if (configScene != null) {
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
        configScene = new Scene(parent);

        ConfigController controller = fxmlLoader.getController();
        controller.setApplication(this);

        Stage stage = new Stage();
        stage.setScene(configScene);
        stage.show();
        return configScene;
    }


    public Scene loadMicrosoftAuth() throws IOException {

        URL authResource = MainApplication.class.getClassLoader().getResource("views/auth.fxml");
        FXMLLoader authFxmlLoader = new FXMLLoader();
        authFxmlLoader.setLocation(authResource);
        authFxmlLoader.setController(new AuthController());
        Parent authParent = authFxmlLoader.load();

        AuthController authController = authFxmlLoader.getController();
        authController.setApplication(this);

        Stage stage = new Stage();
        stage.setTitle("NoteCloud");
        authScene = new Scene(authParent);
        stage.setScene(authScene);
        stage.show();
        this.authStage = stage;
        return authScene;
    }


    public Stage getAuthStage() {
        return authStage;
    }
}