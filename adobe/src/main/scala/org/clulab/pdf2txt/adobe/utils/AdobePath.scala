package org.clulab.pdf2txt.adobe.utils

class AdobePath(stages: Seq[AdobeStage]) {

  def name: String = stages.last.name

  def index: Int = stages.last.index

  def hasTable: Boolean = stages.exists(_.name == AdobeStage.Table)
}

object AdobePath {

  def apply(string: String): AdobePath = {
    assert(string.startsWith("//"))

    val parts = string.drop(2).split('/')
    val stages = parts.map(AdobeStage(_))

    new AdobePath(stages)
  }
}
