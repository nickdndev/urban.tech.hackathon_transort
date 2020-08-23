package models

import play.api.libs.json.{Format, Json}

case class SocketRequest(routeId: String, pathId: String)

object SocketRequest {
    implicit val format: Format[SocketRequest] = Json.format[SocketRequest]
}

