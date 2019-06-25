package com.assignment.route.auth

import akka.http.scaladsl.server.directives.Credentials

protected trait Auth[T] {
  def authentication(credentials: Credentials): Option[T]
}
