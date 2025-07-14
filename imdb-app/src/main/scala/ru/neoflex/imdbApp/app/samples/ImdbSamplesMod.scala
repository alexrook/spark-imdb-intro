package ru.neoflex.imdbApp.app.samples

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Encoder
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import ru.neoflex.imdbApp.models.config.AppConfig
import org.apache.log4j.LogManager

/** создание тестовых данных из IMDB Dataset's
  */
object ImdbSamplesMod {

  import ru.neoflex.imdbApp.dataset._
  import ru.neoflex.imdbApp.models._
  import ru.neoflex.imdbApp.models.conv._

  val SEED = 123L

  val log = LogManager.getLogger(ImdbSamplesMod.getClass())

  def run(appConfig: AppConfig)(spark: SparkSession): Unit = {

    log.debug("Running ImdbSamplesMod")

    val samplesDir = s"${appConfig.files.datasetDir}/samples"

    val imdbDataSets: ImdbDataSets = //наборы данных от IMDB
      ImdbDataSets(
        appConfig.files.datasetDir,
        datasetFileEx = "tsv"
      )(spark = spark)

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

    titleRatingsSample.show(20)

    //  write(titleRatingsSample, samplesDir, "title.rating.sample")(identity)

    //-----------
    //я хочу фильмы только с рейтингом
    val titlesBasicWithRating: Dataset[TitleBasicsItem] =
      imdbDataSets.titleBasicsDataset
        .filter(!_.titleType.contains("tvEpisode")) //и не сериалы
        //Unlike other joins, the left semi join does not include any columns from the right DataFrame in the result set
        .join(broadcast(titleRatingsSample), "tconst", "left_semi")
        .as[TitleBasicsItem]
        .cache()

    titlesBasicWithRating.show(20)

    write(titlesBasicWithRating, samplesDir, "title.basic.sample")(
      convTitleBasicsItemToTitleBasicsRow
    )

    //-----------
    val titlePrincipalsSamples: Dataset[TitlePrincipalsItem] =
      imdbDataSets.titlePrincipalsDataset
        .join(
          broadcast(titlesBasicWithRating),
          "tconst",
          "left_semi"
        )
        .withColumn( //берем от каждого фильма >
          "rowNum",
          row_number().over(Window.partitionBy(col("tconst")).orderBy("nconst"))
        )
        .filter(
          col("rowNum") < 3 //только по 2 principals
        )
        .as[TitlePrincipalsItem]
        .cache()

    titlePrincipalsSamples.show(40)
//    write(titlePrincipalsSamples, samplesDir, "title.principals")(identity)

    //-----------
    val nameBasicSamples: Dataset[NameBasicItem] =
      imdbDataSets.nameBasicsDataset
        .join(
          broadcast(titlePrincipalsSamples),
          "nconst",
          "left_semi"
        )
        .as[NameBasicItem]
        .cache()

    nameBasicSamples.show(40)

    // write(nameBasicSamples, samplesDir, "name.basics")(
    //   convNameBasicItemToNameBasicRow
    // )

    //-----------TV Series-----------------------------------

    val titleEpisodeSamples: Dataset[TitleEpisodeItem] =
      imdbDataSets.titleEpisodeDataset
        .sample(
          withReplacement = false,
          fraction = 1e-4,
          SEED
        )
        .withColumn("rowNum", row_number().over(Window.orderBy("tconst")))
        .filter(col("rowNum") < 21)
        .as[TitleEpisodeItem]
        .cache()

    titleEpisodeSamples.show(20)

    // val seriesSamplesDir: String =
    //   s"${samplesDir}/series" //образцы для работы с сериалами отдельно

    // write(titleEpisodeSamples, seriesSamplesDir, "title.episode")(
    //   identity
    // )

    val titleBasicForEpisodeSamples: Dataset[TitleBasicsItem] = {
      val episodesBasic: Dataset[TitleBasicsItem] =
        imdbDataSets.titleBasicsDataset
          .join(
            broadcast(titleEpisodeSamples),
            "tconst",
            "left_semi"
          )
          .as[TitleBasicsItem]

      val parentBasic: Dataset[TitleBasicsItem] =
        imdbDataSets.titleBasicsDataset
          .alias("titleBasic")
          .join(
            broadcast(titleEpisodeSamples.alias("episode")),
            col("titleBasic.tconst") === col("episode.parentTconst"),
            "left_semi"
          )
          .as[TitleBasicsItem]

      episodesBasic.union(parentBasic)

    }

    titleBasicForEpisodeSamples.show(1000)

    // write(titleBasicForEpisodeSamples, seriesSamplesDir, "title.basic")(
    //   convTitleBasicsItemToTitleBasicsRow
    // )

    println("==========Samples generation done==============")

  }

  /** создание файлов для хранения тестовых наборов
    *
    * @param datasetA
    * @param samplesDir
    * @param fileName
    * @param f
    */
  def write[A, B: Encoder](
    datasetA:   Dataset[A],
    samplesDir: String,
    fileName:   String
  )(
    f: A => B
  ): Unit = {

    def write(delimiter: String, fileExt: String): Unit =
      datasetA
        .repartition(1) //сведение в одну partition для удобного чтения tsv
        .map(f)
        .write
        .mode("overwrite")
        .format("csv")
        .option("header", "true")
        .option("delimiter", delimiter)
        .csv(s"${samplesDir}/${fileName}.$fileExt")

    write("\t", "tsv") //запись как исходных файлах
    write(",", "csv") //запись для вставки в код теста
  }

}
