package org.clulab.pdf2txt.ghostact

import org.clulab.pdf2txt.common.process.ExternalProcess

import java.io.File

class TesseractProcess(executable: String, inputFile: File, outputFile: File) extends ExternalProcess() {
	command.add(executable)
	// These should already be in OS-specific style, because the input comes from
	// searching a directory and the output comes from a temporary file.
	command.add(inputFile.getAbsolutePath)
	command.add(outputFile.getAbsolutePath) // .txt will be added
}
