package org.clulab.pdf2txt.preprocessor

import org.clulab.dynet.Utils
import org.clulab.pdf2txt.common.utils.{TextRange, TextRanges}
import org.clulab.pdf2txt.document.logical.{DocumentBySentence, DocumentByWord, WordDocument}

class CasePreprocessor() extends Preprocessor {

  def preprocess(textRange: TextRange): TextRanges = {
    val document = {
      val processor = CasePreprocessor.processor
      val document = processor.mkDocument(textRange.toString, keepText = false)

      processor.restoreCase(document)
      new DocumentByWord(None, textRange, document.sentences)
    }
    val children = document.getChildren.flatMap { textRange =>
      textRange match {
        case wordDocument: WordDocument =>
          // Use the processorsWord here instead of something else like the original, raw text.
          Seq(wordDocument.preSeparator, TextRange(wordDocument.processorsWord), wordDocument.postSeparator)
        case _ => Some(textRange)
      }
    }

    TextRanges(children)
  }
}

object CasePreprocessor {
  lazy val processor = {
    Utils.initializeDyNet()
    DocumentBySentence.processor
  }
}
