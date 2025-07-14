package ru.neoflex.imdbApp.models

package object conv {

  import ru.neoflex.imdbApp.models._

  val convTitleBasicsItemToTitleBasicsRow: TitleBasicsItem => TitleBasicsRow =
    (item: TitleBasicsItem) =>
      TitleBasicsRow(
        tconst = item.tconst,
        titleType = item.titleType,
        primaryTitle = item.primaryTitle,
        originalTitle = item.originalTitle,
        isAdult = item.isAdult.map {
          case true => 1
          case _    => 0
        },
        startYear = item.startYear,
        endYear = item.endYear,
        runtimeMinutes = item.runtimeMinutes,
        genres = item.genres.mkString(",")
      )

  val convNameBasicItemToNameBasicRow =
    (item: NameBasicItem) =>
      NameBasicRow(
        nconst = item.nconst,
        primaryName = item.primaryName,
        birthYear = item.birthYear,
        deathYear = item.deathYear,
        primaryProfession = item.primaryProfession.mkString(","),
        knownForTitles = item.knownForTitles.mkString(",")
      )

}
