package ru.neoflex.imdbApp

object SimpleApp {

  import org.apache.spark.sql.SparkSession
  import org.apache.spark.sql.Dataset
  import org.apache.spark.sql.Encoder
  import org.apache.spark.sql.functions._

  import ru.neoflex.imdbApp.models._

  def main(args: Array[String]): Unit = {

    val datasetsDir = "file:///var/tmp/data/datasets/imdb"

    val nameBasicRowTSV:    String = s"$datasetsDir/name.basics.tsv"
    val titleAkasTSV:       String = s"$datasetsDir/title.akas.tsv"
    val titleBasicsTSV:     String = s"$datasetsDir/title.basics.tsv"
    val titleCrewTSV:       String = s"$datasetsDir/title.crew.tsv"
    val titleEpisodeTSV:    String = s"$datasetsDir/title.episode.tsv"
    val titlePrincipalsTSV: String = s"$datasetsDir/title.principals.tsv"
    val titleRatingsTSV:    String = s"$datasetsDir/title.ratings.tsv"

    implicit val spark: SparkSession =
      SparkSession.builder.appName("Simple Application").getOrCreate()

    import spark.implicits._

    val nameBasicsDataset: Dataset[NameBasicItem] =
      readIMDBDataset[NameBasicRow, NameBasicItem](nameBasicRowTSV) {
        case NameBasicRow(nconst, primaryName, birthYear, deathYear, primaryProfession, titles) =>
          NameBasicItem(
            nconst = nconst,
            primaryName = primaryName,
            birthYear = birthYear,
            deathYear = deathYear,
            primaryProfession = primaryProfession.asList,
            titles = titles.asList
          )
      }.cache()

    val titleAkasDataset: Dataset[TitleAkasItem] =
      readIMDBDataset[TitleAkasRow, TitleAkasItem](titleAkasTSV) {
        case TitleAkasRow(titleId, ordering, title, region, language, types, attributes, isOriginalTitle) =>
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

    val titleBasicsDataset: Dataset[TitleBasicsItem] =
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

    val titleCrewDataset: Dataset[TitleCrewItem] =
      readIMDBDataset[TitleCrewRow, TitleCrewItem](titleCrewTSV) { case TitleCrewRow(tconst, directors, writers) =>
        TitleCrewItem(
          tconst = tconst,
          directors = directors.asList,
          writers = writers.asList
        )
      }.cache()

    val titleEpisodeDataset: Dataset[TitleEpisodeItem] =
      readIMDBDataset(titleEpisodeTSV)(identity[TitleEpisodeItem]).cache()

    val titlePrincipalsDataset: Dataset[TitlePrincipalsItem] =
      readIMDBDataset(titlePrincipalsTSV) { i: TitlePrincipalsItem =>
        i.copy(
          job = i.job.flatMap(_.clean),
          characters = i.characters.flatMap(_.clean)
        )
      }.cache()

    val titleRatingsDataset: Dataset[TitleRatingItem] =
      readIMDBDataset(titleRatingsTSV)(identity[TitleRatingItem]).cache()

    // nameBasicsDataset.show()
    // titleAkasDataset.show()
    // titleBasicsDataset.show()
    // titleCrewDataset.show()
    // titleEpisodeDataset.show()
    // titlePrincipalsDataset.show()
    titleRatingsDataset.show()

    TiteRank(titleRatingsDataset).randRowNum

    spark.stop()
  }

  def readIMDBDataset[R: Encoder, T: Encoder](fName: String)(f: R => T)(implicit spark: SparkSession): Dataset[T] =
    spark.read
      .format("csv")
      .option("header", "true")
      .option("delimiter", "\t")
      .schema(schema = implicitly[Encoder[R]].schema)
      .load(fName)
      .as[R]
      .map(f)

}
