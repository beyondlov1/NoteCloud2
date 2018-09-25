package com.beyond;

public enum  DocumentType {

    NOTE("note"),TODO("todo"),DOC("end");

    private String type;

    private DocumentType(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
