package com.assignment.actor
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.assignment.actor.PingPongActor.Message.Request.Ping
import com.assignment.model.Entity.Message.Pong
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class PingPongActorSpec
    extends TestKit(ActorSystem("PingPongActorSpec"))
    with ImplicitSender
    with FlatSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  it should "respond correctly to ping" in {
    val actor = system.actorOf(PingPongActor.props)
    actor ! Ping(123, self)
    expectMsg(Pong(123))
  }
}
