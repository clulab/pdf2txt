package org.clulab.pdf2txt.adobe.utils

object AdobeNames {
  // See https://developer.adobe.com/document-services/docs/overview/pdf-extract-api/howtos/extract-api/.
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
  val LI = "LI"
  val Lbl = "Lbl"
  val LBody = "LBody"
  val P = "P"
  val ParagraphSpan = "ParagraphSpan"
  val Reference = "Reference"
  val Sect = "Sect"
  val StyleSpan = "StyleSpan"
  val Sub = "Sub"

  // These are not listed in the documentation.
  val Span = "Span"
  val ExtraCharSpan = "ExtraCharSpan"
  val HyphenSpan = "HyphenSpan"
  val NbspSpan = "NbspSpan"

  val Table = "Table"
  val TD = "TD"
  val TH = "TH"
  val TR = "TR"
  val Title = "Title"
  val TOC = "TOC"
  val TOCI = "TOCI"
  val Watermark = "Watermark"

  val headers = Seq(H, H1, H2, H3, H4, H5, H6)
  val names = Seq(
    Document,
    Aside, Figure, Footnote,
    H, H1, H2, H3, H4, H5, H6, // it is unclear how far
    L, LI, Lbl, LBody,
    P, ParagraphSpan,
    Reference, Sect, Span, ExtraCharSpan, StyleSpan, HyphenSpan, NbspSpan, Sub,
    Table, TD, TH, TR,
    TOC, TOCI,
    Title,
    Watermark
  )
}
