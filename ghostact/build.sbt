name := "pdf2txt-ghostact"
description := "The pdf2txt-ghostact subproject converts PDF to text by first converting the PDF to an image with GhostScript and then converting the image to text with Tesseract."

resolvers ++= Seq(
//  Resolvers.localResolver, // Reserve for Two Six.
//  Resolvers.clulabResolver // processors-models, transitive dependency
)

libraryDependencies ++= {
  Seq(
  )
}
