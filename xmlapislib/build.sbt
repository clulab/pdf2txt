name := "pdf2txt-xmlapislib"
description := "The pdf2txt-xmlapislib subproject provides access to the pre-compiled xml-api library."

resolvers ++= Seq(
//  Resolvers.localResolver, // Reserve for Two Six.
//  Resolvers.clulabResolver // processors-models, transitive dependency
)

// https://stackoverflow.com/questions/67126344/sbt-plugin-add-an-unmanaged-jar-file
Compile / packageBin := baseDirectory.value / "lib" / "xml-apis-1.4.01.jar"
