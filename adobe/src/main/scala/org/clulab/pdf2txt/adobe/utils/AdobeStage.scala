package org.clulab.pdf2txt.adobe.utils

class AdobeStage(val name: String, val index: Int)

object AdobeStage {
  val Document = "Document"
  val Aside = "Aside"
  val Figure = "Figure"
  val Footnote = "Footnote"
  val H = "H"
  val H1 = "H1"
  val H2 = "H2"
  val H3 = "H3"
  val H4 = "H4"
  val H5 = "H5"
  val H6 = "H6"
  val L = "L"
  val Li = "Li"
  val Lbl = "Lbl"
  val Lbody = "Lbody"
  val P = "P"
  val ParagraphSpan = "ParagraphSpan"
  val Reference = "Reference"
  val Sect = "Sect"
  val StyleSpan = "StyleSpan"
  val Sub = "Sub"
  val Table = "Table"
  val TD = "TD"
  val TH = "TH"
  val TR = "TR"
  val Title = "Title"

  val names = Seq(
    Document,
    Aside, Figure, Footnote,
    H, H1, H2, H3, H4, H5, H6, // it is unclear how far
    L, Li, Lbl, Lbody,
    P, ParagraphSpan,
    Reference, Sect, StyleSpan, Sub,
    Table, TD, TH, TR,
    Title
  )

  def apply(string: String): AdobeStage = {
    val (name, index) = {
      val indexEnd = string.indexOf(']')

      if (0 <= indexEnd) {
        val indexStart = string.indexOf('[')
        assert(0 <= indexStart && indexStart < indexEnd)
        assert(indexEnd == string.length - 1)
        val name = string.substring(0, indexStart)
        val index = string.substring(indexStart + 1, indexEnd)

        (name, index.toInt)
      }
      else
        (string, 0)
    }

    assert(0 <= index)
    assert(names.contains(name))
    new AdobeStage(name, index)
  }
}
