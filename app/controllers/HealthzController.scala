package controllers

import com.google.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.Future

class HealthzController @Inject()(controllerComponents: ControllerComponents)
        extends AbstractController(controllerComponents) {

    def healthz = Action.async {
        Future.successful(Ok)
    }
}