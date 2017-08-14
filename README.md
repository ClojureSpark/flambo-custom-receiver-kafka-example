
### 用Clojure和Java混合编译最易成功: 命令行发送消息 + Java处理分析

```bash
➜  ~ which k_g_msg
k_g_msg: aliased to   /Users/clojure/SparkPro/kafka_2.11-0.11.0.0/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic

➜  ~ which k_list
k_list: aliased to    /Users/clojure/SparkPro/kafka_2.11-0.11.0.0/bin/kafka-topics.sh --list --zookeeper localhost:2181

➜  ~ k_list
__consumer_offsets
dashboard
messages
w4u_messages

➜  ~ k_g_msg w4u_messages
>我叫Steve桥布施
>

```

```clojure
flambo-example.zero=> (SparkStringConsumer/main (into-array String [""]))
...
17/08/12 10:07:09 INFO VerifiableProperties: Property zookeeper.connect is overridden to
----------null===========我叫Steve桥布施
17/08/12 10:07:09 INFO Executor: Finished task 0.0 in stage 1.0 (TID 1). 708 bytes result sent to driver
...
```
### Spark和Clojure互操作: 看flambo源码,涉及闭包会更复杂一点

```clojure
;; w4u.clj 重写 SparkStringConsumer.java ok
flambo-example.w4u=> (-main 1)
17/08/14 11:41:54 INFO w4u: =====org.apache.spark.api.java.JavaPairRDD@5b8f6c34=====1502682114000 ms
17/08/14 11:42:00 INFO KafkaRDD: Computing topic w4u_messages, partition 0 offsets 46 -> 47
17/08/14 11:42:00 INFO VerifiableProperties: Verifying properties
17/08/14 11:42:00 INFO VerifiableProperties: Property group.id is overridden to
17/08/14 11:42:00 INFO VerifiableProperties: Property zookeeper.connect is overridden to
17/08/14 11:42:00 INFO w4u: *********(null,乔布斯)*****
17/08/14 11:42:00 INFO Executor: Finished task 0.0 in stage 6.0 (TID 6). 666 bytes result sent to driver
```
# ----------------------------------------------------------------------

### 0. develop 最新版的spark2.2.0, 支持repl跑spark-submit
* @clojurians-org: repl 跑必须加 ` (conf/master "local[*]") `
```clojure
(def c (-> (conf/spark-conf)
           (conf/master "local[*]")
           (conf/app-name "flambo-custom-receiver-kafka-eample")
           (conf/set "spark.akka.timeout" "1000")
           (conf/set-executor-env env)))
```
* run in repl
```clojure
flambo-example.core=> (-main "10")
17/08/05 08:34:04 INFO core: Starting!
17/08/05 08:34:11 INFO core: Starting receiver
17/08/05 08:34:11 INFO core: Store: 1
17/08/05 08:34:11 INFO core: Store: 2
17/08/05 08:34:12 INFO core: Store: 3
17/08/05 08:34:12 INFO core: Store: 4
17/08/05 08:34:12 INFO core: Store: 5
17/08/05 08:34:13 INFO core: Store: 6
17/08/05 08:34:13 INFO core: Store: 7
17/08/05 08:34:13 INFO core: Store: 8
17/08/05 08:34:13 INFO core: Store: 9
17/08/05 08:34:20 INFO core: ---->>> Sending to Kafka fizzbuzz [4 4] , class java.lang.String
17/08/05 08:34:20 INFO core: ---->>> Sending to Kafka fizzbuzz [1 1] , class java.lang.String
17/08/05 08:34:20 INFO core: ---->>> Sending to Kafka fizzbuzz [3 "Fizz"] , class java.lang.String
17/08/05 08:34:20 INFO core: ---->>> Sending to Kafka fizzbuzz [2 2] , class java.lang.String
17/08/05 08:34:20 INFO core: Metadata for  [3 "Fizz"]  is  {:offset 434, :partition 0, :topic "fizzbuzz"}
17/08/05 08:34:20 INFO core: Metadata for  [4 4]  is  {:offset 431, :partition 0, :topic "fizzbuzz"}
17/08/05 08:34:20 INFO core: Metadata for  [2 2]  is  {:offset 432, :partition 0, :topic "fizzbuzz"}
17/08/05 08:34:20 INFO core: Metadata for  [1 1]  is  {:offset 433, :partition 0, :topic "fizzbuzz"}
17/08/05 08:34:20 INFO core: ---->>> Sending to Kafka fizzbuzz [5 "Buzz"] , class java.lang.String
17/08/05 08:34:20 INFO core: Metadata for  [5 "Buzz"]  is  {:offset 435, :partition 0, :topic "fizzbuzz"}
17/08/05 08:34:20 INFO core: ---->>> Sending to Kafka fizzbuzz [8 8] , class java.lang.String
17/08/05 08:34:20 INFO core: ---->>> Sending to Kafka fizzbuzz [9 "Fizz"] , class java.lang.String
17/08/05 08:34:20 INFO core: Metadata for  [8 8]  is  {:offset 436, :partition 0, :topic "fizzbuzz"}
17/08/05 08:34:20 INFO core: Metadata for  [9 "Fizz"]  is  {:offset 437, :partition 0, :topic "fizzbuzz"}
17/08/05 08:34:20 INFO core: ---->>> Sending to Kafka fizzbuzz [6 "Fizz"] , class java.lang.String
17/08/05 08:34:20 INFO core: ---->>> Sending to Kafka fizzbuzz [7 7] , class java.lang.String
17/08/05 08:34:20 INFO core: Metadata for  [6 "Fizz"]  is  {:offset 438, :partition 0, :topic "fizzbuzz"}
17/08/05 08:34:20 INFO core: Metadata for  [7 7]  is  {:offset 439, :partition 0, :topic "fizzbuzz"}
```
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
➜  spark-2.2.0-bin-hadoop2.7 ./bin/spark-submit --class flambo_example.core  /Users/stevechan/Desktop/flambo-custom-receiver-kafka-example/target/flambo-example-standalone.jar 8

