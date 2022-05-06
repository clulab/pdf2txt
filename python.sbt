import ai.kien.python.Python
import org.clulab.sbt.BuildUtils

lazy val javaOpts = {
  if (BuildUtils.isWindows())
    // Fill this in manually.
    Seq("-Djna.library.path=/D:/ProgramFiles/Python39/DLLs")
  else {
    Python().scalapyProperties.get.map { case (key, value) =>
      s"-D$key=$value"
    }.toSeq
  }
}

ThisBuild / run / javaOptions ++= javaOpts
