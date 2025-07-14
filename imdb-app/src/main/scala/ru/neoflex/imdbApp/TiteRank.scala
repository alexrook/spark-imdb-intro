package ru.neoflex.imdbApp

import org.apache.spark.sql.Dataset
import ru.neoflex.imdbApp.models.TitleRatingItem

final case class TiteRank(
  titleRatingsDataset: Dataset[TitleRatingItem]
) {
  import org.apache.spark.sql.expressions.Window
  import org.apache.spark.sql.functions._

  val window = Window.orderBy(col("averageRating").desc)

  def randRowNum =
    titleRatingsDataset
      .withColumn("rank", rank().over(window))
      .withColumn("rowNumber", row_number().over(window))
      .where(col("rowNumber") <= 3)
      .show(100)

}
