package ru.neoflex.imdbApp.kryo

import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Aggregator

import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.sql.TypedColumn
import java.util.{ List => JList, ArrayList }
import scala.collection.mutable.ListBuffer

object StringArrayAggregator
    extends Aggregator[String, ListBuffer[String], String] {

  def zero: ListBuffer[String] = ListBuffer.empty[String]

  def reduce(
    buffer:  ListBuffer[String],
    element: String
  ): ListBuffer[String] = {
    buffer += element
    buffer
  }

  def merge(
    b1: ListBuffer[String],
    b2: ListBuffer[String]
  ): ListBuffer[String] = {
    b1 ++= b2
    b1
  }

  def finish(reduction: ListBuffer[String]): String =
    reduction.mkString("[", ",", "]")

  def bufferEncoder: Encoder[ListBuffer[String]] =
    Encoders.kryo[ListBuffer[String]]

  def outputEncoder: Encoder[String] = Encoders.STRING
}

object KryoEx3 {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName("Kryo Serialization Example")
      .config("spark.kryo.registrationRequired", true)
      .config("spark.serializer", classOf[KryoSerializer].getName())
      .config("spark.kryo.registrator", classOf[KryoReg].getName())
      .getOrCreate()

    import spark.implicits._

    val data = Seq("Alice", "Bob", "Catherine", "David").toDS()

    val stringArrayAggregator: TypedColumn[String, String] =
      StringArrayAggregator.toColumn.name("names")

    val aggregatedData: Dataset[String] = data
      .select(stringArrayAggregator)
      .as[String]

    aggregatedData.printSchema()
    aggregatedData.show(false)

    spark.stop()
  }
}
