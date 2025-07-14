package ru.neoflex.imdbApp

import org.apache.spark.sql.SparkSession

object SimpleApp {

  def main(args: Array[String]): Unit = {
    val ds1 = "file:///var/tmp/data/datasets/imdb/name.basics.tsv"
    val sc = SparkSession.builder.appName("Simple Application").getOrCreate()
    val nameBasicsData = sc.read.textFile(ds1).cache()
    val numAs = nameBasicsData.filter(line => line.contains("a")).count()
    val numBs = nameBasicsData.filter(line => line.contains("b")).count()
    println(s"Lines with a: $numAs, Lines with b: $numBs")
    sc.stop()
  }

}
