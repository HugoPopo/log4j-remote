package com.smile.huber.spark.metrics.log.filter;

import com.smile.huber.spark.metrics.TimeSeriesHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * The real purpose of this class is not actually to filter, though it does it, discriminating logs not originating from MemoryStore or TaskMemoryManager.
 * However this behavior can be change to let all the logs through, simply by replacing DENY by NEUTRAL at the end of the decide method and deleting other returns.
 *
 * What this class actually does is catching com.smile.huber.spark.metrics.log messages describing memory allocation in a Spark application to generate time series and send them to a dedicated application.
 * It can identify what type of memory is allocated/released (Execution or Storage); of course the amount of operated memory, but also the id of the Task or Block the operation originates from.
 *
 * @author Hugo Bertrand
 */
public class MemoryFilter extends Filter {

    public static final String MEMORY_STORE_NAME = "org.apache.spark.storage.memory.MemoryStore";
    public static final String TASK_MEMORY_MANAGER_NAME = "org.apache.spark.memory.TaskMemoryManager";

    private TimeSeriesHandler handler = new TimeSeriesHandler("172.17.0.2", 2003, Runtime.getRuntime().totalMemory());

    private String executor = "driver";
    // TODO: get a unique identifier for the executor (executor id, IP address, etc.)

    public int decide(LoggingEvent loggingEvent) {
        String message = loggingEvent.getMessage().toString();
        String[] tokenizedMessage = message.split(" ");
        // catch logs for Storage memory
        if(loggingEvent.getLogger().getName().equals(MEMORY_STORE_NAME)){
            //
            if(tokenizedMessage[0].equals("Block")){
                String blockId = tokenizedMessage[1];
                // new block entry message example:
                // Block broadcast_3 stored as values in memory (estimated size 3.2 KB, free 366.0 MB)
                if(tokenizedMessage[2].equals("stored")){
                    Double blockSize = Double.parseDouble(tokenizedMessage[9]);
                    String unit = tokenizedMessage[10];
                    System.out.println("Storage: +"+blockSize+unit+" for block "+blockId+" at "+loggingEvent.timeStamp);
                    //TODO send to collector
                }
                // block drop example:
                // Block broadcast_2 of size 3272 dropped from memory (free 383812780)
                else if(tokenizedMessage[5].equals("dropped")){
                    Double blockSize = Double.parseDouble(tokenizedMessage[4]);
                    System.out.println("Storage: -"+blockSize+" bytes for block "+blockId+" at "+loggingEvent.timeStamp);
                    //TODO send to collector
                }
                return NEUTRAL;
            }
        }
        // catch logs about Execution memory
        else if(loggingEvent.getLogger().getName().equals(TASK_MEMORY_MANAGER_NAME)){
            // com.smile.huber.spark.metrics.log example:
            // TaskMemoryManager: Task 2 acquired 5.0 MB for org.apache.spark.util.collection.ExternalSorter@d96100a
            if(tokenizedMessage[0].equals("Task")){
                // data to produce time series
                String taskId = tokenizedMessage[1];
                double amount = Double.parseDouble(tokenizedMessage[3]);
                String unit = tokenizedMessage[4];
                String operation = tokenizedMessage[2];
                long timeStamp = loggingEvent.timeStamp;
                // allocation detection
                if(operation.equals("acquired")){
                    // print for debug
                    System.out.println("Execution: +"+amount+unit+" for Task "+taskId+" at "+loggingEvent.timeStamp+
                        " in "+loggingEvent.getLocationInformation().toString());
                    // send data to handler to produce time series data
                    handler.sendExecutionMetrics(timeStamp/1000, taskId, executor, amount, unit);
                }
                // release detection
                else if(operation.equals("released") || operation.equals("release")){
                    // print for debug
                    System.out.println("Execution: -"+amount+unit+" for Task "+taskId+" at "+loggingEvent.timeStamp+
                            " in "+loggingEvent.getLocationInformation().toString());
                    amount = -amount;
                    // send data to handler to produce time series data
                    handler.sendExecutionMetrics(timeStamp/1000, taskId, executor, amount, unit);
                }
                return NEUTRAL;
            }
        }
        return DENY;
    }
}
