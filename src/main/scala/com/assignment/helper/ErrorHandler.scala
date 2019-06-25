package com.assignment.helper

import com.assignment.model.Entity.Exception.CommonError
import io.circe.{DecodingFailure, ParsingFailure}

object ErrorHandler {
  def buildErrorFormatOutput(exception: Exception) =
    exception match {
      case e: CommonError => e.message
      // TODO ознакомиться с форматом ошибок и на основании полученной информации решить что возвращать.
      case e: DecodingFailure => e.message
      case e: ParsingFailure => e.message
      case _ => "Something went wrong, contact your administrator."
    }
}
