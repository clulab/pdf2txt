package org.clulab.pdf2txt.pdftotext

import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.FileUtils

import java.io.File
import java.nio.file.Files

class PdfToTextConverter() extends PdfConverter() {

  override def convert(inputFile: File): String = {
    val outputFile = File.createTempFile(getClass.getSimpleName + "-", ".txt")
    val process = new PdfToTextProcess(inputFile, outputFile)

    process.execute()

    val string = FileUtils.getTextFromFile(outputFile)

    Files.delete(outputFile.toPath)
    string
  }
}
