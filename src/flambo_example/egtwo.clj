(ns flambo-example.egtwo
  (:require [flambo.conf :as conf]
            [flambo.api :as f]
            [flambo.streaming :as fs])
  #_(:require [clj-kafka.producer :as p]
            [clj-kafka.zk :as zk])
  (:require [clojure.string :as s])
  (:gen-class))

(def master "local[2]")
(def app-name "fkse")
(def conf {})
(def env {
           "spark.executor.memory" "1G",
           "spark.files.overwrite" "true"})

(defn produce-lines
  "Publishes lines from the text to kafka, keyed on a random word in the line with the full line as
  the value."
  [frequency]
  (let [;; brokers (zk/brokers {"zookeeper.connect" "localhost:2181"})
        ;; broker-list (zk/broker-list brokers)
        ;; producer (p/producer {"metadata.broker.list" broker-list})
        topic "test"
        lines (s/split (slurp "resources/data.txt") #"\n")]
    (loop []
      #_(p/send-messages producer
                       (map #(p/message topic
                                        (.getBytes (rand-nth (s/split % #" ")))
                                        (.getBytes %)) lines))
      (Thread/sleep frequency)
      (recur))))


(def c (-> (conf/spark-conf)
           (conf/master master)
           (conf/app-name "adapters")
           (conf/set "spark.akka.timeout" "300")
           (conf/set-executor-env env)))

(def ssc (fs/streaming-context c 2000))


(defn -main
  "This is where the magic happens."
  [& args]
  (let [;; get a streaming context with a 2 second batch interval
        #_stream #_(fs/kafka-stream ssc
                                "localhost:2181"
                                "word-count"
                                {"test" 1})]

    ;; in a separate thread, pull lines from data.txt and randomly publish them into kafka
    #_(future (produce-lines 1000))

    #_(do
    (-> stream ;; this is our initial stream
        (fs/map (memfn _2)) ;; kafka messages are a (key, value). this pulls out the value. the
                            ;; key, if you need it, is in _1
        (fs/flat-map (f/fn [l] (s/split l #" "))) ;; at this point we have our "line of text and stuff",
                                                  ;; so split it into words
        (fs/map (f/fn [w] [w 1])) ;; and for each of those words, get a ["word" 1] pair
        (fs/reduce-by-key-and-window (f/fn [x y] (+ x y)) (* 10 60 1000) 2000) ;; and reduce them
                                                                               ;; by key on a sliding
                                                                               ;; window
        (fs/print) ;; print out the results
        )
    
    (.start ssc)
    (.awaitTermination ssc)
    )
    ))
