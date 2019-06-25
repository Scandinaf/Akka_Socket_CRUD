package com.assignment.model

import com.assignment.model.Entity.Message._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, HCursor, Json}

object Codec {
  implicit val encodeTable: Encoder[Entity.Message.Table] =
    deriveEncoder[Entity.Message.Table]
  implicit val decodeTable: Decoder[Entity.Message.Table] =
    deriveDecoder[Entity.Message.Table]

  implicit val decodePing: Decoder[Ping] = deriveDecoder[Ping]

  implicit val decodeLogin: Decoder[Login] =
    Decoder.forProduct2("username", "password")(Login.apply)

  implicit val decodeAddTable: Decoder[AddTable] = (c: HCursor) =>
    for {
      aId <- c.downField("after_id").as[Int]
      table <- c.downField("table").as[Entity.Message.Table]
    } yield AddTable(aId, table)

  implicit val decodeUpdateTable: Decoder[UpdateTable] = (c: HCursor) =>
    for {
      table <- c.downField("table").as[Entity.Message.Table]
    } yield UpdateTable(table)

  implicit val decodeRemoveTable: Decoder[RemoveTable] =
    Decoder.forProduct1("id")(RemoveTable.apply)

  implicit val encodeTableList: Encoder[TableList] =
    Encoder.forProduct2("$type", "tables")(e => ("table_list", e.tables))

  implicit val encodeTableAdded: Encoder[TableAdded] =
    Encoder.forProduct3("$type", "after_id", "table")(e =>
      ("table_added", e.aId, e.table))

  implicit val encodeTableUpdated: Encoder[TableUpdated] =
    Encoder.forProduct2("$type", "table")(e => ("table_updated", e.table))

  implicit val encodeTableRemoved: Encoder[TableRemoved] =
    Encoder.forProduct2("$type", "id")(e => ("table_removed", e.id))

  implicit val encodeRemovalFailed: Encoder[RemovalFailed] =
    Encoder.forProduct2("$type", "id")(e => ("removal_failed", e.id))

  implicit val encodeUpdateFailed: Encoder[UpdateFailed] =
    Encoder.forProduct2("$type", "id")(e => ("update_failed", e.id))

  implicit val encodeBadRequest: Encoder[BadRequest] =
    Encoder.forProduct2("$type", "message")(e => ("bad_request", e.message))

  implicit val encodeLoginSuccessful: Encoder[LoginSuccessful] =
    Encoder.forProduct2("$type", "user_type")(e =>
      ("login_successful", e.userType))

  implicit val encodePong: Encoder[Pong] =
    Encoder.forProduct2("$type", "seq")(e => ("pong", e.seq))

  val loginFailedJson: Json =
    Json.obj(("$type", Json.fromString("login_failed")))
  val notAuthorizedJson: Json =
    Json.obj(("$type", Json.fromString("not_authorized")))
}
