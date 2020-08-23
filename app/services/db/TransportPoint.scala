package services.db

case class TransportPoint(id: Int,
                          transportId: Int,
                          routeId: String,
                          pathId: String,
                          lat: BigDecimal,
                          lon: BigDecimal,
                          speed: Int,
                          createdAt: Long)
