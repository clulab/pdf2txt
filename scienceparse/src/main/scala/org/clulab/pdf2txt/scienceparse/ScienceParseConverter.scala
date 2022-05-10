package org.clulab.pdf2txt.scienceparse

import org.allenai.scienceparse.{ExtractedMetadata, Parser}
import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser

import java.io.{BufferedInputStream, File, FileInputStream, InputStream}
import scala.collection.JavaConverters._

class ScienceParseConverter() extends PdfConverter {
  val parser: Parser = {
    val parserOpt = Option(Parser.getInstance())

    parserOpt.getOrElse(throw new RuntimeException("ScienceParse returned a null parser instance."))
  }

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

    append(extractedMetadata.title)
    append(extractedMetadata.abstractText)
    Option(extractedMetadata.sections).map(_.asScala).getOrElse(List.empty).foreach { section =>
      append(section.heading)
      append(section.text)
    }
    stringBuffer.toString
  }

  def read(inputStream: InputStream): String = {
    val extractedMetadataOpt = Option(parser.doParse(inputStream))

    extractedMetadataOpt.map(toString).getOrElse("")
  }

  override def convert(file: File): String = {
    new BufferedInputStream(new FileInputStream(file)).autoClose { inputStream =>
      read(inputStream)
    }
  }
}
