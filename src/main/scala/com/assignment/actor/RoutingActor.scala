package com.assignment.actor

import akka.actor.SupervisorStrategy.Resume
import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props, Stash, SupervisorStrategy}
import cats.implicits._
import com.assignment.Main.{authActor, tableRepositoryActor}
import com.assignment.actor.AuthActor.Message.Request.{BasicLogin, InvalidateSession}
import com.assignment.actor.AuthActor.Message.Response.BasicLoginResult
import com.assignment.actor.RoutingActor.Message.Request.{ConnectionClosed, NewConnection, RoutingActorMessage}
import com.assignment.actor.TableActor.Message.Request.{CreateTable, GetList, RemoveTable => InternalRemoveTable, UpdateTable => InternalUpdateTable}
import com.assignment.actor.TableRepositoryActor.Message.Response.{CreateResult, GetListResult}
import com.assignment.actor.blocking.optimistic.RemoveOptimisticBlockingActor.Message.Response.RemoveWithOBResult
import com.assignment.actor.blocking.optimistic.UpdateOptimisticBlockingActor.Message.Response.UpdateWithOBResult
import com.assignment.helper.Logger
import com.assignment.helper.Mapper.{ClientLayerTableToServerLayerTableMapper, ServerLayerTableToClientLayerTableMapper}
import com.assignment.model.Entity.Message._
import com.assignment.model.Entity.SubscribeEvent
import com.assignment.model.User
import com.assignment.model.User.Administrator

import scala.concurrent.duration._

class RoutingActor extends Actor with Stash with Logger {
  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case x => {
        logger.error("Something went wrong during the execution of the actor.",
                     x)
        Resume
      }
    }

  override def receive: Receive = initReceive

  val pingPongActor = context.actorOf(
    PingPongActor.props.withDispatcher("routing-actor-dispatcher"))
  val tableActor = context.actorOf(
    TableActor
      .props(tableRepositoryActor)
      .withDispatcher("routing-actor-dispatcher"))
  var user: Option[User] = None

  val initReceive: Receive = {
    case msg: NewConnection =>
      logger.info(s"New connection opened. SessionId - ${msg.sessionId}")
      context.become(regularReceive(msg.sessionId, msg.outFlowActor))
      unstashAll()
    case _ => stash()
  }

  def regularReceive(sessionId: String, outFlowActor: ActorRef): Receive = {
    case RoutingActorMessage(inMessage: Ping) =>
      pingPongActor ! PingPongActor.Message.Request
        .Ping(inMessage.seq, outFlowActor)

    case RoutingActorMessage(inMessage: Login) =>
      authActor ! BasicLogin(sessionId, inMessage.userName, inMessage.password)

    case BasicLoginResult(result) =>
      outFlowActor ! result.fold(_ => LoginFailed, user => {
        this.user = user.some
        LoginSuccessful(user.role.formatName)
      })

    case RoutingActorMessage(inMessage: AddTable) =>
      checkAdminPermission(outFlowActor) {
        tableActor ! CreateTable(inMessage.afterId, inMessage.table.convert)
      }

    case CreateResult(result) =>
      outFlowActor ! result.fold(
        error => BadRequest(error.message),
        info => {
          context.system.eventStream.publish(
            SubscribeEvent
              .TableAdded(sessionId, info.afterId, info.id, info.table))
          TableAdded(info.afterId, info.table.convertWithId(info.id))
        }
      )

    case msg: SubscribeEvent.TableAdded =>
      if (msg.triggeredEventSessionId != sessionId)
        outFlowActor ! TableAdded(msg.afterId, msg.table.convertWithId(msg.id))

    case RoutingActorMessage(inMessage: UpdateTable) =>
      checkAdminPermission(outFlowActor) {
        excludeId(outFlowActor, inMessage.table) { id =>
          tableActor ! InternalUpdateTable(id, inMessage.table.convert)
        }
      }

    case UpdateWithOBResult(result, ctx: InternalUpdateTable) =>
      outFlowActor ! result.fold(
        _ => UpdateFailed(ctx.id),
        operationStatus =>
          if (operationStatus) {
            context.system.eventStream
              .publish(
                SubscribeEvent.TableUpdated(sessionId, ctx.id, ctx.table))
            TableUpdated(ctx.table.convertWithId(ctx.id))
          } else
            UpdateFailed(ctx.id)
      )

    case msg: SubscribeEvent.TableUpdated =>
      if (msg.triggeredEventSessionId != sessionId)
        outFlowActor ! TableUpdated(msg.table.convertWithId(msg.id))

    case RoutingActorMessage(inMessage: RemoveTable) =>
      checkAdminPermission(outFlowActor) {
        tableActor ! InternalRemoveTable(inMessage.id)
      }

    case RemoveWithOBResult(result, ctx: InternalRemoveTable) =>
      outFlowActor ! result.fold(
        _ => RemovalFailed(ctx.id),
        operationStatus =>
          if (operationStatus) {
            context.system.eventStream
              .publish(SubscribeEvent.TableRemoved(sessionId, ctx.id))
            TableRemoved(ctx.id)
          } else RemovalFailed(ctx.id)
      )

    case msg: SubscribeEvent.TableRemoved =>
      if (msg.triggeredEventSessionId != sessionId)
        outFlowActor ! TableRemoved(msg.id)

    case RoutingActorMessage(SubscribeTables) =>
      subscribeEvents(classOf[SubscribeEvent.TableAdded],
                      classOf[SubscribeEvent.TableUpdated],
                      classOf[SubscribeEvent.TableRemoved])
      tableActor ! GetList

    case GetListResult(result) =>
      outFlowActor ! result.fold(
        error => BadRequest(error.message),
        list => TableList(list.map(tuple => tuple._2.convertWithId(tuple._1))))

    case RoutingActorMessage(UnsubscribeTables) =>
      unsubscribeEvents(classOf[SubscribeEvent.TableAdded],
                        classOf[SubscribeEvent.TableUpdated],
                        classOf[SubscribeEvent.TableRemoved])

    case ConnectionClosed =>
      logger.info(
        s"Beginning the process of closing the connection. SessionId - $sessionId, Self - $self")
      user.foreach(_ => authActor ! InvalidateSession(sessionId))
      context.system.getEventStream.unsubscribe(self)
      context.stop(self)

    case RoutingActorMessage(r: BadRequest) => outFlowActor ! r
  }

  protected def checkAdminPermission(outFlowActor: ActorRef)(f: => Unit) =
    user match {
      case Some(u) if (u.role == Administrator) => f
      case _                                    => outFlowActor ! NotAuthorized
    }

  protected def excludeId(outFlowActor: ActorRef, table: Table)(
      f: Int => Unit) =
    table.id match {
      case Some(id) => f(id)
      // Никогда не произойдет, но на всякий случай добавил.
      case None =>
        outFlowActor ! BadRequest("Invalid request. Id must be passed.")
    }

  private def subscribeEvents(events: Class[_]*) = {
    val eventStream = context.system.getEventStream
    events.foreach(eventStream.subscribe(self, _))
  }

  private def unsubscribeEvents(events: Class[_]*) = {
    val eventStream = context.system.getEventStream
    events.foreach(eventStream.unsubscribe(self, _))
  }

}

object RoutingActor {
  def props: Props =
    Props(new RoutingActor)

  object Message {

    object Request {

      case class RoutingActorMessage(msg: InMessage)

      case class NewConnection(sessionId: String, outFlowActor: ActorRef)

      object ConnectionClosed

    }

  }

}
