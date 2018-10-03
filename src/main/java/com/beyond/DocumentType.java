package com.beyond;

import com.beyond.f.F;
import org.apache.commons.lang3.StringUtils;

public enum  DocumentType {

    NOTE(StringUtils.isBlank(F.NOTE_SUFFIX)?"note":F.NOTE_SUFFIX),
    TODO(StringUtils.isBlank(F.TODO_SUFFIX)?"todo": F.TODO_SUFFIX),
    DOC(StringUtils.isBlank(F.DOC_SUFFIX)?"end":F.DOC_SUFFIX);

    private String type;

    private DocumentType(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
