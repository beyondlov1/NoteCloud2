package com.beyond.service;

import com.beyond.entity.MicrosoftReminder;
import com.beyond.entity.Todo;

/**
 * 未使用
 */
public interface RemindService {

    String addEvent(MicrosoftReminder reminder);

    String deleteEvent(MicrosoftReminder reminder);

    String deleteEvent(String remindId);

    String updateEvent(MicrosoftReminder reminder);

    MicrosoftReminder readEvent(String remindId);
}
