package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.TextRange

trait Preprocessor extends {
  def preprocess(textRange: TextRange): Seq[TextRange]
}
