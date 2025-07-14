package ru.neoflex.imdbApp.app

import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window
import java.time.LocalDate

object Stats {
  import ru.neoflex.imdbApp.models._

  val RATING_BUCKED_COUNT: Int = 5

  /** топ N по жанрам
    *
    * @param genre
    * @param topN
    * @param titleRatings
    * @param titleBasic
    * @return
    */
  def topNByGenre(
    genre:        String,
    topN:         Int,
    titleBasic:   Dataset[TitleBasicsItem],
    titleRatings: Dataset[TitleRatingItem]
  ) = {
    //TODO: WARN WindowExec: No Partition Defined for Window operation! Moving all data to a single partition, this can cause serious performance degradation.
    val window = Window.orderBy(col("averageRating").desc)

    titleBasic
      .filter(_.genres.contains(genre))
      .join(
        titleRatings,
        titleBasic("tconst") === titleRatings("tconst"),
        "inner"
      )
      .withColumn("rank", rank().over(window))
      .withColumn("rowNumber", row_number().over(window))
      .select(
        titleBasic("tconst"),
        col("primaryTitle"),
        col("genres"),
        col("averageRating"),
        col("rank"),
        col("startYear"),
        col("rowNumber")
      )
      .where(col("rowNumber") <= topN)
      .drop("rowNumber")
  }

  /** средний рейтинг для всех жанров
    *
    * @param titleRatings
    * @param titleBasic
    * @return
    */
  def averageRatingsByGenre(
    titleRatings:   Dataset[TitleRatingItem],
    titleBasic:     Dataset[TitleBasicsItem]
  )(implicit spark: SparkSession) = {
    import spark.implicits._
    titleBasic
      .flatMap { //TODO: есть ли более эффективный метод чем flatMap?
        item: TitleBasicsItem =>
          item.genres.map { genre =>
            (
              item.tconst,
              genre
            )
          }
      }
      .toDF("basic_tconst", "genre")
      .join(
        titleRatings,
        col("basic_tconst") === col("tconst"),
        "inner"
      )
      .groupBy(col("genre"))
      .agg(avg("averageRating").as("AvgRatingByGenre"))
  }

  /*
   * среднее значение рейтинга для актера
   */
  def averageRatingsByActor(
    nameBasics:      Dataset[NameBasicItem],
    titlePrincipals: Dataset[TitlePrincipalsItem],
    titleRatings:    Dataset[TitleRatingItem],
    titleBasic:      Dataset[TitleBasicsItem]
  ) =
    titlePrincipals
      .filter { item =>
        item.category.contains("actor") || item.category.contains("self")
      }
      .join(
        titleBasic,
        "tconst",
        "inner"
      )
      .join(titleRatings, "tconst", "inner")
      .select(col("nconst"), col("averageRating"))
      .groupBy("nconst")
      .agg(avg("averageRating").as("AvgRating"))
      .join(nameBasics, "nconst", "inner")
      .select(col("nconst"), col("primaryName"), col("AvgRating"))

  /*
   * число фильмов для группы близких рейтингов
   */

  def ratingCount(
    titleRatings: Dataset[TitleRatingItem]
  ) = {
    val window = Window
      .orderBy(col("averageRating").desc)
    //Функция NTILE используется для распределения строк
    //из результирующего набора данных
    //в указанное количество равномерных групп или корзин.
    titleRatings
      .withColumn(
        "bucket",
        ntile(RATING_BUCKED_COUNT).over(window)
      )
      .repartition(col("bucket"))
      .groupBy("bucket")
      .agg(
        count("tconst").as("TitlesCount"),
        avg("averageRating").as("AvgRating")
      )

  }

  val howLongIsLiving: (Option[Short], Option[Short]) => Option[Short] =
    (birthYear: Option[Short], deathYear: Option[Short]) =>
      for {
        nowYear <-
          //option inversion
          deathYear.fold(Option(LocalDate.now().getYear().toShort))(_ =>
            Option.empty[Short]
          )

        bYear <- birthYear

      } yield (nowYear - bYear).toShort

  def getLivingPersons(
    nameBasicsDataset: Dataset[NameBasicItem]
  )(implicit spark:    SparkSession) = {
    spark.udf.register("how_long_is_living", functions.udf(howLongIsLiving))

    nameBasicsDataset
      .withColumn(
        "im_alive",
        expr("how_long_is_living(birthYear,deathYear)")
      )
      .filter(expr("im_alive is not null"))

  }

  def getSeriesNames(
    titleBasic:     Dataset[TitleBasicsItem],
    titleEpisode:   Dataset[TitleEpisodeItem]
  )(implicit spark: SparkSession) = {
    spark.udf.register("SeriesAgg", functions.udaf(udf.SeriesAgg))

    val titlesWithNames: DataFrame =
      titleBasic.select("tconst", "primaryTitle")

    titleEpisode
      .select(
        col("tconst").alias("EpisodeTconst"),
        col("parentTconst").alias("EpisodeParentTconst")
      )
      .join(
        titlesWithNames.alias("tNamesA"),
        col("EpisodeTconst") === col("tNamesA.tconst")
      )
      .join(
        titlesWithNames.alias("tNamesB"),
        col("EpisodeParentTconst") === col("tNamesB.tconst")
      )
      .groupBy(
        col("EpisodeParentTconst"),
        col("tNamesB.primaryTitle").as("SeriesName")
      )
      .agg(expr("SeriesAgg(tNamesA.primaryTitle)").as("EpisodeNames"))
      .select(
        col("EpisodeParentTconst"),
        col("EpisodeNames"),
        col("SeriesName")
      )

  }

}
