name := "pdf2txt-microsoft"
description := "The pdf2txt-microsoft subproject implements an interface to the microsoft converter."

resolvers ++= Seq(
//  Resolvers.localResolver, // Reserve for Two Six.
//  Resolvers.clulabResolver // processors-models, transitive dependency
)

libraryDependencies ++= {
//  val json4sVersion = "3.5.2"

  Seq(
    "com.microsoft.azure.cognitiveservices" % "azure-cognitiveservices-computervision" % "1.0.9-beta"
  )
}
