package org.clulab.pdf2txt.pdfminer

import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.{FileUtils, MetadataHolder}

import java.io.File
import java.nio.file.Files
import scala.beans.BeanProperty

class PdfMinerConverter(pdfMinerSettings: PdfMinerSettings = PdfMinerConverter.defaultSettings) extends PdfConverter() {
  val pythonFile = {
    val script = FileUtils.getTextFromResource("/org/clulab/pdf2txt/pdfminer/pdf_to_txt_file.py")
    val pythonFile = File.createTempFile(getClass.getSimpleName + "-", ".py")

    FileUtils.printWriterFromFile(pythonFile).autoClose { printWriter =>
      printWriter.print(script)
    }
    pythonFile
  }

  override def convert(inputFile: File, metadataHolderOpt: Option[MetadataHolder] = None): String = {
    val outputFile = File.createTempFile(getClass.getSimpleName + "-", ".txt")
    val process = new PdfMinerProcess(pdfMinerSettings.python, pythonFile, inputFile, outputFile)

    process.execute()

    val text = FileUtils.getTextFromFile(outputFile)

    Files.delete(outputFile.toPath)
    text
  }

  override def close(): Unit = {
    Files.delete(pythonFile.toPath)
  }
}

case class PdfMinerSettings(@BeanProperty var python: String) {
  def this() = this("")
}

object PdfMinerConverter {
  val defaultPython = "python3"
  val defaultSettings: PdfMinerSettings = PdfMinerSettings(defaultPython)
}

