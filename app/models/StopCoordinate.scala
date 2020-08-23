package models

import play.api.libs.json.{Format, Json}

case class StopCoordinate(moveIndex: Int,
                          position: Seq[Double],
                          name: String,
                          duration: Double = 5.0) {

}

object StopCoordinate {
    implicit val format: Format[StopCoordinate] = Json.format[StopCoordinate]
}
