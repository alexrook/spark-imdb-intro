package ru.neoflex.imdbApp.kryo

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.sql.DataFrame
import ru.neoflex.imdbApp.models.config.AppModulesEnum
import ru.neoflex.imdbApp.models.config.FilesConfig
import org.apache.spark.sql.Encoders
import org.apache.spark.sql.Encoder

object KryoEx2 {

  import ru.neoflex.imdbApp.models.config.AppConfig
  import ru.neoflex.imdbApp.app.udf.SeriesAgg

  def main(args: Array[String]): Unit = {
    run(AppConfig.apply("A", AppModulesEnum.KryoEx2, FilesConfig("n")))
  }

  def run(appConfig: AppConfig): Unit = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName("Kryo Serialization Example")
      .config("spark.kryo.registrationRequired", true)
      .config("spark.serializer", classOf[KryoSerializer].getName())
      .config("spark.kryo.registrator", classOf[KryoReg].getName())
      .getOrCreate()

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

    println(s"=======================" + aggregatedData.schema.toDDL)

    aggregatedData.show(false)

    spark.stop()
  }

}