log4j:WARN No appenders could be found for logger (flambo-example.core).
log4j:WARN Please initialize the log4j system properly.
log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties
17/08/03 17:08:54 WARN SparkConf: The configuration key spark.akka.timeout is not supported any more because Spark doesn't use Akka since 2.0
17/08/03 17:08:54 INFO SparkContext: Running Spark version 2.2.0
17/08/03 17:08:54 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
17/08/03 17:08:54 WARN Utils: Your hostname, marching-mbp resolves to a loopback address: 127.0.0.1; using 10.8.8.12 instead (on interface en0)
17/08/03 17:08:54 WARN Utils: Set SPARK_LOCAL_IP if you need to bind to another address
17/08/03 17:08:54 INFO SparkContext: Submitted application: flambo-custom-receiver-kafka-eample
17/08/03 17:08:54 INFO SecurityManager: Changing view acls to: stevechan
17/08/03 17:08:54 INFO SecurityManager: Changing modify acls to: stevechan
17/08/03 17:08:54 INFO SecurityManager: Changing view acls groups to:
17/08/03 17:08:54 INFO SecurityManager: Changing modify acls groups to:
17/08/03 17:08:54 INFO SecurityManager: SecurityManager: authentication disabled; ui acls disabled; users  with view permissions: Set(stevechan); groups with view permissions: Set(); users  with modify permissions: Set(stevechan); groups with modify permissions: Set()
17/08/03 17:08:54 INFO Utils: Successfully started service 'sparkDriver' on port 62210.
17/08/03 17:08:54 INFO SparkEnv: Registering MapOutputTracker
17/08/03 17:08:54 INFO SparkEnv: Registering BlockManagerMaster
17/08/03 17:08:54 INFO BlockManagerMasterEndpoint: Using org.apache.spark.storage.DefaultTopologyMapper for getting topology information
17/08/03 17:08:54 INFO BlockManagerMasterEndpoint: BlockManagerMasterEndpoint up
17/08/03 17:08:54 INFO DiskBlockManager: Created local directory at /private/var/folders/9p/m3jr24_n6rx_pvzzd9zb1z7c0000gr/T/blockmgr-fa649dfd-a8a9-4b43-aa00-b17e3afb576e
17/08/03 17:08:54 INFO MemoryStore: MemoryStore started with capacity 366.3 MB
17/08/03 17:08:55 INFO SparkEnv: Registering OutputCommitCoordinator
17/08/03 17:08:55 WARN Utils: Service 'SparkUI' could not bind on port 4040. Attempting port 4041.
17/08/03 17:08:55 INFO Utils: Successfully started service 'SparkUI' on port 4041.
17/08/03 17:08:55 INFO SparkUI: Bound SparkUI to 0.0.0.0, and started at http://10.8.8.12:4041
17/08/03 17:08:55 INFO SparkContext: Added JAR file:/Users/stevechan/Desktop/flambo-custom-receiver-kafka-example/target/flambo-example-standalone.jar at spark://10.8.8.12:62210/jars/flambo-example-standalone.jar with timestamp 1501751335334
17/08/03 17:08:55 INFO Executor: Starting executor ID driver on host localhost
17/08/03 17:08:55 INFO Utils: Successfully started service 'org.apache.spark.network.netty.NettyBlockTransferService' on port 62211.
17/08/03 17:08:55 INFO NettyBlockTransferService: Server created on 10.8.8.12:62211
17/08/03 17:08:55 INFO BlockManager: Using org.apache.spark.storage.RandomBlockReplicationPolicy for block replication policy
17/08/03 17:08:55 INFO BlockManagerMaster: Registering BlockManager BlockManagerId(driver, 10.8.8.12, 62211, None)
17/08/03 17:08:55 INFO BlockManagerMasterEndpoint: Registering block manager 10.8.8.12:62211 with 366.3 MB RAM, BlockManagerId(driver, 10.8.8.12, 62211, None)
17/08/03 17:08:55 INFO BlockManagerMaster: Registered BlockManager BlockManagerId(driver, 10.8.8.12, 62211, None)
17/08/03 17:08:55 INFO BlockManager: Initialized BlockManager: BlockManagerId(driver, 10.8.8.12, 62211, None)
17/08/03 17:08:56 INFO ReceiverTracker: Starting 1 receivers
17/08/03 17:08:56 INFO ReceiverTracker: ReceiverTracker started
17/08/03 17:08:56 INFO PluggableInputDStream: Slide time = 10000 ms
17/08/03 17:08:56 INFO PluggableInputDStream: Storage level = Serialized 1x Replicated
17/08/03 17:08:56 INFO PluggableInputDStream: Checkpoint interval = null
17/08/03 17:08:56 INFO PluggableInputDStream: Remember interval = 10000 ms
17/08/03 17:08:56 INFO PluggableInputDStream: Initialized and validated org.apache.spark.streaming.dstream.PluggableInputDStream@4272bfe2
17/08/03 17:08:56 INFO MappedDStream: Slide time = 10000 ms
17/08/03 17:08:56 INFO MappedDStream: Storage level = Serialized 1x Replicated
17/08/03 17:08:56 INFO MappedDStream: Checkpoint interval = null
17/08/03 17:08:56 INFO MappedDStream: Remember interval = 10000 ms
17/08/03 17:08:56 INFO MappedDStream: Initialized and validated org.apache.spark.streaming.dstream.MappedDStream@264bcef
17/08/03 17:08:56 INFO ForEachDStream: Slide time = 10000 ms
17/08/03 17:08:56 INFO ForEachDStream: Storage level = Serialized 1x Replicated
17/08/03 17:08:56 INFO ForEachDStream: Checkpoint interval = null
17/08/03 17:08:56 INFO ForEachDStream: Remember interval = 10000 ms
17/08/03 17:08:56 INFO ForEachDStream: Initialized and validated org.apache.spark.streaming.dstream.ForEachDStream@3d6af190
17/08/03 17:08:56 INFO RecurringTimer: Started timer for JobGenerator at time 1501751340000
17/08/03 17:08:56 INFO JobGenerator: Started JobGenerator at 1501751340000 ms
17/08/03 17:08:56 INFO JobScheduler: Started JobScheduler
17/08/03 17:08:56 INFO StreamingContext: StreamingContext started
17/08/03 17:08:56 INFO ReceiverTracker: Receiver 0 started
17/08/03 17:08:56 INFO DAGScheduler: Got job 0 (start at NativeMethodAccessorImpl.java:0) with 1 output partitions
17/08/03 17:08:56 INFO DAGScheduler: Final stage: ResultStage 0 (start at NativeMethodAccessorImpl.java:0)
17/08/03 17:08:56 INFO DAGScheduler: Parents of final stage: List()
17/08/03 17:08:56 INFO DAGScheduler: Missing parents: List()
17/08/03 17:08:56 INFO DAGScheduler: Submitting ResultStage 0 (Receiver 0 ParallelCollectionRDD[0] at makeRDD at ReceiverTracker.scala:620), which has no missing parents
17/08/03 17:08:56 INFO MemoryStore: Block broadcast_0 stored as values in memory (estimated size 52.4 KB, free 366.2 MB)
17/08/03 17:08:56 INFO MemoryStore: Block broadcast_0_piece0 stored as bytes in memory (estimated size 17.4 KB, free 366.2 MB)
17/08/03 17:08:56 INFO BlockManagerInfo: Added broadcast_0_piece0 in memory on 10.8.8.12:62211 (size: 17.4 KB, free: 366.3 MB)
17/08/03 17:08:56 INFO SparkContext: Created broadcast 0 from broadcast at DAGScheduler.scala:1006
17/08/03 17:08:56 INFO DAGScheduler: Submitting 1 missing tasks from ResultStage 0 (Receiver 0 ParallelCollectionRDD[0] at makeRDD at ReceiverTracker.scala:620) (first 15 tasks are for partitions Vector(0))
17/08/03 17:08:56 INFO TaskSchedulerImpl: Adding task set 0.0 with 1 tasks
17/08/03 17:08:56 INFO TaskSetManager: Starting task 0.0 in stage 0.0 (TID 0, localhost, executor driver, partition 0, PROCESS_LOCAL, 5033 bytes)
17/08/03 17:08:56 INFO Executor: Running task 0.0 in stage 0.0 (TID 0)
17/08/03 17:08:56 INFO Executor: Fetching spark://10.8.8.12:62210/jars/flambo-example-standalone.jar with timestamp 1501751335334
17/08/03 17:08:56 INFO TransportClientFactory: Successfully created connection to /10.8.8.12:62210 after 42 ms (0 ms spent in bootstraps)
17/08/03 17:08:56 INFO Utils: Fetching spark://10.8.8.12:62210/jars/flambo-example-standalone.jar to /private/var/folders/9p/m3jr24_n6rx_pvzzd9zb1z7c0000gr/T/spark-de8e7d93-bf1a-4194-af1a-7cb705a01d77/userFiles-0df3ee14-b307-4b8e-9f80-b9f762981a3b/fetchFileTemp540429731167077895.tmp
17/08/03 17:08:57 INFO Executor: Adding file:/private/var/folders/9p/m3jr24_n6rx_pvzzd9zb1z7c0000gr/T/spark-de8e7d93-bf1a-4194-af1a-7cb705a01d77/userFiles-0df3ee14-b307-4b8e-9f80-b9f762981a3b/flambo-example-standalone.jar to class loader
17/08/03 17:08:57 INFO RecurringTimer: Started timer for BlockGenerator at time 1501751338000
17/08/03 17:08:57 INFO BlockGenerator: Started BlockGenerator
17/08/03 17:08:57 INFO BlockGenerator: Started block pushing thread
17/08/03 17:08:57 INFO ReceiverTracker: Registered receiver for stream 0 from 10.8.8.12:62210
17/08/03 17:08:57 INFO ReceiverSupervisorImpl: Starting receiver 0
17/08/03 17:08:57 INFO core: Starting receiver
17/08/03 17:08:57 INFO ReceiverSupervisorImpl: Called receiver 0 onStart
17/08/03 17:08:57 INFO ReceiverSupervisorImpl: Waiting for receiver to be stopped
17/08/03 17:08:57 INFO core: Store: 1
17/08/03 17:08:57 INFO core: Store: 2
17/08/03 17:08:58 INFO MemoryStore: Block input-0-1501751337800 stored as values in memory (estimated size 72.0 B, free 366.2 MB)
17/08/03 17:08:58 INFO BlockManagerInfo: Added input-0-1501751337800 in memory on 10.8.8.12:62211 (size: 72.0 B, free: 366.3 MB)
17/08/03 17:08:58 WARN RandomBlockReplicationPolicy: Expecting 1 replicas with only 0 peer/s.
17/08/03 17:08:58 WARN BlockManager: Block input-0-1501751337800 replicated to only 0 peer(s) instead of 1 peers
17/08/03 17:08:58 INFO BlockGenerator: Pushed block input-0-1501751337800
17/08/03 17:08:58 INFO core: Store: 3
17/08/03 17:08:58 INFO MemoryStore: Block input-0-1501751338200 stored as values in memory (estimated size 48.0 B, free 366.2 MB)
17/08/03 17:08:58 INFO BlockManagerInfo: Added input-0-1501751338200 in memory on 10.8.8.12:62211 (size: 48.0 B, free: 366.3 MB)
17/08/03 17:08:58 WARN RandomBlockReplicationPolicy: Expecting 1 replicas with only 0 peer/s.
17/08/03 17:08:58 WARN BlockManager: Block input-0-1501751338200 replicated to only 0 peer(s) instead of 1 peers
17/08/03 17:08:58 INFO BlockGenerator: Pushed block input-0-1501751338200
17/08/03 17:08:58 INFO core: Store: 4
17/08/03 17:08:58 INFO MemoryStore: Block input-0-1501751338400 stored as values in memory (estimated size 48.0 B, free 366.2 MB)
17/08/03 17:08:58 INFO BlockManagerInfo: Added input-0-1501751338400 in memory on 10.8.8.12:62211 (size: 48.0 B, free: 366.3 MB)
17/08/03 17:08:58 WARN RandomBlockReplicationPolicy: Expecting 1 replicas with only 0 peer/s.
17/08/03 17:08:58 WARN BlockManager: Block input-0-1501751338400 replicated to only 0 peer(s) instead of 1 peers
17/08/03 17:08:58 INFO BlockGenerator: Pushed block input-0-1501751338400
17/08/03 17:08:58 INFO core: Store: 5
17/08/03 17:08:58 INFO core: Store: 6
17/08/03 17:08:58 INFO MemoryStore: Block input-0-1501751338600 stored as values in memory (estimated size 72.0 B, free 366.2 MB)
17/08/03 17:08:58 INFO BlockManagerInfo: Added input-0-1501751338600 in memory on 10.8.8.12:62211 (size: 72.0 B, free: 366.3 MB)
17/08/03 17:08:58 WARN RandomBlockReplicationPolicy: Expecting 1 replicas with only 0 peer/s.
17/08/03 17:08:58 WARN BlockManager: Block input-0-1501751338600 replicated to only 0 peer(s) instead of 1 peers
17/08/03 17:08:58 INFO BlockGenerator: Pushed block input-0-1501751338600
17/08/03 17:08:59 INFO core: Store: 7
17/08/03 17:08:59 INFO MemoryStore: Block input-0-1501751339000 stored as values in memory (estimated size 48.0 B, free 366.2 MB)
17/08/03 17:08:59 INFO BlockManagerInfo: Added input-0-1501751339000 in memory on 10.8.8.12:62211 (size: 48.0 B, free: 366.3 MB)
17/08/03 17:08:59 WARN RandomBlockReplicationPolicy: Expecting 1 replicas with only 0 peer/s.
17/08/03 17:08:59 WARN BlockManager: Block input-0-1501751339000 replicated to only 0 peer(s) instead of 1 peers
17/08/03 17:08:59 INFO BlockGenerator: Pushed block input-0-1501751339000
17/08/03 17:09:00 INFO JobScheduler: Added jobs for time 1501751340000 ms
17/08/03 17:09:00 INFO JobScheduler: Starting job streaming job 1501751340000 ms.0 from job set of time 1501751340000 ms
17/08/03 17:09:00 INFO SparkContext: Starting job: foreachPartition at NativeMethodAccessorImpl.java:0
17/08/03 17:09:00 INFO DAGScheduler: Got job 1 (foreachPartition at NativeMethodAccessorImpl.java:0) with 5 output partitions
17/08/03 17:09:00 INFO DAGScheduler: Final stage: ResultStage 1 (foreachPartition at NativeMethodAccessorImpl.java:0)
17/08/03 17:09:00 INFO DAGScheduler: Parents of final stage: List()
17/08/03 17:09:00 INFO DAGScheduler: Missing parents: List()
17/08/03 17:09:00 INFO DAGScheduler: Submitting ResultStage 1 (MapPartitionsRDD[2] at map at NativeMethodAccessorImpl.java:0), which has no missing parents
17/08/03 17:09:00 INFO MemoryStore: Block broadcast_1 stored as values in memory (estimated size 4.9 KB, free 366.2 MB)
17/08/03 17:09:00 INFO MemoryStore: Block broadcast_1_piece0 stored as bytes in memory (estimated size 2030.0 B, free 366.2 MB)
17/08/03 17:09:00 INFO BlockManagerInfo: Added broadcast_1_piece0 in memory on 10.8.8.12:62211 (size: 2030.0 B, free: 366.3 MB)
17/08/03 17:09:00 INFO SparkContext: Created broadcast 1 from broadcast at DAGScheduler.scala:1006
17/08/03 17:09:00 INFO DAGScheduler: Submitting 5 missing tasks from ResultStage 1 (MapPartitionsRDD[2] at map at NativeMethodAccessorImpl.java:0) (first 15 tasks are for partitions Vector(0, 1, 2, 3, 4))
17/08/03 17:09:00 INFO TaskSchedulerImpl: Adding task set 1.0 with 5 tasks
17/08/03 17:09:00 INFO TaskSetManager: Starting task 0.0 in stage 1.0 (TID 1, localhost, executor driver, partition 0, ANY, 4744 bytes)
17/08/03 17:09:00 INFO TaskSetManager: Starting task 1.0 in stage 1.0 (TID 2, localhost, executor driver, partition 1, ANY, 4744 bytes)
17/08/03 17:09:00 INFO TaskSetManager: Starting task 2.0 in stage 1.0 (TID 3, localhost, executor driver, partition 2, ANY, 4744 bytes)
17/08/03 17:09:00 INFO TaskSetManager: Starting task 3.0 in stage 1.0 (TID 4, localhost, executor driver, partition 3, ANY, 4744 bytes)
17/08/03 17:09:00 INFO TaskSetManager: Starting task 4.0 in stage 1.0 (TID 5, localhost, executor driver, partition 4, ANY, 4744 bytes)
17/08/03 17:09:00 INFO Executor: Running task 0.0 in stage 1.0 (TID 1)
17/08/03 17:09:00 INFO Executor: Running task 1.0 in stage 1.0 (TID 2)
17/08/03 17:09:00 INFO Executor: Running task 2.0 in stage 1.0 (TID 3)
17/08/03 17:09:00 INFO Executor: Running task 3.0 in stage 1.0 (TID 4)
17/08/03 17:09:00 INFO Executor: Running task 4.0 in stage 1.0 (TID 5)
17/08/03 17:09:00 INFO BlockManager: Found block input-0-1501751338600 locally
17/08/03 17:09:00 INFO BlockManager: Found block input-0-1501751339000 locally
17/08/03 17:09:00 INFO BlockManager: Found block input-0-1501751338200 locally
17/08/03 17:09:00 INFO BlockManager: Found block input-0-1501751338400 locally
17/08/03 17:09:00 INFO BlockManager: Found block input-0-1501751337800 locally
17/08/03 17:09:00 INFO core: Sending to Kafka fizzbuzz [7 7]
17/08/03 17:09:00 INFO core: Sending to Kafka fizzbuzz [4 4]
17/08/03 17:09:00 INFO core: Sending to Kafka fizzbuzz [3 "Fizz"]
17/08/03 17:09:00 INFO core: Sending to Kafka fizzbuzz [5 "Buzz"]
17/08/03 17:09:00 INFO core: Sending to Kafka fizzbuzz [1 1]
17/08/03 17:09:00 INFO ProducerConfig: ProducerConfig values:
	acks = 1
	batch.size = 16384
	block.on.buffer.full = false
	bootstrap.servers = [localhost:9092]
	buffer.memory = 33554432
	client.id =
	compression.type = none
	connections.max.idle.ms = 540000
	interceptor.classes = null
	key.serializer = class org.apache.kafka.common.serialization.StringSerializer
	linger.ms = 0
	max.block.ms = 60000
	max.in.flight.requests.per.connection = 5
	max.request.size = 1048576
	metadata.fetch.timeout.ms = 60000
	metadata.max.age.ms = 300000
	metric.reporters = []
	metrics.num.samples = 2
	metrics.sample.window.ms = 30000
	partitioner.class = class org.apache.kafka.clients.producer.internals.DefaultPartitioner
	receive.buffer.bytes = 32768
	reconnect.backoff.ms = 50
	request.timeout.ms = 30000
	retries = 0
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.mechanism = GSSAPI
	security.protocol = PLAINTEXT
	send.buffer.bytes = 131072
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.endpoint.identification.algorithm = null
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.protocol = TLS
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	timeout.ms = 30000
	value.serializer = class org.apache.kafka.common.serialization.StringSerializer

