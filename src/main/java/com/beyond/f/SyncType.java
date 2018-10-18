package com.beyond.f;

public enum SyncType {
    LOOP("LOOP"), LAZY("LAZY");

    private String type;

    private SyncType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
