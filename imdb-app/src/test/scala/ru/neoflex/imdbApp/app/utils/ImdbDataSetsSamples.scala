package ru.neoflex.imdbApp.app.utils

import org.apache.spark.SparkConf
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import ru.neoflex.imdbApp.models.TitleBasicsItem
import ru.neoflex.imdbApp.models.TitleBasicsRow

object ImdbDataSetsSamples {

  val SEED = 123L

  import ru.neoflex.imdbApp.app.ImdbDataSets

  def main(args: Array[String]): Unit = {

    val sparkConf =
      new SparkConf().setAppName("ImdbDatasetSamples").setMaster("local[*]")

    implicit val spark: SparkSession =
      SparkSession.builder.config(sparkConf).getOrCreate()

    import spark.implicits._

    val imdbDataSets =
      ImdbDataSets(datasetDir = "app-vol/datasets/imdb", spark)

    val titleBasicDSSample: Dataset[TitleBasicsRow] =
      imdbDataSets.titleBasicsDataset
        .filter(!_.titleType.contains("tvEpisode"))
        .sample(
          withReplacement = false,
          fraction = 1e-5,
          SEED
        )
        .withColumn("rowNum", row_number().over(Window.orderBy("primaryTitle")))
        .filter(col("rowNum") < 21)
        .drop(col("rowNum"))
        .as[TitleBasicsItem]
        .map { item: TitleBasicsItem =>
          TitleBasicsRow(
            tconst = item.tconst,
            titleType = item.titleType,
            primaryTitle = item.primaryTitle,
            originalTitle = item.originalTitle,
            isAdult = item.isAdult.map {
              case true => 1
              case _    => 0
            },
            startYear = item.startYear,
            endYear = item.endYear,
            runtimeMinutes = item.runtimeMinutes,
            genres = item.genres.mkString(",")
          )
        }

    titleBasicDSSample.write
      .format("csv")
      .option("header", "true")
      .option("delimiter", "\t")
      .csv("target/test-data")

    println("==========Done==============")

  }

}
