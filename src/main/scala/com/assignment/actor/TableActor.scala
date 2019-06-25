package com.assignment.actor

import akka.actor.SupervisorStrategy.Resume
import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props, SupervisorStrategy}
import com.assignment.actor.TableActor.Message.Request.{CreateTable, GetList, RemoveTable, UpdateTable}
import com.assignment.actor.TableRepositoryActor.Message.Request.Create
import com.assignment.actor.blocking.optimistic.OptimisticBlockingActor.Message.Request.InitBlockingContext
import com.assignment.actor.blocking.optimistic.{RemoveOptimisticBlockingActor, UpdateOptimisticBlockingActor}
import com.assignment.helper.Logger
import com.assignment.model.Table

import scala.concurrent.duration._

class TableActor(tableRepositoryActor: ActorRef) extends Actor with Logger {
  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case x => {
        logger.error("Something went wrong during the execution of the actor.",
                     x)
        Resume
      }
    }

  override def receive: Receive = {
    case GetList => tableRepositoryActor.forward(GetList)
    case msg: CreateTable =>
      tableRepositoryActor.forward(Create(msg.afterId, msg.table))

    case msg: UpdateTable =>
      context.actorOf(UpdateOptimisticBlockingActor.props) ! InitBlockingContext(
        msg,
        sender())

    case msg: RemoveTable =>
      context.actorOf(RemoveOptimisticBlockingActor.props) ! InitBlockingContext(
        msg,
        sender())
  }

  override def postStop = logger.info(s"TableActor was stopped. Parent - ${context.parent}")
}

object TableActor {
  def props(tableRepositoryActor: ActorRef): Props =
    Props(new TableActor(tableRepositoryActor))

  object Message {

    object Request {

      object GetList

      case class CreateTable(afterId: Int, table: Table)

      case class RemoveTable(id: Int)

      case class UpdateTable(id: Int, table: Table)

    }

  }

}
