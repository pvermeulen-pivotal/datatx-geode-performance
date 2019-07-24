# Geode Performance #

The Geode Performance project is used to run a performance test on a GemFire cluster.






### Modules ###
   Performance - Main process module
   PerformanceCallable - Performance worker thread
   Domain - Region object 
   Timing - Performance Timing

### Command Line Arguments ###
-C [Number of client connections]
-N [Region name]
-R [Numbder of reads to perform in a single cycle
-W [Number of wrtites to perform in a single cycle]
-I [The wait or interval time between cycles in milliseconds]
-K [The region key prefix for each thread]
-S [Size of the object written to the region]
-H [Locator host name or IP addrerss]
-P [Locator port number]
-E [Length of the performance test in minutes]



-C 100 -N TestPartition -R 10 -W 3 -I 100 -K TestPartitionKey -S 2048 -H 10.76.38.83 -P 10334 -E 5
