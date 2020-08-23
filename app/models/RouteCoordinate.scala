package models

import play.api.libs.json.{Format, Json}

case class RouteCoordinate(routeId: String,
                           totalDuration: Double,
                           moves: Seq[Move])

object RouteCoordinate {
    implicit val format: Format[RouteCoordinate] = Json.format[RouteCoordinate]
}
