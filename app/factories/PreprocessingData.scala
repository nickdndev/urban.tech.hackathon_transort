package factories

import java.io.IOException

import akka.actor.ActorSystem
import com.rabbitmq.client.{AMQP, ConnectionFactory, DefaultConsumer, Envelope}
import javax.inject.{Inject, Singleton}
import models.TransportData
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import services.db.{TransportPointService, TransportService}

import scala.concurrent.ExecutionContext

@Singleton
class PreprocessingData @Inject()(lifecycle: ApplicationLifecycle,
                                  config: Configuration,
                                  ws: WSClient,
                                  configuration: Configuration,
                                  transportPointService: TransportPointService,
                                  transportService: TransportService)
                                 (implicit val executionContext: ExecutionContext, system: ActorSystem) {


    private val exchange: String = config.get[String]("rabbit.exchange")
    private val routingKey: String = config.get[String]("rabbit.routingKey")

    private val factory = new ConnectionFactory
    factory.setHost(config.get[String]("rabbit.host"))
    factory.setPort(config.get[Int]("rabbit.port"))
    factory.setUsername(config.get[String]("rabbit.userName"))
    factory.setPassword(config.get[String]("rabbit.password"))
    private val connection = factory.newConnection
    private val channel = connection.createChannel

    channel.exchangeDeclare(exchange, "topic", true)
    private val queueName = channel.queueDeclare("", true, false, false, null).getQueue
    channel.queueBind(queueName, exchange, routingKey)

    val consumer: DefaultConsumer = new DefaultConsumer(channel) {
        @throws[IOException]
        override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
            try {
                val message = new String(body, "UTF-8")

                val data = Json.parse(message).as[TransportData]
                transportPointService.add(data)
                println(message)
            } catch {
                case ex =>
                    Logger.error("Error dusting persisting data", ex)
            }
        }

    }

    channel.basicConsume(queueName, true, consumer)
}
