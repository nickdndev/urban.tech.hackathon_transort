package services.db

import org.locationtech.jts.geom.Geometry

case class RoutingPath(id: String,
                       routeId: String,
                       pathGeometry: Geometry)
