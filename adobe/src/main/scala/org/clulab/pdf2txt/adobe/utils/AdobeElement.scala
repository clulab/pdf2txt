package org.clulab.pdf2txt.adobe.utils

import org.json4s.{JObject, JString}
import org.json4s.jvalue2monadic // for \

case class AdobeElement(path: AdobePath, text: String) {

  def name: String = path.name

  def index: Int = path.index
}

object AdobeElement {

  def apply(jObject: JObject): AdobeElement = {
    val path = (jObject \ "Path").asInstanceOf[JString].values
    val text = jObject.values.get("Text")
        .map(_.asInstanceOf[String])
        .getOrElse("")

    new AdobeElement(AdobePath(path), text)
  }
}
