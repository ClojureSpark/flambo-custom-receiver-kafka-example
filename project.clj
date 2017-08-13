(defproject flambo-custom-receiver-kafka-eample "0.1.0-SNAPSHOT"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [yieldbot/flambo "0.8.2"]
                 [org.apache.spark/spark-streaming_2.11 "2.2.0"]
                 [org.apache.spark/spark-streaming-kafka_2.11 "1.6.3"]
                 [org.apache.spark/spark-streaming-flume_2.11 "2.2.0"]
                 [org.apache.spark/spark-mllib_2.11 "2.2.0"]]
  :profiles {:provided {:dependencies
                        [[org.apache.spark/spark-core_2.11 "2.2.0"]]}}
  :java-source-paths ["src/java"]
  :jar-name "flambo-example.jar"
  :uberjar-name "flambo-example-standalone.jar"
  :aot :all
  :main flambo-example.w4u)
