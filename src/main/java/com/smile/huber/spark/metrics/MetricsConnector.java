package com.smile.huber.spark.metrics;

import java.util.Map;

public interface MetricsConnector {
    public void sendMetrics(Map<String, Number> metrics, long timeStamp);
}
