package models

import play.api.libs.json.{Format, Json}

case class CurrentPoint(transportId:Int,routeId: String, pathId: String, completed: Double, timestamp: Long)

object CurrentPoint {
    implicit val format: Format[CurrentPoint] = Json.format[CurrentPoint]
}
