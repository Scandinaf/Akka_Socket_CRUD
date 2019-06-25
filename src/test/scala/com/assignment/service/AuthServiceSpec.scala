package com.assignment.service

import cats.implicits._
import com.assignment.model.Entity.Exception.CommonError
import com.assignment.model.User
import com.assignment.model.User.{Administrator, RegularUser}
import com.assignment.repository.UserRepository
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers, OneInstancePerTest}

class AuthServiceSpec
    extends FlatSpec
    with Matchers
    with MockFactory
    with OneInstancePerTest {
  val repositoryStub = stub[UserRepository]
  val service = AuthService.apply(repositoryStub)

  val adminUser =
    User("admin", "admin", Administrator).some
  val regularUser = User("user", "user", RegularUser).some
  val incorrectUser = none[User]

  val adminUserResult = adminUser.asRight[CommonError]
  val regularUserResult = regularUser.asRight[CommonError]
  val incorrectUserResult = incorrectUser.asRight[CommonError]

  (repositoryStub.getUser _).when("admin").returns(adminUserResult)
  (repositoryStub.getUser _).when("user").returns(regularUserResult)
  (repositoryStub.getUser _).when("userrrr").returns(incorrectUserResult)

  it should "authorize as administrator" in {
    val sessionName = "test-session"
    service.login(sessionName, "admin", "admin") should be(
      User("admin", "admin", Administrator).asRight[CommonError])
    service.getUserBySessionId(sessionName) should be(adminUser)
  }

  it should "authorize as user" in {
    val sessionName = "test-session"
    service.login(sessionName, "user", "user") should be(
      User("user", "user", RegularUser).asRight[CommonError])
    service.getUserBySessionId(sessionName) should be(regularUser)
  }

  it should "return an error if the user isn't found" in {
    val sessionName = "test-session"
    service.login(sessionName, "userrrr", "userrrr") should be(
      CommonError(s"Incorrect login or password.").asLeft[User])
  }

  it should "return an error if the user entered the wrong password" in {
    val sessionName = "test-session"
    service.login(sessionName, "user", "userrrr") should be(
      CommonError(s"Incorrect login or password.").asLeft[User])
  }

  it should "correctly invalidate the session" in {
    val sessionName = "test-session"
    service.login(sessionName, "user", "user") should be(
      User("user", "user", RegularUser).asRight[CommonError])
    service.invalidateSession(sessionName) should be(regularUser)
  }

  it should "return nothing for an empty session" in {
    val sessionName = "test-session"
    service.getUserBySessionId(sessionName) should be(None)
  }

  it should "return user for a valid session" in {
    val sessionName = "test-session"
    service.login(sessionName, "user", "user") should be(
      User("user", "user", RegularUser).asRight[CommonError])
    service.getUserBySessionId(sessionName) should be(regularUser)
  }
}
