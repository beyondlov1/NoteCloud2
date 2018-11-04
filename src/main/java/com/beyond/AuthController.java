package com.beyond;

import com.beyond.service.AuthService;
import com.beyond.viewloader.AuthViewLoader;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

public class AuthController {

    @FXML
    private WebView webView;

    private AuthService authService;

    private ApplicationContext context;

    public AuthController(ApplicationContext context) {
        this.context = context;
    }

    @FXML
    private void initialize(){
        authService = context.getAuthService();

        String authorizationUrl = authService.getAuthorizationUrl();
        webView.getEngine().load(authorizationUrl);
        webView.getEngine().locationProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String prefix = "https://login.microsoftonline.com/common/oauth2/nativeclient?code=";
                if (newValue.startsWith(prefix)){
                    String code = newValue.substring(prefix.length());
                    authService.getAccessToken(code);
                    close();
                }
            }
        });
    }

    private void close(){
        context.closeView(AuthViewLoader.class);
    }
}
