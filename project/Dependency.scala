import sbt._
import sbt.librarymanagement.DependencyBuilders

object Dependency {

  lazy val scalaTestLibs = {
    lazy val scalatestVersion = "3.2.18"
    Seq(
      "org.scalatest" %% "scalatest" % scalatestVersion % Test
    )
  }

  lazy val loggingLibs = {
    val jacksonV = "2.15.2"
    val log4jV   = "2.20.0"

    Seq(
      "org.apache.logging.log4j"         % "log4j-api"               % log4jV,
      "org.apache.logging.log4j"         % "log4j-core"              % log4jV
    )
  }

  lazy val pureConfigLibs = {
    val pureConfigV = "0.17.6"
    Seq(
      "com.github.pureconfig" %% "pureconfig" % "0.17.6"
    )
  }

  object SparkLibs {
    val sparkV = "3.5.1"

    lazy val sparkCoreDeps: Seq[ModuleID] =
      Seq(
        "org.apache.spark" %% "spark-core" % sparkV,
        "org.apache.spark" %% "spark-sql"  % sparkV
      )
  }

}
