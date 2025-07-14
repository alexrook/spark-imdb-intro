package ru.neoflex.imdbApp.app.udf

import org.apache.spark.sql._

import org.apache.spark.sql.expressions.Aggregator

import scala.collection.mutable.ListBuffer

object SeriesAgg extends Aggregator[String, ListBuffer[String], Array[String]] {

  def zero: ListBuffer[String] = ListBuffer.empty[String]

  def reduce(buffer: ListBuffer[String], data: String): ListBuffer[String] = {
    buffer += data
    buffer
  }

  def merge(
    a: ListBuffer[String],
    b: ListBuffer[String]
  ): ListBuffer[String] = {
    a ++= b
    a
  }

  def finish(reduction: ListBuffer[String]): Array[String] = {
    val ret = reduction.toArray
    println(ret.mkString("====================="))
    ret
  }

  def bufferEncoder: Encoder[ListBuffer[String]] =
    Encoders.kryo[ListBuffer[String]]

  def outputEncoder: Encoder[Array[String]] =
    Encoders.kryo[Array[String]]

    
}
