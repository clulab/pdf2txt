package org.clulab.pdf2txt.scienceparse

import org.clulab.pdf2txt.common.utils.{Preprocessor, TextRange, TextRanges}

class ParagraphPreprocessor extends Preprocessor {

  // This is a local, light-weight preprocessor that uses no model.
  def preprocess(textRange: TextRange): TextRanges = {
    val paragraphBreaks = textRange.findAll(ParagraphPreprocessor.paragraphBreakRegex).toVector
    val paragraphs = textRange.removeAll(paragraphBreaks)
    // The page number regex does not remove any letters, so non-lettered paragraphs can be removed now.
    val letteredParagraphs = paragraphs.filter(_.exists(_.isLetter))
    val perParagraphParts = letteredParagraphs.map { paragraph =>
      val pageNumbers = paragraph.findAll(ParagraphPreprocessor.pageNumberRegex)
      paragraph.replaceAll(pageNumbers, ParagraphPreprocessor.replacementPageNumberTextRange)
    }
    val partsAndSeparators = perParagraphParts.flatMap { parts =>
      parts :+ ParagraphPreprocessor.paragraphSeparatorTextRange
    }

    TextRanges(partsAndSeparators)
  }
}

object ParagraphPreprocessor {
  // This takes text of a "section" and finds likely paragraph breaks based on mid-text \n's.
  // Because text comes straight from the PDF converter, there should be no other EOL characters.
  // Expect a paragraph break if there is a period followed by new line followed by a
  // capital letter, open parenthesis, or a‘.  These are particular to ScienceParse.
  // ?<= is "positive lookbehind"; ?= is "positive lookahead".  They are not part of the match.
  val paragraphBreakRegex = "(?<=\\.)\\n(?=[A-Z]|\\(|‘)".r
  // This handles page numbers that end up inside a paragraph when the paragraph break is between pages.
  val pageNumberRegex = "\\n\\d+\\n".r
  // End each paragraph with one of these, essentially replacing one \n with two.
  val paragraphSeparatorTextRange = TextRange("\n\n")
  val replacementPageNumberTextRange = TextRange(" ")
}
