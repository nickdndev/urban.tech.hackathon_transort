package actors

import akka.actor.{Actor, ActorRef}
import factories.RebbitMqConsumer
import models.{SocketRequest, TransportData}
import play.api.Logger
import play.api.libs.json.Json
import services.CalculationPositionService

class RabbitMqSocketActor(socket: ActorRef,
                          rabbitMqConsumer: RebbitMqConsumer,
                          calculationPositionService: CalculationPositionService) extends Actor {

    Logger.info("Create new rabbitMq socket actor")

    def receive: Receive = {

        case data: TransportData =>
            calculationPositionService.calculateCurrentPosition(data.transportId, data.routeId, data.pathId,
                data.lon.toDouble, data.lat.toDouble, data.createdAt)
                .foreach { response =>
                    socket ! Json.toJson(response).toString()
                }
        case req: String =>
            Json.parse(req).asOpt[SocketRequest].map { s =>
                rabbitMqConsumer.addSocketActor(s.routeId, s.pathId, self)
            }

        case _ => //Nothing
    }

}


