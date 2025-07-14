package ru.neoflex.imdbApp.app.ingest

import org.apache.logging.log4j.LogManager
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import ru.neoflex.imdbApp.dataset.ImdbDataSets
import ru.neoflex.imdbApp.models.TitlePrincipalsItem

object PartitionExample {

  import ru.neoflex.imdbApp.models.config.AppConfig

  val log = LogManager.getLogger(PartitionExample.getClass())

  def run(appConfig: AppConfig)(spark: SparkSession): Unit = {

    val imdbDataSets: ImdbDataSets = //наборы данных от IMDB
      ImdbDataSets(
        appConfig.files.datasetDir,
        datasetFileEx = "tsv"
      )(spark = spark)

    import spark.implicits._

    val df: Dataset[TitlePrincipalsItem] =
      imdbDataSets.titlePrincipalsDataset //the one that is larger

    log.debug("Initial number of partitions[{}]", df.rdd.getNumPartitions)

// Применение трансформаций
    val groupedDF: Dataset[(String, Long)] = df.groupByKey(_.tconst).count()

    val keysCount: Long = groupedDF.count()

    groupedDF.show()

// Проверка количества партиций после группировки
    log.debug(
      "Number of partitions after groupBy[{}] with keys count[{}]",
      groupedDF.rdd.getNumPartitions,
      keysCount
    )

// Изменение количества партиций
    val repartitionedDF = groupedDF.repartition(10)

// Проверка количества партиций после репартиционирования
    log.debug(
      "Number of partitions after repartition[{}]",
      repartitionedDF.rdd.getNumPartitions
    )

  }
}
