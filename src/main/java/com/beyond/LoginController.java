package com.beyond;

import com.beyond.entity.User;
import com.beyond.f.F;
import com.beyond.service.ConfigService;
import com.beyond.service.impl.ConfigServiceImpl;
import com.beyond.service.LoginService;
import com.beyond.viewloader.FloatViewLoader;
import com.beyond.viewloader.LoginViewLoader;
import com.beyond.viewloader.MainViewLoader;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class LoginController {
    @FXML
    private Text msg;
    @FXML
    private TextField usernameText;
    @FXML
    private PasswordField passwordText;
    @FXML
    private CheckBox rememberUsername;
    @FXML
    private CheckBox rememberPass;
    @FXML
    private Button loginButton;
    @FXML
    private ProgressIndicator progressIndicator;

    private LoginService loginService;

    private ConfigService configService;

    private ApplicationContext context;

    public LoginController(ApplicationContext context) {
        this.context = context;
    }

    public void initialize() {
        initService();
        initViews();
        initListeners();
    }
    private void initService(){
        this.loginService = context.getLoginService();
        this.configService = context.getConfigService();
    }
    private void initViews() {
        String username = configService.getProperty("username");
        String password = configService.getProperty("password");
        if (StringUtils.isNotBlank(username)) {
            usernameText.setText(username);
            usernameText.setFocusTraversable(false);
            rememberUsername.setSelected(true);
        }
        if (StringUtils.isNotBlank(password)) {
            passwordText.setText(password);
            rememberPass.setSelected(true);
        }
    }
    private void initListeners() {
        usernameText.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                msg.setText(null);
                if (event.getCode() == KeyCode.ENTER) {
                    usernameText.setFocusTraversable(false);
                    passwordText.requestFocus();
                }
            }
        });
        passwordText.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                msg.setText(null);
                if (event.getCode() == KeyCode.ENTER) {
                    try {
                        login();
                    } catch (Exception e) {
                        e.printStackTrace();
                        F.logger.info(e.getMessage());
                    }
                }
            }
        });
    }

    public void login() {
        loginingView();
        User user = getUser();
        if (isValid(user)){
            asynLogin(user);
        }
    }
    private void loginingView(){
        msg.setText("登陆中...");
        loginButton.setDisable(true);
    }
    private void asynLogin(User user) {
        String username = user.getUsername();
        String password = user.getPassword();
        Service<User> service = new Service<User>() {
            @Override
            protected Task<User> createTask() {
                return new Task<User>() {
                    @Override
                    protected User call() throws Exception {
                        return loginService.login(user);
                    }
                };
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                try {
                    if (getValue() != null) {
                        loginSuccess();
                    } else {
                        loginFail();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    F.logger.info(e.getMessage());
                    msg.setText("登录失败");
                }
            }

            private void loginSuccess() throws IOException {
                switchView();
                storeConfig();
            }

            private void storeConfig() {
                if (rememberUsername.isSelected()) {
                    configService.setProperty("username", username);
                    configService.storeProperties();
                }
                if (rememberPass.isSelected()) {
                    configService.setProperty("username", username);
                    configService.setProperty("password", password);
                    configService.storeProperties();
                }
            }

            private void switchView() throws IOException {
                context.closeView(LoginViewLoader.class);
                if ("true".equals(F.IS_FLOAT_PRIMARY)){
                    context.loadView(FloatViewLoader.class);
                }else {
                    context.loadView(MainViewLoader.class);
                }
            }

            private void loginFail(){
                msg.setText("用户名或密码错误");
                loginButton.setDisable(false);
                passwordText.setText(null);
                passwordText.requestFocus();
            }
        };
        service.start();
    }
    private User getUser() {
        String username = usernameText.getText();
        String password = passwordText.getText();
        return new User(username, password);
    }
    private boolean isValid(User user) {
        String username = user.getUsername();
        String password = user.getPassword();
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            msg.setText("用户名或密码为空");
            loginButton.setDisable(false);
            return false;
        }
        return true;
    }

    public void rememberPass() {
        rememberUsername.setSelected(true);
    }
}
