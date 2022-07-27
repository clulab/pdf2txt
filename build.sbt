import org.clulab.sbt.Resolvers

name := "pdf2txt"
description := "The pdf2txt project implements the org.clulab.pdf2txt package including the Pdf2txt class."
maintainer := "clulab.org"

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
    "org.clulab" %% "processors-main" % "8.5.1"// up to 8.5.1 as of 2022-06-20
  )
}

val mainClassOpt = Some("org.clulab.pdf2txt.apps.Pdf2txtApp")

lazy val core = (project in file("."))
  .enablePlugins(BuildInfoPlugin, JavaAppPackaging)
  .disablePlugins(PlayScala)
  .dependsOn(common % "compile -> compile; test -> test", adobe, amazon, google, microsoft, pdfminer, pdftotext, tika, scienceparse)
  .aggregate(common, adobe, amazon, google, microsoft, pdfminer, pdftotext, tika, scienceparse)
  .settings(
    assembly / aggregate := false,
    assembly / mainClass := mainClassOpt,
    Compile / run / mainClass := mainClassOpt,
    trapExit := false, // Avoid sbt.TrapExitSecurityException on System.exit().
    run / fork := true, // Avoid shutting down sbt on untrapped System.exit().
    run / javaOptions += "-Xmx12g" // If running exhausts memory, change it here.
  )

lazy val common = project

lazy val adobe = project
  .dependsOn(common % "compile -> compile; test -> test")

lazy val amazon = project
  .dependsOn(common % "compile -> compile; test -> test")
	
lazy val google = project
  .dependsOn(common % "compile -> compile; test -> test")

lazy val microsoft = project
  .dependsOn(common % "compile -> compile; test -> test")

lazy val pdfminer = project
  .dependsOn(common % "compile -> compile; test -> test")

lazy val pdftotext = project
  .dependsOn(common % "compile -> compile; test -> test")

lazy val scienceparse = project
  .dependsOn(common % "compile -> compile; test -> test")

lazy val tika = project
  .dependsOn(common % "compile -> compile; test -> test")
