package org.clulab.pdf2txt.pdftotext

import org.clulab.pdf2txt.common.process.ExternalProcess

import java.io.File

class PdfToTextProcess(inputFile: File, outputFile: File) extends ExternalProcess() {
	command.add("pdftotext")
	command.add("-eol")
	command.add("unix")
	command.add("-enc")
	command.add("UTF-8")
	command.add(inputFile.getAbsolutePath) // Convert to OS style
	command.add(outputFile.getAbsolutePath)
}
