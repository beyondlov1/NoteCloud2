package com.beyond;

import com.beyond.entity.Reminder;
import com.beyond.f.F;
import com.beyond.service.*;
import com.beyond.viewloader.FloatViewLoader;
import com.beyond.viewloader.MainViewLoader;
import com.beyond.viewloader.ViewLoader;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author beyondlov1
 * @date 2018/10/19
 */
public class ApplicationContext {

    //attribute
    private Map<String, Object> map;

    //service
    private MainService mainService;
    private SyncRemindService<Reminder> syncRemindService;
    private AsynRemindService<Reminder> asynRemindService;
    private LoginService loginService;
    private AsynMergeService asynMergeService;
    private ConfigService configService;
    private AuthService authService;
    private FailedTodoService failedTodoService;
    private AsynTodoService asynTodoService;

    //application
    private MainApplication application;

    //main controller
    private MainController mainController;

    //float controller
    private FloatController floatController;

    //observe
    private Map<String, Observable> observableMap;

    //viewloader
    private Map<Class, ViewLoader> viewLoaderMap;

    //showed stages
    private Map<Class, Stage> currentStageMap;

    //showed reminders  viewloader:id
    private Map<ViewLoader,String> messageViewLoaderMap;

    //trayIcon
    private TrayIcon trayIcon;

    private ExecutorService executorService;

    public ApplicationContext() {
        this.map = new HashMap<>();
        this.observableMap = new HashMap<>();
        this.viewLoaderMap = new HashMap<>();
        this.currentStageMap = new HashMap<>();
        this.messageViewLoaderMap = new HashMap<>();
    }

    public void addObservable(String key, Observable observable) {
        observableMap.put(key, observable);
    }

    public void observe(Observer observer, String observableKey) {
        Observable observable = observableMap.get(observableKey);
        if (observable != null) {
            observable.addObserver(observer);
        }
    }

    public Map<String, Object> getAttributes() {
        return map;
    }

    public Object getAttribute(String key) {
        return map.get(key);
    }

    public void setAttribute(String key, String value) {
        map.put(key, value);
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

    public void addViewLoader(ViewLoader viewLoader) {
        viewLoaderMap.put(viewLoader.getClass(), viewLoader);
    }

    private ViewLoader getViewLoader(Class clazz) {
        ViewLoader viewLoader = viewLoaderMap.get(clazz);
        if (viewLoader == null) throw new RuntimeException("viewLoader not found");
        else return viewLoader;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public MainController getMainController() {
        return mainController;
    }

    public void loadView(Class<? extends ViewLoader> viewLoaderClass) {
        try {
            ViewLoader viewLoader = this.getViewLoader(viewLoaderClass);
            viewLoader.load();
            currentStageMap.put(viewLoaderClass, viewLoader.getStage());
        } catch (IOException e) {
            F.logger.error("页面加载出错", e);
        }
    }

    public void closeView(Class<? extends ViewLoader> viewLoaderClass) {
        ViewLoader viewLoader = getViewLoader(viewLoaderClass);
        viewLoader.close();
        currentStageMap.remove(viewLoaderClass);
    }

    public void refresh() {
        if (this.currentStageMap.containsKey(MainViewLoader.class)) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    mainController.refresh();
                }
            });
            return;
        }
        if (this.currentStageMap.containsKey(FloatViewLoader.class)){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    floatController.refresh();
                }
            });
            return;
        }
        this.refreshData();
    }

    public void refreshData(){
        //从文件获取文档
        mainService.pull();
        mainService.initFxDocument();
    }

    public void exit() {
        if (asynMergeService != null) {
            asynMergeService.stopSynchronize();
        }
        if (failedTodoService != null) {
            failedTodoService.stop();
        }
        if (asynTodoService != null){
            asynTodoService.stop();
        }
        if (trayIcon!=null){
            SystemTray.getSystemTray().remove(trayIcon);
        }
    }

    public void execute(Runnable runnable){
        if (executorService==null){
            this.executorService = Executors.newCachedThreadPool();
        }
        executorService.execute(runnable);
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

    public void addCurrentStage(Class<? extends ViewLoader> viewLoaderClass, Stage stage) {
        this.currentStageMap.put(viewLoaderClass, stage);
    }

    public void removeCurrentStage(Class<? extends ViewLoader> viewLoaderClass) {
        this.currentStageMap.remove(viewLoaderClass);
    }

    public Map<Class, Stage> getCurrentStageMap() {
        return currentStageMap;
    }


    public void setTrayIcon(TrayIcon trayIcon) {
        this.trayIcon = trayIcon;
    }

    public TrayIcon getTrayIcon() {
        return trayIcon;
    }

    public void setFloatController(FloatController floatController) {
        this.floatController = floatController;
    }

    public FloatController getFloatController() {
        return floatController;
    }

    public AsynTodoService getAsynTodoService() {
        return asynTodoService;
    }

    public void setAsynTodoService(AsynTodoService asynTodoService) {
        this.asynTodoService = asynTodoService;
    }

    public Map<ViewLoader, String> getMessageViewLoaderMap() {
        return messageViewLoaderMap;
    }

}
