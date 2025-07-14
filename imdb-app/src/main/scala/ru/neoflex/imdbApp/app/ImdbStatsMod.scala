package ru.neoflex.imdbApp.app

import org.apache.spark.sql.SparkSession

object ImdbStatsMod {

  import ru.neoflex.imdbApp.dataset._
  import ru.neoflex.imdbApp.models.config.AppConfig

  def run(appConfig: AppConfig): Unit = {

    implicit val spark: SparkSession =
      SparkSession.builder.appName(appConfig.name).getOrCreate()

    val imdbDataSets: ImdbDataSets =
      ImdbDataSets(
        datasetDir = appConfig.files.datasetDir,
        datasetFileEx = ".tsv",
        spark = spark
      )

    import imdbDataSets._

    Stats
      .topNByGenre(
        genre = "Fantasy",
        topN = 114,
        titleBasic = titleBasicsDataset,
        titleRatings = titleRatingsDataset
      )
    //   .show(500)

    Stats
      .averageRatingsByGenre(
        titleBasic = titleBasicsDataset,
        titleRatings = titleRatingsDataset
      )
    //.show(500)

    Stats
      .averageRatingsByActor(
        nameBasics = nameBasicsDataset,
        titlePrincipals = titlePrincipalsDataset,
        titleRatings = titleRatingsDataset,
        titleBasic = titleBasicsDataset
      )
    //.show(100)

    Stats
      .ratingCount(titleRatings = titleRatingsDataset)
      .show(500)

    spark.stop()
  }

}
