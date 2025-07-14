package ru.neoflex.imdbApp

import com.typesafe.config._
import org.apache.logging.log4j.LogManager
import org.apache.spark.SparkConf
import pureconfig._
import pureconfig.error.ConfigReaderFailures
import pureconfig.error.ThrowableFailure
import pureconfig.generic.auto._

import scala.util.Properties
import ru.neoflex.imdbApp.session.SparkSessionCreator
import ru.neoflex.imdbApp.app.ingest.PartitionExample

object Boot extends SparkSessionCreator {
  import ru.neoflex.imdbApp.models.config._
  import ru.neoflex.imdbApp.buildinfo.BuildInfo
  import ru.neoflex.imdbApp.cli._
  import ru.neoflex.imdbApp.app.stats.ImdbStatsMod
  import ru.neoflex.imdbApp.app.samples.ImdbSamplesMod
  import ru.neoflex.imdbApp.app.udf._

  val log = LogManager.getLogger(Boot.getClass())

  implicit val appModuleConfigReader
    : ConfigReader[AppModulesEnum.AppModulesType] =
    ConfigReader.fromString[AppModulesEnum.AppModulesType](
      ConvertHelpers.catchReadError(str => AppModulesEnum.withName(s = str))
    )

  implicit val appModuleConfigWriter
    : ConfigWriter[AppModulesEnum.AppModulesType] =
    ConfigWriter[String].contramap[AppModulesEnum.AppModulesType](_.toString())

  def main(args: Array[String]): Unit = {
    logBootHeader(args)

    (for {

      rawCfg <- ConfigSource.default.config()
      _ = logConfig(rawCfg, "The loaded config")

      cfgFromLoader <- ConfigSource.fromConfig(rawCfg).at("app").load[AppConfig]

      mergedWithCommandLineAppCfg <-
        loadAppConfig(args = args, init = cfgFromLoader).left.map { ex =>
          ConfigReaderFailures(ThrowableFailure(ex, None))
        }

      _ = logConfig(
        ConfigWriter[AppConfig].to(mergedWithCommandLineAppCfg).atPath("app"),
        "The harvested app config"
      )

    } yield {

      mergedWithCommandLineAppCfg.runModule match {

        case AppModulesEnum.Main =>
          val sparkConf: SparkConf =
            withKryo(
              withAppName(
                new SparkConf,
                mergedWithCommandLineAppCfg.name
              )
            )

          ImdbStatsMod.run(mergedWithCommandLineAppCfg)(
            buildSparkSession(sparkConf)
          )

        case AppModulesEnum.Samples =>
          val sparkConf: SparkConf =
            withPerExecutorMemory(
              withKryo(
                withAppName(
                  new SparkConf(),
                  "Imdb dataset samples generation"
                )
              ),
              "2G"
            )

          ImdbSamplesMod.run(mergedWithCommandLineAppCfg)(
            buildSparkSession(sparkConf)
          )

        case AppModulesEnum.UDAF =>
          val sparkConf: SparkConf =
            withKryo(
              withAppName(
                new SparkConf,
                "UDAF Kryo Serialization Example"
              )
            )

          UdafExample.run(buildSparkSession(sparkConf))

        case AppModulesEnum.UDAFTyped =>
          val sparkConf: SparkConf =
            withKryo(
              withAppName(
                new SparkConf,
                "UDAF Kryo Serialization Example"
              )
            )

          UdafTypedExample.run(buildSparkSession(sparkConf))

        case AppModulesEnum.PartitionExample =>
          val sparkConf: SparkConf =
            withAppName(
              new SparkConf,
              "Partition counting example"
            )

          PartitionExample.run(mergedWithCommandLineAppCfg)(
            buildSparkSession(sparkConf)
          )

      }

      log.debug(
        "{} application completed",
        mergedWithCommandLineAppCfg.runModule
      )

    }).left
      .foreach { ex =>
        log.error(
          "An error[{}] occurred while starting application, exit",
          ex.prettyPrint()
        )
        System.exit(-1)
      }

  }

  def logBootHeader(args: Array[String]): Unit = {
    log.info(s"starting. {}", BuildInfo)
    log.debug(s"The application is launched with args[${args.mkString(" ")}]")
    log.debug(
      s"environment: javaVersion - ${Properties.javaVersion}, javaVendor - ${Properties.javaVendor}, " +
        s"javaVmInfo = ${Properties.javaVmInfo}, javaVmVendor - ${Properties.javaVmVendor}, " +
        s"javaVmVersion - ${Properties.javaVmVersion}"
    )
    for ((key, value) <- sys.env) {
      log.debug(s"env[$key = $value]")
    }

  }

  def logConfig(config: Config, info: String): Unit = {
    val renderOptions: ConfigRenderOptions =
      ConfigRenderOptions
        .defaults()
        .setOriginComments(false)
        .setComments(false)
        .setFormatted(true)

    log.debug(
      s"$info[{}]",
      config.root().render(renderOptions)
    )
  }

}
