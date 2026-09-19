package org.clulab.pdf2txt.adobe.utils

import org.clulab.pdf2txt.adobe.AdobeConverter
import org.json4s.{JObject, JString}
import org.json4s.jvalue2monadic // for \

class AdobeElement(
  val path: AdobePath,
  val text: String,
  val preSeparate: AdobeElement.Separator,
  val extract: AdobeElement.Extractor,
  val postSeparate: AdobeElement.Separator

) {

  def this(path: AdobePath, text: String, preSeparate: AdobeElement.Separator, postSeparate: AdobeElement.Separator) =
    this(path, text, preSeparate, AdobeElement.mkReturnText(text), postSeparate)

  def name: String = path.name

  def index: Int = path.index

  def paragraphIndexOpt: Option[Int] = path.paragraphIndexOpt
}

object AdobeElement {
  type Separator = (Option[AdobeElement], Option[AdobeElement]) => String
  type Extractor = () => String

  def mkReturnText(text: String): () => String = () => {
    text
  }
  val ignoredText = ""
  val ignoreText = mkReturnText(ignoredText)
  val noLine = ""
  val singleLine = "\n"
  val doubleLine = singleLine + singleLine

  def apply(path: AdobePath, text: String): AdobeElement = {
    val returnText: () => String = mkReturnText(text)
    val name = path.name

    val preSeparate = name match {
      // Do not add anything previous to a Sub
      case AdobeNames.Sub => (prevElementOpt: Option[AdobeElement], nextElementOpt: Option[AdobeElement]) => {
        if (prevElementOpt.exists(_.name == AdobeNames.Sub)) {
          val prevParIndexOpt = prevElementOpt.get.paragraphIndexOpt
          val currParIndexOpt = path.paragraphIndexOpt

          if (prevParIndexOpt == currParIndexOpt)
            noLine
          else
            singleLine
        } else
          noLine
      }
      case _ => (prevElementOpt: Option[AdobeElement], nextElementOpt: Option[AdobeElement]) => {
        // If the previous one was a Sub and this one isn't, the previous one needed an extra singleLine
        if (prevElementOpt.exists(_.name == AdobeNames.Sub))
          singleLine
        else
          noLine
      }
    }

    val extract = name match {
      case AdobeNames.Document =>
        ignoreText
      case AdobeNames.Aside =>
        returnText
      case AdobeNames.Figure =>
        ignoreText
      case AdobeNames.Footnote => () => {
        // Footnotes often begin with some numbers and maybe a space.
        if (text.head.isDigit)
          text.dropWhile(_.isDigit).dropWhile(_.isSpaceChar)
        // Sometimes it is a letter like a or b, especially for author affiliations.
        else if (text.head.isLower && text.lift(1).contains(' '))
          text.drop(2)
        else text
      }

      case header if AdobeNames.headers.contains(header) =>
        returnText

      case AdobeNames.L =>
        ignoreText
      case AdobeNames.LI =>
        ignoreText
      case AdobeNames.Lbl =>
        ignoreText
      case AdobeNames.LBody => () => {
        // These sometimes have the bullet still in front.
        if (text.head == '-' || text.head == '*') text.drop(1)
        else text
      }

      case AdobeNames.P =>
        returnText
      case AdobeNames.ParagraphSpan =>
        returnText

      case AdobeNames.Reference => () => {
        // A reference within a reference can usually be ignored.
        if (path.isIn(AdobeNames.Reference)) ""
        else text
      }
      case AdobeNames.Sect =>
        ignoreText
      case AdobeNames.StyleSpan =>
        ignoreText
      case AdobeNames.Sub =>
        returnText

      case AdobeNames.Span =>
        ignoreText
      case AdobeNames.ExtraCharSpan =>
        ignoreText
      case AdobeNames.HyphenSpan =>
        ignoreText
      case AdobeNames.NbspSpan =>
        ignoreText

      case AdobeNames.Table =>
        ignoreText
      case AdobeNames.TD =>
        ignoreText
      case AdobeNames.TH =>
        ignoreText
      case AdobeNames.TR =>
        ignoreText
      case AdobeNames.Title =>
        returnText
      case AdobeNames.Watermark =>
        mkReturnText("Watermark")
      case _ =>
        returnText
    }

    val postSeparate = name match {
      case AdobeNames.ParagraphSpan => (prevElementOpt: Option[AdobeElement], nextElementOpt: Option[AdobeElement]) => {
        // Do not separate ParagraphSpans.
        if (nextElementOpt.exists(_.name == AdobeNames.ParagraphSpan))
          noLine
        else
          doubleLine
      }
      case AdobeNames.Sub => (prevElementOpt: Option[AdobeElement], nextElementOpt: Option[AdobeElement]) => {
        singleLine
      }
      case _ => (prevElementOpt: Option[AdobeElement], nextElementOpt: Option[AdobeElement]) => {
        doubleLine
      }
    }

    new AdobeElement(path, text, preSeparate, extract, postSeparate)
  }

  def apply(jObject: JObject): AdobeElement = {
    val path = (jObject \ "Path").asInstanceOf[JString].values
    val text = jObject.values.get("Text")
        .map(_.asInstanceOf[String])
        .getOrElse("")

    apply(AdobePath(path), text)
  }
}
