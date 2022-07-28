package org.clulab.pdf2txt.pdftotext

import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.FileUtils

import java.io.File
import java.nio.file.Files
import scala.beans.BeanProperty

class PdfToTextConverter(pdfToTextSettings: PdfToTextSettings = PdfToTextConverter.defaultSettings) extends PdfConverter() {

  override def convert(inputFile: File): String = {
    val outputFile = File.createTempFile(getClass.getSimpleName + "-", ".txt")
    val process = new PdfToTextProcess(pdfToTextSettings.pdftotext, inputFile, outputFile)

    process.execute()

    val text = FileUtils.getTextFromFile(outputFile)

    Files.delete(outputFile.toPath)
    text
  }
}

case class PdfToTextSettings(@BeanProperty var pdftotext: String) {
  def this() = this("")
}

object PdfToTextConverter {
  val defaultPdfToText = "pdftotext"
  val defaultSettings: PdfToTextSettings = PdfToTextSettings(defaultPdfToText)
}
