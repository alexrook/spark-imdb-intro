name := "spark-imdb-app"

import Dependency._

libraryDependencies ++= loggingLibs.map(_ % "provided")
libraryDependencies ++= SparkLibs.sparkCoreDeps.map(_ % "provided")
libraryDependencies ++= pureConfigLibs
//Tests
libraryDependencies ++= scalaTestLibs

// Добавляем к проекту настройки BuildInfo
enablePlugins(BuildInfoPlugin)

buildInfoKeys := Seq[BuildInfoKey](
  name,
  version,
  scalaVersion,
  sbtVersion,
  BuildInfoKey.action("gitRevision") {
    import scala.sys.process._
    "git rev-parse HEAD".!!.trim
  },
  BuildInfoKey.action("gitCommitNumber") {
    import scala.sys.process._
    "git rev-list --count HEAD".!!.trim
  }
)

buildInfoOptions += BuildInfoOption.BuildTime
buildInfoPackage := "ru.neoflex.imdbApp.buildinfo"

//disable publishing the main jar produced by `package`
Compile / packageBin / publishArtifact := false

// make run command include the provided dependencies
Compile / run := Defaults
  .runTask(
    Compile / fullClasspath,
    Compile / run / mainClass,
    Compile / run / runner
  )
  .evaluated

// stays inside the sbt console when we press "ctrl-c" while a Spark programm executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true

// Assembly options for publish and publishLocal sbt tasks
enablePlugins(AssemblyPlugin)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "versions", "9", "module-info.class") =>
    MergeStrategy.discard
  case PathList(ps @ _*) if ps.last endsWith ".conf" => MergeStrategy.concat
  case PathList(ps @ _*) if ps.last endsWith ".properties" =>
    MergeStrategy.concat
  case x => MergeStrategy.defaultMergeStrategy(x)
}

assembly / mainClass := Some("ru.neoflex.imdbApp.Boot")
