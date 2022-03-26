package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{StringUtils, TextRange, TextRanges, TripleIndexedSeq}
import org.clulab.pdf2txt.document.logical.{DocumentBySentence, SentenceDocument, WordDocument}
import org.clulab.pdf2txt.languageModel.{AlwaysLanguageModel, LocalSetLanguageModel, LanguageModel}

class LineBreakPreprocessor(globalLanguageModel: LanguageModel = LineBreakPreprocessor.languageModel) extends Preprocessor {

  def isHyphen(wordDocument: WordDocument): Boolean = StringUtils.WORD_BREAK_CHARS.exists(wordDocument.contents.head.matches)

  def isSeparatedBySingleLine(wordDocument: WordDocument): Boolean = isSingleNewline(wordDocument.postSeparator)

  def isSingleNewline(textRange: TextRange): Boolean = StringUtils.LINE_BREAK_STRINGS.exists(textRange.matches)

  def preprocessSentence(sentence: SentenceDocument, languageModel: LanguageModel): TextRanges = {
    val processorsWords = sentence.contents.map(_.processorsWord)
    val tripleIndexOpt = TripleIndexedSeq(sentence.contents.indices).find { case (prevIndex, hyphenIndex, nextIndex) =>
      val prevWord = sentence.contents(prevIndex) // should have no separator before hyphen
      val hyphenWord = sentence.contents(hyphenIndex) // the hyphen followed by a newline
      val nextWord = sentence.contents(nextIndex)

      isHyphen(hyphenWord) && isSeparatedBySingleLine(hyphenWord) && prevWord.postSeparator.isEmpty &&
          !isHyphen(prevWord) && !isHyphen(nextWord) &&
          languageModel.shouldJoin(prevWord.processorsWord, nextWord.processorsWord, processorsWords.take(prevIndex))
    }

    tripleIndexOpt.map { case (prevIndex, hyphenIndex, nextIndex) =>
      val prevWord = sentence.contents(prevIndex)
      val hyphenWord = sentence.contents(hyphenIndex)
      val nextWord = sentence.contents(nextIndex)
      val textRanges = new TextRanges()

      textRanges += sentence.andBefore(prevWord.preSeparator)
      textRanges ++= prevWord.contents
      textRanges ++= nextWord.contents

      // Move the separator from after the prevWord to after the nextWord to preserve as much
      // formatting as possible.  However, avoid a blank line which would separate paragraphs.
      // This might result in \n being placed in front of a period which would affect abbreviations.
      // if (!StringUtils.LINE_BREAK_STRINGS.exists(nextWord.postSeparator.toString.startsWith(_)))
      //   textRanges += hyphenWord.postSeparator
      textRanges += sentence.andAfter(nextWord.postSeparator)

      preprocess(TextRange(textRanges.toString))
    }.getOrElse(TextRanges(sentence))
  }

  def preprocess(textRange: TextRange): TextRanges = {
    val document = DocumentBySentence(textRange)
    val documentLanguageModel = LocalSetLanguageModel(textRange)
    val languageModel = DoubleLanguageModel(globalLanguageModel, documentLanguageModel)
    val textRanges = new TextRanges()

    textRanges += document.preSeparator
    document.contents.foreach { sentence =>
      textRanges ++= preprocessSentence(sentence, languageModel)
    }
    textRanges += document.postSeparator
  }
}

case class DoubleLanguageModel(leftLanguageModel: LanguageModel, rightLanguageModel: LanguageModel) extends LanguageModel {

  override def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean =
      leftLanguageModel.shouldJoin(left, right, prevWords) ||
      rightLanguageModel.shouldJoin(left, right, prevWords)
}

object LineBreakPreprocessor {
  lazy val languageModel = new AlwaysLanguageModel()
}
