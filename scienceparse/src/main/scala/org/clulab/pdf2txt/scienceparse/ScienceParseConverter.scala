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

    def containsText(string: String): Boolean = {
      string.exists(_.isLetter)
    }

    def fancierTrim(text: String): Seq[String] = {
      // takes text of a "section" and finds likely paragraph breaks based on mid-text \n's
      // Expect a paragraph break in science-parse if there is a period followed by new line followed by a capital letter, open paren, or a‘
      // Indicate those with a unique token and then split on that
      val paragraphs = text.replaceAll("(?<=\\.)\\n(?=[A-Z]|\\(|‘)", "<par_break>").split("<par_break>")
      // some extra cleanup
      paragraphs.map(p =>
        p.replaceAll("\\n\\d*\\n", " ") // This handles page numbers that end up inside a paragraph when the paragraph break is between pages
          .replace("- ", "") // the rest handle words split on line breaks in pdfs
          .replace("-\n","")
          .replace("\n", " "))
    }
    def append(textOrNull: String): Unit = {
      val textOpt = Option(textOrNull)

      textOpt.map { text =>

        val trimmedTexts = fancierTrim(text)
        for (t <- trimmedTexts) {
          if (containsText(t)) {
            stringBuffer.append(t)
            if (!t.endsWith(".")) {
              stringBuffer.append(" .")
            }
            stringBuffer.append("\n\n")
          }

        }

//        if (!trimmedText.endsWith("."))
//          stringBuffer.append(" .")
//        stringBuffer.append("\n\n")
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
