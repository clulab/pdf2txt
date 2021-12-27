name := "pdf2txt-tika"
description := "The pdf2txt-tika subproject implements an interface to the tika PDF converters."

resolvers ++= Seq(
//  Resolvers.localResolver, // Reserve for Two Six.
//  Resolvers.clulabResolver // processors-models, transitive dependency
)

libraryDependencies ++= {
  Seq(
    "org.apache.tika" % "tika-core"                     % "2.1.0",
    "org.apache.tika" % "tika-parsers"                  % "2.1.0" pomOnly (),
    "org.apache.tika" % "tika-parsers-standard-package" % "2.1.0"
  )
}

