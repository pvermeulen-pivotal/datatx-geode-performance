# Geode Performance #

The Geode Performance project is used for running performance tests on a GemFire cluster. 

The performance test simulates a Geode client running with a number of concurrent connections (controlled by the -C argument) defined to the client connection pool and performing Geode get and put operations to a single region using the domain object as the region entry. The region entry key is comprised of the key header prefix and thread number folled by a dash "-" and a 10-digit sequence starting with sequence number zero (0). The size of the domain object is controlled by the -S size argument.

Each callable thread runs a cycle that performs Geode get and put operations. The cycle is repeated after waiting for a defined idle time (controlled by the -I argument in milliseconds). After each cycle, the performance test checks to see if the test duration has expired (controlled by the -E argument in minutes) and if the duration has not expired it starts the next cycle and continues until the performance test duration time has expired. The number and the times to perform the get/put operations are captured for each read and write performed in the cycle and is saved in the Timing object (The read rate is controlled by the -R argument and the write rate is controlled by the -W argument).

During the initialization phase, the performance test will seed the region with 100 times the number of reads (defined by the -R argument) objects and the read portion of the cycle gets a random key (from seeded objects) to perform the get operations.

Multiple performance tests can be run concurrently to simulate multiple client’s connection to the Geode cluster.

***Example***

    java -cp conf/:lib/* util.geode.performance.Performance -C 100 -N TestPartition -R 10 -W 3 -I 100  
    -K TestPartitionKey -S 2048 -H 10.76.38.83 -P 10334 -E 5

The example performance test runs with 100 (-C) active connections performing 10 (-R) get operations and 3 (-W) put operations in a cycle. After a cycle, the performance test sleeps for 100ms (-I) before starting the next cycle. The performance test writes to the TestPartition (-N) region, a domain object with an object size (-S) of 2048. The domain object key is prefixed (-K) TestPartitionKey along with a connection id (0-99) and a dash "-" and 10-digit sequence number. The performance test will run for a 5-minute (-E) duration and connects to locator host (-H) and port (-P).

### Modules ###

Performance - Main process module and thread manager  
PerformanceCallable - Performance callable worker thread  
Domain - Object written to the region  
Timing - Performance timing measurements

### Properties ###

log4j.properties - Performance test log4j logging properties  

### log4J Property File ###

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
-R [Number of reads to perform in a single cycle]  
-W [Number of writes to perform in a single cycle]  
-I [The wait or interval time between cycles in milliseconds]  
-K [The region key prefix for each thread]  
-S [Size of the object written to the region]  
-H [Locator host name or IP address]  
-P [Locator port number]  
-E [Length of the performance test in minutes]  

### Output ###

The performance test generates two (2) log files. 

performance-gemfire.log - GemFire client log  
performance.log - Performance log and report  

#### Performance Log ####

The following is a snippet of the performance report generated by the performance test.

[main] INFO  file - Performance Timing Region=TestPartition KeyHeader=TestPartitionKey98- DomainSize=2048 ReadRate:10  
                    WriteRate:3   
[main] INFO  file -      ReadCount=26150.0 AverageReadTime=0.7800382409177821ms TotalReadTime=20398.0ms  
[main] INFO  file -      WriteCount=7845.0 AverageWriteTime=1.5595920968769916ms TotalWriteTime=12235.0ms   
[main] INFO  file -  
[main] INFO  file - Performance Timing Region=TestPartition KeyHeader=TestPartitionKey99- DomainSize=2048 ReadRate:10  
                    WriteRate:3    
[main] INFO  file -      ReadCount=26230.0 AverageReadTime=0.7564239420510865ms TotalReadTime=19841.0ms  
[main] INFO  file -      WriteCount=7869.0 AverageWriteTime=1.5508959207014867ms TotalWriteTime=12204.0ms  
[main] INFO  file -  
[main] INFO  file - Performance Timing Total Region=TestPartition  
[main] INFO  file -      ReadCount=2618510.0 AverageReadTime=0.7757549140541758ms TotalReadTime=2031322.0ms  
[main] INFO  file -      WriteCount=785553.0 AverageWriteTime=1.5275977559757266ms TotalWriteTime=1200009.0ms  

### Build ###

Maven is used to build the project and uses an assembly file to create an archive zip file datatx-geode-performance-1.0.0.-SNAPSHOT-archive.zip.

When the archive datatx-geode-performance-1.0.0.-SNAPSHOT-archive.zip is unziped, a directory is created with the name of performance. Under the performance directory, three (3) sub-directories are created.

    performance
       conf/
       lib/
       logs/
      
#### start_performance.sh ####

    #!/bin/bash
    java -cp ./conf/:./lib/* util.geode.performance.Performance $*
