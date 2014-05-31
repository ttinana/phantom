/*
 * Copyright 2013 newzly ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.newzly.phantom.dsl.crud

import scala.concurrent.blocking
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.Implicits._
import com.newzly.phantom.batch.BatchStatement
import com.newzly.phantom.tables.{ PrimitivesJoda, JodaRow }
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.cassandra.BaseTest

class CountTest extends BaseTest {
  val keySpace: String = "counttests"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      PrimitivesJoda.insertSchema()
    }
  }


  it should "retrieve a count of 0 if the table has been truncated" in {

    val chain = for {
      truncate <- PrimitivesJoda.truncate.future()
      count <- PrimitivesJoda.count.fetch()
    } yield count

    chain successful {
      res => {
        res.isEmpty shouldEqual false
        res.head shouldEqual 0L
      }
    }
  }

  it should "correctly retrieve a count of 1000" in {
    val limit = 1000

    val rows = Iterator.fill(limit)(JodaRow.sample)

    val batch = rows.foldLeft(BatchStatement())((b, row) => {
      val statement = PrimitivesJoda.insert
        .value(_.pkey, row.pkey)
        .value(_.intColumn, row.int)
        .value(_.timestamp, row.bi)
      b.add(statement)
    })

    val chain = for {
      truncate <- PrimitivesJoda.truncate.future()
      batch <- batch.future()
      count <- PrimitivesJoda.count.one()
    } yield count

    chain successful {
      res => {
        res.isDefined shouldBe true
        res.get shouldEqual 1000L
      }
    }
  }

  it should "correctly retrieve a count of 1000 with Twitter futures" in {
    val limit = 1000

    val rows = Iterator.fill(limit)(JodaRow.sample)

    val batch = rows.foldLeft(new BatchStatement())((b, row) => {
      val statement = PrimitivesJoda.insert
        .value(_.pkey, row.pkey)
        .value(_.intColumn, row.int)
        .value(_.timestamp, row.bi)
      b.add(statement)
    })

    val chain = for {
      truncate <- PrimitivesJoda.truncate.execute()
      batch <- batch.execute()
      count <- PrimitivesJoda.count.get()
    } yield count

    chain successful {
      res => {
        res.isDefined shouldBe true
        res.get shouldEqual 1000L
      }
    }
  }
}
