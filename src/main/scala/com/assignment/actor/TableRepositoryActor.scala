package com.assignment.actor

import akka.actor.SupervisorStrategy.Resume
import akka.actor.{Actor, OneForOneStrategy, Props, SupervisorStrategy}
import cats.implicits._
import com.assignment.actor.TableActor.Message.Request.GetList
import com.assignment.actor.TableRepositoryActor.Message.Request._
import com.assignment.actor.TableRepositoryActor.Message.Response._
import com.assignment.actor.TableRepositoryActor.TableRepositoryService
import com.assignment.helper.Logger
import com.assignment.model.Entity.Exception.CommonError
import com.assignment.model.Table

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._

/*
P.S Решение учитывает что можно производить вставку в произвольную позицию. Для очереди реализация будет куда проще.
Это не самое оптимальное решение, в зависимости от данных можно использовать следующие альтернативы
1. Copyonwritearraylist достаточно большой расход по памяти получается, но параллельное чтение все перекрывает.
  (В случае если мы больше читаем, а не пишем)
2. Synchronized arraylist\linkedlist нет необходимости в дополнительной обертке в виде Актора
  (В текущем решении чтение\запись синхронные)
---
Подумать об использовании не immutable объектов т.к в ситуации когда мы обновляем
весь лист вызывается достаточно много аллокаций.
 */
class TableRepositoryActor
    extends Actor
    with TableRepositoryService
    with Logger {
  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case x => {
        logger.error("Something went wrong during the execution of the actor.",
                     x)
        Resume
      }
    }

  protected override val tableStorage = ArrayBuffer.empty[Table]

  override def receive: Receive = {
    case GetList => sender() ! GetListResult(getListWithIndexes())
    case msg: GetMark =>
      sender() ! GetMarkResult(checkOutRange(msg.id) {
        getMark(msg.id)
      })
    case msg: Create =>
      sender() ! CreateResult(createTable(msg.afterId, msg.table))
    case msg: Update =>
      sender() ! UpdateResult(checkOutRange(msg.id) {
        validateMark(msg.id, msg.mark) {
          updateTable(msg.id, msg.table)
        }
      })
    case msg: Remove =>
      sender() ! RemoveResult(checkOutRange(msg.id) {
        validateMark(msg.id, msg.mark) {
          removeTable(msg.id)
        }
      })
  }

  // Возможно обертка в виде Either лишняя, но в продакшен кода это будет некое хранилище.
  protected def getListWithIndexes() =
    getPureListWithIndexes().asRight[CommonError]

  protected def getMark(id: Int) = getPureMark(id).asRight[CommonError]

  protected def createTable(afterId: Int, table: Table) =
    pureCreateTable(afterId, table).asRight[CommonError]

  protected def updateTable(id: Int, table: Table) = {
    pureUpdateTable(id, table)
    true.asRight[CommonError]
  }

  protected def removeTable(id: Int) = {
    pureRemoveTable(id)
    true.asRight[CommonError]
  }

  protected def validateMark(id: Int, mark: Int)(
      f: => CommonErrorEither[Boolean]) =
    if (getPureMark(id) != mark)
      false.asRight[CommonError]
    else f

  protected def checkOutRange[T](id: Int)(f: => CommonErrorEither[T]) =
    if (id > tableStorage.size)
      CommonError(s"There is no such element. Id - $id").asLeft[T]
    else f
}

object TableRepositoryActor {
  def props(): Props =
    Props(new TableRepositoryActor())

  // Попытка показать, что я не такой уж страшный человек.
  // Сделано для возможности протестировать функционал и т.д
  trait TableRepositoryService {
    protected val tableStorage: mutable.Buffer[Table]

    protected def pureRemoveTable(id: Int) =
      tableStorage.remove(id - 1)

    protected def getPureListWithIndexes() =
      tableStorage.toList.mapWithIndex((table, index) => (index + 1, table))

    protected def pureCreateTable(afterId: Int, table: Table) =
      CreateRequestInfo(
        afterId,
        (afterId, tableStorage.size) match {
          case (aId, _) if (aId <= 0) =>
            table +=: tableStorage
            1
          case (aId, size) if (aId >= size) =>
            (tableStorage += table).size
          case (aId, _) =>
            tableStorage.insert(aId, table)
            aId + 1
        },
        table
      )

    protected def pureUpdateTable(id: Int, table: Table) =
      tableStorage.update(id - 1, table)

    protected def getPureMark(id: Int) =
      tableStorage(id - 1).mark
  }

  object Message {

    object Request {

      // Оптимистичная блокировка будет достигаться за счет метки, слепок данных мне ни к чему.
      // Используется обычный целочисленный тип, но для усложнения можно взять несколько timestamp или несколько хешей от объекта.
      // Идеально подойдет ObjectId из монго, 12 байт и все такое.
      case class GetMark(id: Int)

      case class Create(afterId: Int, table: Table)

      case class Update(id: Int, mark: Int, table: Table)

      case class Remove(id: Int, mark: Int)

    }

    object Response {

      type CommonErrorEither[R] = Either[CommonError, R]

      case class CreateRequestInfo(afterId: Int, id: Int, table: Table)

      case class CreateResult(result: CommonErrorEither[CreateRequestInfo])

      case class GetMarkResult(result: CommonErrorEither[Int])

      case class UpdateResult(result: CommonErrorEither[Boolean])

      case class RemoveResult(result: CommonErrorEither[Boolean])

      case class GetListResult(result: CommonErrorEither[List[(Int, Table)]])

    }

  }

}
