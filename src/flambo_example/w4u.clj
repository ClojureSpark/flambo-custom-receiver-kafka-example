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

(defn -main
  [& [n]]
  (let [c (-> (conf/spark-conf)
              (conf/master "local[*]")
              (conf/app-name "Consumer"))
        context (JavaSparkContext. c)
        streaming-context (JavaStreamingContext. context (Duration. 1000))
        parameters (HashMap. {"metadata.broker.list" "localhost:9092"})
        topics (Collections/singleton "w4u_messages")
        stream (streaming/kafka-direct-stream streaming-context String String StringDecoder StringDecoder parameters topics)]
    (streaming/foreach-rdd
     stream
     (fn [rdd arg2]
       (log/info (str "=====" rdd "=====" arg2))
       (api/foreach
        rdd (api/fn [x]
              (log/info (str "*********" x "*****" ))))))
    (.start streaming-context)
    (.awaitTermination streaming-context)))
