package com.assignment.repository

import cats.implicits._
import com.assignment.model.Entity.Exception.CommonError
import com.assignment.model.User

class UserLocalRepository extends UserRepository {
  private val adminUser = User("admin", "admin", User.Administrator)
  private val commonUser = User("user", "user", User.RegularUser)
  private val users =
    Map(adminUser.name -> adminUser, commonUser.name -> commonUser)

  override def getUser(userName: String) =
    users.get(userName).asRight[CommonError]
}

object UserLocalRepository {
  def apply(): UserLocalRepository = new UserLocalRepository()
}
