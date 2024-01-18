name := "pdf2txt-scienceparse"
description := "The pdf2txt-scienceparse subproject implements an interface to the allenai/science-parser converters."

resolvers ++= Seq(
//  Resolvers.localResolver, // Reserve for Two Six.
//  Resolvers.clulabResolver // processors-models, transitive dependency
)

libraryDependencies ++= {
  Seq(
    // This is used to produce the metadata.
    "com.lihaoyi"       %% "ujson"          % "2.0.0"
  )
}
