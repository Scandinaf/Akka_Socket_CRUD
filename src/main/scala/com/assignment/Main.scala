package com.assignment

import akka.http.scaladsl.Http
import com.assignment.bootstrap._
import com.assignment.helper.Logger

import scala.util.{Failure, Success}

object Main
    extends App
    with AppAkka
    with AppRepository
    with AppService
    with AppActor
    with AppRoute
    with AppConfig
    with Logger {

  val binding = Http()
    .bindAndHandle(wsRoute.routes, host, port)
    .onComplete({
      case Success(_) => logger.info(s"Server online at http://$host:$port/")
      case Failure(e) =>
        logger.error("Something went wrong while trying to start the server", e)
        system.terminate()
    })
}
