package com.assignment.bootstrap

import akka.routing.BalancingPool
import com.assignment.Main.system
import com.assignment.actor.{AuthActor, TableRepositoryActor}

trait AppActor {
  _: AppService =>
  // Я считаю что это достаточно глобальные модули системы
  // и другие компоненты должны знать о них явно, а не через простаскивание через всю систему
  // Если бы речь шла о сервисах, то мы бы потеряли гибкость(интерфейс и т.д).
  val authActor =
    system.actorOf(
      BalancingPool(2)
        .props(AuthActor.props)
        .withDispatcher("auth-actor-dispatcher"))

  val tableRepositoryActor = system.actorOf(
    TableRepositoryActor
      .props()
      .withDispatcher("table-repository-actor-dispatcher"))
}
