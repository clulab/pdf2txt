package org.clulab.pdf2txt.tika

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.Test

import java.io.{File, InputStream}

class TikaTest extends Test {
  val pdfFilename = "./tika/src/test/resources/org/clulab/pdf2txt/tika/clu lab.pdf"
  val pdfResourceName = "/org/clulab/pdf2txt/tika/clu lab.pdf"
  val htmlResourceName = "/org/clulab/pdf2txt/tika/clu lab.html"

  def getInputStream(filename: String): InputStream = getClass.getResourceAsStream(filename)

  lazy val tika = new TikaConverter()

  behavior of "Tika"

  it should "detect a PDF file" in {
    val isPdf = getInputStream(pdfResourceName).autoClose { inputStream =>
      tika.isPdf(inputStream)
    }

    isPdf shouldBe true
  }

  it should "not detect an HTML file" in {
    val isPdf = getInputStream(htmlResourceName).autoClose { inputStream =>
      tika.isPdf(inputStream)
    }

    isPdf should not be true
  }

  it should "read a PDF stream" in {
    val text = getInputStream(pdfResourceName).autoClose { inputStream =>
      tika.read(inputStream)
    }

    text should include ("The Computational Language Understanding")
    text should include ("please see our NLP")
  }

  it should "not read an HTML stream" in {
    assertThrows[RuntimeException] {
      getInputStream(htmlResourceName).autoClose { inputStream =>
        tika.read(inputStream)
      }
    }
  }

  ignore should "read a PDF file" in {
    assert(new File(pdfFilename).exists)

    val text = tika.convert(new File(pdfFilename))

    text should include ("The Computational Language Understanding")
    text should include ("please see our NLP")
  }
}
