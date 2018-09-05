# Log filter for Spark memory monitoring

This projects offers a way to monitor the different regions of [Spark Memory](https://0x0fff.com/spark-memory-management/). We will not discuss the extent of this problem here but rather focus on the solution. This document describes in detail the solution's behavior.

Spark uses [log4j](https://logging.apache.org/log4j/2.x/) to manage its logs. As so, memory allocation is traceable. Regarding the originating class of the log and the shape of its message, we can catch the memory allocations or releases. From that, we can generate time series data describing these events.

We then send this time series to a dedicated third party application. In our case, we use [Graphite](https://graphiteapp.org/). We also recommend to use it with [Grafana](https://grafana.com/) for better visualization.

# Inner architecture

## MemoryFilter

This [class](src/main/java/com/smile/huber/spark/metrics/log/filter/MemoryFilter.java) extends the [Filter](https://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/spi/Filter.html) class from Log4j. As so, its main methode is decide, which catches a loggingEvent object. This object contains the name of the sending class, the time stamp and the message of the log, among other information.

We first filter the logs regarding their originating class, like in [CategoryFilter](/src/java/main/com/smile/huber/spark.metrics.log.filter.CategoryFilter). In that case, we discriminate [TaskMemoryManager](https://github.com/apache/spark/blob/master/core/src/main/java/org/apache/spark/memory/TaskMemoryManager.java) for Execution memory and [MemoryStore](https://github.com/apache/spark/blob/master/core/src/main/scala/org/apache/spark/storage/memory/MemoryStore.scala).

When we have logs from either one these two classes, we tokenize the message to catch a pattern corresponding to the allocation of memory, as these types of messages have always the same structure. Knowing the token index of the amount of memory, we are able to produce time series for each memory regions, but also for each Task or Block of Storage.

## TimeSeriesHandler

This [class](src/main/java/com/smile/huber/spark/metrics/TimeSeriesHandler.java) is responsible for receiving the information about memory from MemoryFilter and generate time series entries, then sending them to the dedicated third party service.

Each time, it sends a batch of data: the total JVM heap size, the total allocated execution and storage memories, and the memory allocated by this task or block. Each amount is given in bytes. In order to do so, we use multiplier considering the unit specified in the log message. This can lead to approximation, but precision beyond megabyte is rarely required.

It stores as well the last value of each task or block in HashMaps.

It has an attribute of type MetricsConnector to interface with the metrics oriented service

## MetricsConnector and GraphiteConnector

[MetricsConnector](/src/main/java/com/smile/huber/spark/metrics/MetricsConnector.java) represents the connector to the third party service. The *sendMetrics* methods must use the specificities of this service to record time series.

[GraphiteConnector](src/main/java/com/smile/huber/spark/metrics/GraphiteConnector.java) is an implementation of MetricsConnector. It uses the [simplegraphiteclient](https://github.com/awin/simplegraphiteclient) project to interface with Graphite. The connection parameters are the domain name or IP address, and the port Graphite is listening on.

# How to use it

## Build the project

This is a simple maven project, you can build it using the classical mvn commands.

## Configure the metrics service

This project only connects with Graphite, but you can add your own connector to a third party service. You just need to implement MetricsConnector and define *sendMetrics* as it sends data to you service. Then set the *connector* attribute in TimeSeriesHandler to suit your new connector.

In the case of Graphite, as stated previously, you need to set the host  parameter and the port of the service, as parameters of a new GraphiteConnector object.

```java
private MetricsConnector connector = new GraphiteConnector("172.17.0.2", 2003);
```

## Use it with Spark

You just need to launch a Spark application (with *spark-shell* or *spark-submit*) with your project set as an additional jar file. For example:

```shell
spark-shell --driver-class-path target/appender-1.0-SNAPSHOT.jar
```

# Further work

There is still some work to be done. First, we need to find a way to get a unique identifier of the executor (it could be just IP address). Then there  could be some problem with the size of the Heap found in TimeSeriesHandler, leading to miscalculation of Spark Memory.