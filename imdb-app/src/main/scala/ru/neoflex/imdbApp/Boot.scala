package ru.neoflex.imdbApp

import pureconfig._
import pureconfig.generic.auto._
import org.apache.logging.log4j.LogManager

import scala.util.Properties
import com.typesafe.config._

object Boot {
  import ru.neoflex.imdbApp.models.config._
  import ru.neoflex.imdbApp.buildinfo.BuildInfo
  import ru.neoflex.imdbApp.app.ImdbStatsApp

  val log = LogManager.getLogger(Boot.getClass())

  def main(args: Array[String]): Unit = {
    logBootHeader(args)
    (for {

      rawCfg <- ConfigSource.default.config()
      _ = logConfig(rawCfg)

      appCfg <- ConfigSource.fromConfig(rawCfg).at("app").load[AppConfig]

    } yield {

      ImdbStatsApp.run(appCfg)
      log.debug("imdb statistics application completed")

    }).left
      .foreach { ex =>
        log.error(
          "An error[{}] occurred while reading application config",
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

  def logConfig(config: Config): Unit = {
    val renderOptions = ConfigRenderOptions
      .defaults()
      .setOriginComments(false)
      .setComments(false)
      .setFormatted(true)
    log.debug(
      "The following application configuration will be used[{}]",
      config.root().render(renderOptions)
    )
  }
}
