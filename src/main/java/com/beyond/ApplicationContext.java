package com.beyond;

import com.beyond.entity.Reminder;
import com.beyond.service.*;
import com.beyond.service.impl.ConfigServiceImpl;
import com.beyond.service.impl.SyncRemindServiceImpl;
import com.beyond.viewloader.ViewLoader;
import javafx.application.Platform;

import java.io.IOException;
import java.util.*;

/**
 * @author beyondlov1
 * @date 2018/10/19
 */
public class ApplicationContext {

    private Map<String,Object> map;

    private MainService mainService;
    private SyncRemindService<Reminder> syncRemindService;
    private AsynRemindService<Reminder> asynRemindService;
    private LoginService loginService;
    private AsynMergeService asynMergeService;
    private ConfigService configService;
    private AuthService authService;
    private FailedTodoService failedTodoService;

    private MainApplication application;

    private MainController mainController;

    private Map<String,Observable> observableMap;
    private Map<Class, ViewLoader> viewLoaderMap;

    public ApplicationContext() {
        this.map = new HashMap<>();
        this.observableMap = new HashMap<>();
        this.viewLoaderMap = new HashMap<>();
    }

    public void addObservable(String key,Observable observable){
        observableMap.put(key,observable);
    }

    public void observe(Observer observer,String observableKey){
        Observable observable = observableMap.get(observableKey);
        if (observable!=null){
            observable.addObserver(observer);
        }
    }

    public Map<String, Object> getAttributes() {
        return map;
    }

    public Object getAttribute(String key){
        return map.get(key);
    }

    public void setAttribute(String key,String value){
        map.put(key,value);
    }

    public MainService getMainService() {
        return mainService;
    }

    public void setMainService(MainService mainService) {
        this.mainService = mainService;
    }

    public AsynRemindService<Reminder> getAsynRemindService() {
        return asynRemindService;
    }

    public void setAsynRemindService(AsynRemindService<Reminder> asynRemindService) {
        this.asynRemindService = asynRemindService;
    }

    public LoginService getLoginService() {
        return loginService;
    }

    public void setLoginService(LoginService loginService) {
        this.loginService = loginService;
    }

    public AsynMergeService getAsynMergeService() {
        return asynMergeService;
    }

    public void setAsynMergeService(AsynMergeService asynMergeService) {
        this.asynMergeService = asynMergeService;
    }

    public ConfigService getConfigService() {
        return configService;
    }

    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    public MainApplication getApplication() {
        return application;
    }

    public void setApplication(MainApplication application) {
        this.application = application;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void addViewLoader(ViewLoader viewLoader){
        viewLoaderMap.put(viewLoader.getClass(),viewLoader);
    }

    private ViewLoader getViewLoader(Class clazz) {
        ViewLoader viewLoader = viewLoaderMap.get(clazz);
        if (viewLoader==null) throw new RuntimeException("viewLoader not found");
        else return viewLoader;
    }

    public void setMainController(MainController mainController){
        this.mainController = mainController;
    }

    public void loadView(Class<? extends ViewLoader> viewLoaderClass) throws IOException {
        ViewLoader viewLoader = getViewLoader(viewLoaderClass);
        viewLoader.load();
    }

    public void closeView(Class<? extends ViewLoader> viewLoaderClass) {
        ViewLoader viewLoader = getViewLoader(viewLoaderClass);
        viewLoader.close();
    }

    public void refresh(){
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                mainController.refresh();
            }
        });
    }

    public SyncRemindService<Reminder> getSyncRemindService() {
        return syncRemindService;
    }

    public void setSyncRemindService(SyncRemindService<Reminder> syncRemindService) {
        this.syncRemindService = syncRemindService;
    }

    public FailedTodoService getFailedTodoService() {
        return failedTodoService;
    }

    public void setFailedTodoService(FailedTodoService failedTodoService) {
        this.failedTodoService = failedTodoService;
    }
}
