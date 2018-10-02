package com.beyond;

import com.beyond.entity.User;
import com.beyond.service.LoginService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;

public class LoginController {
    @FXML
    private Text msg;
    @FXML
    private TextField usernameText;
    @FXML
    private TextField passwordText;
    @FXML
    private CheckBox rememberUsername;
    @FXML
    private CheckBox rememberPass;
    @FXML
    private Button login;

    private LoginService loginService;

    @FXML
    private void initialize(){
        loginService = new LoginService();
    }

    public User login(){
        String username = usernameText.getText();
        String password = passwordText.getText();
        System.out.println(username+"  "+password);
        if (StringUtils.isBlank(username)||StringUtils.isBlank(password)){
            msg.setText("用户名或密码为空");
            return null;
        }

        User user = new User(username,password);
        return loginService.login(user);
    }
}
