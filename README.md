# Nautilus Backend

The application extracts data from an external resource(RabbitMq) about the current coordinate of buses and calculate the smooth path and estimate the next bus stop (time) on the map


**Structure:**

- /conf  configuration file for RabbitMq,PosgresSQl
- /app   resource core


**Project:**

Bakckend uses Actor system for Socket and routing messsages for RabbitMQ \
Also backend contans module to gather data from RabbitMq for history data

- module for gather data from RabbitMq (factories.PreprocessingData.scala)
- module for RabbitMq Consumer / Soket Actor (factories.RebbitMqConsumer.scala)


**Build:**

execute command: sbt stage

