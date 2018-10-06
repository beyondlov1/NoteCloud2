package com.beyond;

import com.beyond.service.AuthService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

public class AuthController {

    @FXML
    private WebView webView;

    private MainApplication application;

    private AuthService authService;

    @FXML
    private void initialize(){
        authService = new AuthService();
        authService.setApplication(application);
        String authorizationUrl = authService.getAuthorizationUrl();
        webView.getEngine().load(authorizationUrl);
        webView.getEngine().locationProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String prefix = "https://login.microsoftonline.com/common/oauth2/nativeclient?code=";
                if (newValue.startsWith(prefix)){
                    String code = newValue.substring(prefix.length());
                    authService.getAccessToken(code);
                    application.getAuthStage().close();
                }
            }
        });
    }

    public void setApplication(MainApplication application) {
        this.application = application;
    }

    public MainApplication getApplication() {
        return application;
    }
}
