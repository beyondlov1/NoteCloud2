package com.beyond;


import com.beyond.f.F;
import com.beyond.service.ConfigService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class ConfigController {
    @FXML
    private TextField insertKey;
    @FXML
    private TextField updateKey;
    @FXML
    private TextField backKey;
    @FXML
    private TextField noteSuffix;
    @FXML
    private TextField todoSuffix;
    @FXML
    private TextField docSuffix;

    private ConfigService configService;

    private MainApplication application;

    public void initialize(){
        configService = new ConfigService(F.CONFIG_PATH);

        //获取设置值
        if (StringUtils.isNotBlank(F.NOTE_SUFFIX)){
            noteSuffix.setText(F.NOTE_SUFFIX);
        }
        if (StringUtils.isNotBlank(F.TODO_SUFFIX)){
            todoSuffix.setText(F.TODO_SUFFIX);
        }
        if (StringUtils.isNotBlank(F.DOC_SUFFIX)){
            docSuffix.setText(F.DOC_SUFFIX);
        }
    }

    public void save() {
        if (StringUtils.isNotBlank(noteSuffix.getText())){
            F.NOTE_SUFFIX = noteSuffix.getText();
            configService.setProperty("noteSuffix", F.NOTE_SUFFIX);
        }
        if (StringUtils.isNotBlank(todoSuffix.getText())){
            F.TODO_SUFFIX = todoSuffix.getText();
            configService.setProperty("todoSuffix", F.TODO_SUFFIX);
        }
        if (StringUtils.isNotBlank(docSuffix.getText())){
            F.DOC_SUFFIX = docSuffix.getText();
            configService.setProperty("docSuffix", F.DOC_SUFFIX);
        }
        configService.storeProperties();

        //跳转
        try {
            application.loadMainView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        //跳转
        try {
            application.loadMainView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MainApplication getApplication() {
        return application;
    }

    public void setApplication(MainApplication application) {
        this.application = application;
    }
}
