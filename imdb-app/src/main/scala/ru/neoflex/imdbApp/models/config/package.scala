package ru.neoflex.imdbApp.models

package object config {

  case class AppConfig(
    name:  String,
    files: FilesConfig
  )

  case class FilesConfig(
    datasetDir: String
  )
  
}
