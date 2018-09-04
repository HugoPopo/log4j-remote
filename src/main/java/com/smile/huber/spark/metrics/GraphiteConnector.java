package com.smile.huber.spark.metrics;

import com.zanox.lib.simplegraphiteclient.SimpleGraphiteClient;

import java.util.Map;

public class GraphiteConnector implements MetricsConnector {

    private SimpleGraphiteClient graphite;

    public GraphiteConnector(String host, int port){
        this.graphite = new SimpleGraphiteClient(host, port);
    }

    public void sendMetrics(Map<String, Number> metrics, long timeStamp) {
        graphite.sendMetrics(metrics, timeStamp);
    }
}
