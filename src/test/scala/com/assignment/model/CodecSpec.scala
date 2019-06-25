package com.assignment.model

import cats.implicits._
import com.assignment.model.Codec.decodeTable
import io.circe.parser.parse
import org.scalatest.{FlatSpec, Matchers}

class CodecSpec extends FlatSpec with Matchers {

  it should "parse correctly Table Entity" in {
    for {
      json <- parse(
        """{"id" : 1, "name" : "test table #1", "participants" : 10}""")
      entity <- json.as[Entity.Message.Table]
    } yield entity should be(Entity.Message.Table(1.some, "test table #1", 10))

  }

  it should "parse correctly Table Entity with field Id = null" in {
    for {
      json <- parse(
        """{"id" : null, "name" : "test table #1", "participants" : 10}""")
      entity <- json.as[Entity.Message.Table]
    } yield
      entity should be(Entity.Message.Table(none[Int], "test table #1", 10))

  }

  it should "parse correctly Table Entity without field Id" in {
    for {
      json <- parse("""{"name" : "test table #1", "participants" : 10}""")
      entity <- json.as[Entity.Message.Table]
    } yield
      entity should be(Entity.Message.Table(none[Int], "test table #1", 10))

  }
}
