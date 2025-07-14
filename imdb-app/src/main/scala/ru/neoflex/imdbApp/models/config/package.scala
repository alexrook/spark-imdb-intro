package ru.neoflex.imdbApp.models

package object config {

  object AppModulesEnum extends Enumeration {
    type AppModulesType = Value
    val Main, Samples, UDAF, UDAFTyped = Value
  }

  case class AppConfig(
    name:      String,
    runModule: AppModulesEnum.AppModulesType,
    files:     FilesConfig
  )

  case class FilesConfig(
    datasetDir: String
  )

}
