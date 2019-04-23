package scooby.syntax

import cats.effect.{ContextShift, IO}
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.scalatest.{FlatSpec, Matchers}
import scooby.mixins._
import scooby.utils.SqlBuilder
import scooby.{Address, Customer}

import scala.concurrent.ExecutionContext

class QueryOpsSpec extends FlatSpec with Matchers {

  implicit def contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)

  val xa = Transactor.fromDriverManager[IO](
    "org.h2.Driver",
    "jdbc:h2:mem:;DB_CLOSE_DELAY=-1",
    "sa", ""
  )

  "FindOne" should "return single record" in {
    val query = Customer.FindById(1)
    val expect = Customer(query.id, "John Doe", 40, Address("1 street", None, "P0 C03"))
    implicit val queryBuilder = SqlBuilder[Customer.FindById, Customer] { input =>
      sql"""SELECT ${expect.id}, ${expect.name}, ${expect.age}, ${expect.address.line1}, null, ${expect.address.postcode} WHERE ${input.id} = 1"""
    }
    FindOne(query, queryBuilder).run.transact(xa).unsafeRunSync() shouldEqual Some(expect)
  }

  "Find" should "return a CanBuildFrom collection" in {
    val customer1 = Customer(1L, "John Doe", 40, Address("1 street", None, "P0 C03"))
    val customer2 = Customer(2L, "Tom Smith", 40, Address("2 street", None, "P1 C04"))

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

    val query = Customer.FindByAge(40)
    val expect = List(customer1, customer2)

    val tranx = for {
      _ <- createTable
      _ <- Insert[Customer](customer1, Customer.insert).run
      _ <- Insert[Customer](customer2, Customer.insert).run
      result <- Find[Customer.FindByAge, Customer, List](query, Customer.fba).run
    } yield result

    tranx.transact(xa).unsafeRunSync() shouldEqual expect
  }
}
