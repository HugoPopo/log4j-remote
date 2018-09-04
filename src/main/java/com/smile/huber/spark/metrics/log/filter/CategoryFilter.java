package com.smile.huber.spark.metrics.log.filter;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A com.smile.huber.spark.metrics.log filter which discriminates on the originating class of the com.smile.huber.spark.metrics.log.
 * This class is specified in properties file.
 *
 * This filter is exclusive and allows only logs from the given class,
 * however it can be turned into an inclusive filter,
 * by replacing here NEUTRAL by ACCEPT and DENY by NEUTRAL.
 *
 * @author Hugo Bertrand
 */
public class CategoryFilter extends Filter {

    private String categoryName;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int decide(LoggingEvent loggingEvent) {
        if(loggingEvent.getLogger().getName().equals(this.categoryName))
            return NEUTRAL;
        return DENY;
    }
}
