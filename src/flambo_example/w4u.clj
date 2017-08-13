(ns flambo-example.w4u
  (:require [flambo.conf :as conf]
            [flambo.api :as api]
            [flambo.function :as function]
            [flambo.streaming :as streaming]
            [clojure.tools.logging :as log])
  (:import (kafka.serializer StringDecoder)
           (org.apache.spark.api.java JavaSparkContext)
           (org.apache.spark.streaming Duration)
           (org.apache.spark.streaming.api.java JavaPairInputDStream)
           (org.apache.spark.streaming.api.java JavaStreamingContext)
           (org.apache.spark.streaming.kafka KafkaUtils)
           ;;
           (kafka.producer Partitioner)
           (kafka.utils VerifiableProperties)
           ;;
           (java.util Collections)
           (java.util HashMap)
           (java.util Map)
           (java.util Set))
  (:gen-class))

(def c (-> (conf/spark-conf)
           (conf/master "local[*]")
           (conf/app-name "Consumer")))

(def context (JavaSparkContext. c))

(def streaming-context (JavaStreamingContext. context (Duration. 1000)))

(def parameters (HashMap. {"metadata.broker.list" "localhost:9092"}))

(def topics (Collections/singleton "w4u_messages"))

(def stream (KafkaUtils/createDirectStream streaming-context (class String) (class String) (class StringDecoder) (class StringDecoder) parameters topics))

#_(api/defsparkfn publish-using-partitions [rdd _]
  (.foreachPartition
   rdd
   (function/void-function
    (api/fn [xs]
      (doseq [x (iterator-seq xs)]
        ;;
        (log/info (str "----" x))
        ;;
        )))))

(streaming/foreach-rdd stream
                       (fn [rdd arg2]
                         ;; clojure.lang.ArityException: Wrong number of args (2) passed to: w4u/fn--57
                         ;; 17/08/13 18:06:26 INFO w4u: ----org.apache.spark.api.java.JavaPairRDD@6a270134===1502618786000 ms
                         (log/info (str "----" rdd "===" arg2))

                         #_(-> rdd #_(f/parallelize c [1 2 3 4 5])
                             (api/foreach (api/fn [x]
                                          (log/info (str "++++++" x "+++++"))
                                            )))

                         ;; java.lang.NoSuchMethodException: java.lang.Class.<init>(kafka.utils.VerifiableProperties)
                         ;;(api/foreach rdd #())

                         ;; java.lang.ClassCastException: Cannot cast flambo_example.w4u$fn__57$fn__58 to org.apache.spark.api.java.function.VoidFunction
                         ;; (.foreach rdd (fn [record] (log/info record)))

                         ;; Caused by: java.lang.NoSuchMethodException: java.lang.Class.<init>(kafka.utils.VerifiableProperties)
                         (api/foreach rdd (api/fn [x]
                                            (log/info (str "*********" x "*****" ))
                                            ;;x
                                            ))
                         
                         )
                       )

#_(-> stream
    (streaming/foreach-rdd publish-using-partitions))

(.start streaming-context)
(.awaitTermination streaming-context)
