package ru.neoflex.imdbApp

package object models {

  implicit class StrArrayOps(v: String) {

    def clean: Option[String] =
      Option(v.replaceAll("\\\\N", "")).filter(_.nonEmpty)

    def asList: List[String] =
      v.replaceAll("[\\[\\]\\\\N]", "").split(",").toList,

  }

  // title.akas.tsv.gz

  // titleId (string) - a tconst, an alphanumeric unique identifier of the title
  // ordering (integer) – a number to uniquely identify rows for a given titleId
  // title (string) – the localized title
  // region (string) - the region for this version of the title
  // language (string) - the language of the title
  // types (array) - Enumerated set of attributes for this alternative title. One or more of the following: "alternative", "dvd", "festival", "tv", "video", "working", "original", "imdbDisplay". New values may be added in the future without warning
  // attributes (array) - Additional terms to describe this alternative title, not enumerated
  // isOriginalTitle (boolean) – 0: not original title; 1: original title

  case class TitleAkasRow(
    titleId:         String,
    ordering:        Int,
    title:           String,
    region:          String,
    language:        String,
    types:           String,
    attributes:      String,
    isOriginalTitle: Short
  )

  case class TitleAkasItem(
    titleId:         String,
    ordering:        Int,
    title:           String,
    region:          Option[String],
    language:        Option[String],
    types:           List[String],
    attributes:      List[String],
    isOriginalTitle: Boolean
  )

//   name.basics.tsv.gz

//     nconst (string) - alphanumeric unique identifier of the name/person
//     primaryName (string)– name by which the person is most often credited
//     birthYear – in YYYY format
//     deathYear – in YYYY format if applicable, else '\N'
//     primaryProfession (array of strings)– the top-3 professions of the person
//     knownForTitles (array of tconsts) – titles the person is known for
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

}
