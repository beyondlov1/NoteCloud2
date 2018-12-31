package com.beyond;

import com.beyond.entity.User;
import com.beyond.f.F;
import com.beyond.service.*;
import com.beyond.service.impl.*;
import com.beyond.viewloader.*;
import com.sun.org.apache.bcel.internal.generic.FLOAD;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class MainApplication extends Application {

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
        loadConfig();
        createContext();
        showStartStage();
    }

    private void loadConfig() {
        ConfigService configService = context.getConfigService();
        if (configService == null) {
            configService = new ConfigServiceImpl(F.CONFIG_PATH);
            context.setConfigService(configService);
        }
        configService.loadPropertiesTo(F.class);
        //特殊处理
        F.DEFAULT_REMOTE_PATH = F.DEFAULT_REMOTE_ROOT_PATH + F.DEFAULT_REMOTE_RELATIVE_PATH;
        F.DEFAULT_REMOTE_FILE_INFO_PATH = F.DEFAULT_REMOTE_ROOT_PATH + F.DEFAULT_REMOTE_FILE_INFO_RELATIVE_PATH;
        F.DEFAULT_LOGIN_PATH = F.DEFAULT_REMOTE_ROOT_PATH;

    }
    private void createContext() {
        context.setApplication(this);
        if (F.DEFAULT_REMOTE_PATH.contains("teracloud")){
            context.setLoginService(new LoginServiceTeraImpl());
        }else {
            context.setLoginService(new LoginServiceNutStoreImpl());
        }
        context.setAsynMergeService(new AsynMergeService(context));
        context.setAuthService(new AuthService(context));
        context.setSyncRemindService(new SyncRemindServiceImpl(context.getAuthService()));
        context.setAsynRemindService(new AsynRemindServiceImpl(context.getSyncRemindService()));
        context.setFailedTodoService(new FailedTodoService(context));
        context.setAsynTodoService(new AsynTodoService(new TodoServiceImpl(context)));
        MainService mainService = new MainService(context);
        context.setMainService(mainService);
        mainService.init();

        ViewLoader mainViewLoader = new MainViewLoader(context);
        mainViewLoader.setLocation("views/main.fxml");
        MainController mainController = new MainController(context);
        mainViewLoader.setController(mainController);
        context.addViewLoader(mainViewLoader);

        ViewLoader loginViewLoader = new LoginViewLoader(context);
        loginViewLoader.setLocation("views/login.fxml");
        loginViewLoader.setController(new LoginController(context));
        context.addViewLoader(loginViewLoader);

        ViewLoader configViewLoader = new ConfigViewLoader(context);
        configViewLoader.setLocation("views/config.fxml");
        configViewLoader.setController(new ConfigController(context));
        context.addViewLoader(configViewLoader);

        ViewLoader authViewLoader = new AuthViewLoader(context);
        authViewLoader.setLocation("views/auth.fxml");
        authViewLoader.setController(new AuthController(context));
        context.addViewLoader(authViewLoader);

        ViewLoader floatViewLoader = new FloatViewLoader(context);
        floatViewLoader.setLocation("views/float.fxml");
        FloatController floatController = new FloatController(context);
        floatViewLoader.setController(floatController);
        context.addViewLoader(floatViewLoader);

        context.setMainController(mainController);
        context.setFloatController(floatController);

    }
    private void showStartStage() {
        User user = new User(
                F.configService.getProperty("username"),
                F.configService.getProperty("password"));
        if (isLogin(user)) {
            loadConfig();
            if ("true".equals(F.IS_FLOAT_PRIMARY)){
                context.loadView(FloatViewLoader.class);
            }else {
                context.loadView(MainViewLoader.class);
            }
        } else {
            context.loadView(LoginViewLoader.class);
        }
    }
    private boolean isLogin(User user) {
        if (StringUtils.isNotBlank(user.getUsername()) && StringUtils.isNotBlank(user.getPassword())) {
            User login = context.getLoginService().login(user);
            return login != null;
        } else {
            return false;
        }
    }

}