17/08/03 17:09:00 INFO ProducerConfig: ProducerConfig values:
	acks = 1
	batch.size = 16384
	block.on.buffer.full = false
	bootstrap.servers = [localhost:9092]
	buffer.memory = 33554432
	client.id =
	compression.type = none
	connections.max.idle.ms = 540000
	interceptor.classes = null
	key.serializer = class org.apache.kafka.common.serialization.StringSerializer
	linger.ms = 0
	max.block.ms = 60000
	max.in.flight.requests.per.connection = 5
	max.request.size = 1048576
	metadata.fetch.timeout.ms = 60000
	metadata.max.age.ms = 300000
	metric.reporters = []
	metrics.num.samples = 2
	metrics.sample.window.ms = 30000
	partitioner.class = class org.apache.kafka.clients.producer.internals.DefaultPartitioner
	receive.buffer.bytes = 32768
	reconnect.backoff.ms = 50
	request.timeout.ms = 30000
	retries = 0
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.mechanism = GSSAPI
	security.protocol = PLAINTEXT
	send.buffer.bytes = 131072
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.endpoint.identification.algorithm = null
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.protocol = TLS
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	timeout.ms = 30000
	value.serializer = class org.apache.kafka.common.serialization.StringSerializer

17/08/03 17:09:00 INFO ProducerConfig: ProducerConfig values:
	acks = 1
	batch.size = 16384
	block.on.buffer.full = false
	bootstrap.servers = [localhost:9092]
	buffer.memory = 33554432
	client.id =
	compression.type = none
	connections.max.idle.ms = 540000
	interceptor.classes = null
	key.serializer = class org.apache.kafka.common.serialization.StringSerializer
	linger.ms = 0
	max.block.ms = 60000
	max.in.flight.requests.per.connection = 5
	max.request.size = 1048576
	metadata.fetch.timeout.ms = 60000
	metadata.max.age.ms = 300000
	metric.reporters = []
	metrics.num.samples = 2
	metrics.sample.window.ms = 30000
	partitioner.class = class org.apache.kafka.clients.producer.internals.DefaultPartitioner
	receive.buffer.bytes = 32768
	reconnect.backoff.ms = 50
	request.timeout.ms = 30000
	retries = 0
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.mechanism = GSSAPI
	security.protocol = PLAINTEXT
	send.buffer.bytes = 131072
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.endpoint.identification.algorithm = null
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.protocol = TLS
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	timeout.ms = 30000
	value.serializer = class org.apache.kafka.common.serialization.StringSerializer

