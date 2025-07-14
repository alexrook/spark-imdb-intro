package ru.neoflex.imdbApp.dataset

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Encoder
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.columnar.compression.Encoder

final case class ImdbDataSets(
  datasetDir:    String,
  datasetFileEx: String,
  spark:         SparkSession
) {
  import ru.neoflex.imdbApp.models._

  import spark.implicits._

  def getDatasetAbsolutePath(datasetFileName: String) =
    s"${datasetDir}/$datasetFileName.$datasetFileEx"

  protected[dataset] val nameBasicRowTSV: String =
    getDatasetAbsolutePath("name.basics")

  protected[dataset] val titleAkasTSV: String =
    getDatasetAbsolutePath("title.akas")

  protected[dataset] val titleBasicsTSV: String =
    getDatasetAbsolutePath("title.basics")

  protected[dataset] val titleCrewTSV: String =
    getDatasetAbsolutePath("title.crew")

  protected[dataset] val titleEpisodeTSV: String =
    getDatasetAbsolutePath("title.episode")

  protected[dataset] val titlePrincipalsTSV: String =
    getDatasetAbsolutePath("title.principals")

  protected[dataset] val titleRatingsTSV: String =
    getDatasetAbsolutePath("title.ratings")

  lazy val nameBasicsDataset: Dataset[NameBasicItem] =
    readIMDBDataset[NameBasicRow, NameBasicItem](nameBasicRowTSV) {
      case NameBasicRow(
            nconst,
            primaryName,
            birthYear,
            deathYear,
            primaryProfession,
            knownForTitles
          ) =>
        NameBasicItem(
          nconst = nconst,
          primaryName = primaryName,
          birthYear = birthYear,
          deathYear = deathYear,
          primaryProfession = primaryProfession.asList,
          knownForTitles = knownForTitles.asList
        )
    }.cache()

  lazy val titleAkasDataset: Dataset[TitleAkasItem] =
    readIMDBDataset[TitleAkasRow, TitleAkasItem](titleAkasTSV) {
      case TitleAkasRow(
            titleId,
            ordering,
            title,
            region,
            language,
            types,
            attributes,
            isOriginalTitle
          ) =>
        TitleAkasItem(
          titleId = titleId,
          ordering = ordering,
          title = title,
          region = region.clean,
          language = language.clean,
          types = types.asList,
          attributes = attributes.asList,
          isOriginalTitle = isOriginalTitle.map(_ > 0)
        )
    }.cache()

  lazy val titleBasicsDataset: Dataset[TitleBasicsItem] =
    readIMDBDataset[TitleBasicsRow, TitleBasicsItem](titleBasicsTSV) {
      case TitleBasicsRow(
            tconst,
            titleType,
            primaryTitle,
            originalTitle,
            isAdult,
            startYear,
            endYear,
            runtimeMinutes,
            genres
          ) =>
        TitleBasicsItem(
          tconst = tconst,
          titleType = titleType,
          primaryTitle = primaryTitle,
          originalTitle = originalTitle,
          isAdult = isAdult.map(_ > 0),
          startYear = startYear,
          endYear = endYear,
          runtimeMinutes = runtimeMinutes,
          genres = genres.asList
        )
    }.cache()

  lazy val titleCrewDataset: Dataset[TitleCrewItem] =
    readIMDBDataset[TitleCrewRow, TitleCrewItem](titleCrewTSV) {
      case TitleCrewRow(tconst, directors, writers) =>
        TitleCrewItem(
          tconst = tconst,
          directors = directors.asList,
          writers = writers.asList
        )
    }.cache()

  lazy val titleEpisodeDataset: Dataset[TitleEpisodeItem] =
    readIMDBDataset(titleEpisodeTSV)(identity[TitleEpisodeItem]).cache()

  lazy val titlePrincipalsDataset: Dataset[TitlePrincipalsItem] =
    readIMDBDataset(titlePrincipalsTSV) { i: TitlePrincipalsItem =>
      i.copy(
        job = i.job.flatMap(_.clean),
        characters = i.characters.flatMap(_.clean)
      )
    }.cache()

  lazy val titleRatingsDataset: Dataset[TitleRatingItem] =
    readIMDBDataset(titleRatingsTSV)(identity[TitleRatingItem]).cache()

  def readIMDBDataset[R: Encoder, T: Encoder](
    fName: String
  )(f:     R => T): Dataset[T] =
    spark.read
      .format("csv")
      .option("header", "true")
      .option("delimiter", "\t")
      .schema(schema = implicitly[Encoder[R]].schema)
      .load(fName)
      .as[R]
      .map(f)

}
