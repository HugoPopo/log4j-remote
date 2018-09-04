package com.smile.huber.spark.metrics.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Example of an overwrite of the basic log4j appender
 */
public class CustomAppender extends AppenderSkeleton {

    private FileOutputStream fileOutputStream;

    public CustomAppender(){
        super();
        try{
            new FileOutputStream("/home/huber/Desktop/test.com.smile.huber.spark.metrics.log");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void close(){
    }

    public boolean requiresLayout() {
        return false;
    }

    protected void append(LoggingEvent loggingEvent) {
        String message = loggingEvent.getRenderedMessage();
        byte[] strToBytes = message.getBytes();
        try {
            fileOutputStream.write(strToBytes);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
