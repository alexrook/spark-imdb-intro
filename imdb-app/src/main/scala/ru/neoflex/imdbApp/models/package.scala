package ru.neoflex.imdbApp

package object models {

  implicit class StrArrayOps(v: String) {

    def clean: Option[String] =
      Option(v).map(_.replaceAll("\\\\N", "")).filter(_.nonEmpty)

    def asList: List[String] =
      Option(v)
        .map(_.replaceAll("[\\[\\]\\\\N]", "").split(",").toList)
        .getOrElse(List.empty[String])

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
    isOriginalTitle: Option[Short]
  )

  case class TitleAkasItem(
    titleId:         String,
    ordering:        Int,
    title:           String,
    region:          Option[String],
    language:        Option[String],
    types:           List[String],
    attributes:      List[String],
    isOriginalTitle: Option[Boolean]
  )

  // title.basics.tsv.gz

  //   tconst (string) - alphanumeric unique identifier of the title
  //   titleType (string) – the type/format of the title (e.g. movie, short, tvseries, tvepisode, video, etc)
  //   primaryTitle (string) – the more popular title / the title used by the filmmakers on promotional materials at the point of release
  //   originalTitle (string) - original title, in the original language
  //   isAdult (boolean) - 0: non-adult title; 1: adult title
  //   startYear (YYYY) – represents the release year of a title. In the case of TV Series, it is the series start year
  //   endYear (YYYY) – TV Series end year. ‘\N’ for all other title types
  //   runtimeMinutes – primary runtime of the title, in minutes
  //   genres (string array) – includes up to three genres associated with the title

  case class TitleBasicsRow(
    tconst:         String,
    titleType:      String,
    primaryTitle:   String,
    originalTitle:  String,
    isAdult:        Option[Short],
    startYear:      Option[Short],
    endYear:        Option[Short],
    runtimeMinutes: Option[Short],
    genres:         String
  )

  case class TitleBasicsItem(
    tconst:         String,
    titleType:      String,
    primaryTitle:   String,
    originalTitle:  String,
    isAdult:        Option[Boolean],
    startYear:      Option[Short],
    endYear:        Option[Short],
    runtimeMinutes: Option[Short],
    genres:         List[String]
  )

  // title.crew.tsv.gz

  //   tconst (string) - alphanumeric unique identifier of the title
  //   directors (array of nconsts) - director(s) of the given title
  //   writers (array of nconsts) – writer(s) of the given title

  case class TitleCrewRow(
    tconst:    String,
    directors: String,
    writers:   String
  )

  case class TitleCrewItem(
    tconst:    String,
    directors: List[String],
    writers:   List[String]
  )

  // title.episode.tsv.gz

  //   tconst (string) - alphanumeric identifier of episode
  //   parentTconst (string) - alphanumeric identifier of the parent TV Series
  //   seasonNumber (integer) – season number the episode belongs to
  //   episodeNumber (integer) – episode number of the tconst in the TV series
  case class TitleEpisodeItem(
    tconst:        String,
    parentTconst:  String,
    seasonNumber:  Option[Short],
    episodeNumber: Option[Short]
  )

// title.principals.tsv.gz

//     tconst (string) - alphanumeric unique identifier of the title
//     ordering (integer) – a number to uniquely identify rows for a given titleId
//     nconst (string) - alphanumeric unique identifier of the name/person
//     category (string) - the category of job that person was in
//     job (string) - the specific job title if applicable, else '\N'
//     characters (string) - the name of the character played if applicable, else '\N'
  case class TitlePrincipalsItem(
    tconst:     String,
    ordering:   Int,
    nconst:     String,
    category:   String,
    job:        Option[String],
    characters: Option[String]
  )

// title.ratings.tsv.gz

//     tconst (string) - alphanumeric unique identifier of the title
//     averageRating – weighted average of all the individual user ratings
//     numVotes - number of votes the title has received
  case class TitleRatingItem(
    tconst:        String,
    averageRating: Float,
    numVotes:      Int
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
    birthYear:         Option[Short],
    deathYear:         Option[Short],
    primaryProfession: String,
    knownForTitles:    String
  )

  case class NameBasicItem(
    nconst:            String,
    primaryName:       String,
    birthYear:         Option[Short],
    deathYear:         Option[Short],
    primaryProfession: List[String],
    knownForTitles:    List[String]
  )

}