17/08/03 17:09:00 INFO ProducerConfig: ProducerConfig values:
	acks = 1
	batch.size = 16384
	block.on.buffer.full = false
	bootstrap.servers = [localhost:9092]
	buffer.memory = 33554432
	client.id =
	compression.type = none
	connections.max.idle.ms = 540000
	interceptor.classes = null
	key.serializer = class org.apache.kafka.common.serialization.StringSerializer
	linger.ms = 0
	max.block.ms = 60000
	max.in.flight.requests.per.connection = 5
	max.request.size = 1048576
	metadata.fetch.timeout.ms = 60000
	metadata.max.age.ms = 300000
	metric.reporters = []
	metrics.num.samples = 2
	metrics.sample.window.ms = 30000
	partitioner.class = class org.apache.kafka.clients.producer.internals.DefaultPartitioner
	receive.buffer.bytes = 32768
	reconnect.backoff.ms = 50
	request.timeout.ms = 30000
	retries = 0
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.mechanism = GSSAPI
	security.protocol = PLAINTEXT
	send.buffer.bytes = 131072
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.endpoint.identification.algorithm = null
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.protocol = TLS
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	timeout.ms = 30000
	value.serializer = class org.apache.kafka.common.serialization.StringSerializer

17/08/03 17:09:00 INFO ProducerConfig: ProducerConfig values:
	acks = 1
	batch.size = 16384
	block.on.buffer.full = false
	bootstrap.servers = [localhost:9092]
	buffer.memory = 33554432
	client.id =
	compression.type = none
	connections.max.idle.ms = 540000
	interceptor.classes = null
	key.serializer = class org.apache.kafka.common.serialization.StringSerializer
	linger.ms = 0
	max.block.ms = 60000
	max.in.flight.requests.per.connection = 5
	max.request.size = 1048576
	metadata.fetch.timeout.ms = 60000
	metadata.max.age.ms = 300000
	metric.reporters = []
	metrics.num.samples = 2
	metrics.sample.window.ms = 30000
	partitioner.class = class org.apache.kafka.clients.producer.internals.DefaultPartitioner
	receive.buffer.bytes = 32768
	reconnect.backoff.ms = 50
	request.timeout.ms = 30000
	retries = 0
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.mechanism = GSSAPI
	security.protocol = PLAINTEXT
	send.buffer.bytes = 131072
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.endpoint.identification.algorithm = null
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.protocol = TLS
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	timeout.ms = 30000
	value.serializer = class org.apache.kafka.common.serialization.StringSerializer

17/08/03 17:09:00 INFO ProducerConfig: ProducerConfig values:
	acks = 1
	batch.size = 16384
	block.on.buffer.full = false
	bootstrap.servers = [localhost:9092]
	buffer.memory = 33554432
	client.id = producer-5
	compression.type = none
	connections.max.idle.ms = 540000
	interceptor.classes = null
	key.serializer = class org.apache.kafka.common.serialization.StringSerializer
	linger.ms = 0
	max.block.ms = 60000
	max.in.flight.requests.per.connection = 5
	max.request.size = 1048576
	metadata.fetch.timeout.ms = 60000
	metadata.max.age.ms = 300000
	metric.reporters = []
	metrics.num.samples = 2
	metrics.sample.window.ms = 30000
	partitioner.class = class org.apache.kafka.clients.producer.internals.DefaultPartitioner
	receive.buffer.bytes = 32768
	reconnect.backoff.ms = 50
	request.timeout.ms = 30000
	retries = 0
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.mechanism = GSSAPI
	security.protocol = PLAINTEXT
	send.buffer.bytes = 131072
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.endpoint.identification.algorithm = null
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.protocol = TLS
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	timeout.ms = 30000
	value.serializer = class org.apache.kafka.common.serialization.StringSerializer

17/08/03 17:09:00 INFO ProducerConfig: ProducerConfig values:
	acks = 1
	batch.size = 16384
	block.on.buffer.full = false
	bootstrap.servers = [localhost:9092]
	buffer.memory = 33554432
	client.id = producer-3
	compression.type = none
	connections.max.idle.ms = 540000
	interceptor.classes = null
	key.serializer = class org.apache.kafka.common.serialization.StringSerializer
	linger.ms = 0
	max.block.ms = 60000
	max.in.flight.requests.per.connection = 5
	max.request.size = 1048576
	metadata.fetch.timeout.ms = 60000
	metadata.max.age.ms = 300000
	metric.reporters = []
	metrics.num.samples = 2
	metrics.sample.window.ms = 30000
	partitioner.class = class org.apache.kafka.clients.producer.internals.DefaultPartitioner
	receive.buffer.bytes = 32768
	reconnect.backoff.ms = 50
	request.timeout.ms = 30000
	retries = 0
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.mechanism = GSSAPI
	security.protocol = PLAINTEXT
	send.buffer.bytes = 131072
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.endpoint.identification.algorithm = null
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.protocol = TLS
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	timeout.ms = 30000
	value.serializer = class org.apache.kafka.common.serialization.StringSerializer

17/08/03 17:09:00 INFO ProducerConfig: ProducerConfig values:
	acks = 1
	batch.size = 16384
	block.on.buffer.full = false
	bootstrap.servers = [localhost:9092]
	buffer.memory = 33554432
	client.id = producer-4
	compression.type = none
	connections.max.idle.ms = 540000
	interceptor.classes = null
	key.serializer = class org.apache.kafka.common.serialization.StringSerializer
	linger.ms = 0
	max.block.ms = 60000
	max.in.flight.requests.per.connection = 5
	max.request.size = 1048576
	metadata.fetch.timeout.ms = 60000
	metadata.max.age.ms = 300000
	metric.reporters = []
	metrics.num.samples = 2
	metrics.sample.window.ms = 30000
	partitioner.class = class org.apache.kafka.clients.producer.internals.DefaultPartitioner
	receive.buffer.bytes = 32768
	reconnect.backoff.ms = 50
	request.timeout.ms = 30000
	retries = 0
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.mechanism = GSSAPI
	security.protocol = PLAINTEXT
	send.buffer.bytes = 131072
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.endpoint.identification.algorithm = null
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.protocol = TLS
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	timeout.ms = 30000
	value.serializer = class org.apache.kafka.common.serialization.StringSerializer

