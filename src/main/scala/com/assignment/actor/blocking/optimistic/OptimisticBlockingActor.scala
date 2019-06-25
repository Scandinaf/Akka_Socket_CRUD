package com.assignment.actor.blocking.optimistic

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorRef, OneForOneStrategy, SupervisorStrategy}
import com.assignment.actor.blocking.optimistic.OptimisticBlockingActor.Message.Request.InitBlockingContext
import com.assignment.helper.Logger

import scala.concurrent.duration._

trait OptimisticBlockingActor[T] extends Actor with Logger {
  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case x => {
        logger.error("Something went wrong during the execution of the actor.",
                     x)
        Stop
      }
    }

  override def receive: Receive = blockingStartReceive

  val blockingStartReceive: Receive = {
    case initContext: InitBlockingContext[T] =>
      catchContext(initContext.blockingContext, initContext.callBackActor)
    case msg =>
      logger.error(
        s"Unfortunately, we don't work with this type of message. Msg - $msg")
  }

  protected def catchContext(blockingContext: T, callBackActor: ActorRef) =
    context.become(blockingProcessReceive(blockingContext, callBackActor))

  def blockingProcessReceive(blockingContext: T,
                             callBackActor: ActorRef): Receive

  protected def stopActor =
    context.stop(self)

}

object OptimisticBlockingActor {
  object Message {
    object Request {
      case class InitBlockingContext[T](blockingContext: T,
                                        callBackActor: ActorRef)
    }
  }
}
