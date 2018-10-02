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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;

public class MainApplication extends Application {

    private Logger logger = LogManager.getLogger();

    private ConfigService configService;

    private LoginService loginService;

    public static void main(String[] args) {
        launch(args);
    }

    public void init() {
        String configPath ="config/config.properties";
        this.configService = new ConfigService(configPath);
        this.loginService = new LoginService();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
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

        //判斷能否登陸
        if (StringUtils.isNotBlank(configService.getProperty("username")) && StringUtils.isNotBlank(configService.getProperty("password"))) {
            String username = configService.getProperty("username");
            String password = configService.getProperty("password");
            User user = new User(username,password);
            User login = loginService.login(user);
            if (login!=null){
                F.USERNAME = configService.getProperty("username");
                F.PASSWORD = configService.getProperty("password");
                loadMainView(primaryStage);
            }else {
                loadLoginView(primaryStage);
            }
        }else {
            loadLoginView(primaryStage);
        }


    }

    private void loadLoginView(Stage primaryStage) throws IOException {
        URL loginResource = MainApplication.class.getClassLoader().getResource("views/login.fxml");
        FXMLLoader loginFxmlLoader = new FXMLLoader();
        loginFxmlLoader.setLocation(loginResource);
        loginFxmlLoader.setController(new LoginController());
        Parent loginParent = loginFxmlLoader.load();

        primaryStage.setTitle("NoteCloud");
        Scene scene = new Scene(loginParent);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadMainView(Stage primaryStage) throws IOException {
        URL mainResource = MainApplication.class.getClassLoader().getResource("views/main.fxml");

        //加载fxml
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(mainResource);
        fxmlLoader.setController(new MainController());
        Parent parent = fxmlLoader.load();
        //Parent parent = FXMLLoader.load(Objects.requireNonNull(mainResource)); //这种方法不能获取到controller

        primaryStage.setTitle("NoteCloud");
        Scene scene = new Scene(parent);
        primaryStage.setScene(scene);
        primaryStage.show();

        MainController controller = fxmlLoader.getController();
        controller.startSynchronize();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                controller.stopSynchronize();
            }
        });
    }
}