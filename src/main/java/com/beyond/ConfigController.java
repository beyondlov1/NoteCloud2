package com.beyond;


import com.beyond.f.F;
import com.beyond.service.ConfigService;
import com.beyond.viewloader.AuthViewLoader;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
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
    @FXML
    private CheckBox microsoftEventSwitch;

    private ConfigService configService;

    private MainApplication application;

    private ApplicationContext context;

    public ConfigController(ApplicationContext context) {
        this.context = context;
    }

    public void initialize() {
        configService = context.getConfigService();
        application = context.getApplication();

        //获取设置值
        if (StringUtils.isNotBlank(F.NOTE_SUFFIX)) {
            noteSuffix.setText(F.NOTE_SUFFIX);
        }
        if (StringUtils.isNotBlank(F.TODO_SUFFIX)) {
            todoSuffix.setText(F.TODO_SUFFIX);
        }
        if (StringUtils.isNotBlank(F.DOC_SUFFIX)) {
            docSuffix.setText(F.DOC_SUFFIX);
        }
        if (StringUtils.isNotBlank(F.ACCESS_TOKEN)) {
            microsoftEventSwitch.setSelected(true);
        }
    }

    public void save() {
        if (StringUtils.isNotBlank(noteSuffix.getText())) {
            F.NOTE_SUFFIX = noteSuffix.getText();
            configService.setProperty("noteSuffix", F.NOTE_SUFFIX);
        }
        if (StringUtils.isNotBlank(todoSuffix.getText())) {
            F.TODO_SUFFIX = todoSuffix.getText();
            configService.setProperty("todoSuffix", F.TODO_SUFFIX);
        }
        if (StringUtils.isNotBlank(docSuffix.getText())) {
            F.DOC_SUFFIX = docSuffix.getText();
            configService.setProperty("docSuffix", F.DOC_SUFFIX);
        }
        configService.storeProperties();

        //跳转
        application.getConfigStage().close();
    }

    public void cancel() {
        application.getConfigStage().close();
    }

    public void accessMicrosoftEvent() {
        boolean isSelected = microsoftEventSwitch.isSelected();
        if (isSelected) {
            try {
                context.loadView(AuthViewLoader.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public MainApplication getApplication() {
        return application;
    }

    public void setApplication(MainApplication application) {
        this.application = application;
    }

}
