package ru.neoflex.imdbApp.session

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import ru.neoflex.imdbApp.kryo.KryoReg

trait SparkSessionCreator {

  def buildSparkSession(sparkConf: SparkConf) =
    SparkSession.builder().config(sparkConf).getOrCreate()

  def withKryo(inital: SparkConf): SparkConf =
    inital
      .set(
        "spark.serializer",
        "org.apache.spark.serializer.KryoSerializer"
      )
      .set("spark.kryo.registrator", classOf[KryoReg].getName())
  // .set("spark.kryo.registrationRequired", true)

  def withAppName(
    initial: SparkConf,
    appName: String
  ): SparkConf =
    initial.setAppName(appName)

  def withPerExecutorMemory(initial: SparkConf, mem: String) =
    initial.set("spark.executor.memory", mem)

}
