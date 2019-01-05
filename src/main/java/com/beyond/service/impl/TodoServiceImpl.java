package com.beyond.service.impl;

import com.beyond.ApplicationContext;
import com.beyond.MessageController;
import com.beyond.entity.Document;
import com.beyond.entity.FxDocument;
import com.beyond.entity.Todo;
import com.beyond.f.F;
import com.beyond.service.MainService;
import com.beyond.service.TodoService;
import com.beyond.viewloader.RemindViewLoader;
import com.beyond.viewloader.ViewLoader;
import javafx.application.Platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author beyondlov1
 * @date 2018/12/31
 */
public class TodoServiceImpl implements TodoService {

    private ApplicationContext context;

    private MainService mainService;

    public TodoServiceImpl(ApplicationContext context) {
        this.context = context;
    }

    public void init() {
        this.mainService = context.getMainService();
    }

    @Override
    public void deleteExpiredTodo() {
        List<Todo> todoList = this.getExpiredTodoList();
        mainService.bulkDeleteWithoutEvent(todoList);
        context.refresh();
    }

    @Override
    public void popup() {
        List<Todo> expiringTodoList = this.getExpiringTodoList();
        if (expiringTodoList.isEmpty()) {
            return;
        }
        for (Todo todo : expiringTodoList) {
            if (context.getRemindingViewLoaderMap().containsValue(todo.getId())) {
                continue;
            }
            ViewLoader remindViewLoader = new RemindViewLoader(context);
            remindViewLoader.setLocation("views/remind.fxml");
            MessageController messageController = new MessageController(context);
            FxDocument fxDocument = new FxDocument(todo);
            messageController.setFxDocument(fxDocument);
            messageController.setViewLoader(remindViewLoader);
            remindViewLoader.setController(messageController);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        remindViewLoader.load();
                        context.getRemindingViewLoaderMap().put(remindViewLoader, todo.getId());
                    } catch (IOException e) {
                        F.logger.error("页面加载错误", e);
                    }
                }
            });
        }
    }

    private List<Todo> getExpiredTodoList() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -30);// 过期todo延迟30分钟后过期
        Date expireDate = calendar.getTime();
        List<Todo> todoList = new ArrayList<>();
        List<Document> documents = mainService.findAllFromCache();
        for (Document document : documents) {
            if (document instanceof Todo) {
                Todo todo = (Todo) document;
                if (todo.getReminder() != null
                        && todo.getReminder().getRemindTime() != null
                        && todo.getReminder().getRemindTime().before(expireDate)) {
                    todoList.add(todo);
                }
            }
        }
        return todoList;
    }

    private List<Todo> getExpiringTodoList() {
        Date curr = new Date();
        Date preCurr = new Date(System.currentTimeMillis() - F.EXPIRE_TODO_DELETE_PERIOD);
        List<Todo> todoList = new ArrayList<>();
        List<Document> documents = mainService.findAllFromCache();
        for (Document document : documents) {
            if (document instanceof Todo) {
                Todo todo = (Todo) document;
                if (todo.getReminder() != null
                        && todo.getReminder().getRemindTime() != null
                        && todo.getReminder().getRemindTime().before(curr)
                        && todo.getReminder().getRemindTime().after(preCurr)) {
                    todoList.add(todo);
                }
            }
        }
        return todoList;
    }

}
