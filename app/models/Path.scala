package models

import play.api.libs.json.{Format, Json}

case class Path(coordinates: Seq[(Double,Double)])


object Path {
    implicit val format: Format[Path] = Json.format[Path]
}