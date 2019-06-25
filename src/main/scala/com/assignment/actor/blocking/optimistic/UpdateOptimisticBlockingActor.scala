package com.assignment.actor.blocking.optimistic

import akka.actor.{ActorRef, Props}
import cats.implicits._
import com.assignment.Main.tableRepositoryActor
import com.assignment.actor.TableActor.Message.Request.UpdateTable
import com.assignment.actor.TableRepositoryActor.Message.Request.{GetMark, Update}
import com.assignment.actor.TableRepositoryActor.Message.Response.{CommonErrorEither, GetMarkResult, UpdateResult}
import com.assignment.actor.blocking.optimistic.UpdateOptimisticBlockingActor.Message.Response.UpdateWithOBResult

class UpdateOptimisticBlockingActor
    extends OptimisticBlockingActor[UpdateTable] {
  override protected def catchContext(blockingContext: UpdateTable,
                                      callBackActor: ActorRef): Unit = {
    tableRepositoryActor ! GetMark(blockingContext.id)
    super.catchContext(blockingContext, callBackActor: ActorRef)
  }

  override def blockingProcessReceive(blockingContext: UpdateTable,
                                      callBackActor: ActorRef): Receive = {
    case msg: GetMarkResult =>
      msg.result.fold(
        error =>
          callBackActor ! UpdateWithOBResult(error.asLeft[Boolean],
                                             blockingContext),
        mark =>
          tableRepositoryActor ! Update(blockingContext.id,
                                        mark,
                                        blockingContext.table)
      )
    case msg: UpdateResult =>
      callBackActor ! UpdateWithOBResult(msg.result, blockingContext)
      stopActor
    case msg =>
      logger.error(
        s"Unfortunately, we don't work with this type of message. Msg - $msg")
  }
}

object UpdateOptimisticBlockingActor {
  def props: Props =
    Props(new UpdateOptimisticBlockingActor)

  object Message {
    object Response {
      case class UpdateWithOBResult[T](result: CommonErrorEither[Boolean],
                                       ctx: T)
    }
  }
}
