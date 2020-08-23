package services.db

import javax.inject.{Inject, Singleton}
import models.TransportData
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase

import scala.concurrent.{ExecutionContext, Future}
//import utils.TransportPostgresProfile.api._
@Singleton
class TransportPointService @Inject()()(@NamedDatabase("transport_data") protected val dbConfigProvider: DatabaseConfigProvider)
    extends TransportPointSchema {

    import profile.api._

    def findByRouteId(routeId: String)(implicit ec: ExecutionContext): Future[Seq[TransportPoint]] =
        db.run {
            TransportPointItems
                .filter(_.routeId === routeId)
                .result
        }

    def findByPathId(pathId: String)(implicit ec: ExecutionContext): Future[Seq[TransportPoint]] =
        db.run {
            TransportPointItems
                .filter(_.pathId === pathId)
                .result
        }

    def add(data: TransportData)(implicit ec: ExecutionContext): Future[Int] =
        db.run {
            TransportPointItems += TransportPoint(0,
                data.transportId,
                data.routeId,
                data.pathId,
                data.lat,
                data.lon,
                data.speed,
                data.createdAt)
        }.
            recover { case ex =>
                Logger.error("Error during inserting data", ex)
                -1
            }


}