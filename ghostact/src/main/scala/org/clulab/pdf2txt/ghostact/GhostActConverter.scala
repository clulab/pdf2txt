package org.clulab.pdf2txt.ghostact

import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.{FileUtils, StringUtils}

import java.io.File
import scala.beans.BeanProperty

class GhostActConverter(ghostActSettings: GhostActSettings = GhostActConverter.defaultSettings) extends PdfConverter() {

  override def convert(inputFile: File): String = {
    // The simple name needs to have %d in it to that multiple pages can be converted.
    // So, hopefully the prefix will suffice to make a unique file name and the suffix is extra.
    val imgOutputFile = File.createTempFile(getClass.getSimpleName + "-", ".%d")
    val ghostScriptProcess = new GhostScriptProcess(ghostActSettings.ghostscript, ghostActSettings.device, ghostActSettings.resolution,
        inputFile, imgOutputFile)

    ghostScriptProcess.execute()

    val base = StringUtils.beforeLast(imgOutputFile.getAbsolutePath, '.', true)
    val imgOutputFiles = Range(1, Int.MaxValue).toStream
        .map { index => new File(s"$base.${index}") }
        .takeWhile(_.exists)
        .toVector
    val texts =  imgOutputFiles.map { imgInputFile =>
      val tesseractProcess = new TesseractProcess(ghostActSettings.tesseract, imgInputFile, new File(imgInputFile.getAbsolutePath))
      val txtOutputFile = new File(imgInputFile.getAbsolutePath + ".txt")

      tesseractProcess.execute()

      val text = FileUtils.getTextFromFile(txtOutputFile)

      imgInputFile.delete()
      txtOutputFile.delete()

      text
    }
    val text = texts.mkString("\f\n")

    imgOutputFile.delete()
    text
  }
}

case class GhostActSettings(@BeanProperty var ghostscript: String, @BeanProperty var device: String,
    @BeanProperty var resolution: Int, @BeanProperty var tesseract: String) {
  def this() = this("", "", 0, "")
}

object GhostActConverter {
  val defaultGhostscript = "gswin64"
  val defaultDevice = "png16m"
  val defaultResolution = 400
  val defaultTesseract = "tesseract"
  val defaultSettings: GhostActSettings = GhostActSettings(defaultGhostscript, defaultDevice, defaultResolution, defaultTesseract)
}
