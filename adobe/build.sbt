name := "pdf2txt-adobe"
description := "The pdf2txt-adobe subproject implements an interface to the adobe converters."

resolvers ++= Seq(
//  Resolvers.localResolver, // Reserve for Two Six.
//  Resolvers.clulabResolver // processors-models, transitive dependency
)

libraryDependencies ++= {
  val json4sVersion = "3.5.2"

  Seq(
    "com.adobe.documentservices"  % "pdfservices-sdk" % "2.2.2",
    "net.lingala.zip4j"           % "zip4j"           % "2.10.0",
    "org.json4s"                 %% "json4s-core"     % json4sVersion,
    "org.json4s"                 %% "json4s-jackson"  % json4sVersion
  )
}
