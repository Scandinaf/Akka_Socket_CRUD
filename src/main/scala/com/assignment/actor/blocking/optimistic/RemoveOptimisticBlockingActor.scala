package com.assignment.actor.blocking.optimistic

import akka.actor.{ActorRef, Props}
import cats.implicits._
import com.assignment.Main.tableRepositoryActor
import com.assignment.actor.TableActor.Message.Request.RemoveTable
import com.assignment.actor.TableRepositoryActor.Message.Request.{GetMark, Remove}
import com.assignment.actor.TableRepositoryActor.Message.Response.{CommonErrorEither, GetMarkResult, RemoveResult}
import com.assignment.actor.blocking.optimistic.RemoveOptimisticBlockingActor.Message.Response.RemoveWithOBResult

class RemoveOptimisticBlockingActor
    extends OptimisticBlockingActor[RemoveTable] {
  override protected def catchContext(blockingContext: RemoveTable,
                                      callBackActor: ActorRef): Unit = {
    tableRepositoryActor ! GetMark(blockingContext.id)
    super.catchContext(blockingContext, callBackActor: ActorRef)
  }
  override def blockingProcessReceive(blockingContext: RemoveTable,
                                      callBackActor: ActorRef): Receive = {
    case msg: GetMarkResult =>
      msg.result.fold(
        error =>
          callBackActor ! RemoveWithOBResult(error.asLeft[Boolean],
                                             blockingContext),
        mark => tableRepositoryActor ! Remove(blockingContext.id, mark)
      )
    case msg: RemoveResult =>
      callBackActor ! RemoveWithOBResult(msg.result, blockingContext)
      stopActor
    case msg =>
      logger.error(
        s"Unfortunately, we don't work with this type of message. Msg - $msg")
  }
}

object RemoveOptimisticBlockingActor {
  def props: Props =
    Props(new RemoveOptimisticBlockingActor)

  object Message {
    object Response {
      case class RemoveWithOBResult[T](result: CommonErrorEither[Boolean],
                                       ctx: T)
    }
  }
}
