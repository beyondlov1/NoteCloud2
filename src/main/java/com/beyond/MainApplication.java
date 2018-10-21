package com.beyond;

import com.beyond.entity.User;
import com.beyond.f.F;
import com.beyond.f.SyncType;
import com.beyond.service.*;
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

    public void createContext() {
        context = new ApplicationContext();
        context.setLoginService(new LoginServiceNutStoreImpl());
        context.setSyncService(new SyncService());
        context.setApplication(this);
        context.setMainService(new MainService());
        context.setBindService(new BindService(context.getMainService().getFxDocuments()));
        context.setAsynRemindService(new AsynRemindServiceImpl(new SyncRemindServiceImpl()));
        context.setAuthService(new AuthService());

        context.addObservable("onMerge", context.getSyncService().getMergeService());

//        if (F.SYNC_TYPE == SyncType.LAZY) {
//            try {
//                Object localDocumentRepository = ReflectUtils.getSourceObjectFromProxy(context.getMainService().getDefaultLocalRepository(), LocalDocumentRepositoryProxy.class,LocalDocumentRepository.class);
//                context.addObservable("onDefaultLocalRepositoryChanged", (LocalDocumentRepository) localDocumentRepository);
//                context.observe(context.getSyncService(), "onDefaultLocalRepositoryChanged");
//            } catch (NoSuchFieldException | IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        //加载配置文件
        ConfigService configService = new ConfigService(F.CONFIG_PATH);
        configService.loadConfig(F.class);

        this.primaryStage = primaryStage;

        //创建上下文
        createContext();
        context.setConfigService(configService);

        //判斷能否登陸
        if (StringUtils.isNotBlank(F.configService.getProperty("username")) && StringUtils.isNotBlank(F.configService.getProperty("password"))) {
            F.USERNAME = F.configService.getProperty("username");
            F.PASSWORD = F.configService.getProperty("password");
            User user = new User(F.USERNAME, F.PASSWORD);
            User login = context.getLoginService().login(user);
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
            primaryStage.show();
            return loginScene;
        }

        URL loginResource = MainApplication.class.getClassLoader().getResource("views/login.fxml");
        FXMLLoader loginFxmlLoader = new FXMLLoader();
        loginFxmlLoader.setLocation(loginResource);
        loginFxmlLoader.setController(new LoginController(context));
        Parent loginParent = loginFxmlLoader.load();

        LoginController loginController = loginFxmlLoader.getController();
        loginController.setContext(context);

        primaryStage.setTitle("NoteCloud");
        loginScene = new Scene(loginParent);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                context.getSyncService().stopSynchronize();
            }
        });

        primaryStage.setScene(loginScene);
        return loginScene;
    }

    public Scene loadMainView() throws IOException {

        if (mainScene != null) {
            if (mainStage == null) {
                mainStage = new Stage();
            }
            mainStage.setScene(mainScene);
            mainStage.show();
            return mainScene;
        }

        URL mainResource = MainApplication.class.getClassLoader().getResource("views/main.fxml");

        //加载fxml
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(mainResource);
        fxmlLoader.setController(new MainController(context));
        Parent parent = fxmlLoader.load();
        //Parent parent = FXMLLoader.load(Objects.requireNonNull(mainResource)); //这种方法不能获取到controller
        mainScene = new Scene(parent);

        //创建stage
        mainStage = new Stage();
        mainStage.setTitle("NoteCloud");
        mainStage.setScene(mainScene);

        final SyncService syncService = context.getSyncService();
        MainController controller = fxmlLoader.getController();
        context.observe(controller, "onMerge");

        if (F.SYNC_TYPE == SyncType.LOOP) {
            syncService.startSynchronize();
        }
        if (F.SYNC_TYPE == SyncType.LAZY) {
            context.addObservable("mainController",controller);
            context.observe(syncService,"mainController");
        }

        mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                syncService.stopSynchronize();
            }
        });

        mainStage.show();
        return mainScene;
    }


    public Scene loadConfigView() throws IOException {

        if (configScene != null) {
            if (configStage == null) {
                configStage = new Stage();
            }
            configStage.setScene(configScene);
            configStage.show();
            return configScene;
        }

        URL configResource = MainApplication.class.getClassLoader().getResource("views/config.fxml");

        //加载fxml
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(configResource);
        fxmlLoader.setController(new ConfigController(context));
        Parent parent = fxmlLoader.load();
        //Parent parent = FXMLLoader.load(Objects.requireNonNull(mainResource)); //这种方法不能获取到controller
        configScene = new Scene(parent);

        configStage = new Stage();
        configStage.setTitle("NoteCloud");
        configStage.setScene(configScene);
        configStage.show();
        return configScene;
    }


    public Scene loadMicrosoftAuth() throws IOException {

        URL authResource = MainApplication.class.getClassLoader().getResource("views/auth.fxml");
        FXMLLoader authFxmlLoader = new FXMLLoader();
        authFxmlLoader.setLocation(authResource);
        authFxmlLoader.setController(new AuthController(context));
        Parent authParent = authFxmlLoader.load();

//        AuthController authController = authFxmlLoader.getController();

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