17/08/03 17:09:00 INFO ProducerConfig: ProducerConfig values:
	acks = 1
	batch.size = 16384
	block.on.buffer.full = false
	bootstrap.servers = [localhost:9092]
	buffer.memory = 33554432
	client.id = producer-1
	compression.type = none
	connections.max.idle.ms = 540000
	interceptor.classes = null
	key.serializer = class org.apache.kafka.common.serialization.StringSerializer
	linger.ms = 0
	max.block.ms = 60000
	max.in.flight.requests.per.connection = 5
	max.request.size = 1048576
	metadata.fetch.timeout.ms = 60000
	metadata.max.age.ms = 300000
	metric.reporters = []
	metrics.num.samples = 2
	metrics.sample.window.ms = 30000
	partitioner.class = class org.apache.kafka.clients.producer.internals.DefaultPartitioner
	receive.buffer.bytes = 32768
	reconnect.backoff.ms = 50
	request.timeout.ms = 30000
	retries = 0
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.mechanism = GSSAPI
	security.protocol = PLAINTEXT
	send.buffer.bytes = 131072
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.endpoint.identification.algorithm = null
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.protocol = TLS
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	timeout.ms = 30000
	value.serializer = class org.apache.kafka.common.serialization.StringSerializer

17/08/03 17:09:00 INFO ProducerConfig: ProducerConfig values:
	acks = 1
	batch.size = 16384
	block.on.buffer.full = false
	bootstrap.servers = [localhost:9092]
	buffer.memory = 33554432
	client.id = producer-2
	compression.type = none
	connections.max.idle.ms = 540000
	interceptor.classes = null
	key.serializer = class org.apache.kafka.common.serialization.StringSerializer
	linger.ms = 0
	max.block.ms = 60000
	max.in.flight.requests.per.connection = 5
	max.request.size = 1048576
	metadata.fetch.timeout.ms = 60000
	metadata.max.age.ms = 300000
	metric.reporters = []
	metrics.num.samples = 2
	metrics.sample.window.ms = 30000
	partitioner.class = class org.apache.kafka.clients.producer.internals.DefaultPartitioner
	receive.buffer.bytes = 32768
	reconnect.backoff.ms = 50
	request.timeout.ms = 30000
	retries = 0
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.mechanism = GSSAPI
	security.protocol = PLAINTEXT
	send.buffer.bytes = 131072
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.endpoint.identification.algorithm = null
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.protocol = TLS
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	timeout.ms = 30000
	value.serializer = class org.apache.kafka.common.serialization.StringSerializer

