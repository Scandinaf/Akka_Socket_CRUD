package com.assignment.route

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{Directives, Route}
import com.assignment.helper.Logger
import com.assignment.route.handler.WebSocketMessageHandler

class WebServerRoute(implicit val actorSystem: ActorSystem)
    extends Directives
    with WebSocketMessageHandler
    with Logger {

  lazy val routes: Route =
    get {
      path("ws_api") {
        handleWebSocketMessages(handler())
      }
    }
}

object WebServerRoute {
  def apply(implicit actorSystem: ActorSystem): WebServerRoute =
    new WebServerRoute
}
