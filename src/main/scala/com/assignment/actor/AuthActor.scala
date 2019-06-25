package com.assignment.actor

import akka.actor.SupervisorStrategy.Resume
import akka.actor.{Actor, OneForOneStrategy, Props, SupervisorStrategy}
import cats.implicits._
import com.assignment.actor.AuthActor.Message.Request.{BasicLogin, InvalidateSession}
import com.assignment.actor.AuthActor.Message.Response.BasicLoginResult
import com.assignment.actor.TableRepositoryActor.Message.Response.CommonErrorEither
import com.assignment.helper.Logger
import com.assignment.model.Entity.Exception.CommonError
import com.assignment.model.User
import com.assignment.service.AuthService

import scala.concurrent.duration._

class AuthActor(implicit authService: AuthService) extends Actor with Logger {
  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case x => {
        logger.error("Something went wrong during the execution of the actor.",
                     x)
        Resume
      }
    }

  override def receive: Receive = {

    case InvalidateSession(sessionId: String) =>
      authService.invalidateSession(sessionId) match {
        case Some(u) =>
          logger.info(s"Session successfully closed. User - ${u.name}")
        case None =>
          logger.warn("Couldn't detect session")
      }

    case BasicLogin(sessionId: String, userName: String, password: String) =>
      sender() ! authService
        .login(sessionId, userName, password)
        .fold(
          error => {
            logger.error(s"Something went wrong. Error - ${error.message}")
            BasicLoginResult(error.asLeft[User])
          },
          user => BasicLoginResult(user.asRight[CommonError])
        )
  }
}

object AuthActor {
  def props(implicit authService: AuthService): Props = Props(new AuthActor())

  object Message {

    object Request {

      case class InvalidateSession(sessionId: String)

      case class BasicLogin(sessionId: String,
                            userName: String,
                            password: String)

    }

    object Response {

      case class BasicLoginResult(result: CommonErrorEither[User])

    }

  }

}
