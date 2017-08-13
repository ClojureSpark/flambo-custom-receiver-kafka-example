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

(api/defsparkfn publish-using-partitions [rdd _]
  (.foreachPartition
   rdd
   (function/void-function
    (api/fn [xs]
      (doseq [x (iterator-seq xs)]
        ;;
        (log/info (str "----" x))
        ;;
        )))))

(-> stream
    (streaming/foreach-rdd publish-using-partitions))

(.start streaming-context)
(.awaitTermination streaming-context)
