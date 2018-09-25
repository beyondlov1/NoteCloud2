package com.beyond.filter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FilterContainer implements Filter{
    private List<Filter> filters;
    private FilterType filterType;

    private List<Filter> okFilters;

    public FilterContainer(FilterType filterType){
        this.filterType = filterType;
        this.filters = new LinkedList<>();
        this.okFilters = new ArrayList<>();
    }

    public void add(Filter filter) {
        filters.add(filter);
    }

    public Boolean filter(){
        boolean isOk = false;
        if (filterType == FilterType.AND){
            for (Filter filter : filters) {
                isOk = filter.filter();
                if (!isOk){
                    return false;
                }
                okFilters.add(filter);
            }
            return true;
        }

        if (filterType == FilterType.OR){
            for (Filter filter : filters) {
                isOk = filter.filter();
                if (isOk){
                    okFilters.add(filter);
                }
            }

            return !okFilters.isEmpty();
        }

        return null;
    }

    public enum FilterType {
        AND,OR
    }

    public List<Filter> getOkFilters() {
        return okFilters;
    }
}
