package com.beyond;

import com.beyond.entity.User;
import com.beyond.f.F;
import com.beyond.service.ConfigService;
import com.beyond.service.LoginService;
import com.beyond.viewloader.FloatViewLoader;
import com.beyond.viewloader.LoginViewLoader;
import com.beyond.viewloader.MainViewLoader;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.MenuItem;
import java.io.IOException;
import java.net.URL;

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

    private void initService() {
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
        if (isValid(user)) {
            asynLogin(user);
        }
    }

    private void loginingView() {
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
                addToTray();
            }

            private void addToTray() {
                try {
                    // ensure awt toolkit is initialized.
                    Toolkit.getDefaultToolkit();

                    // app requires system tray support, just exit if there is no support.
                    if (!SystemTray.isSupported()) {
                        System.out.println("No system tray support, application exiting.");
                        context.exit();
                        Platform.exit();
                    }

                    // set up a system tray icon.
                    SystemTray tray = SystemTray.getSystemTray();
                    URL imageLoc = new URL(
                            "http://icons.iconarchive.com/icons/scafer31000/bubble-circle-3/16/GameCenter-icon.png"
                    );
                    Image image = ImageIO.read(imageLoc);
                    TrayIcon trayIcon = new TrayIcon(image);
                    context.setTrayIcon(trayIcon);

                    // if the user double-clicks on the tray icon, show the main app stage.
                    trayIcon.addActionListener(event -> Platform.runLater(this::showMainStage));

                    // if the user selects the default menu item (which includes the app name),
                    // show the main app stage.
                    java.awt.MenuItem openItem = new java.awt.MenuItem("hello, world");
                    openItem.addActionListener(event -> Platform.runLater(this::showMainStage));

                    // the convention for tray icons seems to be to set the default icon for opening
                    // the application stage in a bold font.
                    Font defaultFont = Font.decode(null);
                    Font boldFont = defaultFont.deriveFont(Font.BOLD);
                    openItem.setFont(boldFont);

                    // to really exit the application, the user must go to the system tray icon
                    // and select the exit option, this will shutdown JavaFX and remove the
                    // tray icon (removing the tray icon will also shut down AWT).
                    java.awt.MenuItem exitItem = new MenuItem("Exit");
                    exitItem.addActionListener(event -> {
                        context.exit();
                        Platform.exit();
                    });

                    // setup the popup menu for the application.
                    final PopupMenu popup = new PopupMenu();
                    popup.add(openItem);
                    popup.addSeparator();
                    popup.add(exitItem);
                    trayIcon.setPopupMenu(popup);

                    // add the application tray icon to the system tray.
                    tray.add(trayIcon);
                } catch (AWTException | IOException e) {
                    F.logger.error("Unable to init system tray",e);
                }
            }

            private void showMainStage() {
                context.loadView(MainViewLoader.class);
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

            private void switchView() {
                context.closeView(LoginViewLoader.class);
                if ("true".equals(F.IS_FLOAT_PRIMARY)) {
                    context.loadView(FloatViewLoader.class);
                } else {
                    context.loadView(MainViewLoader.class);
                }
            }

            private void loginFail() {
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