17/08/03 17:09:00 INFO AppInfoParser: Kafka version : 0.10.1.1
17/08/03 17:09:00 INFO AppInfoParser: Kafka version : 0.10.1.1
17/08/03 17:09:00 INFO AppInfoParser: Kafka commitId : f10ef2720b03b247
17/08/03 17:09:00 INFO AppInfoParser: Kafka commitId : f10ef2720b03b247
17/08/03 17:09:00 INFO AppInfoParser: Kafka version : 0.10.1.1
17/08/03 17:09:00 INFO AppInfoParser: Kafka version : 0.10.1.1
17/08/03 17:09:00 INFO AppInfoParser: Kafka version : 0.10.1.1
17/08/03 17:09:00 INFO AppInfoParser: Kafka commitId : f10ef2720b03b247
17/08/03 17:09:00 INFO AppInfoParser: Kafka commitId : f10ef2720b03b247
17/08/03 17:09:00 INFO AppInfoParser: Kafka commitId : f10ef2720b03b247
17/08/03 17:09:00 WARN NetworkClient: Error while fetching metadata with correlation id 0 : {fizzbuzz=LEADER_NOT_AVAILABLE}
17/08/03 17:09:00 WARN NetworkClient: Error while fetching metadata with correlation id 0 : {fizzbuzz=LEADER_NOT_AVAILABLE}
17/08/03 17:09:00 WARN NetworkClient: Error while fetching metadata with correlation id 0 : {fizzbuzz=LEADER_NOT_AVAILABLE}
17/08/03 17:09:00 WARN NetworkClient: Error while fetching metadata with correlation id 0 : {fizzbuzz=LEADER_NOT_AVAILABLE}
17/08/03 17:09:00 WARN NetworkClient: Error while fetching metadata with correlation id 0 : {fizzbuzz=LEADER_NOT_AVAILABLE}
17/08/03 17:09:00 INFO core: Sending to Kafka fizzbuzz [2 2]
17/08/03 17:09:00 INFO core: Sending to Kafka fizzbuzz [6 "Fizz"]
17/08/03 17:09:00 INFO Executor: Finished task 2.0 in stage 1.0 (TID 3). 666 bytes result sent to driver
17/08/03 17:09:00 INFO Executor: Finished task 4.0 in stage 1.0 (TID 5). 666 bytes result sent to driver
17/08/03 17:09:00 INFO Executor: Finished task 0.0 in stage 1.0 (TID 1). 709 bytes result sent to driver
17/08/03 17:09:00 INFO Executor: Finished task 1.0 in stage 1.0 (TID 2). 666 bytes result sent to driver
17/08/03 17:09:00 INFO Executor: Finished task 3.0 in stage 1.0 (TID 4). 666 bytes result sent to driver
17/08/03 17:09:00 INFO core: Metadata for  [3 "Fizz"]  is  {:offset 0, :partition 0, :topic "fizzbuzz"}
17/08/03 17:09:00 INFO core: Metadata for  [7 7]  is  {:offset 1, :partition 0, :topic "fizzbuzz"}
17/08/03 17:09:00 INFO core: Metadata for  [5 "Buzz"]  is  {:offset 3, :partition 0, :topic "fizzbuzz"}
17/08/03 17:09:00 INFO core: Metadata for  [6 "Fizz"]  is  {:offset 4, :partition 0, :topic "fizzbuzz"}
17/08/03 17:09:00 INFO core: Metadata for  [1 1]  is  {:offset 6, :partition 0, :topic "fizzbuzz"}
17/08/03 17:09:00 INFO core: Metadata for  [2 2]  is  {:offset 5, :partition 0, :topic "fizzbuzz"}
17/08/03 17:09:00 INFO core: Metadata for  [4 4]  is  {:offset 2, :partition 0, :topic "fizzbuzz"}
17/08/03 17:09:00 INFO TaskSetManager: Finished task 4.0 in stage 1.0 (TID 5) in 637 ms on localhost (executor driver) (1/5)
17/08/03 17:09:00 INFO TaskSetManager: Finished task 0.0 in stage 1.0 (TID 1) in 644 ms on localhost (executor driver) (2/5)
17/08/03 17:09:00 INFO TaskSetManager: Finished task 1.0 in stage 1.0 (TID 2) in 642 ms on localhost (executor driver) (3/5)
17/08/03 17:09:00 INFO TaskSetManager: Finished task 2.0 in stage 1.0 (TID 3) in 641 ms on localhost (executor driver) (4/5)
17/08/03 17:09:00 INFO TaskSetManager: Finished task 3.0 in stage 1.0 (TID 4) in 641 ms on localhost (executor driver) (5/5)
17/08/03 17:09:00 INFO TaskSchedulerImpl: Removed TaskSet 1.0, whose tasks have all completed, from pool
17/08/03 17:09:00 INFO DAGScheduler: ResultStage 1 (foreachPartition at NativeMethodAccessorImpl.java:0) finished in 0.653 s
17/08/03 17:09:00 INFO DAGScheduler: Job 1 finished: foreachPartition at NativeMethodAccessorImpl.java:0, took 0.714318 s
17/08/03 17:09:00 INFO JobScheduler: Finished job streaming job 1501751340000 ms.0 from job set of time 1501751340000 ms
17/08/03 17:09:00 INFO JobScheduler: Total delay: 0.880 s for time 1501751340000 ms (execution: 0.840 s)
17/08/03 17:09:00 INFO ReceivedBlockTracker: Deleting batches:
17/08/03 17:09:00 INFO InputInfoTracker: remove old batch metadata:
17/08/03 17:09:10 INFO JobScheduler: Added jobs for time 1501751350000 ms
17/08/03 17:09:10 INFO JobScheduler: Starting job streaming job 1501751350000 ms.0 from job set of time 1501751350000 ms
17/08/03 17:09:10 INFO SparkContext: Starting job: foreachPartition at NativeMethodAccessorImpl.java:0
17/08/03 17:09:10 INFO DAGScheduler: Job 2 finished: foreachPartition at NativeMethodAccessorImpl.java:0, took 0.000034 s
17/08/03 17:09:10 INFO JobScheduler: Finished job streaming job 1501751350000 ms.0 from job set of time 1501751350000 ms
17/08/03 17:09:10 INFO JobScheduler: Total delay: 0.035 s for time 1501751350000 ms (execution: 0.030 s)
17/08/03 17:09:10 INFO MapPartitionsRDD: Removing RDD 2 from persistence list
17/08/03 17:09:10 INFO BlockRDD: Removing RDD 1 from persistence list
17/08/03 17:09:10 INFO PluggableInputDStream: Removing blocks of RDD BlockRDD[1] at receiverStream at core.clj:93 of time 1501751350000 ms
17/08/03 17:09:10 INFO BlockManager: Removing RDD 2
17/08/03 17:09:10 INFO BlockManager: Removing RDD 1
17/08/03 17:09:10 INFO ReceivedBlockTracker: Deleting batches:
17/08/03 17:09:10 INFO InputInfoTracker: remove old batch metadata:
17/08/03 17:09:10 INFO BlockManagerInfo: Removed input-0-1501751337800 on 10.8.8.12:62211 in memory (size: 72.0 B, free: 366.3 MB)
17/08/03 17:09:10 INFO BlockManagerInfo: Removed input-0-1501751338200 on 10.8.8.12:62211 in memory (size: 48.0 B, free: 366.3 MB)
17/08/03 17:09:10 INFO BlockManagerInfo: Removed input-0-1501751338600 on 10.8.8.12:62211 in memory (size: 72.0 B, free: 366.3 MB)
17/08/03 17:09:10 INFO BlockManagerInfo: Removed input-0-1501751338400 on 10.8.8.12:62211 in memory (size: 48.0 B, free: 366.3 MB)
17/08/03 17:09:10 INFO BlockManagerInfo: Removed input-0-1501751339000 on 10.8.8.12:62211 in memory (size: 48.0 B, free: 366.3 MB)
17/08/03 17:09:20 INFO JobScheduler: Added jobs for time 1501751360000 ms
17/08/03 17:09:20 INFO JobScheduler: Starting job streaming job 1501751360000 ms.0 from job set of time 1501751360000 ms
17/08/03 17:09:20 INFO SparkContext: Starting job: foreachPartition at NativeMethodAccessorImpl.java:0
17/08/03 17:09:20 INFO DAGScheduler: Job 3 finished: foreachPartition at NativeMethodAccessorImpl.java:0, took 0.000024 s
17/08/03 17:09:20 INFO JobScheduler: Finished job streaming job 1501751360000 ms.0 from job set of time 1501751360000 ms
17/08/03 17:09:20 INFO JobScheduler: Total delay: 0.040 s for time 1501751360000 ms (execution: 0.031 s)
17/08/03 17:09:20 INFO MapPartitionsRDD: Removing RDD 4 from persistence list
17/08/03 17:09:20 INFO BlockRDD: Removing RDD 3 from persistence list
17/08/03 17:09:20 INFO BlockManager: Removing RDD 4
17/08/03 17:09:20 INFO BlockManager: Removing RDD 3
17/08/03 17:09:20 INFO PluggableInputDStream: Removing blocks of RDD BlockRDD[3] at receiverStream at core.clj:93 of time 1501751360000 ms
17/08/03 17:09:20 INFO ReceivedBlockTracker: Deleting batches: 1501751340000 ms
17/08/03 17:09:20 INFO InputInfoTracker: remove old batch metadata: 1501751340000 ms
17/08/03 17:09:30 INFO JobScheduler: Added jobs for time 1501751370000 ms
17/08/03 17:09:30 INFO JobScheduler: Starting job streaming job 1501751370000 ms.0 from job set of time 1501751370000 ms
17/08/03 17:09:30 INFO SparkContext: Starting job: foreachPartition at NativeMethodAccessorImpl.java:0
17/08/03 17:09:30 INFO DAGScheduler: Job 4 finished: foreachPartition at NativeMethodAccessorImpl.java:0, took 0.000033 s
17/08/03 17:09:30 INFO JobScheduler: Finished job streaming job 1501751370000 ms.0 from job set of time 1501751370000 ms
17/08/03 17:09:30 INFO JobScheduler: Total delay: 0.036 s for time 1501751370000 ms (execution: 0.031 s)
17/08/03 17:09:30 INFO MapPartitionsRDD: Removing RDD 6 from persistence list
17/08/03 17:09:30 INFO BlockManager: Removing RDD 6
17/08/03 17:09:30 INFO BlockRDD: Removing RDD 5 from persistence list
17/08/03 17:09:30 INFO BlockManager: Removing RDD 5
17/08/03 17:09:30 INFO PluggableInputDStream: Removing blocks of RDD BlockRDD[5] at receiverStream at core.clj:93 of time 1501751370000 ms
17/08/03 17:09:30 INFO ReceivedBlockTracker: Deleting batches: 1501751350000 ms
17/08/03 17:09:30 INFO InputInfoTracker: remove old batch metadata: 1501751350000 ms
17/08/03 17:09:40 INFO JobScheduler: Added jobs for time 1501751380000 ms
17/08/03 17:09:40 INFO JobScheduler: Starting job streaming job 1501751380000 ms.0 from job set of time 1501751380000 ms
17/08/03 17:09:40 INFO SparkContext: Starting job: foreachPartition at NativeMethodAccessorImpl.java:0
17/08/03 17:09:40 INFO DAGScheduler: Job 5 finished: foreachPartition at NativeMethodAccessorImpl.java:0, took 0.000025 s
17/08/03 17:09:40 INFO JobScheduler: Finished job streaming job 1501751380000 ms.0 from job set of time 1501751380000 ms
17/08/03 17:09:40 INFO MapPartitionsRDD: Removing RDD 8 from persistence list
17/08/03 17:09:40 INFO JobScheduler: Total delay: 0.044 s for time 1501751380000 ms (execution: 0.034 s)
17/08/03 17:09:40 INFO BlockManager: Removing RDD 8
17/08/03 17:09:40 INFO BlockRDD: Removing RDD 7 from persistence list
17/08/03 17:09:40 INFO BlockManager: Removing RDD 7
17/08/03 17:09:40 INFO PluggableInputDStream: Removing blocks of RDD BlockRDD[7] at receiverStream at core.clj:93 of time 1501751380000 ms
17/08/03 17:09:40 INFO ReceivedBlockTracker: Deleting batches: 1501751360000 ms
17/08/03 17:09:40 INFO InputInfoTracker: remove old batch metadata: 1501751360000 ms
17/08/03 17:09:50 INFO JobScheduler: Added jobs for time 1501751390000 ms
17/08/03 17:09:50 INFO JobScheduler: Starting job streaming job 1501751390000 ms.0 from job set of time 1501751390000 ms
17/08/03 17:09:50 INFO BlockManagerInfo: Removed broadcast_1_piece0 on 10.8.8.12:62211 in memory (size: 2030.0 B, free: 366.3 MB)
17/08/03 17:09:50 INFO SparkContext: Starting job: foreachPartition at NativeMethodAccessorImpl.java:0
17/08/03 17:09:50 INFO DAGScheduler: Job 6 finished: foreachPartition at NativeMethodAccessorImpl.java:0, took 0.000040 s
17/08/03 17:09:50 INFO JobScheduler: Finished job streaming job 1501751390000 ms.0 from job set of time 1501751390000 ms
17/08/03 17:09:50 INFO JobScheduler: Total delay: 0.046 s for time 1501751390000 ms (execution: 0.038 s)
17/08/03 17:09:50 INFO MapPartitionsRDD: Removing RDD 10 from persistence list
17/08/03 17:09:50 INFO BlockManager: Removing RDD 10
17/08/03 17:09:50 INFO BlockRDD: Removing RDD 9 from persistence list
17/08/03 17:09:50 INFO BlockManager: Removing RDD 9
17/08/03 17:09:50 INFO PluggableInputDStream: Removing blocks of RDD BlockRDD[9] at receiverStream at core.clj:93 of time 1501751390000 ms
17/08/03 17:09:50 INFO ReceivedBlockTracker: Deleting batches: 1501751370000 ms
17/08/03 17:09:50 INFO InputInfoTracker: remove old batch metadata: 1501751370000 ms
17/08/03 17:10:00 INFO JobScheduler: Added jobs for time 1501751400000 ms
17/08/03 17:10:00 INFO JobScheduler: Starting job streaming job 1501751400000 ms.0 from job set of time 1501751400000 ms
17/08/03 17:10:00 INFO SparkContext: Starting job: foreachPartition at NativeMethodAccessorImpl.java:0
17/08/03 17:10:00 INFO DAGScheduler: Job 7 finished: foreachPartition at NativeMethodAccessorImpl.java:0, took 0.000034 s
17/08/03 17:10:00 INFO JobScheduler: Finished job streaming job 1501751400000 ms.0 from job set of time 1501751400000 ms
17/08/03 17:10:00 INFO MapPartitionsRDD: Removing RDD 12 from persistence list
17/08/03 17:10:00 INFO JobScheduler: Total delay: 0.033 s for time 1501751400000 ms (execution: 0.025 s)
17/08/03 17:10:00 INFO BlockManager: Removing RDD 12
17/08/03 17:10:00 INFO BlockRDD: Removing RDD 11 from persistence list
17/08/03 17:10:00 INFO BlockManager: Removing RDD 11
17/08/03 17:10:00 INFO PluggableInputDStream: Removing blocks of RDD BlockRDD[11] at receiverStream at core.clj:93 of time 1501751400000 ms
17/08/03 17:10:00 INFO ReceivedBlockTracker: Deleting batches: 1501751380000 ms
17/08/03 17:10:00 INFO InputInfoTracker: remove old batch metadata: 1501751380000 ms
17/08/03 17:10:10 INFO JobScheduler: Added jobs for time 1501751410000 ms
17/08/03 17:10:10 INFO JobScheduler: Starting job streaming job 1501751410000 ms.0 from job set of time 1501751410000 ms
17/08/03 17:10:10 INFO SparkContext: Starting job: foreachPartition at NativeMethodAccessorImpl.java:0
17/08/03 17:10:10 INFO DAGScheduler: Job 8 finished: foreachPartition at NativeMethodAccessorImpl.java:0, took 0.000057 s
17/08/03 17:10:10 INFO JobScheduler: Finished job streaming job 1501751410000 ms.0 from job set of time 1501751410000 ms
17/08/03 17:10:10 INFO MapPartitionsRDD: Removing RDD 14 from persistence list
17/08/03 17:10:10 INFO JobScheduler: Total delay: 0.032 s for time 1501751410000 ms (execution: 0.026 s)
17/08/03 17:10:10 INFO BlockManager: Removing RDD 14
17/08/03 17:10:10 INFO BlockRDD: Removing RDD 13 from persistence list
17/08/03 17:10:10 INFO BlockManager: Removing RDD 13
17/08/03 17:10:10 INFO PluggableInputDStream: Removing blocks of RDD BlockRDD[13] at receiverStream at core.clj:93 of time 1501751410000 ms
17/08/03 17:10:10 INFO ReceivedBlockTracker: Deleting batches: 1501751390000 ms
17/08/03 17:10:10 INFO InputInfoTracker: remove old batch metadata: 1501751390000 ms
17/08/03 17:10:20 INFO JobScheduler: Added jobs for time 1501751420000 ms
17/08/03 17:10:20 INFO JobScheduler: Starting job streaming job 1501751420000 ms.0 from job set of time 1501751420000 ms
17/08/03 17:10:20 INFO SparkContext: Starting job: foreachPartition at NativeMethodAccessorImpl.java:0
17/08/03 17:10:20 INFO DAGScheduler: Job 9 finished: foreachPartition at NativeMethodAccessorImpl.java:0, took 0.000023 s
17/08/03 17:10:20 INFO JobScheduler: Finished job streaming job 1501751420000 ms.0 from job set of time 1501751420000 ms
17/08/03 17:10:20 INFO JobScheduler: Total delay: 0.031 s for time 1501751420000 ms (execution: 0.025 s)
17/08/03 17:10:20 INFO MapPartitionsRDD: Removing RDD 16 from persistence list
17/08/03 17:10:20 INFO BlockManager: Removing RDD 16
17/08/03 17:10:20 INFO BlockRDD: Removing RDD 15 from persistence list
17/08/03 17:10:20 INFO BlockManager: Removing RDD 15
17/08/03 17:10:20 INFO PluggableInputDStream: Removing blocks of RDD BlockRDD[15] at receiverStream at core.clj:93 of time 1501751420000 ms
17/08/03 17:10:20 INFO ReceivedBlockTracker: Deleting batches: 1501751400000 ms
17/08/03 17:10:20 INFO InputInfoTracker: remove old batch metadata: 1501751400000 ms
17/08/03 17:10:26 INFO StreamingContext: Invoking stop(stopGracefully=false) from shutdown hook
17/08/03 17:10:26 INFO ReceiverTracker: Sent stop signal to all 1 receivers
17/08/03 17:10:26 INFO ReceiverSupervisorImpl: Received stop signal
17/08/03 17:10:26 INFO ReceiverSupervisorImpl: Stopping receiver with message: Stopped by driver:
17/08/03 17:10:26 INFO core: Stopping receiver
17/08/03 17:10:26 INFO ReceiverSupervisorImpl: Called receiver onStop
17/08/03 17:10:26 INFO ReceiverSupervisorImpl: Deregistering receiver 0
17/08/03 17:10:26 ERROR ReceiverTracker: Deregistered receiver for stream 0: Stopped by driver
17/08/03 17:10:26 INFO ReceiverSupervisorImpl: Stopped receiver 0
17/08/03 17:10:26 INFO BlockGenerator: Stopping BlockGenerator
17/08/03 17:10:26 INFO RecurringTimer: Stopped timer for BlockGenerator after time 1501751426600
17/08/03 17:10:26 INFO BlockGenerator: Waiting for block pushing thread to terminate
17/08/03 17:10:26 INFO BlockGenerator: Pushing out the last 0 blocks
17/08/03 17:10:26 INFO BlockGenerator: Stopped block pushing thread
17/08/03 17:10:26 INFO BlockGenerator: Stopped BlockGenerator
17/08/03 17:10:26 INFO ReceiverSupervisorImpl: Stopped receiver without error
17/08/03 17:10:26 INFO Executor: Finished task 0.0 in stage 0.0 (TID 0). 709 bytes result sent to driver
17/08/03 17:10:26 INFO TaskSetManager: Finished task 0.0 in stage 0.0 (TID 0) in 90061 ms on localhost (executor driver) (1/1)
17/08/03 17:10:26 INFO TaskSchedulerImpl: Removed TaskSet 0.0, whose tasks have all completed, from pool
17/08/03 17:10:26 INFO DAGScheduler: ResultStage 0 (start at NativeMethodAccessorImpl.java:0) finished in 90.079 s
17/08/03 17:10:26 INFO ReceiverTracker: All of the receivers have deregistered successfully
17/08/03 17:10:26 INFO ReceiverTracker: ReceiverTracker stopped
17/08/03 17:10:26 INFO JobGenerator: Stopping JobGenerator immediately
17/08/03 17:10:26 INFO RecurringTimer: Stopped timer for JobGenerator after time 1501751420000
17/08/03 17:10:26 INFO JobGenerator: Stopped JobGenerator
17/08/03 17:10:26 INFO JobScheduler: Stopped JobScheduler
17/08/03 17:10:26 INFO StreamingContext: StreamingContext stopped successfully
17/08/03 17:10:26 INFO SparkContext: Invoking stop() from shutdown hook
17/08/03 17:10:26 INFO SparkUI: Stopped Spark web UI at http://10.8.8.12:4041
17/08/03 17:10:26 INFO MapOutputTrackerMasterEndpoint: MapOutputTrackerMasterEndpoint stopped!
17/08/03 17:10:26 INFO MemoryStore: MemoryStore cleared
17/08/03 17:10:26 INFO BlockManager: BlockManager stopped
17/08/03 17:10:26 INFO BlockManagerMaster: BlockManagerMaster stopped
17/08/03 17:10:26 INFO OutputCommitCoordinator$OutputCommitCoordinatorEndpoint: OutputCommitCoordinator stopped!
17/08/03 17:10:26 INFO SparkContext: Successfully stopped SparkContext
17/08/03 17:10:26 INFO ShutdownHookManager: Shutdown hook called
17/08/03 17:10:26 INFO ShutdownHookManager: Deleting directory /private/var/folders/9p/m3jr24_n6rx_pvzzd9zb1z7c0000gr/T/spark-de8e7d93-bf1a-4194-af1a-7cb705a01d77
➜  spark-2.2.0-bin-hadoop2.7

