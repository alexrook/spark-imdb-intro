package ru.neoflex.imdbApp

import org.apache.spark.sql.Dataset

object SimpleApp {

  import org.apache.spark.sql.SparkSession

  import org.apache.spark.sql.Encoder

  import ru.neoflex.imdbApp.models._

  def main(args: Array[String]): Unit = {

    val datasetsDir = "file:///var/tmp/data/datasets/imdb"

    val nameBasicRowTSV: String = s"$datasetsDir/name.basics.tsv"
    val titleAkasTSV:    String = s"$datasetsDir/title.akas.tsv"

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
      }

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
            isOriginalTitle = isOriginalTitle > 0
          )
      }

    nameBasicsDataset.show()

    titleAkasDataset.show()

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
