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
    val slf4jV = "2.0.13"
    Seq(
      "org.slf4j" % "slf4j-api" % slf4jV,
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
	"org.apache.spark" %% "spark-sql" % sparkV
      )
  }

}
