package com.assignment.bootstrap

import com.assignment.route.WebServerRoute

trait AppRoute {
  _: AppAkka with AppActor =>
  val wsRoute = WebServerRoute.apply
}
