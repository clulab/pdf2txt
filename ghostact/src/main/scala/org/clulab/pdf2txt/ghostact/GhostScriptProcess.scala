package org.clulab.pdf2txt.ghostact

import org.clulab.pdf2txt.common.process.ExternalProcess

import java.io.File

class GhostScriptProcess(executable: String, device: String, resolution: Int, inputFile: File, outputFile: File) extends ExternalProcess() {
  //.\gswin64 -dBATCH -dNOPAUSE -r400 -sDEVICE=png16m -sOutputFile=.\clulab_%d .\clulab.pdf
	command.add(executable)
	command.add("-dBATCH")
	command.add("-dNOPAUSE")
	command.add(s"-sDEVICE=$device")
	command.add(s"-r$resolution")
	// These should already be in OS-specific style, because the input comes from
	// searching a directory and the output comes from a temporary file.
	command.add(s"-sOutputFile=${outputFile.getAbsolutePath}")
	command.add(inputFile.getAbsolutePath)
}
