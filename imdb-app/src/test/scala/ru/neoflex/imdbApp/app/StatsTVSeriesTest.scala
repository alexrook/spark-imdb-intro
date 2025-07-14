package ru.neoflex.imdbApp.app.stats

import com.holdenkarau.spark.testing.DatasetSuiteBase
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import ru.neoflex.imdbApp.dataset.SamplesDatasets

class StatsTVSeriesTest
    extends AnyWordSpec
    with DatasetSuiteBase
    with SamplesDatasets {

  override implicit def reuseContextIfPossible: Boolean = true

  "Stats" when {

    "run" should {

      "return getSeriesNames correctly" in {
        val actual =
          Stats.getSeriesNames(
            titleBasic = imdbDataSetSeriesSamples.titleBasicsDataset,
            titleEpisode = imdbDataSetSeriesSamples.titleEpisodeDataset
          )(spark)

        actual.show(40)

        actual.collect().foreach { case (_, seriesNames, _) =>
          assert(seriesNames.nonEmpty)
        }

      }

    }
  }
}
