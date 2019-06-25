package com.assignment.actor

import com.assignment.actor.TableRepositoryActor.Message.Response.CreateRequestInfo
import com.assignment.actor.TableRepositoryActor.TableRepositoryService
import com.assignment.model.Table
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class TableRepositoryActorSpec
    extends FlatSpec
    with Matchers
    with BeforeAndAfterEach
    with TableRepositoryService {
  override protected val tableStorage: mutable.Buffer[Table] =
    ArrayBuffer.empty[Table]

  override def beforeEach = tableStorage.clear()

  it should "correctly insert items into the collection" in {
    pureCreateTable(1, Table("test table #1", 1)) should equal(
      CreateRequestInfo(1, 1, Table("test table #1", 1)))
  }

  it should "correctly insert items at the beginning of the collection" in {
    pureCreateTable(-1, Table("test table #1", 1)) should equal(
      CreateRequestInfo(-1, 1, Table("test table #1", 1)))

    pureCreateTable(0, Table("test table #2", 1)) should equal(
      CreateRequestInfo(0, 1, Table("test table #2", 1)))

    pureCreateTable(-233, Table("test table #3", 1)) should equal(
      CreateRequestInfo(-233, 1, Table("test table #3", 1)))
  }

  it should "correctly insert items at the end of the collection" in {
    pureCreateTable(100, Table("test table #1", 1)) should equal(
      CreateRequestInfo(100, 1, Table("test table #1", 1)))

    pureCreateTable(1, Table("test table #2", 1)) should equal(
      CreateRequestInfo(1, 2, Table("test table #2", 1)))

    pureCreateTable(100, Table("test table #3", 1)) should equal(
      CreateRequestInfo(100, 3, Table("test table #3", 1)))
  }

  it should "Correctly insert elements into the middle of the collection" in {
    pureCreateTable(1, Table("test table #1", 1)) should equal(
      CreateRequestInfo(1, 1, Table("test table #1", 1)))

    pureCreateTable(1, Table("test table #2", 1)) should equal(
      CreateRequestInfo(1, 2, Table("test table #2", 1)))

    pureCreateTable(1, Table("test table #3", 1)) should equal(
      CreateRequestInfo(1, 2, Table("test table #3", 1)))

    tableStorage.size should be(3)
  }

  it should "Correctly update elements into the collection" in {
    pureCreateTable(0, Table("test table #1", 1))
    pureCreateTable(1, Table("test table #2", 1))
    pureCreateTable(2, Table("test table #3", 1))

    pureUpdateTable(3, Table("test table #3_update", 10))
    tableStorage(2) should equal(Table("test table #3_update", 10))
    pureUpdateTable(2, Table("test table #2_update", 15))
    tableStorage(1) should equal(Table("test table #2_update", 15))

  }

  it should "Correctly remove elements into the collection" in {
    pureCreateTable(0, Table("test table #1", 1))
    pureCreateTable(1, Table("test table #2", 1))
    pureCreateTable(2, Table("test table #3", 1))

    pureRemoveTable(1)
    tableStorage.size should be(2)
    tableStorage.head should equal(Table("test table #2", 1))
  }

  it should "return the correct collection" in {
    pureCreateTable(0, Table("test table #1", 1))
    pureCreateTable(1, Table("test table #2", 1))
    pureCreateTable(2, Table("test table #3", 1))
    getPureListWithIndexes() should equal(
      List((1, Table("test table #1", 1)),
           (2, Table("test table #2", 1)),
           (3, Table("test table #3", 1))))
  }

}
