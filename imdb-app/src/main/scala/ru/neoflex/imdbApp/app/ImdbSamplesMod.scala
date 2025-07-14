package ru.neoflex.imdbApp

import org.apache.spark.SparkConf
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import ru.neoflex.imdbApp.models.config.AppConfig

/** создание тестовых данных
  */
object ImdbSamplesMod {

  import ru.neoflex.imdbApp.dataset._
  import ru.neoflex.imdbApp.models._

  val SEED = 123L

  def run(appConfig: AppConfig): Unit = {

    val sparkConf =
      new SparkConf().setAppName("ImdbDatasetSamples")

    implicit val spark: SparkSession =
      SparkSession.builder.config(sparkConf).getOrCreate()

    val imdbDataSets: ImdbDataSets =
      ImdbDataSets(appConfig.files.datasetDir, spark = spark)

    import spark.implicits._

    val titleRatingsSample: Dataset[TitleRatingItem] =
      imdbDataSets.titleRatingsDataset
        .sample(
          withReplacement = false,
          fraction = 1e-4,
          SEED
        )
        .withColumn("rowNum", row_number().over(Window.orderBy("tconst")))
        .filter(col("rowNum") < 21)
        .as[TitleRatingItem]

    //titleRatingsSample.show()

    //я хочу фильмы только с рейтингом
    val titlesWithRating: Dataset[TitleBasicsItem] =
      imdbDataSets.titleBasicsDataset
        .filter(!_.titleType.contains("tvEpisode")) //и не сериалы
        .join(titleRatingsSample, "tconst")
        .drop("averageRating", "numVotes", "rowNum")
        .as[TitleBasicsItem]
        .cache()

    titlesWithRating.show(20)
    println(titlesWithRating.count())

    // val titleBasicSample: Dataset[TitleBasicsRow] =
    //   titlesWithRating
    //     .as[TitleBasicsItem]
    //     .map { item: TitleBasicsItem =>
    //       TitleBasicsRow(
    //         tconst = item.tconst,
    //         titleType = item.titleType,
    //         primaryTitle = item.primaryTitle,
    //         originalTitle = item.originalTitle,
    //         isAdult = item.isAdult.map {
    //           case true => 1
    //           case _    => 0
    //         },
    //         startYear = item.startYear,
    //         endYear = item.endYear,
    //         runtimeMinutes = item.runtimeMinutes,
    //         genres = item.genres.mkString(",")
    //       )
    //     }

    // titleBasicSample.show()

    // titleBasicSample.write
    //   .mode("append")
    //   .format("csv")
    //   .option("header", "true")
    //   .option("delimiter", "\t")
    //   .csv(s"${appConfig.files.datasetDir}/samples/titleBasicSample")

    // val titleRatingsSample: Dataset[TitleRatingItem] =
    //   titleBasicSample
    //     .join(
    //       imdbDataSets.titleRatingsDataset,
    //       "tconst"
    //     )
    //     .as[TitleRatingItem]

    // titleRatingsSample.show(100)

    println("==========Done==============")

  }

}
