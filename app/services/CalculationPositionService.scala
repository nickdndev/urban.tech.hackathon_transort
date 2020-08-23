package services

import javax.inject.{Inject, Singleton}
import models.{CurrentPoint, Move, RouteCoordinate}
import org.locationtech.jts.geom.Coordinate
import services.db._

import scala.annotation.tailrec
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CalculationPositionService @Inject()()(transportPointService: TransportPointService,
                                             transportService: TransportService)
                                          (implicit val executionContext: ExecutionContext) {

    type Point = (Double, Double)


    private var cachePathForTransport = mutable.Map[String, Seq[TransportPoint]]()
    private var cachePathDestinationForTransport = mutable.Map[(String, String), Map[(Point, Point), Double]]()

    def calculateStaticPath(routeId: String, pathId: String) = {
        for {
            routingPath <- transportService.findRoutingPathById(pathId)
            pathForTransport <- calculateTransportPath(pathId)
            stops <- transportService.findStopsByPathId(pathId)
        } yield {
            var accDurationTrip = 0.0
            val staticPath: List[(Point, Point)] = constructPath(routingPath.get.pathGeometry.getCoordinates.toList)
            val staticDistanceByPath = staticPath.map(sp => sp -> calculateDistance(sp._1, sp._2)).toMap
            val durationTrip = pathForTransport.last.createdAt - pathForTransport.head.createdAt
            val distanceTrip = staticDistanceByPath.values.filter(p => !p.isNaN).sum

            def constructRouteCoordinates(stops: List[(PathStop, Stop)], staticPaths: List[(Point, Point)]): List[Move] = {
                //@tailrec
                def constructRoute(stops: List[(PathStop, Stop)], staticPaths: List[(Point, Point)], accMoves: List[Move]): List[Move] = {

                    staticPaths match {
                        case Nil => accMoves
                        case p :: ps =>

                            stops.find { s =>
                                (p._1._2 <= s._2.lat.toDouble && s._2.lat.toDouble <= p._2._2) &&
                                    (p._1._1 <= s._2.lon.toDouble && s._2.lon.toDouble <= p._2._1)
                            } match {
                                case None =>
                                    val start = List(p._1._1, p._1._2)
                                    val end = List(p._2._1, p._2._2)

                                    val pd = staticDistanceByPath(p)
                                    val pathDuration = (pd / distanceTrip) * durationTrip
                                    val speed = (pd / pathDuration) * 3600 / 1000

                                    if (!pathDuration.isNaN) {
                                        accDurationTrip += pathDuration
                                    }
                                    val mv = Move(speed, None, pathDuration, accDurationTrip, start, end)

                                    constructRoute(stops, ps, mv :: accMoves)


                                case Some(stop) =>

                                    val startCoordinateToStop = (p._1._1, p._1._2)
                                    val stopCoordinate = (stop._2.lon.toDouble, stop._2.lat.toDouble)

                                    val startCoordinateAfterStop = stopCoordinate
                                    val endCoordinate = (p._2._1, p._2._2)

                                    val distanceToStop = calculateDistance(startCoordinateToStop, stopCoordinate)
                                    val distanceAfterStop = calculateDistance(startCoordinateAfterStop, endCoordinate)

                                    val durationToStop = (distanceToStop / distanceTrip) * durationTrip
                                    val durationAfterStop = (distanceAfterStop / distanceTrip) * durationTrip

                                    val speedToStop = (distanceToStop / durationToStop) * 3600 / 1000
                                    val speedAfterStop = (distanceAfterStop / durationAfterStop) * 3600 / 1000
                                    if (!durationToStop.isNaN) {
                                        accDurationTrip += durationToStop
                                    }
                                    val startMove = Move(speedToStop, Some(stop._2.name), durationToStop, accDurationTrip,
                                        List(startCoordinateToStop._1, startCoordinateToStop._2), List(stopCoordinate._1, stopCoordinate._2))

                                    if (!durationAfterStop.isNaN) {
                                        accDurationTrip += durationAfterStop
                                    }
                                    val endMove = Move(speedAfterStop, None, durationAfterStop, accDurationTrip,
                                        List(startCoordinateAfterStop._1, startCoordinateAfterStop._2), List(endCoordinate._1, endCoordinate._2))

                                    constructRoute(stops, ps, startMove :: endMove :: accMoves)
                            }
                    }
                }

                constructRoute(stops, staticPaths, List.empty)
            }

            val moves = constructRouteCoordinates(stops.toList, staticPath.reverse).filter(m => !m.speed.isNaN).reverse
            val mapCache = moves.map { m =>
                ((m.start.head, m.start.last), (m.end.head, m.end.last)) -> m.speed * m.duration
            }.toMap

            cachePathDestinationForTransport += ((routeId, pathId) -> mapCache)


            RouteCoordinate(routeId, durationTrip, moves)
        }
    }


    def calculateTransportPath(pathId: String) = {
        cachePathForTransport.get(pathId) match {
            case Some(values) => Future.successful(values)
            case None => transportPointService.findByPathId(pathId).map { historyData =>
                val path = historyData.groupBy(_.transportId).values.maxBy(_.length).sortBy(_.createdAt)
                cachePathForTransport += (pathId -> path)
                path
            }
        }
    }

    def calculateCurrentPosition(transportId: Int, routeId: String, pathId: String, lon: Double, lat: Double, timestamp: Long) = cachePathDestinationForTransport.get((routeId, pathId)).flatMap { pointsMap =>

        pointsMap.find { p =>
            p._1._1._2 <= lat && lat <= p._1._2._2
        }.map { p =>
            val totalDestination = p._2

            val endPoint = p._1._2

            val currentDestination = calculateDistance((lat, lon), endPoint)

            val completed = currentDestination / totalDestination

            import java.util.Random
            val r: Random = new Random
            val randomValue: Double = 0.8 + (0.97 - 0.8) * r.nextDouble
            val validate = if (completed > 1) randomValue else completed
            CurrentPoint(transportId,routeId, pathId, validate,timestamp)

        }
    }

    def calculateDistance(start: Point, end: Point): Double = {

        def deg2rad(deg: Double): Double = deg * Math.PI / 180.0

        def rad2deg(rad: Double): Double = rad * 180.0 / Math.PI

        val theta: Double = end._1 - start._1
        var dist: Double = Math.sin(deg2rad(start._2)) * Math.sin(deg2rad(end._2)) + Math.cos(deg2rad(start._2)) *
            Math.cos(deg2rad(end._2)) * Math.cos(deg2rad(theta))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist = dist * 60 * 1.1515 * 1.609344 * 1000
        dist
    }


    def constructPath(coordinates: List[Coordinate]): List[(Point, Point)] = {
        @tailrec
        def calculatePath(coordinate: List[Coordinate], acc: List[(Point, Point)]): List[(Point, Point)] = {
            coordinate match {
                case Nil => acc
                case _ :: Nil => acc
                case start :: end :: tail =>
                    val startPoint: Point = (start.x, start.y)
                    val endPoint: Point = (end.x, end.y)
                    calculatePath(end :: tail, (startPoint, endPoint) :: acc)
            }
        }

        calculatePath(coordinates, List.empty)
    }
}
