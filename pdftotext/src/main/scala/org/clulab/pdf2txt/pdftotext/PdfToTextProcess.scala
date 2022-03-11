package org.clulab.pdf2txt.pdftotext

import org.clulab.pdf2txt.common.process.ExternalProcess

class PdfToTextProcess(zip: String, archive: String, file: String, cdOpt: Option[String] = None) extends ExternalProcess() {
	cdOpt.map(directory)
	command.add(zip)
	command.add(archive)
	command.add(file)
}
