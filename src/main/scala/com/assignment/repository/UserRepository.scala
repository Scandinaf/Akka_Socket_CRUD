package com.assignment.repository

import com.assignment.actor.TableRepositoryActor.Message.Response.CommonErrorEither
import com.assignment.model.User

// Данный трейт был сделан с поправкой на будущее.
// Получение данных из других источников(DB, HTTP, etc...)
// Меняем на EitherT -> cats и все работает без каких либо проблем.
trait UserRepository {
  // Эскалируем ошибку т.к это не зона ответственности репозитория.
  def getUser(userName: String): CommonErrorEither[Option[User]]
}
