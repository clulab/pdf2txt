package org.clulab.pdf2txt.scienceparse

import org.allenai.scienceparse.{ExtractedMetadata, Parser}
import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.{MetadataHolder, TextRange}

import java.io.{BufferedInputStream, File, FileInputStream, InputStream}
import scala.collection.JavaConverters._

class ScienceParseConverter(scienceParseSettings: ScienceParseSettings = ScienceParseConverter.defaultSettings) extends PdfConverter {
  override val metaExtension: String = ".json"

  val parser: Parser = {
    val parserOpt = Option(Parser.getInstance())

    parserOpt.getOrElse(throw new RuntimeException("ScienceParse returned a null parser instance."))
  }
  val paragraphPreprocessor = new ParagraphPreprocessor()

  def toString(extractedMetadata: ExtractedMetadata, metadataHolderOpt: Option[MetadataHolder]): String = {
    val stringBuilder = new StringBuilder()

    def append(textOrNull: String): Unit = {
      val textOpt = Option(textOrNull)

      textOpt.map { text =>
        val textRanges = paragraphPreprocessor.preprocess(TextRange(text))

        textRanges.toString(stringBuilder)
      }
    }

    metadataHolderOpt.foreach { metadataHolder =>
      metadataHolder.set(Some(getMetadata(extractedMetadata)))
    }

    append(extractedMetadata.title)
    append(extractedMetadata.abstractText)
    Option(extractedMetadata.sections).map(_.asScala).getOrElse(List.empty).foreach { section =>
      append(section.heading)
      append(section.text)
    }
    stringBuilder.toString
  }

  def getMetadata(extractedMetadata: ExtractedMetadata): String = {
    val metadata = ujson.Obj(
      "abstractText" -> ujson.Str(extractedMetadata.getAbstractText),
      "authors" -> ujson.Arr.from(
        extractedMetadata.getAuthors.asScala.map(ujson.Str)
      ),
      "creator" -> ujson.Str(extractedMetadata.getCreator),
      "emails" -> ujson.Arr.from(
        extractedMetadata.getEmails.asScala.map(ujson.Str)
      ),
      "referenceMentions" -> ujson.Arr.from(
        extractedMetadata.getReferenceMentions.asScala.map { referenceMention =>
          ujson.Obj(
            "context" -> ujson.Str(referenceMention.getContext),
            "endOffset" -> ujson.Num(referenceMention.getEndOffset),
            "referenceId" -> ujson.Num(referenceMention.getReferenceID),
            "startOffset" -> ujson.Num(referenceMention.getStartOffset)
          )
        }
      ),
      "references" -> ujson.Arr.from(
        extractedMetadata.getReferences.asScala.map { reference =>
          ujson.Obj(
            "authors" -> reference.getAuthor.asScala.map { author =>
              ujson.Str(author)
            },
            // val citeRegEx = reference.getCiteRegEx
            // val shortCiteRegEx = reference.getShortCiteRegEx
            "title" -> ujson.Str(reference.getTitle),
            "venue" -> ujson.Str(reference.getVenue),
            "year" -> ujson.Num(reference.getYear)
          )
        }
      ),
      "sections" -> ujson.Arr.from(
        extractedMetadata.getSections.asScala.map { section =>
          ujson.Obj(
            "heading" -> section.getHeading,
            "text" -> section.getText
          )
        }
      ),
      "source" -> ujson.Str(extractedMetadata.getSource.toString),
      "title" -> ujson.Str(extractedMetadata.getTitle),
      "year" -> ujson.Num(extractedMetadata.getYear)
    )
    val json = ujson.write(metadata, indent = 4, escapeUnicode = false)

    json
  }

  def read(inputStream: InputStream, metadataHolderOpt: Option[MetadataHolder] = None): String = {
    val extractedMetadataOpt = Option(parser.doParse(inputStream))

    extractedMetadataOpt.map { extractedMetadata => toString(extractedMetadata, metadataHolderOpt) }.getOrElse("")
  }

  override def convert(file: File, metadataHolderOpt: Option[MetadataHolder] = None): String = {
    new BufferedInputStream(new FileInputStream(file)).autoClose { inputStream =>
      read(inputStream, metadataHolderOpt)
    }
  }
}

case class ScienceParseSettings()

object ScienceParseConverter {
  val defaultSettings: ScienceParseSettings = ScienceParseSettings()
}
