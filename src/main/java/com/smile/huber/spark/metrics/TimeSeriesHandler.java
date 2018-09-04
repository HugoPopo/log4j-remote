package com.smile.huber.spark.metrics;

import com.smile.huber.spark.metrics.GraphiteConnector;
import com.smile.huber.spark.metrics.MetricsConnector;

import java.util.HashMap;
import java.util.Map;

public class TimeSeriesHandler {

    //private SimpleGraphiteClient graphiteClient;
    private MetricsConnector connector = new GraphiteConnector("172.17.0.2", 2003);

    private HashMap<String, Long> unitMultiplier = new HashMap<String, Long>() {{
        put("B", (long) 1);
        put("KB", (long) 1024.0);
        put("MB", (long) (1024*1024));
        put("GB", (long) (1024*1024*1024.0));
    }};

    private double sparkMemory, executionMemory, storageMemory;

    private HashMap<String, Double> currentValuesForTasks = new HashMap<String, Double>();

    public TimeSeriesHandler(String ip, int port, long totalHeapSize){
        System.out.println("Total heap size: "+totalHeapSize+" ~"+(totalHeapSize/1024/1024)+" MB");
        this.sparkMemory = (totalHeapSize - 300*1024*1024)*0.6;
        System.out.println("Spark memory: "+this.sparkMemory+" ~"+(this.sparkMemory/1024/1024)+" MB");
    }

    public void sendExecutionMetrics(long timeStamp, String taskId, String executor, double amount, String unit){
        if(!currentValuesForTasks.containsKey(taskId)){
            currentValuesForTasks.put(taskId, amount*unitMultiplier.get(unit));
        } else {
            currentValuesForTasks.put(taskId, currentValuesForTasks.get(taskId) + amount*unitMultiplier.get(unit));
        }
        Map<String, Number> allAnswers = new HashMap<String, Number>();
        allAnswers.put(executor+".totalHeap", sparkMemory);
        allAnswers.put(executor+".execution", executionMemory);
        allAnswers.put(executor+".storage", storageMemory);
        allAnswers.put(executor+".task."+taskId, currentValuesForTasks.get(taskId));
        connector.sendMetrics(allAnswers, timeStamp);
    }
}
