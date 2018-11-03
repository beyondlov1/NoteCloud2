package com.beyond;

import com.beyond.entity.User;
import com.beyond.f.F;
import com.beyond.f.SyncType;
import com.beyond.service.*;
import com.beyond.viewloader.*;
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


    private Scene mainScene;
    private Scene loginScene;
    private Scene configScene;
    private Scene authScene;

    private Stage primaryStage;
    private Stage mainStage;
    private Stage loginStage;
    private Stage configStage;
    private Stage authStage;

    private ApplicationContext context;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        context = new ApplicationContext();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setPrimaryStage(primaryStage);
        loadConfig();
        createContext();
        showStage();
    }

    private void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void createContext() {
        context.setLoginService(new LoginServiceNutStoreImpl());
        context.setSyncService(new SyncService());
        context.setApplication(this);
        context.setMainService(new MainService());
        context.setBindService(new BindService(context.getMainService().getFxDocuments()));
        context.setAsynRemindService(new AsynRemindServiceImpl(new SyncRemindServiceImpl()));
        context.setAuthService(new AuthService(context));
        context.setApplication(this);
        context.addObservable("onMerge", context.getSyncService().getMergeService());

        ViewLoader mainViewLoader = new MainViewLoader(context);
        mainViewLoader.setLocation("views/main.fxml");
        mainViewLoader.setStage(mainStage);
        mainViewLoader.setController(new MainController(context));
        context.addViewLoader(mainViewLoader);

        ViewLoader loginViewLoader = new LoginViewLoader(context);
        loginViewLoader.setLocation("views/login.fxml");
        loginViewLoader.setStage(loginStage);
        loginViewLoader.setController(new LoginController(context));
        context.addViewLoader(loginViewLoader);

        ViewLoader configViewLoader = new ConfigViewLoader(context);
        configViewLoader.setLocation("views/config.fxml");
        configViewLoader.setStage(configStage);
        configViewLoader.setController(new LoginController(context));
        context.addViewLoader(configViewLoader);

        ViewLoader authViewLoader = new AuthViewLoader(context);
        authViewLoader.setLocation("views/auth.fxml");
        authViewLoader.setStage(authStage);
        authViewLoader.setController(new AuthController(context));
        context.addViewLoader(authViewLoader);

    }

    private void showStage() throws Exception {
        User user = new User(
                F.configService.getProperty("username"),
                F.configService.getProperty("password"));
        if (isLogin(user)) {
            loadConfig();
            context.loadView(MainViewLoader.class);
        } else {
            context.loadView(LoginViewLoader.class);
        }
    }

    private void loadConfig() {
        ConfigService configService = context.getConfigService();
        if (configService == null) {
            configService = new ConfigService(F.CONFIG_PATH);
            context.setConfigService(configService);
        }
        configService.loadConfig(F.class);
    }

    private boolean isLogin(User user) {
        if (StringUtils.isNotBlank(user.getUsername()) && StringUtils.isNotBlank(user.getPassword())) {
            User login = context.getLoginService().login(user);
            return login != null;
        } else {
            return false;
        }
    }

    public Stage getAuthStage() {
        return authStage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Stage getMainStage() {
        return mainStage;
    }

    public Stage getLoginStage() {
        return loginStage;
    }

    public Stage getConfigStage() {
        return configStage;
    }
}