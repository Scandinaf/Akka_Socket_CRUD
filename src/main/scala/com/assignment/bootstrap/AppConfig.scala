package com.assignment.bootstrap

import com.typesafe.config.ConfigFactory

trait AppConfig {
  val config = ConfigFactory.load()
  val host = config.getString("server.host")
  val port = config.getInt("server.port")
}
