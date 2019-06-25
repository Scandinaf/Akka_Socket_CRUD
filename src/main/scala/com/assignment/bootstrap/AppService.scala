package com.assignment.bootstrap

import com.assignment.service.AuthService

trait AppService {
  _: AppRepository =>
  implicit val authService = AuthService.apply
}
