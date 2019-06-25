package com.assignment.route.handler.helper

import cats.implicits._
import com.assignment.helper.Logger
import com.assignment.model.Codec._
import com.assignment.model.Entity.Message._
import io.circe.parser.parse
import io.circe.syntax._
import io.circe.{DecodingFailure, Json}

trait Parser {
  _: Logger =>

  protected def parseJson(jsonStr: String): Either[io.circe.Error, InMessage] =
    for {
      json <- parse(jsonStr)
      msgType <- json.hcursor.get[String](InMessage.Field.msgType)
      result <- buildEntityByType(msgType, json)
    } yield result

  private def buildEntityByType(msgType: String,
                                json: Json): Either[io.circe.Error, InMessage] =
    msgType match {
      case Login.m_type             => json.as[Login]
      case AddTable.m_type          => json.as[AddTable]
      case UpdateTable.m_type       => json.as[UpdateTable]
      case RemoveTable.m_type       => json.as[RemoveTable]
      case SubscribeTables.m_type   => SubscribeTables.asRight[io.circe.Error]
      case UnsubscribeTables.m_type => UnsubscribeTables.asRight[io.circe.Error]
      case Ping.m_type              => json.as[Ping]
      case ut @ _ =>
        Left(DecodingFailure.apply(s"Unsupported type. Type - $ut", List.empty))
    }

  protected def encodeEntity(msg: OutMessage): String =
    (msg match {
      case e: BadRequest      => e.asJson
      case NotAuthorized      => notAuthorizedJson
      case e: Pong            => e.asJson
      case e: LoginSuccessful => e.asJson
      case LoginFailed        => loginFailedJson
      case e: RemovalFailed   => e.asJson
      case e: UpdateFailed    => e.asJson
      case e: TableUpdated    => e.asJson
      case e: TableRemoved    => e.asJson
      case e: TableAdded      => e.asJson
      case e: TableList       => e.asJson
    }).noSpaces
}
