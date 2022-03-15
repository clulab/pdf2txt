package org.clulab.pdf2txt.scienceparse

import org.allenai.scienceparse.{ExtractedMetadata, Parser}
import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser

import java.io.{BufferedInputStream, File, FileInputStream, InputStream}
import scala.collection.JavaConverters._

class ScienceParseConverter() extends PdfConverter {
  val parser = Parser.getInstance()

  def toString(extractedMetadata: ExtractedMetadata): String = {
    val stringBuffer = new StringBuffer()

    def append(textOrNull: String): Unit = {
      val textOpt = Option(textOrNull)

      textOpt.map { text =>
        val trimmedText = text.trim

        stringBuffer.append(trimmedText)
        if (!trimmedText.endsWith("."))
          stringBuffer.append(" .")
        stringBuffer.append("\n\n")
      }
    }

    extractedMetadata.sections.asScala.foreach { section =>
      append(section.heading)
      append(section.text)
    }
    stringBuffer.toString
  }

  def read(inputStream: InputStream): String = {
    val extractedMetadata = parser.doParse(inputStream)

    toString(extractedMetadata)
  }

  override def convert(file: File): String = {
    new BufferedInputStream(new FileInputStream(file)).autoClose { inputStream =>
      read(inputStream)
    }
  }
}
