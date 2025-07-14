package ru.neoflex.imdbApp.dataset

import org.apache.spark.sql.SparkSession

trait SamplesDatasets {

  def spark: SparkSession

  lazy val imdbDataSetSamples: ImdbDataSets = ImdbDataSets(
    datasetDir = "imdb-app/src/test/resources/ds",
    datasetFileEx = "sample.tsv",
    spark = spark
  )

}
