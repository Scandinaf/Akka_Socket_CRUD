package com.assignment.helper

import cats.implicits._
import com.assignment.model.Entity.Message.{Table => ClientLayerTable}
import com.assignment.model.{Table => ServerLayerTable}

object Mapper {

  implicit class ClientLayerTableToServerLayerTableMapper(
      table: ClientLayerTable) {
    def convert =
      ServerLayerTable(table.name, table.participants)
  }

  implicit class ServerLayerTableToClientLayerTableMapper(
      table: ServerLayerTable) {
    def convertWithId(id: Int) =
      ClientLayerTable(id.some, table.name, table.participants)
  }
}
