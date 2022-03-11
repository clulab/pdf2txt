package org.clulab.pdf2txt.tika

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.Test

import java.io.InputStream

class TestTika extends Test {
  val pdfFilename = "/org/clulab/pdf2txt/tika/clulab.pdf"
  val htmlFilename = "/org/clulab/pdf2txt/tika/clulab.html"

  def getInputStream(filename: String): InputStream = getClass.getResourceAsStream(filename)

  val tika = new TikaConverter()

  behavior of "Tika"

  it should "detect a PDF file" in {
    val isPdf = getInputStream(pdfFilename).autoClose { inputStream =>
      tika.isPdf(inputStream)
    }

    isPdf shouldBe true
  }

  it should "not detect an HTML file" in {
    val isPdf = getInputStream(htmlFilename).autoClose { inputStream =>
      tika.isPdf(inputStream)
    }

    isPdf should not be true
  }

  it should "read a PDF file" in {
    val text = getInputStream(pdfFilename).autoClose { inputStream =>
      tika.read(inputStream)
    }

    text should not be empty
  }

  it should "not read an HTML file" in {
    assertThrows[RuntimeException] {
      getInputStream(htmlFilename).autoClose { inputStream =>
        tika.read(inputStream)
      }
    }
  }
}
