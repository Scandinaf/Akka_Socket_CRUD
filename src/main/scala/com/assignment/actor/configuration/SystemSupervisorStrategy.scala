package com.assignment.actor.configuration

import akka.actor.SupervisorStrategy.Resume
import akka.actor.{OneForOneStrategy, SupervisorStrategy, SupervisorStrategyConfigurator}
import com.assignment.helper.Logger

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by serge on 24.03.2018.
  */
class SystemSupervisorStrategy
  extends SupervisorStrategyConfigurator
    with Logger {
  override def create(): SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case x => {
        logger.error("Something went wrong during the execution of the actor.", x)
        Resume
      }
    }
}
