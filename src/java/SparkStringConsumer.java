package pl.training.spark.streaming;

import kafka.serializer.StringDecoder;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// Clojure Call: (SparkStringConsumer/main (into-array String ["11"]))

public class SparkStringConsumer {

    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf()
                .setAppName("Consumer")
                .setMaster("local");

        try (JavaSparkContext context = new JavaSparkContext(conf);
             JavaStreamingContext streamingContext = new JavaStreamingContext(context, new Duration(1_000))) {

            Map<String, String> parameters = new HashMap<>();
            parameters.put("metadata.broker.list", "sandbox.hortonworks.com:6667");
            Set<String> topics = Collections.singleton("messages");

            JavaPairInputDStream<String, String> stream =
                    KafkaUtils.createDirectStream(streamingContext, String.class, String.class,
                            StringDecoder.class, StringDecoder.class, parameters, topics);

            stream.foreachRDD(rdd -> rdd.foreach(record -> {
                System.out.println(record._1 + ": " + record._2);
            }));

            streamingContext.start();
            streamingContext.awaitTermination();
        }
    }

}
