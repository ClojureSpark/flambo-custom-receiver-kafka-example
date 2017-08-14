(ns flambo-example.socket
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
        stream (.socketTextStream ssc "localhost" 9999)]
    (do
      (.print stream)
      (.start ssc)
      (.awaitTermination ssc))))
