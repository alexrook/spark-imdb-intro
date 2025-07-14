package ru.neoflex.imdbApp.app

import com.holdenkarau.spark.testing.DatasetSuiteBase
import org.scalatest.wordspec.AnyWordSpec

class StatsTest extends AnyWordSpec with DatasetSuiteBase {

  override implicit def reuseContextIfPossible: Boolean = true

  "Stats" when {
    "smoke" should {
      "run" in {
        val sqlCtx = sqlContext
        import sqlCtx.implicits._
        val input1 = sc.parallelize(List(1, 2, 3)).toDS
        assertDatasetEquals(input1, input1) // equal

        val input2 = sc.parallelize(List(4, 5, 6)).toDS
        intercept[org.scalatest.exceptions.TestFailedException] {
          assertDatasetEquals(input1, input2) // not equal
        }
      }
    }

  }
}
