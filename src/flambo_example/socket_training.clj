(ns flambo-example.socket-training
  (:require [flambo.conf :as conf]
            [flambo.api :as api]
            [flambo.function :as function]
            [flambo.streaming :as streaming]
            [clojure.tools.logging :as log])
  (:import [org.apache.spark.streaming.api.java JavaStreamingContext]
           [org.apache.spark.streaming.receiver Receiver]
           [org.apache.spark.storage StorageLevel]
           [org.apache.kafka.common.serialization StringSerializer]
           [org.apache.kafka.clients.producer KafkaProducer ProducerRecord Callback RecordMetadata]
           [java.util Map]
           [java.io PrintWriter]
           [java.net ServerSocket]
           [breeze.linalg DenseVector]
           [org.apache.spark.mllib.linalg Vectors]
           [org.apache.spark.mllib.regression StreamingLinearRegressionWithSGD LabeledPoint]
           [org.apache.spark.streaming Seconds StreamingContext])
  (:gen-class))

(def env {"spark.executor.memory" "2G" "spark.files.overwrite" "true"})

(defn -main
  [& args]
  (let [c (-> (conf/spark-conf)
              (conf/master "local[*]")
              (conf/app-name "flambo-custom-receiver-kafka-eample")
              (conf/set "spark.akka.timeout" "1000")
              (conf/set-executor-env env))
        ssc (streaming/streaming-context c 10000)
        stream (.socketTextStream ssc "localhost" 9999)
        ;; 基于原始文本元素生成活动流
        events (streaming/map
                stream
                (fn [record]
                  (let [event (clojure.string/split record #",")]
                    (list (nth event 0) (nth event 1) (nth event 2)))))]
    (do
      ;; 简单的片段式(10s一次)统计分析,只有count,sum等的时候才会触发最终计算
      ;; 文档API详情: src/clojure/flambo/api.clj & src/clojure/flambo/streaming.clj
      (streaming/foreach-rdd
       events
       (fn [rdd time]
         (let [rcount (.count rdd)
               uniq-users (-> rdd
                              (streaming/map (fn [& a] (first a)))
                              api/distinct api/count)]
           (log/info (str "=====rdd: " rdd "=====time: " time "======rcount: " rcount "=====uniq-users: " uniq-users)))))
      (.start ssc)
      (.awaitTermination ssc))))
