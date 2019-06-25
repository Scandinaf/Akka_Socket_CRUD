package com.assignment.actor

import akka.actor.{Actor, ActorRef, Props}
import com.assignment.actor.PingPongActor.Message.Request.Ping
import com.assignment.helper.Logger
import com.assignment.model.Entity.Message.Pong

class PingPongActor extends Actor with Logger {
  override def receive: Receive = {
    case ping: Ping =>
      logger.info(s"Ping message $ping")
      ping.callBackActor ! Pong(ping.seq)
  }

  override def postStop =
    logger.info(s"TableActor was stopped. Parent - ${context.parent}")
}

object PingPongActor {
  def props: Props = Props(new PingPongActor())

  object Message {
    object Request {
      case class Ping(seq: Int, callBackActor: ActorRef)
    }
  }
}
