package com.beyond.filter.impl;

import com.beyond.filter.AbstractKeyFilter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class KeyFilter extends AbstractKeyFilter {
    private KeyEvent keyEvent;
    private KeyCode keyCode;
    private AssistKey assistKey;

    public KeyFilter(){

    }

    public KeyFilter(KeyEvent keyEvent, KeyCode keyCode) {
        this.keyEvent = keyEvent;
        this.keyCode = keyCode;
    }

    public KeyFilter(KeyEvent keyEvent,  KeyCode keyCode, AssistKey assistKey) {
        this.keyEvent = keyEvent;
        this.keyCode = keyCode;
        this.assistKey = assistKey;
    }

    @Override
    public Boolean filter() {
        if (keyEvent == null) return false;
        if (assistKey != null) {
            boolean isAssistKeyDown = false;
            switch (assistKey) {
                case ALT:
                    isAssistKeyDown = keyEvent.isAltDown();
                    break;
                case CTRL:
                    isAssistKeyDown = keyEvent.isControlDown();
                    break;
                default:
                    break;
            }

            if (isAssistKeyDown) {
                return isKey(keyEvent, keyCode);
            }else {
                return false;
            }
        }else {
            return isKey(keyEvent, keyCode);
        }

    }

    public enum AssistKey {
        CTRL, ALT
    }

    public KeyEvent getKeyEvent() {
        return keyEvent;
    }

    public void setKeyEvent(KeyEvent keyEvent) {
        this.keyEvent = keyEvent;
    }

    public KeyCode getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(KeyCode keyCode) {
        this.keyCode = keyCode;
    }

    public AssistKey getAssistKey() {
        return assistKey;
    }

    public void setAssistKey(AssistKey assistKey) {
        this.assistKey = assistKey;
    }
}
