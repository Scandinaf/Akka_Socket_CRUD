package com.assignment.bootstrap

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

trait AppAkka {
  implicit val system: ActorSystem = ActorSystem("akkaWebSocketServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val forkJoinEC: ExecutionContext =
    system.dispatchers.lookup("fork-join-dispatcher-common")
}
