package org.clulab.pdf2txt.pdfminer

import org.clulab.pdf2txt.common.process.ExternalProcess

import java.io.File

class PdfMinerProcess(pythonFile: File, inputFile: File, outputFile: File) extends ExternalProcess() {
	command.add("python")
	command.add(pythonFile.getAbsolutePath)
	command.add(inputFile.getAbsolutePath)
	command.add(outputFile.getAbsolutePath)
}
