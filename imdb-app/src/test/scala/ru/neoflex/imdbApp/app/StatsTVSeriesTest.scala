package ru.neoflex.imdbApp.app

import com.holdenkarau.spark.testing.DatasetSuiteBase
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import ru.neoflex.imdbApp.dataset.SamplesDatasets

class StatsTVSeriesTest
    extends AnyWordSpec
    with DatasetSuiteBase
    with SamplesDatasets {

  override implicit def reuseContextIfPossible: Boolean = true

  "Stats" when {
    import spark.implicits._

    "run" should {

      "return getSeriesNames correctly" in {
        val actual: DataFrame =
          Stats.getSeriesNames(
            titleBasic = imdbDataSetSeriesSamples.titleBasicsDataset,
            titleEpisode = imdbDataSetSeriesSamples.titleEpisodeDataset
          )(spark)

        actual.show(1000)

      }

    }
  }
}
