package org.clulab.pdf2txt.adobe.utils

class AdobePath(stages: Seq[AdobeStage]) {

  def name: String = stages.last.name

  def index: Int = stages.last.index

  def isIn(name: String): Boolean = stages.dropRight(1).exists(_.name == name)

  def paragraphIndexOpt: Option[Int] = {
    val stageIndex = stages.lastIndexWhere(_.name == AdobeNames.P)

    stages.lift(stageIndex).map(_.index)
  }
}

object AdobePath {

  def apply(string: String): AdobePath = {
    assert(string.startsWith("//"))

    val parts = string.drop(2).split('/').toSeq
    val stages = parts.map(AdobeStage(_))

    new AdobePath(stages)
  }
}
