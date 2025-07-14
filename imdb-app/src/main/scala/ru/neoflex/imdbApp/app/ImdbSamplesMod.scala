package ru.neoflex.imdbApp

import org.apache.spark.SparkConf
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import ru.neoflex.imdbApp.models.config.AppConfig
import org.apache.spark.sql.Encoder

/** создание тестовых данных
  */
object ImdbSamplesMod {

  import ru.neoflex.imdbApp.dataset._
  import ru.neoflex.imdbApp.models._

  val SEED = 123L

  def run(appConfig: AppConfig): Unit = {

    val samplesDir = s"${appConfig.files.datasetDir}/samples"
    val sparkConf =
      new SparkConf().setAppName("ImdbDatasetSamples")

    implicit val spark: SparkSession =
      SparkSession.builder.config(sparkConf).getOrCreate()

    val imdbDataSets: ImdbDataSets =
      ImdbDataSets(
        appConfig.files.datasetDir,
        datasetFileEx = "tsv",
        spark = spark
      )

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

    // write(titleRatingsSample, samplesDir, "title.rating.sample")(identity)

    //я хочу фильмы только с рейтингом
    val titlesBasicWithRating: Dataset[TitleBasicsItem] =
      imdbDataSets.titleBasicsDataset
        .filter(!_.titleType.contains("tvEpisode")) //и не сериалы
        .join(titleRatingsSample, "tconst")
        .drop("averageRating", "numVotes", "rowNum")
        .as[TitleBasicsItem]
        .cache()

    titlesBasicWithRating.show(20)

    // write(titlesBasicWithRating, samplesDir, "title.basic.sample") {
    //   item: TitleBasicsItem =>
    //     TitleBasicsRow(
    //       tconst = item.tconst,
    //       titleType = item.titleType,
    //       primaryTitle = item.primaryTitle,
    //       originalTitle = item.originalTitle,
    //       isAdult = item.isAdult.map {
    //         case true => 1
    //         case _    => 0
    //       },
    //       startYear = item.startYear,
    //       endYear = item.endYear,
    //       runtimeMinutes = item.runtimeMinutes,
    //       genres = item.genres.mkString(",")
    //     )
    // }

    val titlePrincipalsSamples =
      titlesBasicWithRating
        .join(
          imdbDataSets.titlePrincipalsDataset,
          "tconst",
          "inner"
        )
        .as[TitlePrincipalsItem]
        .cache()

    write(titlePrincipalsSamples, samplesDir, "title.principals")(identity)

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
