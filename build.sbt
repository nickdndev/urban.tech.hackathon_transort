import Dependencies._
import sbt.Keys._
import sbt._

name := "nautilus-backend"

organization := "mostransport"

scalaVersion := "2.12.8"

lazy val `nautilus-backend` = Project(id = "nautilus-backend", base = file("."))
    .enablePlugins(PlayScala)
    .settings(
        libraryDependencies ++= Seq(
            guice,
            ws,
            "com.typesafe.play" %% "play-ahc-ws-standalone" % "2.0.7",
            rabbitMq,
            playSlick,
            slick,
            postgres,
            "com.github.tminglei" %% "slick-pg_jts_lt" % "0.17.3",
            "com.github.tminglei" %% "slick-pg" %  "0.17.3"
        )
    )
    .dependsOn(
    )