```

### 4. zookeeper and kafka server 

* zookeeper
```txt
[2017-08-03 17:09:00,473] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:setData cxid:0x188 zxid:0xd3 txntype:-1 reqpath:n/a Error Path:/config/topics/fizzbuzz Error:KeeperErrorCode = NoNode for /config/topics/fizzbuzz (org.apache.zookeeper.server.PrepRequestProcessor)
[2017-08-03 17:09:00,571] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:create cxid:0x18d zxid:0xd4 txntype:-1 reqpath:n/a Error Path:/config/topics Error:KeeperErrorCode = NodeExists for /config/topics (org.apache.zookeeper.server.PrepRequestProcessor)
[2017-08-03 17:09:00,571] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:setData cxid:0x18e zxid:0xd5 txntype:-1 reqpath:n/a Error Path:/config/topics/fizzbuzz Error:KeeperErrorCode = NoNode for /config/topics/fizzbuzz (org.apache.zookeeper.server.PrepRequestProcessor)
[2017-08-03 17:09:00,572] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:setData cxid:0x18f zxid:0xd6 txntype:-1 reqpath:n/a Error Path:/config/topics/fizzbuzz Error:KeeperErrorCode = NoNode for /config/topics/fizzbuzz (org.apache.zookeeper.server.PrepRequestProcessor)
[2017-08-03 17:09:00,572] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:setData cxid:0x190 zxid:0xd7 txntype:-1 reqpath:n/a Error Path:/config/topics/fizzbuzz Error:KeeperErrorCode = NoNode for /config/topics/fizzbuzz (org.apache.zookeeper.server.PrepRequestProcessor)
[2017-08-03 17:09:00,572] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:setData cxid:0x191 zxid:0xd8 txntype:-1 reqpath:n/a Error Path:/config/topics/fizzbuzz Error:KeeperErrorCode = NoNode for /config/topics/fizzbuzz (org.apache.zookeeper.server.PrepRequestProcessor)
[2017-08-03 17:09:00,572] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:create cxid:0x193 zxid:0xda txntype:-1 reqpath:n/a Error Path:/config/topics Error:KeeperErrorCode = NodeExists for /config/topics (org.apache.zookeeper.server.PrepRequestProcessor)
[2017-08-03 17:09:00,572] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:create cxid:0x194 zxid:0xdb txntype:-1 reqpath:n/a Error Path:/config/topics Error:KeeperErrorCode = NodeExists for /config/topics (org.apache.zookeeper.server.PrepRequestProcessor)
[2017-08-03 17:09:00,572] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:create cxid:0x195 zxid:0xdc txntype:-1 reqpath:n/a Error Path:/config/topics Error:KeeperErrorCode = NodeExists for /config/topics (org.apache.zookeeper.server.PrepRequestProcessor)
[2017-08-03 17:09:00,573] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:create cxid:0x196 zxid:0xdd txntype:-1 reqpath:n/a Error Path:/config/topics Error:KeeperErrorCode = NodeExists for /config/topics (org.apache.zookeeper.server.PrepRequestProcessor)
[2017-08-03 17:09:00,574] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:create cxid:0x197 zxid:0xde txntype:-1 reqpath:n/a Error Path:/config/topics/fizzbuzz Error:KeeperErrorCode = NodeExists for /config/topics/fizzbuzz (org.apache.zookeeper.server.PrepRequestProcessor)
[2017-08-03 17:09:00,574] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:create cxid:0x198 zxid:0xdf txntype:-1 reqpath:n/a Error Path:/config/topics/fizzbuzz Error:KeeperErrorCode = NodeExists for /config/topics/fizzbuzz (org.apache.zookeeper.server.PrepRequestProcessor)
[2017-08-03 17:09:00,574] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:create cxid:0x199 zxid:0xe0 txntype:-1 reqpath:n/a Error Path:/config/topics/fizzbuzz Error:KeeperErrorCode = NodeExists for /config/topics/fizzbuzz (org.apache.zookeeper.server.PrepRequestProcessor)
[2017-08-03 17:09:00,580] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:create cxid:0x19b zxid:0xe2 txntype:-1 reqpath:n/a Error Path:/config/topics/fizzbuzz Error:KeeperErrorCode = NodeExists for /config/topics/fizzbuzz (org.apache.zookeeper.server.PrepRequestProcessor)
[2017-08-03 17:09:00,581] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:create cxid:0x1a1 zxid:0xe7 txntype:-1 reqpath:n/a Error Path:/brokers/topics/fizzbuzz Error:KeeperErrorCode = NodeExists for /brokers/topics/fizzbuzz (org.apache.zookeeper.server.PrepRequestProcessor)
[2017-08-03 17:09:00,581] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:create cxid:0x1a2 zxid:0xe8 txntype:-1 reqpath:n/a Error Path:/brokers/topics/fizzbuzz Error:KeeperErrorCode = NodeExists for /brokers/topics/fizzbuzz (org.apache.zookeeper.server.PrepRequestProcessor)
[2017-08-03 17:09:00,582] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:create cxid:0x1a3 zxid:0xe9 txntype:-1 reqpath:n/a Error Path:/brokers/topics/fizzbuzz Error:KeeperErrorCode = NodeExists for /brokers/topics/fizzbuzz (org.apache.zookeeper.server.PrepRequestProcessor)
[2017-08-03 17:09:00,582] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:create cxid:0x1a4 zxid:0xea txntype:-1 reqpath:n/a Error Path:/brokers/topics/fizzbuzz Error:KeeperErrorCode = NodeExists for /brokers/topics/fizzbuzz (org.apache.zookeeper.server.PrepRequestProcessor)
[2017-08-03 17:09:00,586] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:create cxid:0x1a9 zxid:0xeb txntype:-1 reqpath:n/a Error Path:/brokers/topics/fizzbuzz/partitions/0 Error:KeeperErrorCode = NoNode for /brokers/topics/fizzbuzz/partitions/0 (org.apache.zookeeper.server.PrepRequestProcessor)
[2017-08-03 17:09:00,587] INFO Got user-level KeeperException when processing sessionid:0x15da61b63f60000 type:create cxid:0x1aa zxid:0xec txntype:-1 reqpath:n/a Error Path:/brokers/topics/fizzbuzz/partitions Error:KeeperErrorCode = NoNode for /brokers/topics/fizzbuzz/partitions (org.apache.zookeeper.server.PrepRequestProcessor)

