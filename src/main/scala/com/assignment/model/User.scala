package com.assignment.model

import com.assignment.model.User.Role

case class User(name: String, password: String, role: Role)

object User {

  sealed trait Role {
    val formatName: String
  }

  object Administrator extends Role {
    val formatName = "admin"
  }

  object RegularUser extends Role {
    val formatName = "user"
  }

}
