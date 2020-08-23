package controllers

import actors.RabbitMqSocketActor
import akka.actor.{ActorSystem, Props}
import akka.stream.Materializer
import com.google.inject.Inject
import factories.RebbitMqConsumer
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.streams.ActorFlow
import play.api.mvc.{AbstractController, ControllerComponents, WebSocket}
import services.CalculationPositionService
import services.db.TransportService

import scala.concurrent.ExecutionContext

class TransportController @Inject()(controllerComponents: ControllerComponents,
                                    transportService: TransportService,
                                    rebbitMqConsumer: RebbitMqConsumer,
                                    calculationPositionService: CalculationPositionService)
                                   (implicit system: ActorSystem,
                                    mat: Materializer,
                                    ec: ExecutionContext)
    extends AbstractController(controllerComponents) {

    def addLinkSocket: WebSocket = WebSocket.accept[String, String] { _ =>
        ActorFlow.actorRef { out =>
            Props(classOf[RabbitMqSocketActor], out, rebbitMqConsumer, calculationPositionService)
        }
    }

    def pathByIdAndRouteId(routeId: String, pathId: String) = Action.async {
        calculationPositionService.calculateStaticPath(routeId, pathId).map { r =>
            Ok(Json.toJson(r))
        }.recover { case ex =>
            Logger.error("Error during path by id", ex)
            InternalServerError
        }
    }
}
