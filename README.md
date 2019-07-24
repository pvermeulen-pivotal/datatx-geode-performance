# Geode Performance #

The Geode Performance project is used to run a performance test on a GemFire cluster.






### Modules ###

Performance - Main process module  
PerformanceCallable - Performance worker thread  
Domain - Region object  
Timing - Performance Timing   

### Properties ###

log4j.properties - Performance test log4j logging properties  

### Log4J Property File ###

log4j.rootLogger=INFO, file  
log4j.appender.file=org.apache.log4j.RollingFileAppender  
log4j.appender.file.File=logs/performance-test.log  
log4j.appender.file.MaxFileSize=2000KB  
log4j.appender.file.MaxBackupIndex=5  
log4j.appender.file.layout=org.apache.log4j.PatternLayout  
log4j.appender.file.layout.ConversionPattern=[%t] %-5p %c - %m%n  

### Command Line Arguments ###

-C [Number of client connections]  
-N [Region name]  
-R [Numbder of reads to perform in a single cycle]  
-W [Number of wrtites to perform in a single cycle]  
-I [The wait or interval time between cycles in milliseconds]  
-K [The region key prefix for each thread]  
-S [Size of the object written to the region]  
-H [Locator host name or IP addrerss]  
-P [Locator port number]  
-E [Length of the performance test in minutes]  



-C 100 -N TestPartition -R 10 -W 3 -I 100 -K TestPartitionKey -S 2048 -H 10.76.38.83 -P 10334 -E 5
