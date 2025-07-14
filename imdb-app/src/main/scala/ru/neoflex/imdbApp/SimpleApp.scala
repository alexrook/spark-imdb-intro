package ru.neoflex.imdbApp

import org.apache.spark.sql.Dataset

object SimpleApp {

  import org.apache.spark.sql.SparkSession

  import org.apache.spark.sql.Encoder

  case class NameBasicRow(
    nconst:            String,
    primaryName:       String,
    birthYear:         Option[Int],
    deathYear:         Option[Int],
    primaryProfession: String,
    titles:            String
  )

  case class NameBasicItem(
    nconst:            String,
    primaryName:       String,
    birthYear:         Option[Int],
    deathYear:         Option[Int],
    primaryProfession: List[String],
    titles:            List[String]
  )

  def main(args: Array[String]): Unit = {

    val nameBasicRowTSV: String = "file:///var/tmp/data/datasets/imdb/name.basics.tsv"

    val spark: SparkSession =
      SparkSession.builder.appName("Simple Application").getOrCreate()

    import spark.implicits._

    val schema = implicitly[Encoder[NameBasicRow]].schema

    println(schema)

    val nameBasicsDataset: Dataset[NameBasicItem] =
      spark.read
        .format("csv")
        .option("header", "true")
        .option("delimiter", "\t")
        .schema(schema = schema)
        .load(nameBasicRowTSV)
        .as[NameBasicRow]
        .map { case NameBasicRow(nconst, primaryName, birthYear, deathYear, primaryProfession, titles) =>
          NameBasicItem(
            nconst = nconst,
            primaryName = primaryName,
            birthYear = birthYear,
            deathYear = deathYear,
            primaryProfession = primaryProfession.replaceAll("[\\[\\]]", "").split(",").toList,
            titles = titles.replaceAll("[\\[\\]]", "").split(",").toList
          )

        }

    nameBasicsDataset.show()

    spark.stop()
  }

}
