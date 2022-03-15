import org.clulab.sbt.BuildUtils
import org.clulab.sbt.Resolvers

name := "pdf2txt"
description := "The pdf2txt project implements the org.clulab.pdf2txt package including the Pdf2txt class."

// Last checked 2021-08-23
val scala11 = "2.11.12" // up to 2.11.12
val scala12 = "2.12.15" // up to 2.12.15
val scala13 = "2.13.8"  // up to 2.13.8

// scala13 is skipped here.
ThisBuild / crossScalaVersions := Seq(scala12, scala11)
ThisBuild / scalaVersion := crossScalaVersions.value.head

resolvers ++= Seq(
//  Resolvers.localResolver,  // Reserve for Two Six.
  Resolvers.clulabResolver, // glove, processors-models
//  Resolvers.jitpackResolver // Ontologies
)

libraryDependencies ++= {
  Seq(
    "org.clulab" %% "processors-main" % "8.4.7"// up to 8.4.7 as of 2021-12-21
  )
}

lazy val core = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .disablePlugins(PlayScala, JavaAppPackaging, SbtNativePackager)
  .dependsOn(common % "compile -> compile; test -> test", pdfminer, pdftotext, tika, scienceparse)
  .aggregate(common, pdfminer, pdftotext, tika, scienceparse)
  .settings(
    assembly / aggregate := false,
    assembly / mainClass := Some("org.clulab.pdf2txt.apps.HelloWorldApp")
  )

lazy val common = project

lazy val pdfminer = project
  .dependsOn(common % "compile -> compile; test -> test")

lazy val pdftotext = project
  .dependsOn(common % "compile -> compile; test -> test")

lazy val scienceparse = project
  .dependsOn(common % "compile -> compile; test -> test")

lazy val tika = project
  .dependsOn(common % "compile -> compile; test -> test")