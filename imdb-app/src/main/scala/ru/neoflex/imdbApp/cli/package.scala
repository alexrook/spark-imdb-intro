package ru.neoflex.imdbApp

import scopt.OParser

package object cli {
  import ru.neoflex.imdbApp.models.config._

  val builder = OParser.builder[AppConfig]

  val commandLineParser: OParser[Unit, AppConfig] = {
    import builder._
    OParser.sequence(
      programName("imdb-stats-app"),
      head("imdb-stats-app", "0.0.1"),
      opt[String]('m', "mod")
        .action((module, appConfig) =>
          appConfig.copy(runModule = AppModulesEnum.withName(module))
        )
        .optional()
        .text(
          s"The app module to run, one of[${AppModulesEnum.values.mkString(",")}]"
        ),
      opt[String]('j', "job-name")
        .action((jobName, appConfig) => appConfig.copy(name = jobName))
        .optional()
        .text(
          s"The app job name"
        )
    )
  }

  def loadAppConfig(
    args: Array[String],
    init: AppConfig
  ): Either[Exception, AppConfig] =
    OParser.parse(commandLineParser, args, init).toRight {
      new Exception("An error occured while parsing command line")
    }

}
