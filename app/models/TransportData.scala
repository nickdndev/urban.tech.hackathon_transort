package models

import play.api.libs.json.{Format, Json}

case class TransportData(transportId: Int,
                         routeId: String,
                         pathId: String,
                         lat: BigDecimal,
                         lon: BigDecimal,
                         speed: Int,
                         createdAt: Long)

object TransportData {
    implicit val format: Format[TransportData] = Json.format[TransportData]
}
