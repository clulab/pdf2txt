name := "pdf2txt-amazon"
description := "The pdf2txt-amazon subproject implements an interface to the AWS amazon converter."

resolvers ++= Seq(
//  Resolvers.localResolver, // Reserve for Two Six.
//  Resolvers.clulabResolver // processors-models, transitive dependency
)

libraryDependencies ++= {
  val json4sVersion = "3.5.2"

  Seq(
    "software.amazon.awssdk" % "s3"       % "2.17.237",
    "software.amazon.awssdk" % "textract" % "2.17.224"
  )
}
