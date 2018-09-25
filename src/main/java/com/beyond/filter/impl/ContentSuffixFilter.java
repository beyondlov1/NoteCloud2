package com.beyond.filter.impl;

import com.beyond.filter.AbstractStringFilter;
import org.apache.commons.lang3.StringUtils;

public class ContentSuffixFilter extends AbstractStringFilter {

    private String content;
    private String suffix;

    public ContentSuffixFilter(String suffix){
        this.suffix = suffix;
    }

    public ContentSuffixFilter(String content, String suffix) {
        this.content = content;
        this.suffix = suffix;
    }

    @Override
    public Boolean filter() {
        if (StringUtils.isNotBlank(suffix)){
            return content.endsWith(suffix);
        }
        return false;
    }

    public String getContent() {
        return content;
    }

    public String getSuffix() {
        return suffix;
    }
}
