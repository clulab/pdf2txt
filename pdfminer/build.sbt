name := "pdf2txt-pdfminer"
description := "The pdf2txt-pdfminer subproject implements an interface to the tika PDF converters."

resolvers ++= Seq(
//  Resolvers.localResolver, // Reserve for Two Six.
//  Resolvers.clulabResolver // processors-models, transitive dependency
)

libraryDependencies ++= {
  Seq(
    "me.shadaj" %% "scalapy-core" % "0.5.2"
  )
}
