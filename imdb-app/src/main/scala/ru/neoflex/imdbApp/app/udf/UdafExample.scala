package ru.neoflex.imdbApp.app.udf

import org.apache.spark.sql.Encoder
import org.apache.spark.sql.Encoders
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.log4j.LogManager

object UdafExample {

  import ru.neoflex.imdbApp.app.udf.SeriesAgg

  val log = LogManager.getLogger(UdafExample.getClass())

  def run(spark: SparkSession): Unit = {

    log.debug("Running UdafExample")

    import spark.implicits.{ newStringArrayEncoder => _, _ }

    //Basic
    val basicD = List(List("Hello", "Kryo"), List("Spark", "Serialization"))

    // Создание DataFrame с использованием Kryo Encoder
    val df = basicD.toDF("words")

    df.show(
      false
    ) // show(false) используется для полного отображения содержимого колон

    val sAgg = udaf(SeriesAgg)

    // UDAF
    val data = Seq("Hello", "Spark", "Kryo", "Serialization").toDF("value")

    // Агрегация данных с использованием UDAF
    val aggregatedData = {
      implicit val enc: Encoder[Array[String]] = Encoders.kryo[Array[String]]
      data
        .agg(sAgg(data("value")).as("aggregated"))
        .as[Array[String]]
        .map(_.mkString(","))
    }

    println(s"Schema DDL:" + aggregatedData.schema.toDDL)

    aggregatedData.show(false)

    spark.stop()
  }

}
