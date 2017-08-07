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
           #_[org.apache.spark.mllib.regression StreamingLinearRegressionWithSGD LabeledPoint]
           [java.util Map])
  (:gen-class))

(defn num-range-receiver
  [n]
  (proxy [Receiver] [(StorageLevel/MEMORY_AND_DISK_2)]
    (onStart []
      (require '[clojure.tools.logging :as log])
      (log/info "Starting receiver")
      (future
        (doseq [x (range 1 n)]
          (log/info (str "Store: " x))
          (.store this x)
          (Thread/sleep (rand-int 500)))))
    (onStop [] (log/info "Stopping receiver"))))

(api/defsparkfn fizzbuzz [x]
  (let [r (cond
            (zero? (mod x 15)) "FizzBuzz"
            (zero? (mod x 5)) "Buzz"
            (zero? (mod x 3)) "Fizz"
            :else x)]
    (str [x r])))

(api/defsparkfn publish [rdd _]
  (doseq [x (.collect rdd)]
    (log/info (str "====>>> Sending to Kafka fizzbuzz " x " , " (type x)))
    #_(send! @producer "fizzbuzz" x (->callback x)) ))

(api/defsparkfn publish-using-partitions [rdd _]
  (.foreachPartition
   rdd
   (function/void-function
    (api/fn [xs]
      (doseq [x (iterator-seq xs)]
        (log/info (str "---->>> Sending to Kafka fizzbuzz " x " , " (type x) ))
        #_(send! (memoized-producer producer-config) "fizzbuzz" x (->callback x)))))))

(def env {"spark.executor.memory" "1G"
          "spark.files.overwrite" "true"})

(def c (-> (conf/spark-conf)
           (conf/master "local[*]")
           (conf/app-name "flambo-custom-receiver-kafka-eample")
           (conf/set "spark.akka.timeout" "1000")
           (conf/set-executor-env env)))

#_(defn -main [& [n]]
  (log/info "Starting!")
  (let [ssc (streaming/streaming-context c 10000)
        stream (.receiverStream ^JavaStreamingContext ssc (num-range-receiver (Integer/parseInt n)))]
    (-> stream
        (streaming/map fizzbuzz)
        (streaming/foreach-rdd publish-using-partitions))
    (.start ssc)
    (.awaitTerminationOrTimeout ssc 90000)))

;; ------------>>> comment --->>>>>>>

(def ssc (streaming/streaming-context c 10000))

(def stream (.socketTextStream ssc "localhost" 9999))
