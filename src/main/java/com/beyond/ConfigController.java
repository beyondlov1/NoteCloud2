package com.beyond;


import com.beyond.f.F;
import com.beyond.service.ConfigService;
import com.beyond.service.impl.ConfigServiceImpl;
import com.beyond.viewloader.AuthViewLoader;
import com.beyond.viewloader.ConfigViewLoader;
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

    private ApplicationContext context;

    public ConfigController(ApplicationContext context) {
        this.context = context;
    }

    public void initialize() {
        initService();
        initViews();
    }
    private void initService() {
        configService = context.getConfigService();
    }
    private void initViews() {
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
        storeConfig();
        close();
    }
    public void cancel() {
        close();
    }
    private void storeConfig() {
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
    }
    private void close(){
        context.closeView(ConfigViewLoader.class);
    }

    public void accessMicrosoftEvent() {
        boolean isSelected = microsoftEventSwitch.isSelected();
        if (isSelected) {
            showAuthView();
        }
    }
    private void showAuthView() {
        try {
            context.loadView(AuthViewLoader.class);
        } catch (IOException e) {
            F.logger.info(e.getMessage());
        }
    }

}
