package com.beyond;

public enum DocumentAction {
    SAVE("SAVE"),UPDATE("UPDATE"),DELETE("DELETE"),NULL("NULL"),SAVE_OR_UPDATE("SAVE_OR_UPDATE");
    private String action;
    private DocumentAction(String action) {
        this.action = action;
    }
}
