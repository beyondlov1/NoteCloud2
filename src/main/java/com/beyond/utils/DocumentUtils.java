package com.beyond.utils;

import com.beyond.entity.Document;
import com.beyond.entity.Note;
import com.beyond.entity.Todo;
import com.beyond.f.F;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.UUID;

import static com.beyond.DocumentType.*;
import static com.beyond.DocumentType.DOC;

/**
 * @author beyondlov1
 * @date 2019/01/05
 */
public class DocumentUtils {

    /**
     * 根据内容创建document
     * @param content
     * @return
     */
    public static Document createDocument(String content) {
        Document document;
        int length = content.length();
        if (content.endsWith(NOTE.getType() + "\n")) {
            content = content.substring(0, length - NOTE.getType().length() - 1);
            Note note = new Note();
            note.setContent(content);
            document = note;
        } else if (content.endsWith(TODO.getType() + "\n")) {
            content = content.substring(0, length - TODO.getType().length() - 1);
            Todo todo = new Todo();
            todo.setContent(content);
            todo.setRemindTimeFromContent();
            document = todo;
        } else if (content.endsWith(DOC.getType() + "\n")) {
            content = content.substring(0, length - DOC.getType().length() - 1);
            document = new Document();
            document.setContent(content);
        } else {
            document = new Document();
            document.setContent(content);
        }

        Date curr = new Date();
        document.setId(UUID.randomUUID().toString().replace("-", ""));
        document.setCreateTime(curr);
        document.setLastModifyTime(curr);
        document.setVersion(1);
        document.setContent(F.CONTENT_PREFIX + document.getContent());

        return document;
    }

    /**
     * 校验内容和事件
     * @param content
     * @param keyEvent
     * @return
     */
    public static boolean validContentAndEvent(String content, KeyEvent keyEvent) {
        if (StringUtils.isNotBlank(content)) {
            int length = content.length();
            if (length > NOTE.getType().length() + 1 && content.endsWith(NOTE.getType() + "\n")) {
                return true;
            }
            if (length > TODO.getType().length() + 1 && content.endsWith(TODO.getType() + "\n")) {
                return true;
            }
            if (length > DOC.getType().length() + 1 && content.endsWith(DOC.getType() + "\n")) {
                return true;
            }
            if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.S) {
                return true;
            }
        }
        return false;
    }
}
