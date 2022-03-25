package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.common.utils.{CustomSystem, Systemish, Test}

import java.io.{ByteArrayOutputStream, OutputStream, PrintStream}
import java.nio.charset.StandardCharsets

class Pdf2txtAppTest extends Test {

  class ExitException extends RuntimeException

  class TestSystem(protected val outByteArrayOutputStream: ByteArrayOutputStream, protected val errByteArrayOutputStream: ByteArrayOutputStream)
      extends CustomSystem(TestSystem.newPrintStream(outByteArrayOutputStream), TestSystem.newPrintStream(errByteArrayOutputStream)) {

    def getOutString: String = outByteArrayOutputStream.toString(TestSystem.CHARSET_NAME)

    def getErrString: String = errByteArrayOutputStream.toString(TestSystem.CHARSET_NAME)

    override def exit(status: Int): Unit = {
      if (statusOpt.isEmpty) {
        super.exit(status)
        out.close()
        err.close()
      }
      throw new ExitException()
    }
  }

  object TestSystem {
    val CHARSET_NAME: String = StandardCharsets.UTF_8.name()

    // https://stackoverflow.com/questions/1760654/java-printstream-to-string
    def newPrintStream(outputStream: OutputStream): PrintStream =
      new PrintStream(outputStream, true, CHARSET_NAME)

    def apply(): TestSystem = {
      new TestSystem(new ByteArrayOutputStream(), new ByteArrayOutputStream())
    }
  }

  def newPdf2txtApp(args: Array[String], system: Systemish): Unit = {
    try {
      new Pdf2txtApp(args, system = system)
    }
    catch {
      case _: ExitException =>
      case throwable: Throwable => throw throwable
    }
  }

  behavior of "Pdf2txt"

  it should "catch unknown arguments" in {
    val system = TestSystem()
    newPdf2txtApp(Array("-unknown"), system)
    val outString = system.getOutString
    val errString = system.getErrString

    outString should be (empty)
    errString should include ("unknown")
    system.statusOpt should be (Some(-1))
  }

  it should "show help" in {
    val system = TestSystem()
    newPdf2txtApp(Array("-help"), system)
    val outString = system.getOutString
    val errString = system.getErrString

    outString should include ("Syntax:")
    errString should be (empty)
    system.statusOpt should be (Some(0))
  }

  it should "catch unknown converters" in {
    val system = TestSystem()
    newPdf2txtApp(Array("-converter", "unknown"), system)
    val outString = system.getOutString
    val errString = system.getErrString

    outString should be (empty)
    errString should include ("converter")
    errString should include ("unknown")
    system.statusOpt should be (Some(-1))
  }

  it should "catch unknown languageModels" in {
    val system = TestSystem()
    newPdf2txtApp(Array("-languageModel", "unknown"), system)
    val outString = system.getOutString
    val errString = system.getErrString

    outString should be (empty)
    errString should include ("languageModel")
    errString should include ("unknown")
    system.statusOpt should be (Some(-1))
  }

  it should "catch unknown preprocessor values" in {
    val system = TestSystem()
    newPdf2txtApp(Array("-line", "unknown"), system)
    val outString = system.getOutString
    val errString = system.getErrString

    outString should be (empty)
    errString should include ("line")
    errString should include ("unknown")
    system.statusOpt should be (Some(-1))
  }

  it should "show the configuration" in {
    val system = TestSystem()
    newPdf2txtApp(Array.empty, system)
    val outString = system.getOutString
    val errString = system.getErrString

    outString should include ("Configuration")
    outString should include ("true")
    errString should be (empty)
    system.statusOpt should be (None)
  }

  it should "allow for a configuration file" in {
    val system = TestSystem()
    newPdf2txtApp(Array("-conf", "./src/test/resources/Pdf2txtAppTest"), system)
    val outString = system.getOutString
    val errString = system.getErrString

    outString should be (empty)
    errString should include ("converter")
    errString should include ("test")
    system.statusOpt should be (Some(-1))
  }

  it should "complain about a missing configuration file" in {
    val system = TestSystem()
    newPdf2txtApp(Array("-conf", "./src/test/resources/missing"), system)
    val outString = system.getOutString
    val errString = system.getErrString

    outString should be (empty)
    errString should include ("Configuration")
    errString should include ("exist")
    system.statusOpt should be (Some(-1))
  }

  it should "complain about a corrupt configuration file" in {
    val system = TestSystem()
    newPdf2txtApp(Array("-conf", "./src/test/resources/corrupt"), system)
    val outString = system.getOutString
    val errString = system.getErrString

    outString should be (empty)
    errString should include ("corrupt")
    system.statusOpt should be (Some(-1))
  }

  it should "complain about an incompatible configuration file" in {
    val system = TestSystem()
    newPdf2txtApp(Array("-conf", "./src/test/resources/incompatible"), system)
    val outString = system.getOutString
    val errString = system.getErrString

    outString should be (empty)
    errString should include ("Pdf2txt")
    errString should include ("incompatible")
    system.statusOpt should be (Some(-1))
  }

  it should "not try to convert a file to a directory" in {
    val system = TestSystem()
    newPdf2txtApp(Array("-in", "./src/test/resources/inFile.pdf", "-out", "./src/test/resources/outDir"), system)
    val outString = system.getOutString
    val errString = system.getErrString

    outString should be (empty)
    errString should include ("input file")
    errString should include ("existing output directory")
    system.statusOpt should be (Some(-1))
  }

  it should "not try to convert a directory to a file" in {
    val system = TestSystem()
    newPdf2txtApp(Array("-in", "./src/test/resources/inDir", "-out", "./src/test/resources/outFile.txt"), system)
    val outString = system.getOutString
    val errString = system.getErrString

    outString should be (empty)
    errString should include ("input directory")
    errString should include ("existing output file")
    system.statusOpt should be (Some(-1))
  }

  it should "not try to convert a file to an existing file" in {
    val system = TestSystem()
    newPdf2txtApp(Array("-in", "./src/test/resources/inFile.pdf", "-out", "./src/test/resources/outFile.txt"), system)
    val outString = system.getOutString
    val errString = system.getErrString

    outString should be (empty)
    errString should include ("input file")
    errString should include ("existing output file")
    system.statusOpt should be (Some(-1))
  }

  it should "try to convert a file to an existing file with overwrite enabled" in {
    val system = TestSystem()
    newPdf2txtApp(Array("-overwrite", "true", "-in", "./src/test/resources/inFile.pdf", "-out", "./src/test/resources/outFile.txt"), system)
    val outString = system.getOutString
    val errString = system.getErrString

    outString should include ("Configuration")
    errString should be (empty)
    system.statusOpt should be (None)
  }

  it should "take txt files into account" in {
    val system = TestSystem()
    newPdf2txtApp(Array("-converter", "text", "-in", "./src/test/resources/inFile.txt", "-out", "./src/test/resources/outFile.txt"), system)
    val outString = system.getOutString
    val errString = system.getErrString

    outString should be (empty)
    errString should include ("input file")
    errString should include ("existing output file")
    system.statusOpt should be (Some(-1))
  }
}
