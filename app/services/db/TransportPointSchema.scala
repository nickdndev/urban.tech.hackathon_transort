package services.db

import org.locationtech.jts.geom.Geometry
import play.api.db.slick.HasDatabaseConfigProvider
import utils.TransportPostgresProfile
import utils.TransportPostgresProfile.api._

trait TransportPointSchema extends HasDatabaseConfigProvider[TransportPostgresProfile] {

    // import profile.api._

    private[db] val TransportPointItems = TableQuery[TransportPointTable]

    private[db] class TransportPointTable(tag: Tag) extends Table[TransportPoint](tag, "history_coordinate") {

        def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

        def transportId = column[Int]("transport_id")

        def routeId = column[String]("route_id")

        def pathId = column[String]("path_id")

        def lat = column[BigDecimal]("lat")

        def lon = column[BigDecimal]("lon")

        def speed = column[Int]("speed")

        def createdAt = column[Long]("created_at")

        def * = (
            id,
            transportId,
            routeId,
            pathId,
            lat,
            lon,
            speed,
            createdAt) <> (TransportPoint.tupled, TransportPoint.unapply)
    }

    private[db] val RoutingPathItems = TableQuery[RoutingPathTable]

    private[db] class RoutingPathTable(tag: Tag) extends Table[RoutingPath](tag, "route_path") {

        def id = column[String]("id", O.PrimaryKey)

        def routeId = column[String]("route_id")

        def pathGeometry = column[Geometry]("path_geometry")

        def * = (
            id,
            routeId,
            pathGeometry) <> (RoutingPath.tupled, RoutingPath.unapply)
    }

    private[db] val RouteItems = TableQuery[RouteTable]

    private[db] class RouteTable(tag: Tag) extends Table[Route](tag, "route") {

        def id = column[String]("id", O.PrimaryKey)

        def number = column[String]("number")

        def transportType = column[String]("type")

        def * = (
            id,
            number,
            transportType) <> (Route.tupled, Route.unapply)
    }

    private[db] val StopItems = TableQuery[StopTable]

    private[db] class StopTable(tag: Tag) extends Table[Stop](tag, "stop") {

        def id = column[String]("id", O.PrimaryKey)

        def name = column[String]("name")

        def lat = column[BigDecimal]("lat")

        def lon = column[BigDecimal]("lon")

        def * = (
            id,
            name,
            lat,
            lon) <> (Stop.tupled, Stop.unapply)
    }

    private[db] val PathStopItems = TableQuery[PathStopTable]

    private[db] class PathStopTable(tag: Tag) extends Table[PathStop](tag, "path_stop") {

        def routePathId = column[String]("route_path_id", O.PrimaryKey)

        def stopId = column[String]("stop_id")

        def weight = column[Int]("weight")

        def * = (
            routePathId,
            stopId,
            weight) <> (PathStop.tupled, PathStop.unapply)
    }

}
