package ru.neoflex.imdbApp.app

import org.scalatest.wordspec.AnyWordSpec
import com.holdenkarau.spark.testing.SharedSparkContext

class StatsTest extends AnyWordSpec with SharedSparkContext {

  override implicit def reuseContextIfPossible: Boolean = true

  "Stats" when {
    "smoke" should {
      "run" in {
        val list = List(1, 2, 3, 4)
        val rdd  = sc.parallelize(list)

        assert(rdd.count === list.length)
      }
    }
  }

}
