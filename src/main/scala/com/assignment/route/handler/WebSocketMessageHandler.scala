package com.assignment.route.handler

import java.util.UUID

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{OverflowStrategy, ThrottleMode}
import com.assignment.Main.forkJoinEC
import com.assignment.actor.RoutingActor
import com.assignment.actor.RoutingActor.Message.Request.{ConnectionClosed, NewConnection, RoutingActorMessage}
import com.assignment.helper.{ErrorHandler, Logger}
import com.assignment.model.Entity.Exception.CommonError
import com.assignment.model.Entity.Message._
import com.assignment.route.handler.helper.Parser

import scala.concurrent.duration._
import scala.util.Failure

trait WebSocketMessageHandler extends Parser {
  _: Logger =>

  val actorSystem: ActorSystem
  val timeout = 1 seconds

  protected def buildInMessagesFlow(
      routingActor: ActorRef): Sink[Message, NotUsed] =
    Flow[Message]
      .throttle(60, timeout, 40, ThrottleMode.shaping)
      .async("in-messages-flow-dispatcher")
      .collect {
        case TextMessage.Strict(msg) => Right(msg)
        case ut @ _                  => Left(CommonError(s"Unsupported type. Type - $ut"))
      }
      .map(e => e.flatMap(parseJson(_)))
      .map(_.fold(
        error => {
          logger.error(s"Something went wrong. Error - $error")
          RoutingActorMessage(
            BadRequest(ErrorHandler.buildErrorFormatOutput(error)))
        },
        RoutingActorMessage(_)
      ))
      .to(Sink.actorRef[RoutingActorMessage](routingActor, ConnectionClosed))

  protected def buildOutMessageFlow(sessionId: String, routingActor: ActorRef) =
    Source
      .actorRef[OutMessage](1000, OverflowStrategy.fail)
      .async("out-messages-flow-dispatcher")
      .mapMaterializedValue { actor =>
        routingActor ! NewConnection(sessionId, actor)
      }
      .map { (receivedMessage: OutMessage) =>
        TextMessage(encodeEntity(receivedMessage))
      }

  def reportErrorsFlow[T](routingActor: ActorRef): Flow[T, T, Any] = {
    Flow[T]
      .watchTermination()((_, f) =>
        f.onComplete {
          case Failure(cause) =>
            logger.error(s"WS stream failed with $cause")
            routingActor ! ConnectionClosed
          case _ =>
      })
  }

  def handler(): Flow[Message, Message, Any] = {
    val sid: String = UUID.randomUUID().toString
    val routingActor: ActorRef =
      actorSystem.actorOf(
        RoutingActor.props.withDispatcher("routing-actor-dispatcher"))

    Flow
      .fromSinkAndSource(buildInMessagesFlow(routingActor),
                         buildOutMessageFlow(sid, routingActor))
      .via(reportErrorsFlow(routingActor))
  }
}
