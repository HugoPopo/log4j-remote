package com.huber.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CustomAppender extends AppenderSkeleton {

    private FileOutputStream fileOutputStream;

    public CustomAppender(){
        super();
        try{
            new FileOutputStream("/home/huber/Desktop/test.log");
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
