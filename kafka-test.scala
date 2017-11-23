 import org.apache.kafka.common.serialization.StringDeserializer
 import org.apache.spark.sql.SparkSession
 import org.apache.spark.sql.functions.count
 import org.apache.spark.streaming.{Seconds, StreamingContext}
 import org.apache.spark.streaming.kafka010._
 import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
 import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
 import org.apache.spark.sql.types.{StringType, StructType, TimestampType}

object KafkaTest {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder
      .master("local[5]")
      .appName("Spark Word Count")
      .getOrCreate()

    val ssc = new StreamingContext(spark.sparkContext, Seconds(3))


    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "12345",
      "auto.offset.reset" -> "earliest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("dbserver1.inventory.customers")
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )



    stream.foreachRDD { (rdd, time) =>
      val data = rdd.map(record => record.value)
      data.foreach(println)
      println(time)

    }

    ssc.start()             // Start the computation
    ssc.awaitTermination()

  }
}
