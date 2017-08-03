(ns flambo-example.core
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
           [java.util Map])
  ;; `-main` + `gen-class` => 作为一个class, 给spark-submit去调用
  (:gen-class))

;;; ------------- kafka -----------

(defn producer! [cfg] (KafkaProducer. ^Map cfg))

(def producer-config
  {"value.serializer"  StringSerializer
   "key.serializer"    StringSerializer
   ;; 这里的9092是kafka的server端口: `lsof -i:9092`
   "bootstrap.servers" "localhost:9092"})

(def producer (delay (producer! producer-config)))
;; memoize kafka的连接
(def memoized-producer (memoize producer!))

;; 发送消息producer生产者
(defn send!
  ([producer topic data]
   (send! producer topic data nil))
  ([^KafkaProducer producer topic data ?callback]
   (let [record (ProducerRecord. topic data)]
     (.send producer record ?callback))))

;; onCompletion完成事件发送的数据,目前只是打印出来
(defn metadata->str [^RecordMetadata x]
  (str {:offset (.offset x)
        :partition (.partition x)
        :topic (.topic x)}))

;; Callback监听器, onCompletion完成事件的回调方法, log =>
;; Metadata for  [3 "Fizz"]  is  {:offset 0, :partition 0, :topic "fizzbuzz"}
;; Metadata for  [4 4]  is  {:offset 2, :partition 0, :topic "fizzbuzz"}
(defn ->callback [x]
  (reify Callback
    (onCompletion [_ metadata ex]
      (if ex
        (log/error "Sending " x " failed with " (.getMessage ex))
        (log/info "Metadata for " x " is " (metadata->str metadata))))))

;;; ------------ spark ------------

;; Spark的消息接收Receiver监听器, onStart监听开始的回调, future是等待执行返回, onStop监听结束的回调
;; (def f (future (Thread/sleep 10000) (println "done") 100)) ;;=> @f done 100
(defn num-range-receiver
  [n]
  (proxy [Receiver] [(StorageLevel/MEMORY_AND_DISK_2)]
    (onStart []
      (require '[clojure.tools.logging :as log]) ;; required, otherwise there's unbound var exception
      (log/info "Starting receiver")
      (future
        (doseq [x (range 1 n)]
          (log/info (str "Store: " x))
          ;; 这里的this是Receiver监听器的this, .store ?
          (.store this x)
          ;; 代表执行的时间是不定的
          (Thread/sleep (rand-int 500)))))
    (onStop [] (log/info "Stopping receiver"))))

;; 流的中游: api/defsparkfn => fizzbuzz 数据生成器, 代表接受了什么消息,数据分析之后, 返回什么结果
(api/defsparkfn fizzbuzz [x]
  (let [r (cond
            (zero? (mod x 15)) "FizzBuzz"
            (zero? (mod x 5)) "Buzz"
            (zero? (mod x 3)) "Fizz"
            :else x)]
    (str [x r])))

;; 消息生产: 消息推送到Kafka, rdd格式数据, 并把(->callback x)传给Kafka
(api/defsparkfn publish [rdd _]
  (doseq [x (.collect rdd)]
    (log/info (str "Sending to Kafka fizzbuzz " x))
    (send! @producer "fizzbuzz" x (->callback x))))

;; partitions模式
(api/defsparkfn publish-using-partitions [rdd _]
  (.foreachPartition rdd
    (function/void-function
      (api/fn [xs]
        (doseq [x (iterator-seq xs)]
          (log/info (str "Sending to Kafka fizzbuzz " x))
          (send! (memoized-producer producer-config) "fizzbuzz" x (->callback x)))))))

(def env {"spark.executor.memory" "1G"
          "spark.files.overwrite" "true"})

;; [& [n]]接受来自spark-submit命令的参数
(defn -main [& [n]]
  (log/info "Starting!")
  ;; 打开spark streaming, receiverStream接收kafka消息流
  (let [c (-> (conf/spark-conf)
              (conf/app-name "flambo-custom-receiver-kafka-eample")
              (conf/set "spark.akka.timeout" "1000")
              (conf/set-executor-env env))
        ssc (streaming/streaming-context c 10000)
        stream (.receiverStream ^JavaStreamingContext ssc (num-range-receiver (Integer/parseInt n)))]
    ;; 消息数据的流处理streaming/map, streaming/foreach-rdd. map fizzbuzz数据分析函数, foreach-rdd推送消息publish (partitions模式)
    (-> stream
        (streaming/map fizzbuzz)
        (streaming/foreach-rdd publish-using-partitions))
    (.start ssc)
    (.awaitTerminationOrTimeout ssc 90000)))
