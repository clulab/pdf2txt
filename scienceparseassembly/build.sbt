name := "pdf2txt-scienceparseassembly"
description := "The pdf2txt-scienceparseassembly subproject combines pre-compiled allenai/science-parser libraries into a single jar file."

unmanagedBase := {
  val suffix = CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((major, minor)) => s"-$major.$minor"
    case None => ""
  }

  baseDirectory.value / ("lib" + suffix)
}

// Do not include Scala libraries here.
// They will be accounted for in scienceparselib.
ThisBuild / autoScalaLibrary := false

