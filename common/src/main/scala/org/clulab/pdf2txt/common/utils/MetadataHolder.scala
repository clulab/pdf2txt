package org.clulab.pdf2txt.common.utils

class MetadataHolder(valueOpt: Option[String] = None) extends Holder[Option[String]](valueOpt)

object MetadataHolder {
  val default = new MetadataHolder(None)
}
