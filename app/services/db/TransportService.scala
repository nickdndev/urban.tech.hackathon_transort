package services.db

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TransportService @Inject()()(@NamedDatabase("transport") protected val dbConfigProvider: DatabaseConfigProvider)
    extends TransportPointSchema {

    import profile.api._

    def findRoutingPathById(id: String)(implicit ec: ExecutionContext): Future[Option[RoutingPath]] =
        db.run {
            RoutingPathItems
                .filter(_.id === id)
                .result
                .headOption
        }

    def findRoutingPathByRouteId(routeId: String)(implicit ec: ExecutionContext): Future[Seq[RoutingPath]] =
        db.run {
            RoutingPathItems
                .filter(_.routeId === routeId)
                .result
        }

    def findRouteById(id: String)(implicit ec: ExecutionContext): Future[Seq[Route]] =
        db.run {
            RouteItems
                .filter(_.id === id)
                .result
        }

    def findStopById(id: String)(implicit ec: ExecutionContext): Future[Seq[Stop]] =
        db.run {
            StopItems
                .filter(_.id === id)
                .result
        }

    def findStopsByPathId(pathId: String)(implicit ec: ExecutionContext): Future[Seq[(PathStop, Stop)]] =
        db.run((for {
            (ps, s) <- PathStopItems.filter(_.routePathId === pathId) join StopItems on (_.stopId === _.id)
        } yield (ps, s))
            .sortBy(_._1.weight)
            .result)

}