package org.clulab.pdf2txt.document.logical

import org.clulab.pdf2txt.LanguageModel
import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.common.utils.{StringUtils, TextRange}
import org.clulab.pdf2txt.document.{Document, DocumentConstructor}
import org.clulab.processors.clu.CluProcessor

import scala.util.matching.Regex

class DocumentByWord protected(rawText: String, range: Range) extends Document(rawText, range) {

  def this(rawText: String) = this(rawText, Range(0, rawText.length))

  def newContent(range: Range): WordContent = new WordContent(rawText, range)

  def newSeparator(range: Range): WordSeparator = new WordSeparator(rawText, range)

  def newContents(range: Range): Seq[WordContent] = Seq(newContent(range))

  def newPostSeparators(): Seq[WordSeparator] = Seq(newSeparator(Range(range.end, range.end)))

  def oldParse(): Seq[Word] = {
    val separators = DocumentByWord.separatorRegex.findAllMatchIn(rawText.substring(range)).map { separator =>
      newSeparator(Range(range.start + separator.start, range.start + separator.end))
    }.toSeq
    val preContents =
        if (separators.isEmpty) Seq.empty // Save it for post, if necessary.
        else newContents(Range(range.start, separators.head.start))
    val interContents = separators.sliding(2).map { case Seq (left, right) =>
      newContent(Range(left.end, right.start))
    }
    val (postContents, postSeparators) =
      if (separators.isEmpty)
        if (this.isEmpty) noContentsOrSeparators
        // Handle the entire content and close with a Separator.
        else (newContents(range), newPostSeparators())
      else
        if (separators.last.end >= range.end) noContentsOrSeparators
        // Handle any content trailing the last Separator.
        else (newContents(Range(separators.last.end, range.end)), newPostSeparators())
    val allContents = preContents ++ interContents ++ postContents
    val allSeparators = separators ++ postSeparators

    assert(allContents.length == separators.length)

    val words = allContents.zip(allSeparators).map { case (content, separator) =>
      Word(rawText, content, separator)
    }

    words
  }

  def parse(): Seq[Word] = {
    val document = DocumentByWord.processor.mkDocument(rawText, keepText = true)




    null
  }

  val words: Seq[Word] = parse()

  override def addCookedText(stringBuilder: StringBuilder): Unit = {
    words.foldRight(None: Option[Word]) { (currWord, nextWordOpt) =>
      currWord.addCookedText(stringBuilder, nextWordOpt)
      Some(currWord)
    }
  }
}

object DocumentByWord extends DocumentConstructor {
  val separatorRegex: Regex = StringUtils.WHITESPACE_STRINGS.mkString("(", "|", ")+").r
  val processor = new CluProcessor()


  def apply(rawText: String): DocumentByWord = new DocumentByWord(rawText)
}

class WordContent(rawText: String, range: Range) extends TextRange(rawText, range) {

  def addCookedText(stringBuilder: StringBuilder, nextContentOpt: Option[WordContent]): Boolean = {

    def addRawText(): Boolean = {
      stringBuilder ++ rawText
      false
    }

    def addCooked(): Boolean = {
      true
    }

    if (this.isEmpty || nextContentOpt.isEmpty)
      addRawText()
    else {
      // Need a sentence.
      // val combine = LanguageModel.instance.shouldCombine()
true
    }
  }
}

class WordSeparator(rawText: String, range: Range) extends TextRange(rawText, range) {
}

case class Word protected (rawText: String, content: WordContent, separator: WordSeparator)
  extends TextRange(rawText, Range(content.start, separator.end)) {

  def addCookedText(stringBuilder: StringBuilder, nextWordOpt: Option[Word]): Unit = {
    if (!content.addCookedText(stringBuilder, nextWordOpt.map(_.content)))
      separator.addText(stringBuilder)
  }
}
