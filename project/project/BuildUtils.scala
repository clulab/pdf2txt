package org.clulab.sbt

import sbt.IO
import sbt.MavenRepository

import java.io.File
import java.util.Properties

object BuildUtils {

  def singleLine(text: String): String = text.stripMargin.replace('\n', ' ').trim

  // See https://stackoverflow.com/questions/25665848/how-to-load-setting-values-from-a-java-properties-file.
  def getProperty(fileName: String, propertyName: String): String = {
    val properties = {
      val properties = new Properties()
      IO.load(properties, new File(fileName))
      properties
    }
    val property = properties.getProperty(propertyName)
    // println(s"$fileName:$propertyName = $property")
    property
  }

  def keepRepos(prefix: String) = (repo: MavenRepository) => {
    repo.root.startsWith(prefix)
  }

  // Avoid in particular those starting with "file:"
  val keepHttpRepos = keepRepos("http")

  def isWindows(): Boolean = {
    System.getProperty("os.name").toLowerCase().contains("win")
  }

  // One shouldn't use the giter8 packaged format because of backslashes in Windows.
  def pkgToDir(pkg: String): String = pkg.replace('.', '/')

  // See also https://repo1.maven.org/maven2/com/typesafe/play/play-json_2.12.
  // Up to 2.8.7 theoretically, but 2.8.1 practically because of unresolved dependencies in webapp.
  // Again, only theoretically, because 2.7.4 is the last one to support Scala 2.11.
  val sbtPluginVersion = "2.7.4"
  val artifactory = true
  val compression = true
}