```
* kafka 

```txt
[2017-08-03 17:09:00,573] INFO Topic creation {"version":1,"partitions":{"0":[0]}} (kafka.admin.AdminUtils$)
[2017-08-03 17:09:00,581] INFO [KafkaApi-0] Auto creation of topic fizzbuzz with 1 partitions and replication factor 1 is successful (kafka.server.KafkaApis)
[2017-08-03 17:09:00,581] INFO Topic creation {"version":1,"partitions":{"0":[0]}} (kafka.admin.AdminUtils$)
[2017-08-03 17:09:00,581] INFO Topic creation {"version":1,"partitions":{"0":[0]}} (kafka.admin.AdminUtils$)
[2017-08-03 17:09:00,581] INFO Topic creation {"version":1,"partitions":{"0":[0]}} (kafka.admin.AdminUtils$)
[2017-08-03 17:09:00,581] INFO Topic creation {"version":1,"partitions":{"0":[0]}} (kafka.admin.AdminUtils$)
[2017-08-03 17:09:00,593] INFO [ReplicaFetcherManager on broker 0] Removed fetcher for partitions fizzbuzz-0 (kafka.server.ReplicaFetcherManager)
[2017-08-03 17:09:00,597] INFO Loading producer state from offset 0 for partition fizzbuzz-0 with message format version 2 (kafka.log.Log)
[2017-08-03 17:09:00,598] INFO Completed load of log fizzbuzz-0 with 1 log segments, log start offset 0 and log end offset 0 in 3 ms (kafka.log.Log)
[2017-08-03 17:09:00,598] INFO Created log for partition [fizzbuzz,0] in /tmp/kafka-logs with properties {compression.type -> producer, message.format.version -> 0.11.0-IV2, file.delete.delay.ms -> 60000, max.message.bytes -> 1000012, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, min.insync.replicas -> 1, segment.jitter.ms -> 0, preallocate -> false, min.cleanable.dirty.ratio -> 0.5, index.interval.bytes -> 4096, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, cleanup.policy -> [delete], flush.ms -> 9223372036854775807, segment.ms -> 604800000, segment.bytes -> 1073741824, retention.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760, flush.messages -> 9223372036854775807}. (kafka.log.LogManager)
[2017-08-03 17:09:00,599] INFO Partition [fizzbuzz,0] on broker 0: No checkpointed highwatermark is found for partition fizzbuzz-0 (kafka.cluster.Partition)
[2017-08-03 17:09:00,599] INFO Partition [fizzbuzz,0] on broker 0: fizzbuzz-0 starts at Leader Epoch 0 from offset 0. Previous Leader Epoch was: -1 (kafka.cluster.Partition)
[2017-08-03 17:09:00,825] INFO Updated PartitionLeaderEpoch. New: {epoch:0, offset:0}, Current: {epoch:-1, offset-1} for Partition: fizzbuzz-0. Cache now contains 0 entries. (kafka.server.epoch.LeaderEpochFileCache)
[2017-08-03 17:10:18,994] INFO [Group Metadata Manager on Broker 0]: Removed 0 expired offsets in 1 milliseconds. (kafka.coordinator.group.GroupMetadataManager)

```
