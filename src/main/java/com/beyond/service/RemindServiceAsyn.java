package com.beyond.service;

import com.beyond.callback.Callback;
import com.beyond.entity.MicrosoftReminder;

/**
 * 未使用
 */
public interface RemindServiceAsyn {

    void addEvent(MicrosoftReminder reminder, Callback callback);

    void deleteEvent(MicrosoftReminder reminder, Callback callback);

    void deleteEvent(String remindId, Callback callback);

    void updateEvent(MicrosoftReminder reminder, Callback callback);

    void readEvent(String remindId, Callback callback);
}
