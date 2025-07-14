package ru.neoflex.imdbApp.app

import com.holdenkarau.spark.testing.DatasetSuiteBase
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import ru.neoflex.imdbApp.dataset.SamplesDatasets
import org.apache.spark.sql.{ Dataset, Row }
import org.apache.spark.sql.SparkSession
import spire.std.array

class StatsTest extends AnyWordSpec with DatasetSuiteBase with SamplesDatasets {

  override implicit def reuseContextIfPossible: Boolean = true

  "Stats" when {
    import spark.implicits._

    "run" should {

      // "load test datasets from resources" in {
      //   imdbDataSetSamples.titleBasicsDataset.show()
      //   imdbDataSetSamples.titleRatingsDataset.show()
      // }

      "return topNByGenre correctly" in {
        val actual =
          Stats
            .topNByGenre(
              genre = "Comedy",
              topN = 20,
              titleBasic = imdbDataSetSamples.titleBasicsDataset,
              titleRatings = imdbDataSetSamples.titleRatingsDataset
            )
            .cache()

        actual.show(100)
        println(s"topNByGenre fields ${actual.schema.fieldNames.mkString(",")}")

        //подсчитано вручную
        //cat title.basics.sample.tsv |awk -F"\t" '{print $9}'|grep Comedy|wc -l
        assert(actual.count() == 5)

        val ratings: Array[Float] =
          actual.map(_.getAs[Float]("averageRating")).as[Float].collect()

        val sortedByHand: Array[Float] = ratings.sorted.reverse

        ratings should contain theSameElementsInOrderAs sortedByHand

      }

      "return averageRatingsByGenre correctly" in {

        val actual: Dataset[Row] =
          Stats
            .averageRatingsByGenre(
              titleRatings = imdbDataSetSamples.titleRatingsDataset,
              titleBasic = imdbDataSetSamples.titleBasicsDataset
            )(spark)
            .cache()

        actual.show(100)

        val genres = actual.map(_.getAs[String]("genre")).as[String].collect()
        //жанры не дублируются
        assert(genres.toSet.size == genres.length)
        //TODO: подсчитать суммарные рейтинги вручную ?

      }

      "return ratingCount correctly" in {

        val actual: Dataset[Row] =
          Stats
            .ratingCount(titleRatings = imdbDataSetSamples.titleRatingsDataset)
            .cache()

        actual.show(100)

        val expected: Array[(Int, Long, Double)] =
          imdbDataSetSamples.titleRatingsDataset
            .collect()
            .sortBy(_.averageRating)
            .reverse
            .grouped(4) //Stats.RATING_BUCKED_COUNT - 1 == 20/5(bucket size)
            .map { xa =>
              val sum   = xa.map(_.averageRating.toDouble).sum
              val count = xa.length
              (count.toLong, sum / count)
            }
            .zipWithIndex
            .map { case (((cnt: Long, avg: Double), idx: Int)) =>
              (idx + 1, cnt, avg)
            }
            .toArray

        //  expected.foreach(println)
        //actual.schema.fields.foreach(println)

        val actualCollected: Array[(Int, Long, Double)] =
          actual
            .map { item =>
              (
                item.getAs[Int]("bucket"),
                item.getAs[Long]("TitlesCount"),
                item.getAs[Double]("AvgRating")
              )
            }
            .collect()

        actualCollected should contain theSameElementsAs expected

      }

    }

  }
}
