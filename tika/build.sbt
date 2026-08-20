name := "pdf2txt-tika"
description := "The pdf2txt-tika subproject implements an interface to the tika PDF converters."

resolvers ++= Seq(
//  Resolvers.localResolver, // Reserve for Two Six.
//  Resolvers.clulabResolver // processors-models, transitive dependency
)

libraryDependencies ++= {
  val tikaVersion = "2.9.0" // up to 2.9.0 on Java 8

  Seq(
    "org.apache.tika" % "tika-core"                     % tikaVersion,
    "org.apache.tika" % "tika-parsers"                  % tikaVersion pomOnly (),
    // Use of xml-apis results in a circular dependency.
    "org.apache.tika" % "tika-parsers-standard-package" % tikaVersion exclude("xml-apis", "xml-apis")
  )
}
