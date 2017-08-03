
### 1. start  zookeeper and kafka

```txt
➜  kafka_2.11-0.11.0.0 bin/zookeeper-server-start.sh config/zookeeper.properties

[2017-08-03 11:19:59,155] INFO Reading configuration from: config/zookeeper.properties (org.apache.zookeeper.server.quorum.QuorumPeerConfig)
```
```txt
➜  kafka_2.11-0.11.0.0 bin/kafka-server-start.sh config/server.properties

[2017-08-03 11:20:19,290] INFO KafkaConfig values:
	advertised.host.name = null
```
### 2. uberjar

```txt
➜  flambo-custom-receiver-kafka-example git:(master) ✗ lein uberjar
Compiling flambo-example.core
Created /Users/stevechan/Desktop/flambo-custom-receiver-kafka-example/target/flambo-example.jar
Created /Users/stevechan/Desktop/flambo-custom-receiver-kafka-example/target/flambo-example-standalone.jar
➜  flambo-custom-receiver-kafka-example git:(master) ✗

```
### 3. spark-submit run clojure standalone jar

```txt
➜  spark-2.2.0-bin-hadoop2.7  ./bin/spark-submit --class flambo_example.core  /Users/stevechan/Desktop/flambo-custom-receiver-kafka-example/target/flambo-example-standalone.jar 1
log4j:WARN No appenders could be found for logger (flambo-example.core).
log4j:WARN Please initialize the log4j system properly.
log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties
17/08/03 15:20:18 WARN SparkConf: The configuration key spark.akka.timeout is not supported any more because Spark doesn't use Akka since 2.0
17/08/03 15:20:18 INFO SparkContext: Running Spark version 2.2.0
17/08/03 15:20:18 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
17/08/03 15:20:18 WARN Utils: Your hostname, marching-mbp resolves to a loopback address: 127.0.0.1; using 10.8.8.12 instead (on interface en0)
17/08/03 15:20:18 WARN Utils: Set SPARK_LOCAL_IP if you need to bind to another address
17/08/03 15:20:18 INFO SparkContext: Submitted application: flambo-custom-receiver-kafka-eample
17/08/03 15:20:18 INFO SecurityManager: Changing view acls to: stevechan
17/08/03 15:20:18 INFO SecurityManager: Changing modify acls to: stevechan
17/08/03 15:20:18 INFO SecurityManager: Changing view acls groups to:
17/08/03 15:20:18 INFO SecurityManager: Changing modify acls groups to:
17/08/03 15:20:18 INFO SecurityManager: SecurityManager: authentication disabled; ui acls disabled; users  with view permissions: Set(stevechan); groups with view permissions: Set(); users  with modify permissions: Set(stevechan); groups with modify permissions: Set()
17/08/03 15:20:18 INFO Utils: Successfully started service 'sparkDriver' on port 60765.
17/08/03 15:20:19 INFO SparkEnv: Registering MapOutputTracker
17/08/03 15:20:19 INFO SparkEnv: Registering BlockManagerMaster
17/08/03 15:20:19 INFO BlockManagerMasterEndpoint: Using org.apache.spark.storage.DefaultTopologyMapper for getting topology information
17/08/03 15:20:19 INFO BlockManagerMasterEndpoint: BlockManagerMasterEndpoint up
17/08/03 15:20:19 INFO DiskBlockManager: Created local directory at /private/var/folders/9p/m3jr24_n6rx_pvzzd9zb1z7c0000gr/T/blockmgr-1bfead6e-0f26-4f20-a671-11681aa77e49
17/08/03 15:20:19 INFO MemoryStore: MemoryStore started with capacity 366.3 MB
17/08/03 15:20:19 INFO SparkEnv: Registering OutputCommitCoordinator
17/08/03 15:20:19 WARN Utils: Service 'SparkUI' could not bind on port 4040. Attempting port 4041.
17/08/03 15:20:19 INFO Utils: Successfully started service 'SparkUI' on port 4041.
17/08/03 15:20:19 INFO SparkUI: Bound SparkUI to 0.0.0.0, and started at http://10.8.8.12:4041
17/08/03 15:20:19 INFO SparkContext: Added JAR file:/Users/stevechan/Desktop/flambo-custom-receiver-kafka-example/target/flambo-example-standalone.jar at spark://10.8.8.12:60765/jars/flambo-example-standalone.jar with timestamp 1501744819462
17/08/03 15:20:19 INFO Executor: Starting executor ID driver on host localhost
17/08/03 15:20:19 INFO Utils: Successfully started service 'org.apache.spark.network.netty.NettyBlockTransferService' on port 60766.
17/08/03 15:20:19 INFO NettyBlockTransferService: Server created on 10.8.8.12:60766
17/08/03 15:20:19 INFO BlockManager: Using org.apache.spark.storage.RandomBlockReplicationPolicy for block replication policy
17/08/03 15:20:19 INFO BlockManagerMaster: Registering BlockManager BlockManagerId(driver, 10.8.8.12, 60766, None)
17/08/03 15:20:19 INFO BlockManagerMasterEndpoint: Registering block manager 10.8.8.12:60766 with 366.3 MB RAM, BlockManagerId(driver, 10.8.8.12, 60766, None)
17/08/03 15:20:19 INFO BlockManagerMaster: Registered BlockManager BlockManagerId(driver, 10.8.8.12, 60766, None)
17/08/03 15:20:19 INFO BlockManager: Initialized BlockManager: BlockManagerId(driver, 10.8.8.12, 60766, None)
17/08/03 15:20:20 INFO ReceiverTracker: Starting 1 receivers
17/08/03 15:20:20 INFO ReceiverTracker: ReceiverTracker started
17/08/03 15:20:20 INFO PluggableInputDStream: Slide time = 10000 ms
17/08/03 15:20:20 INFO PluggableInputDStream: Storage level = Serialized 1x Replicated
17/08/03 15:20:20 INFO PluggableInputDStream: Checkpoint interval = null
17/08/03 15:20:20 INFO PluggableInputDStream: Remember interval = 10000 ms
17/08/03 15:20:20 INFO PluggableInputDStream: Initialized and validated org.apache.spark.streaming.dstream.PluggableInputDStream@6417c10c
17/08/03 15:20:20 INFO MappedDStream: Slide time = 10000 ms
17/08/03 15:20:20 INFO MappedDStream: Storage level = Serialized 1x Replicated
17/08/03 15:20:20 INFO MappedDStream: Checkpoint interval = null
17/08/03 15:20:20 INFO MappedDStream: Remember interval = 10000 ms
17/08/03 15:20:20 INFO MappedDStream: Initialized and validated org.apache.spark.streaming.dstream.MappedDStream@3c100acd
17/08/03 15:20:20 INFO ForEachDStream: Slide time = 10000 ms
17/08/03 15:20:20 INFO ForEachDStream: Storage level = Serialized 1x Replicated
17/08/03 15:20:20 INFO ForEachDStream: Checkpoint interval = null
17/08/03 15:20:20 INFO ForEachDStream: Remember interval = 10000 ms
17/08/03 15:20:20 INFO ForEachDStream: Initialized and validated org.apache.spark.streaming.dstream.ForEachDStream@30acbdf2
17/08/03 15:20:20 INFO RecurringTimer: Started timer for JobGenerator at time 1501744830000
17/08/03 15:20:20 INFO JobGenerator: Started JobGenerator at 1501744830000 ms
17/08/03 15:20:20 INFO ReceiverTracker: Receiver 0 started
17/08/03 15:20:20 INFO JobScheduler: Started JobScheduler
17/08/03 15:20:20 INFO StreamingContext: StreamingContext started
17/08/03 15:20:20 INFO DAGScheduler: Got job 0 (start at NativeMethodAccessorImpl.java:0) with 1 output partitions
17/08/03 15:20:20 INFO DAGScheduler: Final stage: ResultStage 0 (start at NativeMethodAccessorImpl.java:0)
17/08/03 15:20:20 INFO DAGScheduler: Parents of final stage: List()
17/08/03 15:20:20 INFO DAGScheduler: Missing parents: List()
17/08/03 15:20:20 INFO DAGScheduler: Submitting ResultStage 0 (Receiver 0 ParallelCollectionRDD[0] at makeRDD at ReceiverTracker.scala:620), which has no missing parents
17/08/03 15:20:20 INFO MemoryStore: Block broadcast_0 stored as values in memory (estimated size 52.4 KB, free 366.2 MB)
17/08/03 15:20:20 INFO MemoryStore: Block broadcast_0_piece0 stored as bytes in memory (estimated size 17.4 KB, free 366.2 MB)
17/08/03 15:20:20 INFO BlockManagerInfo: Added broadcast_0_piece0 in memory on 10.8.8.12:60766 (size: 17.4 KB, free: 366.3 MB)
17/08/03 15:20:20 INFO SparkContext: Created broadcast 0 from broadcast at DAGScheduler.scala:1006
17/08/03 15:20:20 INFO DAGScheduler: Submitting 1 missing tasks from ResultStage 0 (Receiver 0 ParallelCollectionRDD[0] at makeRDD at ReceiverTracker.scala:620) (first 15 tasks are for partitions Vector(0))
17/08/03 15:20:20 INFO TaskSchedulerImpl: Adding task set 0.0 with 1 tasks
17/08/03 15:20:20 INFO TaskSetManager: Starting task 0.0 in stage 0.0 (TID 0, localhost, executor driver, partition 0, PROCESS_LOCAL, 5033 bytes)
17/08/03 15:20:20 INFO Executor: Running task 0.0 in stage 0.0 (TID 0)
17/08/03 15:20:20 INFO Executor: Fetching spark://10.8.8.12:60765/jars/flambo-example-standalone.jar with timestamp 1501744819462
17/08/03 15:20:20 INFO TransportClientFactory: Successfully created connection to /10.8.8.12:60765 after 28 ms (0 ms spent in bootstraps)
17/08/03 15:20:21 INFO Utils: Fetching spark://10.8.8.12:60765/jars/flambo-example-standalone.jar to /private/var/folders/9p/m3jr24_n6rx_pvzzd9zb1z7c0000gr/T/spark-0d5e4e40-af6f-461d-8914-533f9213ac62/userFiles-e6b92a55-78f1-47c3-b313-6e82d1ba13f5/fetchFileTemp7731014785379825370.tmp
17/08/03 15:20:21 INFO Executor: Adding file:/private/var/folders/9p/m3jr24_n6rx_pvzzd9zb1z7c0000gr/T/spark-0d5e4e40-af6f-461d-8914-533f9213ac62/userFiles-e6b92a55-78f1-47c3-b313-6e82d1ba13f5/flambo-example-standalone.jar to class loader
17/08/03 15:20:21 INFO RecurringTimer: Started timer for BlockGenerator at time 1501744821400
17/08/03 15:20:21 INFO BlockGenerator: Started BlockGenerator
17/08/03 15:20:21 INFO BlockGenerator: Started block pushing thread
17/08/03 15:20:21 INFO ReceiverTracker: Registered receiver for stream 0 from 10.8.8.12:60765
17/08/03 15:20:21 INFO ReceiverSupervisorImpl: Starting receiver 0
17/08/03 15:20:21 INFO core: Starting receiver
17/08/03 15:20:21 INFO ReceiverSupervisorImpl: Called receiver 0 onStart
17/08/03 15:20:21 INFO ReceiverSupervisorImpl: Waiting for receiver to be stopped
17/08/03 15:20:30 INFO JobScheduler: Added jobs for time 1501744830000 ms
17/08/03 15:20:30 INFO JobScheduler: Starting job streaming job 1501744830000 ms.0 from job set of time 1501744830000 ms
17/08/03 15:20:30 INFO SparkContext: Starting job: foreachPartition at NativeMethodAccessorImpl.java:0
17/08/03 15:20:30 INFO DAGScheduler: Job 1 finished: foreachPartition at NativeMethodAccessorImpl.java:0, took 0.000470 s
17/08/03 15:20:30 INFO JobScheduler: Finished job streaming job 1501744830000 ms.0 from job set of time 1501744830000 ms
17/08/03 15:20:30 INFO JobScheduler: Total delay: 0.184 s for time 1501744830000 ms (execution: 0.134 s)
17/08/03 15:20:30 INFO ReceivedBlockTracker: Deleting batches:
17/08/03 15:20:30 INFO InputInfoTracker: remove old batch metadata:
17/08/03 15:20:40 INFO JobScheduler: Added jobs for time 1501744840000 ms
17/08/03 15:20:40 INFO JobScheduler: Starting job streaming job 1501744840000 ms.0 from job set of time 1501744840000 ms
17/08/03 15:20:40 INFO SparkContext: Starting job: foreachPartition at NativeMethodAccessorImpl.java:0
17/08/03 15:20:40 INFO DAGScheduler: Job 2 finished: foreachPartition at NativeMethodAccessorImpl.java:0, took 0.000024 s
17/08/03 15:20:40 INFO JobScheduler: Finished job streaming job 1501744840000 ms.0 from job set of time 1501744840000 ms
17/08/03 15:20:40 INFO JobScheduler: Total delay: 0.046 s for time 1501744840000 ms (execution: 0.036 s)
17/08/03 15:20:40 INFO MapPartitionsRDD: Removing RDD 2 from persistence list
17/08/03 15:20:40 INFO BlockRDD: Removing RDD 1 from persistence list
17/08/03 15:20:40 INFO BlockManager: Removing RDD 2
17/08/03 15:20:40 INFO PluggableInputDStream: Removing blocks of RDD BlockRDD[1] at receiverStream at core.clj:93 of time 1501744840000 ms
17/08/03 15:20:40 INFO BlockManager: Removing RDD 1
17/08/03 15:20:40 INFO ReceivedBlockTracker: Deleting batches:
17/08/03 15:20:40 INFO InputInfoTracker: remove old batch metadata:
17/08/03 15:20:50 INFO JobScheduler: Added jobs for time 1501744850000 ms
17/08/03 15:20:50 INFO JobScheduler: Starting job streaming job 1501744850000 ms.0 from job set of time 1501744850000 ms
17/08/03 15:20:50 INFO SparkContext: Starting job: foreachPartition at NativeMethodAccessorImpl.java:0
17/08/03 15:20:50 INFO DAGScheduler: Job 3 finished: foreachPartition at NativeMethodAccessorImpl.java:0, took 0.000024 s
17/08/03 15:20:50 INFO JobScheduler: Finished job streaming job 1501744850000 ms.0 from job set of time 1501744850000 ms
17/08/03 15:20:50 INFO JobScheduler: Total delay: 0.068 s for time 1501744850000 ms (execution: 0.059 s)
17/08/03 15:20:50 INFO MapPartitionsRDD: Removing RDD 4 from persistence list
17/08/03 15:20:50 INFO BlockManager: Removing RDD 4
17/08/03 15:20:50 INFO BlockRDD: Removing RDD 3 from persistence list
17/08/03 15:20:50 INFO BlockManager: Removing RDD 3
17/08/03 15:20:50 INFO PluggableInputDStream: Removing blocks of RDD BlockRDD[3] at receiverStream at core.clj:93 of time 1501744850000 ms
17/08/03 15:20:50 INFO ReceivedBlockTracker: Deleting batches: 1501744830000 ms
17/08/03 15:20:50 INFO InputInfoTracker: remove old batch metadata: 1501744830000 ms
17/08/03 15:21:00 INFO JobScheduler: Added jobs for time 1501744860000 ms
17/08/03 15:21:00 INFO JobScheduler: Starting job streaming job 1501744860000 ms.0 from job set of time 1501744860000 ms
17/08/03 15:21:00 INFO SparkContext: Starting job: foreachPartition at NativeMethodAccessorImpl.java:0
17/08/03 15:21:00 INFO DAGScheduler: Job 4 finished: foreachPartition at NativeMethodAccessorImpl.java:0, took 0.000024 s
17/08/03 15:21:00 INFO JobScheduler: Finished job streaming job 1501744860000 ms.0 from job set of time 1501744860000 ms
17/08/03 15:21:00 INFO MapPartitionsRDD: Removing RDD 6 from persistence list
17/08/03 15:21:00 INFO JobScheduler: Total delay: 0.040 s for time 1501744860000 ms (execution: 0.035 s)
17/08/03 15:21:00 INFO BlockManager: Removing RDD 6
17/08/03 15:21:00 INFO BlockRDD: Removing RDD 5 from persistence list
17/08/03 15:21:00 INFO BlockManager: Removing RDD 5
17/08/03 15:21:00 INFO PluggableInputDStream: Removing blocks of RDD BlockRDD[5] at receiverStream at core.clj:93 of time 1501744860000 ms
17/08/03 15:21:00 INFO ReceivedBlockTracker: Deleting batches: 1501744840000 ms
17/08/03 15:21:00 INFO InputInfoTracker: remove old batch metadata: 1501744840000 ms
17/08/03 15:21:10 INFO JobScheduler: Added jobs for time 1501744870000 ms
17/08/03 15:21:10 INFO JobScheduler: Starting job streaming job 1501744870000 ms.0 from job set of time 1501744870000 ms
17/08/03 15:21:10 INFO SparkContext: Starting job: foreachPartition at NativeMethodAccessorImpl.java:0
17/08/03 15:21:10 INFO DAGScheduler: Job 5 finished: foreachPartition at NativeMethodAccessorImpl.java:0, took 0.000024 s
17/08/03 15:21:10 INFO JobScheduler: Finished job streaming job 1501744870000 ms.0 from job set of time 1501744870000 ms
17/08/03 15:21:10 INFO MapPartitionsRDD: Removing RDD 8 from persistence list
17/08/03 15:21:10 INFO JobScheduler: Total delay: 0.038 s for time 1501744870000 ms (execution: 0.031 s)
17/08/03 15:21:10 INFO BlockManager: Removing RDD 8
17/08/03 15:21:10 INFO BlockRDD: Removing RDD 7 from persistence list
17/08/03 15:21:10 INFO BlockManager: Removing RDD 7
17/08/03 15:21:10 INFO PluggableInputDStream: Removing blocks of RDD BlockRDD[7] at receiverStream at core.clj:93 of time 1501744870000 ms
17/08/03 15:21:10 INFO ReceivedBlockTracker: Deleting batches: 1501744850000 ms
17/08/03 15:21:10 INFO InputInfoTracker: remove old batch metadata: 1501744850000 ms
17/08/03 15:21:20 INFO JobScheduler: Added jobs for time 1501744880000 ms
17/08/03 15:21:20 INFO JobScheduler: Starting job streaming job 1501744880000 ms.0 from job set of time 1501744880000 ms
17/08/03 15:21:20 INFO SparkContext: Starting job: foreachPartition at NativeMethodAccessorImpl.java:0
17/08/03 15:21:20 INFO DAGScheduler: Job 6 finished: foreachPartition at NativeMethodAccessorImpl.java:0, took 0.000026 s
17/08/03 15:21:20 INFO JobScheduler: Finished job streaming job 1501744880000 ms.0 from job set of time 1501744880000 ms
17/08/03 15:21:20 INFO MapPartitionsRDD: Removing RDD 10 from persistence list
17/08/03 15:21:20 INFO JobScheduler: Total delay: 0.045 s for time 1501744880000 ms (execution: 0.038 s)
17/08/03 15:21:20 INFO BlockManager: Removing RDD 10
17/08/03 15:21:20 INFO BlockRDD: Removing RDD 9 from persistence list
17/08/03 15:21:20 INFO BlockManager: Removing RDD 9
17/08/03 15:21:20 INFO PluggableInputDStream: Removing blocks of RDD BlockRDD[9] at receiverStream at core.clj:93 of time 1501744880000 ms
17/08/03 15:21:20 INFO ReceivedBlockTracker: Deleting batches: 1501744860000 ms
17/08/03 15:21:20 INFO InputInfoTracker: remove old batch metadata: 1501744860000 ms
17/08/03 15:21:30 INFO JobScheduler: Added jobs for time 1501744890000 ms
17/08/03 15:21:30 INFO JobScheduler: Starting job streaming job 1501744890000 ms.0 from job set of time 1501744890000 ms
17/08/03 15:21:30 INFO SparkContext: Starting job: foreachPartition at NativeMethodAccessorImpl.java:0
17/08/03 15:21:30 INFO DAGScheduler: Job 7 finished: foreachPartition at NativeMethodAccessorImpl.java:0, took 0.000030 s
17/08/03 15:21:30 INFO JobScheduler: Finished job streaming job 1501744890000 ms.0 from job set of time 1501744890000 ms
17/08/03 15:21:30 INFO JobScheduler: Total delay: 0.035 s for time 1501744890000 ms (execution: 0.030 s)
17/08/03 15:21:30 INFO MapPartitionsRDD: Removing RDD 12 from persistence list
17/08/03 15:21:30 INFO BlockManager: Removing RDD 12
17/08/03 15:21:30 INFO BlockRDD: Removing RDD 11 from persistence list
17/08/03 15:21:30 INFO BlockManager: Removing RDD 11
17/08/03 15:21:30 INFO PluggableInputDStream: Removing blocks of RDD BlockRDD[11] at receiverStream at core.clj:93 of time 1501744890000 ms
17/08/03 15:21:30 INFO ReceivedBlockTracker: Deleting batches: 1501744870000 ms
17/08/03 15:21:30 INFO InputInfoTracker: remove old batch metadata: 1501744870000 ms
17/08/03 15:21:40 INFO JobScheduler: Added jobs for time 1501744900000 ms
17/08/03 15:21:40 INFO JobScheduler: Starting job streaming job 1501744900000 ms.0 from job set of time 1501744900000 ms
17/08/03 15:21:40 INFO SparkContext: Starting job: foreachPartition at NativeMethodAccessorImpl.java:0
17/08/03 15:21:40 INFO DAGScheduler: Job 8 finished: foreachPartition at NativeMethodAccessorImpl.java:0, took 0.000029 s
17/08/03 15:21:40 INFO JobScheduler: Finished job streaming job 1501744900000 ms.0 from job set of time 1501744900000 ms
17/08/03 15:21:40 INFO MapPartitionsRDD: Removing RDD 14 from persistence list
17/08/03 15:21:40 INFO JobScheduler: Total delay: 0.038 s for time 1501744900000 ms (execution: 0.029 s)
17/08/03 15:21:40 INFO BlockManager: Removing RDD 14
17/08/03 15:21:40 INFO BlockRDD: Removing RDD 13 from persistence list
17/08/03 15:21:40 INFO BlockManager: Removing RDD 13
17/08/03 15:21:40 INFO PluggableInputDStream: Removing blocks of RDD BlockRDD[13] at receiverStream at core.clj:93 of time 1501744900000 ms
17/08/03 15:21:40 INFO ReceivedBlockTracker: Deleting batches: 1501744880000 ms
17/08/03 15:21:40 INFO InputInfoTracker: remove old batch metadata: 1501744880000 ms
17/08/03 15:21:50 INFO JobScheduler: Added jobs for time 1501744910000 ms
17/08/03 15:21:50 INFO JobScheduler: Starting job streaming job 1501744910000 ms.0 from job set of time 1501744910000 ms
17/08/03 15:21:50 INFO SparkContext: Starting job: foreachPartition at NativeMethodAccessorImpl.java:0
17/08/03 15:21:50 INFO DAGScheduler: Job 9 finished: foreachPartition at NativeMethodAccessorImpl.java:0, took 0.000024 s
17/08/03 15:21:50 INFO JobScheduler: Finished job streaming job 1501744910000 ms.0 from job set of time 1501744910000 ms
17/08/03 15:21:50 INFO JobScheduler: Total delay: 0.036 s for time 1501744910000 ms (execution: 0.027 s)
17/08/03 15:21:50 INFO MapPartitionsRDD: Removing RDD 16 from persistence list
17/08/03 15:21:50 INFO BlockManager: Removing RDD 16
17/08/03 15:21:50 INFO BlockRDD: Removing RDD 15 from persistence list
17/08/03 15:21:50 INFO BlockManager: Removing RDD 15
17/08/03 15:21:50 INFO PluggableInputDStream: Removing blocks of RDD BlockRDD[15] at receiverStream at core.clj:93 of time 1501744910000 ms
17/08/03 15:21:50 INFO ReceivedBlockTracker: Deleting batches: 1501744890000 ms
17/08/03 15:21:50 INFO InputInfoTracker: remove old batch metadata: 1501744890000 ms
17/08/03 15:21:50 INFO StreamingContext: Invoking stop(stopGracefully=false) from shutdown hook
17/08/03 15:21:50 INFO ReceiverTracker: Sent stop signal to all 1 receivers
17/08/03 15:21:50 INFO ReceiverSupervisorImpl: Received stop signal
17/08/03 15:21:50 INFO ReceiverSupervisorImpl: Stopping receiver with message: Stopped by driver:
17/08/03 15:21:50 INFO core: Stopping receiver
17/08/03 15:21:50 INFO ReceiverSupervisorImpl: Called receiver onStop
17/08/03 15:21:50 INFO ReceiverSupervisorImpl: Deregistering receiver 0
17/08/03 15:21:50 ERROR ReceiverTracker: Deregistered receiver for stream 0: Stopped by driver
17/08/03 15:21:50 INFO ReceiverSupervisorImpl: Stopped receiver 0
17/08/03 15:21:50 INFO BlockGenerator: Stopping BlockGenerator
17/08/03 15:21:51 INFO RecurringTimer: Stopped timer for BlockGenerator after time 1501744911000
17/08/03 15:21:51 INFO BlockGenerator: Waiting for block pushing thread to terminate
17/08/03 15:21:51 INFO BlockGenerator: Pushing out the last 0 blocks
17/08/03 15:21:51 INFO BlockGenerator: Stopped block pushing thread
17/08/03 15:21:51 INFO BlockGenerator: Stopped BlockGenerator
17/08/03 15:21:51 INFO ReceiverSupervisorImpl: Stopped receiver without error
17/08/03 15:21:51 INFO Executor: Finished task 0.0 in stage 0.0 (TID 0). 709 bytes result sent to driver
17/08/03 15:21:51 INFO TaskSetManager: Finished task 0.0 in stage 0.0 (TID 0) in 90138 ms on localhost (executor driver) (1/1)
17/08/03 15:21:51 INFO TaskSchedulerImpl: Removed TaskSet 0.0, whose tasks have all completed, from pool
17/08/03 15:21:51 INFO DAGScheduler: ResultStage 0 (start at NativeMethodAccessorImpl.java:0) finished in 90.160 s
17/08/03 15:21:51 INFO ReceiverTracker: All of the receivers have deregistered successfully
17/08/03 15:21:51 INFO ReceiverTracker: ReceiverTracker stopped
17/08/03 15:21:51 INFO JobGenerator: Stopping JobGenerator immediately
17/08/03 15:21:51 INFO RecurringTimer: Stopped timer for JobGenerator after time 1501744910000
17/08/03 15:21:51 INFO JobGenerator: Stopped JobGenerator
17/08/03 15:21:51 INFO JobScheduler: Stopped JobScheduler
17/08/03 15:21:51 INFO StreamingContext: StreamingContext stopped successfully
17/08/03 15:21:51 INFO SparkContext: Invoking stop() from shutdown hook
17/08/03 15:21:51 INFO SparkUI: Stopped Spark web UI at http://10.8.8.12:4041
17/08/03 15:21:51 INFO MapOutputTrackerMasterEndpoint: MapOutputTrackerMasterEndpoint stopped!
17/08/03 15:21:51 INFO MemoryStore: MemoryStore cleared
17/08/03 15:21:51 INFO BlockManager: BlockManager stopped
17/08/03 15:21:51 INFO BlockManagerMaster: BlockManagerMaster stopped
17/08/03 15:21:51 INFO OutputCommitCoordinator$OutputCommitCoordinatorEndpoint: OutputCommitCoordinator stopped!
17/08/03 15:21:51 INFO SparkContext: Successfully stopped SparkContext
17/08/03 15:21:51 INFO ShutdownHookManager: Shutdown hook called
17/08/03 15:21:51 INFO ShutdownHookManager: Deleting directory /private/var/folders/9p/m3jr24_n6rx_pvzzd9zb1z7c0000gr/T/spark-0d5e4e40-af6f-461d-8914-533f9213ac62
➜  spark-2.2.0-bin-hadoop2.7

```
