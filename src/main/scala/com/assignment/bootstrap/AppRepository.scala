package com.assignment.bootstrap

import com.assignment.repository.{UserLocalRepository, UserRepository}

trait AppRepository {
  implicit val userLocalRepository: UserRepository = UserLocalRepository.apply
}
