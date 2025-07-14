package ru.neoflex.imdbApp.app.stats

import org.apache.spark.sql.SparkSession
import org.apache.log4j.LogManager

object ImdbStatsMod {

  import ru.neoflex.imdbApp.dataset._
  import ru.neoflex.imdbApp.models.config.AppConfig

  val log = LogManager.getLogger(ImdbStatsMod.getClass())

  def run(appConfig: AppConfig)(implicit spark: SparkSession): Unit = {

    log.debug("Running ImdbStatsMod")

    val imdbDataSets: ImdbDataSets =
      ImdbDataSets(
        datasetDir = appConfig.files.datasetDir,
        datasetFileEx = "tsv"
      )(spark = spark)

    import imdbDataSets._

    Stats
      .topNByGenre(
        genre = "Fantasy",
        topN = 114,
        titleBasic = titleBasicsDataset,
        titleRatings = titleRatingsDataset
      )
      .show(500)

    Stats
      .averageRatingsByGenre(
        titleBasic = titleBasicsDataset,
        titleRatings = titleRatingsDataset
      )
      .show(500)

    Stats
      .averageRatingsByActor(
        nameBasics = nameBasicsDataset,
        titlePrincipals = titlePrincipalsDataset,
        titleRatings = titleRatingsDataset,
        titleBasic = titleBasicsDataset
      )
      .show(100)

    Stats
      .ratingCount(titleRatings = titleRatingsDataset)
      .show(500)

    Stats
      .getLivingPersons(nameBasicsDataset)
      .show()

    Stats
      .getSeriesNames(
        titleBasic = titleBasicsDataset,
        titleEpisode = titleEpisodeDataset
      )
      .show(100)

  }

}
