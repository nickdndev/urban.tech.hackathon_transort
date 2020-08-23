import sbt._

object Dependencies {

    val rabbitMq = "com.rabbitmq" % "amqp-client" % "5.7.3"

    val playSlickVersion = "3.0.0"
    val slickVersion = "3.2.3"
    val postgresVersion = "42.2.2"

    val playSlick = "com.typesafe.play" %% "play-slick" % playSlickVersion
    val slick = "com.typesafe.slick" %% "slick" % slickVersion
    val postgres = "org.postgresql" % "postgresql" % postgresVersion
}
