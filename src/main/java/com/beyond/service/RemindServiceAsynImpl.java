package com.beyond.service;

import com.beyond.callback.Callback;
import com.beyond.entity.MicrosoftReminder;
import com.beyond.entity.Todo;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 未使用
 */
public class RemindServiceAsynImpl implements RemindServiceAsyn {

    private RemindService remindServiceRemote;

    public RemindServiceAsynImpl(RemindService remindServiceRemote){
        this.remindServiceRemote = remindServiceRemote;
    }


    @Override
    public void addEvent(MicrosoftReminder reminder, Callback callback) {
        Service service = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        return remindServiceRemote.addEvent(reminder);
                    }
                };
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                callback.call(getValue());
            }
        };
        service.start();
        
    }

    @Override
    public void deleteEvent(MicrosoftReminder reminder, Callback callback) {
        Service service = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        return remindServiceRemote.deleteEvent(reminder);
                    }
                };
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                callback.call(getValue());
            }
        };
        service.start();
        
    }

    @Override
    public void deleteEvent(String remindId, Callback callback) {
        Service service = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        return remindServiceRemote.deleteEvent(remindId);
                    }
                };
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                callback.call(getValue());
            }
        };
        service.start();
        
    }

    @Override
    public void updateEvent(MicrosoftReminder reminder, Callback callback) {
        Service service = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        return remindServiceRemote.updateEvent(reminder);
                    }
                };
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                callback.call(getValue());
            }
        };
        service.start();
    }

    @Override
    public void readEvent(String remindId, Callback callback) {
        Service<MicrosoftReminder> service = new Service<MicrosoftReminder>() {
            @Override
            protected Task<MicrosoftReminder> createTask() {
                return new Task<MicrosoftReminder>() {
                    @Override
                    protected MicrosoftReminder call() throws Exception {
                        return remindServiceRemote.readEvent(remindId);
                    }
                };
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                callback.call(getValue());
            }
        };
        service.start();
        
    }
}
