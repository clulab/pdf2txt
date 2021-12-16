package org.clulab.pdf2txt.tika

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser

import org.apache.tika.config.TikaConfig
import org.apache.tika.detect.Detector
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.sax.BodyContentHandler

import java.io.InputStream

class Tika() {
  val config = {
    getClass.getResourceAsStream("/org/clulab/pdf2txt/tika/tika-config.xml").autoClose { source =>
      new TikaConfig(source)
    }
  }
  val detector: Detector = config.getDetector
  val parser = new AutoDetectParser(config)

  def read(inputStream: InputStream): String = {
    val handler = new BodyContentHandler();
    val metadata = new Metadata();

    parser.parse(inputStream, handler, metadata);
    handler.toString();
  }
}
