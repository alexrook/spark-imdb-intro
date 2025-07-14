package ru.neoflex.imdbApp.app

object ImdbStatsApp {

  import org.apache.spark.sql.SparkSession

  import ru.neoflex.imdbApp.models.config.AppConfig

  def run(appConfig: AppConfig): Unit = {

    implicit val spark: SparkSession =
      SparkSession.builder.appName(appConfig.name).getOrCreate()

    val imdbDataSets: ImdbDataSets = ImdbDataSets(appConfig, spark = spark)

    import imdbDataSets._

    Stats
      .topNByGenre(
        genre = "Fantasy",
        topN = 114,
        titleRatings = titleRatingsDataset,
        titleBasic = titleBasicsDataset
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
