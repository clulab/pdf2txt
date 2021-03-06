package org.clulab.pdf2txt.pdfminer

import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.FileUtils

import java.io.File
import java.nio.file.Files

class PdfMinerConverter() extends PdfConverter() {
  val pythonFile = {
    val script = FileUtils.getTextFromResource("/org/clulab/pdf2txt/pdfminer/pdf_to_txt_file.py")
    val pythonFile = File.createTempFile(getClass.getSimpleName + "-", ".py")

    FileUtils.printWriterFromFile(pythonFile).autoClose { printWriter =>
      printWriter.print(script)
    }
    pythonFile
  }

  override def convert(inputFile: File): String = {
    val outputFile = File.createTempFile(getClass.getSimpleName + "-", ".txt")
    val process = new PdfMinerProcess(pythonFile, inputFile, outputFile)

    process.execute()

    val string = FileUtils.getTextFromFile(outputFile)

    Files.delete(outputFile.toPath)
    string
  }

  override def close(): Unit = {
    Files.delete(pythonFile.toPath)
  }
}
