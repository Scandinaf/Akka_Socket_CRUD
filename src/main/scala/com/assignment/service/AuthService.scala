package com.assignment.service

import java.util.concurrent.ConcurrentHashMap

import cats.implicits._
import com.assignment.helper.Logger
import com.assignment.model.Entity.Exception.CommonError
import com.assignment.model.User
import com.assignment.repository.UserRepository

class AuthService(implicit private val userRepository: UserRepository)
    extends Logger {
  // Задумка в том чтобы можно было выдерживать большую нагрузку(некое подобие кеша),
  // поднять несколько AuthActor которые могут создать состояние гонки.
  // Возможно в рамках тестового задания это было излишне
  private val sessionStorage = new ConcurrentHashMap[String, User](20)

  def getUserBySessionId(sessionId: String): Option[User] =
    Option.apply(sessionStorage.get(sessionId))

  def invalidateSession(sessionId: String): Option[User] =
    Option.apply(sessionStorage.remove(sessionId))

  def invalidateAllSessions = sessionStorage.clear()

  def login(sessionId: String, userName: String, password: String) = {
    userRepository
      .getUser(userName)
      .flatMap({
        case Some(u) if u.password == password =>
          logger.info(s"Logged in. Session - $sessionId. User - $userName")
          sessionStorage.put(sessionId, u)
          u.asRight[CommonError]
        case _ =>
          CommonError(s"Incorrect login or password.").asLeft[User]
      })
  }
}

object AuthService {
  def apply(implicit userRepository: UserRepository): AuthService =
    new AuthService()
}
