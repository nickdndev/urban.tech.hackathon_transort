package models

import play.api.libs.json.{Format, Json}

case class Move(speed: Double,
                name:Option[String]=None,
                duration: Double,
                staysUntil: Double,
                start: Seq[Double],
                end: Seq[Double])

object Move {
    implicit val format: Format[Move] = Json.format[Move]
}
