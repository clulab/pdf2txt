package org.clulab.pdf2txt.common.utils

import java.io.{BufferedOutputStream, File, FileFilter, FileNotFoundException, FileOutputStream, OutputStreamWriter, PrintWriter}
import java.nio.charset.StandardCharsets


object FileUtils {
  val utf8: String = StandardCharsets.UTF_8.toString

  def findFiles(collectionDir: String, extension: String): Seq[File] =
      findFiles(collectionDir, Seq(extension))

  def findFiles(collectionDir: String, extensions: Seq[String]): Seq[File] = {
    val dir = new File(collectionDir)
    val fileFilter = new FileFilter {
      override def accept(file: File): Boolean = {
        file.isFile && extensions.exists(file.getCanonicalPath.endsWith)
      }
    }
    val files = Option(dir.listFiles(fileFilter))
      .getOrElse(throw new FileNotFoundException(collectionDir))

    files
  }

  def printWriterFromFile(file: File): PrintWriter = {
    new PrintWriter(
      new OutputStreamWriter(
        new BufferedOutputStream(
          new FileOutputStream(file)
        ),
        utf8
      )
    )
  }
}
