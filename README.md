# Geode Performance #

The Geode Performance project is used for running performance test on a GemFire cluster. 

The performance test simulates a Geode client running with a number of concurrent connections (controlled by the -C number of client connections) defined to the client connection pool and performing Geode get and put operations to a single region using the domain object as the region entry. The region entry key is comprised of the key header prefix with a 10 digit sequence starting with sequence number zero (0). The size of the domain object is controlled by the -S size argument.

Each callable thread runs a cycle that performs Geode get and put operations and the cycle is repeated after waiting for a defined idle time. After each cycle, the performance test checks to see if the test duration has expired and if not it starts the next cycle and continues until the performance test durtion time has expired. The number and the time to perform the get/put operations are captured for each read and write performed in the cycle and is saved in the Timing object.

During the initialization phase of the prtocess, the performance test test will seed the region with (100 X number of reads) (-R) objects and the read portition of the cycle gets a random key (from seeded objects) to perform the get operations.

Multiple performance tests can be run to simulate multiple clients connection to the Geode cluster.

***Example***

    java -cp conf/:lib/* util.geode.performance.Performance -C 100 -N TestPartition -R 10 -W 3 -I 100 -K TestPartitionKey -S 
    2048 -H 10.76.38.83 -P 10334 -E 5

The example performance test run with 100 (-C) active connections performing 10 (-R) get operations and 3 (-W) put operations in a cycle. After a cycle, the performance test sleeps for 100ms (-I) before starting the next cycle. The performance test writes to the TestPartition (-N) region a domain object with a object size (-S) of 2048. The domain object key is prefixed (-K) TestPartitionKey along with a connection id (0-99) and 10 digit sequence number. The performance test will run for a 5 minute (-E) duration and connects to locator host (-H) and ort (-P).\  

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

