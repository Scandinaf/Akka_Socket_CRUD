package com.assignment.route.auth

import akka.http.scaladsl.server.directives.Credentials
import com.assignment.helper.Logger
import com.assignment.repository.UserRepository

trait BasicAuth extends Auth[String] {
  self: Logger =>
  val userRepository: UserRepository

  override def authentication(credentials: Credentials) =
    credentials match {
      case p @ Credentials.Provided(name) =>
        userRepository
          .getUser(name)
          .fold(
            error => {
              logger.error(s"Something went wrong. Error - ${error.message}")
              None
            }, {
              case Some(user) if (p.verify(user.password)) =>
                logger.info(
                  s"The user has successfully passed the authentication process. User - $name")
                Option.apply(user.name)
              case _ =>
                logger.info(s"Authentication attempt failed. User - $name")
                None
            }
          )
      case _ =>
        logger.warn(
          s"Incorrect behavior, additional research is needed. Credentials - $credentials")
        None
    }
}
