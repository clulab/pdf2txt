package org.clulab.pdf2txt.tika

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser

import org.apache.tika.config.TikaConfig
import org.apache.tika.detect.Detector
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.sax.BodyContentHandler

import java.io.InputStream

class Tika(config: TikaConfig = new TikaConfig()) {
  val detector: Detector = config.getDetector
  val parser = new AutoDetectParser(config)

  def isPdf(inputStream: InputStream): Boolean = {
    val metadata = new Metadata()
    val mediaType = detector.detect(inputStream, metadata)

    inputStream.reset()
    mediaType.toString == "application/pdf"
  }

  def read(inputStream: InputStream): String = {
    val handler = new BodyContentHandler()
    val metadata = new Metadata()

    if (isPdf(inputStream)) {
      parser.parse(inputStream, handler, metadata)
      handler.toString()
    }
    else
      throw new RuntimeException("Not PDF!")
  }
}
