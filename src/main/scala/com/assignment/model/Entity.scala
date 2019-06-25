package com.assignment.model

object Entity {

  object Exception {
    case class CommonError(val message: String) extends java.lang.Exception
  }

  object SubscribeEvent {
    sealed trait SubscribeEvent {
      val triggeredEventSessionId: String
    }
    case class TableAdded(triggeredEventSessionId: String,
                          afterId: Int,
                          id: Int,
                          table: Table)
        extends SubscribeEvent
    case class TableUpdated(triggeredEventSessionId: String,
                            id: Int,
                            table: Table)
        extends SubscribeEvent
    case class TableRemoved(triggeredEventSessionId: String, id: Int)
        extends SubscribeEvent
  }

  object Message {
    sealed trait InMessage
    object InMessage {
      object Field {
        val msgType = "$type"
      }
    }

    case class Ping(seq: Int) extends InMessage
    object Ping {
      val m_type = "ping"
    }

    case class Login(userName: String, password: String) extends InMessage
    object Login {
      val m_type = "login"
    }

    case class AddTable(afterId: Int, table: Table) extends InMessage
    object AddTable {
      val m_type = "add_table"
    }

    case class UpdateTable(table: Table) extends InMessage
    object UpdateTable {
      val m_type = "update_table"
    }

    case class RemoveTable(id: Int) extends InMessage
    object RemoveTable {
      val m_type = "remove_table"
    }

    object SubscribeTables extends InMessage {
      val m_type = "subscribe_tables"
    }

    object UnsubscribeTables extends InMessage {
      val m_type = "unsubscribe_tables"
    }

    sealed trait OutMessage
    case class TableList(tables: List[Table]) extends OutMessage
    case class TableAdded(aId: Int, table: Table) extends OutMessage
    case class TableUpdated(table: Table) extends OutMessage
    case class TableRemoved(id: Int) extends OutMessage
    case class RemovalFailed(id: Int) extends OutMessage
    case class UpdateFailed(id: Int) extends OutMessage
    case class BadRequest(message: String) extends InMessage with OutMessage
    object NotAuthorized extends OutMessage
    case class LoginSuccessful(userType: String) extends OutMessage
    object LoginFailed extends OutMessage
    case class Pong(seq: Int) extends OutMessage
    case class Table(id: Option[Int] = None, name: String, participants: Int)
  }

}
