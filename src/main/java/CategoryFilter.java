package com.huber.log;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class CategoryFilter extends Filter {

    private String categoryName;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int decide(LoggingEvent loggingEvent) {
        System.out.println("Filter- expected:"+this.categoryName+" actual:"+loggingEvent.getLogger().getName()+"\n");

        if(loggingEvent.getLogger().getName().equals(this.categoryName))
            return NEUTRAL;
        return DENY;
    }
}
