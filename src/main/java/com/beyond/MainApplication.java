package com.beyond;

import com.beyond.entity.User;
import com.beyond.f.F;
import com.beyond.service.*;
import com.beyond.viewloader.*;
import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

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
        showStage();
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
        mainViewLoader.setController(new MainController(context));
        context.addViewLoader(mainViewLoader);

        ViewLoader loginViewLoader = new LoginViewLoader(context);
        loginViewLoader.setLocation("views/login.fxml");
        loginViewLoader.setController(new LoginController(context));
        context.addViewLoader(loginViewLoader);

        ViewLoader configViewLoader = new ConfigViewLoader(context);
        configViewLoader.setLocation("views/config.fxml");
        configViewLoader.setController(new LoginController(context));
        context.addViewLoader(configViewLoader);

        ViewLoader authViewLoader = new AuthViewLoader(context);
        authViewLoader.setLocation("views/auth.fxml");
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
}