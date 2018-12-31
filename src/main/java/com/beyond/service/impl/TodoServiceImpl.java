package com.beyond.service.impl;

import com.beyond.ApplicationContext;
import com.beyond.RepositoryFactory;
import com.beyond.entity.Document;
import com.beyond.entity.FxDocument;
import com.beyond.entity.Todo;
import com.beyond.f.F;
import com.beyond.repository.Repository;
import com.beyond.service.MainService;
import com.beyond.service.TodoService;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.time.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author beyondlov1
 * @date 2018/12/31
 */
public class TodoServiceImpl implements TodoService {

    private ApplicationContext context;

    private Repository<Document> defaultLocalRepository;

    @SuppressWarnings("unchecked")
    public TodoServiceImpl(ApplicationContext context){
        this.context = context;
        this.defaultLocalRepository = RepositoryFactory.getLocalRepository(F.DEFAULT_LOCAL_PATH);
    }

    @Override
    public void deleteExpiredTodo() {
        List<Todo> todoList = this.getExpiredTodoList();
        this.bulkDeleteWithoutEvent(todoList);
        context.refresh();
    }

    private void bulkDeleteWithoutEvent(List<Todo> todoList) {
        Date current = new Date();
        for (Todo todo : todoList) {
            if (todo.getReminder()!=null
                    &&todo.getReminder().getRemindTime()!=null
                    && todo.getReminder().getRemindTime().before(current)){
                defaultLocalRepository.delete(todo);
                F.logger.info("delete expired todo, id:"+todo.getId());
            }
        }
        defaultLocalRepository.save();
    }

    private List<Todo> getExpiredTodoList() {
        List<Todo> todoList = new ArrayList<>();
        List<Document> documents = defaultLocalRepository.selectAll();
        for (Document document : documents) {
            if (document instanceof Todo){
                todoList.add((Todo) document);
            }
        }
        return todoList;
    }

}
