package org.clulab.pdf2txt.pdftotext

import org.clulab.pdf2txt.common.process.ExternalProcess

import java.io.File

class PdfToTextProcess(inputFile: File, outputFile: File) extends ExternalProcess() {
	command.add("pdftotext")
	command.add("-eol")
	command.add("unix")
	command.add("-enc")
	command.add("UTF-8")
	// These should already be in OS-specific style, because the input comes from
	// searching a directory and the output comes from a temporary file.
	command.add(inputFile.getAbsolutePath)
	command.add(outputFile.getAbsolutePath)
}
