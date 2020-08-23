package modules

import com.google.inject.AbstractModule
import factories.RebbitMqConsumer
import play.api.{Configuration, Environment}

class TransportModule(environment: Environment, configuration: Configuration)
    extends AbstractModule {

    override def configure(): Unit = {
        bind(classOf[RebbitMqConsumer]).asEagerSingleton()
    }
}
