package scooby.syntax

import cats.effect.{ContextShift, IO}
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.scalatest.{FlatSpec, Matchers}
import scooby._
import scooby.utils.SqlInserted
import scooby.{Address, Customer}

import scala.concurrent.ExecutionContext

class CreateOpsSpec extends FlatSpec with Matchers {

  implicit def contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)

  val xa = Transactor.fromDriverManager[IO](
    "org.h2.Driver",
    "jdbc:h2:mem:;DB_CLOSE_DELAY=-1",
    "sa", ""
  )

  "Insert" should "create a new record" in {
    val customer = Customer(1L, "John Doe", 40, Address("1 street", None, "P0 C03"))

    val createTable = sql"""
        |CREATE TABLE customers(
        |  id BIGINT NOT NULL PRIMARY KEY,
        |  name VARCHAR NOT NULL,
        |  age INT NOT NULL,
        |  line1 VARCHAR NOT NULL,
        |  line2 VARCHAR,
        |  postcode VARCHAR NOT NULL
        |)
        """.stripMargin.update.run


    val tranx = for {
      _ <- createTable
      result <- Insert[Customer](customer, Customer.insert).run
    } yield result

    tranx.transact(xa).unsafeRunSync() shouldEqual SqlInserted(1)
  }
}
