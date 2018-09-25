package com.beyond.filter;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


public abstract class AbstractKeyFilter implements Filter {

    @Override
    public abstract Boolean filter();

    protected boolean isKey(KeyEvent keyEvent, KeyCode keyCode){
        return (keyEvent != null) && keyCode == keyEvent.getCode();
    }

}
