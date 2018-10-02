package com.beyond;

import com.beyond.entity.User;
import com.beyond.f.F;
import com.beyond.service.ConfigService;
import com.beyond.service.LoginService;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
    private Button login;

    private LoginService loginService;
    private ConfigService configService;

    private MainApplication application;

    @FXML
    private void initialize(){
        this.loginService = new LoginService();
        this.configService = new ConfigService(F.CONFIG_PATH);
        initViews();
        initListeners();
    }

    private void initViews() {
        String username = configService.getProperty("username");
        String password = configService.getProperty("password");
        if (StringUtils.isNotBlank(username)){
            usernameText.setText(username);
            usernameText.setFocusTraversable(false);
            rememberUsername.setSelected(true);
        }
        if (StringUtils.isNotBlank(password)){
            passwordText.setText(password);
            rememberPass.setSelected(true);
        }
    }

    private void initListeners(){
        usernameText.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode()== KeyCode.ENTER){
                    usernameText.setFocusTraversable(false);
                    passwordText.requestFocus();
                }
            }
        });
        passwordText.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode()== KeyCode.ENTER){
                    try {
                        login();
                    } catch (IOException e) {
                        e.printStackTrace();
                        F.logger.info(e.getMessage());
                    }
                }
            }
        });
    }

    @FXML
    public User login() throws IOException {
        String username = usernameText.getText();
        String password = passwordText.getText();
        System.out.println(username+"  "+password);
        if (StringUtils.isBlank(username)||StringUtils.isBlank(password)){
            msg.setText("用户名或密码为空");
            return null;
        }

        User user = new User(username,password);
        User login = loginService.login(user);
        if (login!=null){
            application.loadMainView();
            if (rememberUsername.isSelected()){
                configService.setProperty("username",username);
            }
            if (rememberPass.isSelected()){
                configService.setProperty("username",username);
                configService.setProperty("password",password);
            }
            configService.storeProperties();
        }else {
            msg.setText("用户名或密码错误");
        }
        return login;
    }

    @FXML
    public void rememberPass(){
        rememberUsername.setSelected(true);
    }

    public void setApplication(MainApplication application) {
        this.application = application;
    }

    public MainApplication getApplication() {
        return application;
    }
}
